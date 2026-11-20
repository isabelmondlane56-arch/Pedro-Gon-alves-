package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.AcademyProgress
import com.example.model.ActivityLog
import com.example.model.AiGeneration
import com.example.model.AiMode
import com.example.model.ContentReport
import com.example.model.DesignProject
import com.example.model.DesignTemplateType
import com.example.model.MarketCategory
import com.example.model.MarketProduct
import com.example.model.PayoutMethod
import com.example.model.PayoutProvider
import com.example.model.ProductReview
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MoneyHubRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun <T> Flow<T>.logFirestoreErrors(
        operationType: OperationType,
        path: String,
        expectedUid: String
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    // --- USER PROFILE ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user profile observation" }
        val docRef = db.collection("users").document(uid)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .logFirestoreErrors(OperationType.GET, docRef.path, uid)
        )
    }

    suspend fun ensureUserProfile(
        defaultLanguage: String,
        defaultTheme: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Unauthenticated"))
        val docPath = "users/$uid"
        return try {
            val user = auth.currentUser ?: error("User not signed in")
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val safeName = (user.displayName?.takeIf { it.isNotBlank() } ?: "Empreendedor").take(80)
                val safeEmail = (user.email?.takeIf { it.isNotBlank() } ?: "user@moneyhub.app").take(120)
                val photo = user.photoUrl?.toString()?.take(500)
                val lang = if (defaultLanguage in listOf("pt", "en")) defaultLanguage else "pt"
                val theme = if (defaultTheme in listOf("system", "dark", "light")) defaultTheme else "dark"

                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "photoUrl" to photo,
                    "country" to "Moçambique",
                    "language" to lang,
                    "themeMode" to theme,
                    "plan" to "FREE",
                    "role" to "USER",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null } as Map<String, Any>

                docRef.set(payload).await()
                logActivityInternal(uid, "ACCOUNT", "Conta Money Hub criada", "Bem-vindo ao ecossistema")
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        displayName: String,
        country: String,
        language: String,
        themeMode: String,
        bio: String?,
        photoUrl: String?
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
        val docPath = "users/${uid ?: "unauthenticated"}"
        return try {
            val activeUid = requireUserId()
            val cleanName = displayName.trim().take(80)
            require(cleanName.isNotEmpty()) { "Display name required" }
            val cleanCountry = country.trim().take(60)
            require(cleanCountry.length >= 2) { "Country must be at least 2 characters" }
            val safeLang = if (language in listOf("pt", "en")) language else "pt"
            val safeTheme = if (themeMode in listOf("system", "dark", "light")) themeMode else "dark"

            val updates = mutableMapOf<String, Any>(
                "displayName" to cleanName,
                "country" to cleanCountry,
                "language" to safeLang,
                "themeMode" to safeTheme,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val cleanBio = bio?.trim()?.take(300)
            if (!cleanBio.isNullOrEmpty()) {
                updates["bio"] = cleanBio
            }
            val cleanPhoto = photoUrl?.trim()?.take(500)
            if (!cleanPhoto.isNullOrEmpty()) {
                updates["photoUrl"] = cleanPhoto
            }

            db.collection("users").document(activeUid).update(updates).await()
            logActivityInternal(activeUid, "PROFILE", "Perfil atualizado", "$cleanName • $cleanCountry")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    // --- MONEY ACADEMY PROGRESS ---

    fun observeAcademyProgress(userId: String): Flow<List<AcademyProgress>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val colRef = db.collection("users").document(uid).collection("academy_progress")
        emitAll(
            colRef.snapshots()
                .map { snap -> snap.toObjects(AcademyProgress::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .logFirestoreErrors(OperationType.LIST, colRef.path, uid)
        )
    }

    suspend fun toggleLessonCompletion(
        courseId: String,
        lessonId: String,
        totalLessons: Int,
        lessonTitle: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
        val docPath = "users/${uid ?: "unauthenticated"}/academy_progress/$courseId"
        return try {
            val activeUid = requireUserId()
            require(courseId.isNotBlank() && totalLessons > 0)
            val docRef = db.collection("users").document(activeUid)
                .collection("academy_progress").document(courseId)
            val snap = docRef.get().await()

            val currentIds = if (snap.exists()) {
                @Suppress("UNCHECKED_CAST")
                (snap.get("completedLessonIds") as? List<String>) ?: emptyList()
            } else emptyList()

            val updatedIds = if (currentIds.contains(lessonId)) {
                currentIds - lessonId
            } else {
                (currentIds + lessonId).distinct().take(50)
            }
            val percent = ((updatedIds.size.toDouble() / totalLessons.toDouble()) * 100.0)
                .toLong().coerceIn(0L, 100L)

            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "completedLessonIds" to updatedIds,
                        "progressPercent" to percent,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to activeUid,
                        "courseId" to courseId,
                        "completedLessonIds" to updatedIds,
                        "progressPercent" to percent,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            if (!currentIds.contains(lessonId)) {
                logActivityInternal(activeUid, "ACADEMY", "Aula concluída: $lessonTitle", "$percent% do curso")
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    // --- MONEY DESIGN PROJECTS ---

    fun observeDesignProjects(userId: String): Flow<List<DesignProject>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val colRef = db.collection("users").document(uid).collection("design_projects")
        emitAll(
            colRef.snapshots()
                .map { snap ->
                    snap.toObjects(DesignProject::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .logFirestoreErrors(OperationType.LIST, colRef.path, uid)
        )
    }

    suspend fun saveDesignProject(
        existingId: String?,
        templateType: DesignTemplateType,
        title: String,
        headline: String,
        subheadline: String,
        ctaText: String,
        brandName: String,
        paletteId: String
    ): Result<String> {
        val uid = auth.currentUser?.uid
        val basePath = "users/${uid ?: "unauthenticated"}/design_projects"
        return try {
            val activeUid = requireUserId()
            val cleanTitle = title.trim().take(100)
            require(cleanTitle.isNotEmpty()) { "Project title cannot be empty" }
            val cleanHeadline = headline.trim().take(120)
            val cleanSub = subheadline.trim().take(240)
            val cleanCta = ctaText.trim().take(60)
            val cleanBrand = brandName.trim().take(60)
            val cleanPalette = paletteId.trim().ifBlank { "gold_navy" }.take(40)

            val colRef = db.collection("users").document(activeUid).collection("design_projects")
            val docRef = if (!existingId.isNullOrBlank()) colRef.document(existingId) else colRef.document()

            if (!existingId.isNullOrBlank()) {
                docRef.update(
                    mapOf(
                        "templateType" to templateType.code,
                        "title" to cleanTitle,
                        "headline" to cleanHeadline,
                        "subheadline" to cleanSub,
                        "ctaText" to cleanCta,
                        "brandName" to cleanBrand,
                        "paletteId" to cleanPalette,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to activeUid,
                        "templateType" to templateType.code,
                        "title" to cleanTitle,
                        "headline" to cleanHeadline,
                        "subheadline" to cleanSub,
                        "ctaText" to cleanCta,
                        "brandName" to cleanBrand,
                        "paletteId" to cleanPalette,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            logActivityInternal(activeUid, "DESIGN", "Projeto guardado: $cleanTitle", templateType.labelPt)
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, basePath)
            Result.failure(e)
        }
    }

    suspend fun deleteDesignProject(projectId: String): Result<Unit> {
        val uid = auth.currentUser?.uid
        val docPath = "users/${uid ?: "unauthenticated"}/design_projects/$projectId"
        return try {
            val activeUid = requireUserId()
            require(projectId.isNotBlank())
            db.collection("users").document(activeUid)
                .collection("design_projects").document(projectId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- MONEY AI GENERATIONS ---

    fun observeAiGenerations(userId: String): Flow<List<AiGeneration>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val colRef = db.collection("users").document(uid).collection("ai_generations")
        emitAll(
            colRef.snapshots()
                .map { snap ->
                    snap.toObjects(AiGeneration::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .logFirestoreErrors(OperationType.LIST, colRef.path, uid)
        )
    }

    suspend fun saveAiGeneration(mode: AiMode, prompt: String, response: String): Result<Unit> {
        val uid = auth.currentUser?.uid
        val path = "users/${uid ?: "unauthenticated"}/ai_generations"
        return try {
            val activeUid = requireUserId()
            val cleanPrompt = prompt.trim().take(1500)
            val cleanResponse = response.trim().take(12000)
            if (cleanPrompt.isEmpty() || cleanResponse.isEmpty()) {
                return Result.failure(IllegalArgumentException("Prompt and response cannot be empty"))
            }
            val docRef = db.collection("users").document(activeUid).collection("ai_generations").document()
            docRef.set(
                mapOf(
                    "userId" to activeUid,
                    "mode" to mode.code,
                    "prompt" to cleanPrompt,
                    "response" to cleanResponse,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logActivityInternal(activeUid, "AI", "Money AI: ${mode.labelPt}", cleanPrompt.take(80))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteAiGeneration(generationId: String): Result<Unit> {
        val uid = auth.currentUser?.uid
        val path = "users/${uid ?: "unauthenticated"}/ai_generations/$generationId"
        return try {
            val activeUid = requireUserId()
            db.collection("users").document(activeUid)
                .collection("ai_generations").document(generationId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- ACTIVITY LOGS & PAYOUT METHODS ---

    fun observeActivityLogs(userId: String): Flow<List<ActivityLog>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val colRef = db.collection("users").document(uid).collection("activity_logs")
        emitAll(
            colRef.snapshots()
                .map { snap ->
                    snap.toObjects(ActivityLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                        .take(30)
                }
                .logFirestoreErrors(OperationType.LIST, colRef.path, uid)
        )
    }

    fun observePayoutMethods(userId: String): Flow<List<PayoutMethod>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val colRef = db.collection("users").document(uid).collection("payout_methods")
        emitAll(
            colRef.snapshots()
                .map { snap ->
                    snap.toObjects(PayoutMethod::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .logFirestoreErrors(OperationType.LIST, colRef.path, uid)
        )
    }

    suspend fun addPayoutMethod(
        provider: PayoutProvider,
        accountLabel: String,
        maskedAccount: String,
        currency: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
        val path = "users/${uid ?: "unauthenticated"}/payout_methods"
        return try {
            val activeUid = requireUserId()
            val cleanLabel = accountLabel.trim().take(80)
            val cleanAccount = maskedAccount.trim().take(60)
            require(cleanLabel.length >= 2 && cleanAccount.length >= 3) { "Dados do método inválidos" }
            val safeCurrency = if (currency in listOf("MZN", "USD", "EUR")) currency else "MZN"

            val docRef = db.collection("users").document(activeUid).collection("payout_methods").document()
            docRef.set(
                mapOf(
                    "userId" to activeUid,
                    "provider" to provider.code,
                    "accountLabel" to cleanLabel,
                    "maskedAccount" to cleanAccount,
                    "currency" to safeCurrency,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logActivityInternal(activeUid, "WALLET", "Método configurado: ${provider.label}", safeCurrency)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deletePayoutMethod(methodId: String): Result<Unit> {
        val uid = auth.currentUser?.uid
        val path = "users/${uid ?: "unauthenticated"}/payout_methods/$methodId"
        return try {
            val activeUid = requireUserId()
            db.collection("users").document(activeUid)
                .collection("payout_methods").document(methodId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- MONEY MARKET ---

    fun observeActiveMarketProducts(): Flow<List<MarketProduct>> = flow {
        val uid = requireUserId()
        val query = db.collection("market_products").whereEqualTo("status", "ACTIVE")
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.toObjects(MarketProduct::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .logFirestoreErrors(OperationType.LIST, "market_products", uid)
        )
    }

    fun observeMyMarketProducts(userId: String): Flow<List<MarketProduct>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val query = db.collection("market_products").whereEqualTo("sellerId", uid)
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.toObjects(MarketProduct::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .logFirestoreErrors(OperationType.LIST, "market_products", uid)
        )
    }

    suspend fun publishMarketProduct(
        sellerName: String,
        sellerCountry: String,
        title: String,
        description: String,
        category: MarketCategory,
        priceUsd: Double,
        priceMzn: Double,
        affiliatePercent: Long,
        paletteId: String
    ): Result<String> {
        val path = "market_products"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim().take(100)
            val cleanDesc = description.trim().take(1000)
            require(cleanTitle.length >= 3) { "Title must be at least 3 characters" }
            require(cleanDesc.length >= 10) { "Description must be at least 10 characters" }

            val cleanSeller = sellerName.trim().ifBlank { "Criador Money Hub" }.take(80)
            val cleanCountry = sellerCountry.trim().ifBlank { "Moçambique" }.take(60)
            val safeUsd = priceUsd.coerceIn(0.0, 100000.0)
            val safeMzn = priceMzn.coerceIn(0.0, 10000000.0)
            val safeAff = affiliatePercent.coerceIn(0L, 80L)
            val cleanPalette = paletteId.trim().ifBlank { "gold_navy" }.take(40)

            val docRef = db.collection("market_products").document()
            docRef.set(
                mapOf(
                    "sellerId" to uid,
                    "sellerName" to cleanSeller,
                    "sellerCountry" to cleanCountry,
                    "title" to cleanTitle,
                    "description" to cleanDesc,
                    "category" to category.code,
                    "priceUsd" to safeUsd,
                    "priceMzn" to safeMzn,
                    "affiliatePercent" to safeAff,
                    "ratingAvg" to 0.0,
                    "reviewCount" to 0L,
                    "paletteId" to cleanPalette,
                    "status" to "ACTIVE",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logActivityInternal(uid, "MARKET", "Produto publicado: $cleanTitle", category.labelPt)
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteMarketProduct(productId: String): Result<Unit> {
        val path = "market_products/$productId"
        return try {
            requireUserId()
            require(productId.isNotBlank())
            db.collection("market_products").document(productId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeProductReviews(productId: String): Flow<List<ProductReview>> = flow {
        val uid = requireUserId()
        val colRef = db.collection("market_products").document(productId).collection("reviews")
        emitAll(
            colRef.snapshots()
                .map { snap ->
                    snap.toObjects(ProductReview::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .logFirestoreErrors(OperationType.LIST, colRef.path, uid)
        )
    }

    suspend fun submitReview(
        productId: String,
        authorName: String,
        rating: Long,
        comment: String
    ): Result<Unit> {
        val path = "market_products/$productId/reviews"
        return try {
            val uid = requireUserId()
            val cleanComment = comment.trim().take(400)
            require(cleanComment.length >= 2) { "Comment too short" }
            val safeRating = rating.coerceIn(1L, 5L)
            val safeAuthor = authorName.trim().ifBlank { "Membro Money Hub" }.take(80)

            val productRef = db.collection("market_products").document(productId)
            val reviewRef = productRef.collection("reviews").document()
            reviewRef.set(
                mapOf(
                    "authorId" to uid,
                    "authorName" to safeAuthor,
                    "rating" to safeRating,
                    "comment" to cleanComment,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val allReviews = productRef.collection("reviews").get().await()
            val ratings = allReviews.documents.mapNotNull { it.getLong("rating") }
            val count = ratings.size.toLong()
            val avg = if (count > 0L) (ratings.sum().toDouble() / count.toDouble()) else safeRating.toDouble()

            productRef.update(
                mapOf(
                    "ratingAvg" to avg,
                    "reviewCount" to count,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- MODERATION & ADMIN ---

    suspend fun submitContentReport(
        targetType: String,
        targetId: String,
        reason: String
    ): Result<Unit> {
        val path = "content_reports"
        return try {
            val uid = requireUserId()
            val cleanReason = reason.trim().take(300)
            require(cleanReason.length >= 5) { "Reason must be at least 5 characters" }
            require(targetId.isNotBlank())
            val safeType = if (targetType == "SELLER") "SELLER" else "MARKET_PRODUCT"

            db.collection("content_reports").document().set(
                mapOf(
                    "reporterId" to uid,
                    "targetType" to safeType,
                    "targetId" to targetId.take(128),
                    "reason" to cleanReason,
                    "status" to "OPEN",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeAdminReports(): Flow<List<ContentReport>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("content_reports").snapshots()
                .map { snap ->
                    snap.toObjects(ContentReport::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .logFirestoreErrors(OperationType.LIST, "content_reports", uid)
        )
    }

    suspend fun updateReportStatus(reportId: String, status: String): Result<Unit> {
        val path = "content_reports/$reportId"
        return try {
            requireUserId()
            val safeStatus = if (status in listOf("OPEN", "REVIEWED", "DISMISSED")) status else "REVIEWED"
            db.collection("content_reports").document(reportId)
                .update(
                    mapOf(
                        "status" to safeStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun moderateProductStatus(productId: String, status: String): Result<Unit> {
        val path = "market_products/$productId"
        return try {
            requireUserId()
            val safeStatus = if (status in listOf("ACTIVE", "FLAGGED", "ARCHIVED")) status else "FLAGGED"
            db.collection("market_products").document(productId)
                .update(
                    mapOf(
                        "status" to safeStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    private suspend fun logActivityInternal(
        uid: String,
        actionType: String,
        title: String,
        detail: String?
    ) {
        try {
            val payload = mapOf(
                "userId" to uid,
                "actionType" to actionType.take(40),
                "title" to title.take(120),
                "detail" to detail?.take(250),
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            db.collection("users").document(uid).collection("activity_logs").document().set(payload).await()
        } catch (_: Exception) {
            // Non-blocking audit log
        }
    }
}
