package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DesignCopySuggestion
import com.example.data.MoneyAiRepository
import com.example.data.MoneyHubRepository
import com.example.data.PreferencesRepository
import com.example.localization.AppLanguage
import com.example.model.AcademyProgress
import com.example.model.ActivityLog
import com.example.model.AiGeneration
import com.example.model.AiMode
import com.example.model.AiResponseState
import com.example.model.ContentReport
import com.example.model.DesignProject
import com.example.model.DesignTemplateType
import com.example.model.MarketCategory
import com.example.model.MarketProduct
import com.example.model.PayoutMethod
import com.example.model.PayoutProvider
import com.example.model.ProductReview
import com.example.model.ThemeMode
import com.example.model.UiState
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"
private const val STOP_TIMEOUT_MILLIS = 5000L

class MainViewModel(
    private val repository: MoneyHubRepository,
    private val aiRepository: MoneyAiRepository,
    private val prefsRepository: PreferencesRepository,
    val currentUserId: String
) : ViewModel() {

    val isAdminUser: Boolean = run {
        val user = Firebase.auth.currentUser
        user?.email.equals("isabelmondlane56@gmail.com", ignoreCase = true) && user?.isEmailVerified == true
    }

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val events: SharedFlow<String> = _events.asSharedFlow()

    val userProfileState: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId)
            .toUiStateFlow("Failed to load profile")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val academyProgressState: StateFlow<UiState<List<AcademyProgress>>> =
        repository.observeAcademyProgress(currentUserId)
            .toUiStateFlow("Failed to load academy progress")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val designProjectsState: StateFlow<UiState<List<DesignProject>>> =
        repository.observeDesignProjects(currentUserId)
            .toUiStateFlow("Failed to load design projects")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val aiGenerationsState: StateFlow<UiState<List<AiGeneration>>> =
        repository.observeAiGenerations(currentUserId)
            .toUiStateFlow("Failed to load AI history")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val activityLogsState: StateFlow<UiState<List<ActivityLog>>> =
        repository.observeActivityLogs(currentUserId)
            .toUiStateFlow("Failed to load activity history")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val payoutMethodsState: StateFlow<UiState<List<PayoutMethod>>> =
        repository.observePayoutMethods(currentUserId)
            .toUiStateFlow("Failed to load payout methods")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val marketProductsState: StateFlow<UiState<List<MarketProduct>>> =
        repository.observeActiveMarketProducts()
            .toUiStateFlow("Failed to load Money Market")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val myProductsState: StateFlow<UiState<List<MarketProduct>>> =
        repository.observeMyMarketProducts(currentUserId)
            .toUiStateFlow("Failed to load your products")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val adminReportsState: StateFlow<UiState<List<ContentReport>>> =
        if (isAdminUser) {
            repository.observeAdminReports()
                .toUiStateFlow("Failed to load moderation reports")
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)
        } else {
            MutableStateFlow<UiState<List<ContentReport>>>(UiState.Success(emptyList())).asStateFlow()
        }

    private val _selectedProductId = MutableStateFlow<String?>(null)
    val selectedProductReviewsState: StateFlow<UiState<List<ProductReview>>> =
        _selectedProductId.flatMapLatest { prodId ->
            if (prodId.isNullOrBlank()) {
                flowOf(UiState.Success(emptyList()))
            } else {
                repository.observeProductReviews(prodId)
                    .toUiStateFlow("Failed to load reviews")
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Success(emptyList()))

    private val _aiResponseState = MutableStateFlow<AiResponseState>(AiResponseState.Idle)
    val aiResponseState: StateFlow<AiResponseState> = _aiResponseState.asStateFlow()

    private val _designDraftAiCopy = MutableStateFlow<DesignCopySuggestion?>(null)
    val designDraftAiCopy: StateFlow<DesignCopySuggestion?> = _designDraftAiCopy.asStateFlow()

    private val _isGeneratingDesignCopy = MutableStateFlow(false)
    val isGeneratingDesignCopy: StateFlow<Boolean> = _isGeneratingDesignCopy.asStateFlow()

    private val _prefilledAiMode = MutableStateFlow<AiMode?>(null)
    val prefilledAiMode: StateFlow<AiMode?> = _prefilledAiMode.asStateFlow()

    private val _prefilledAiPrompt = MutableStateFlow<String?>(null)
    val prefilledAiPrompt: StateFlow<String?> = _prefilledAiPrompt.asStateFlow()

    private val _prefilledDesignHeadline = MutableStateFlow<String?>(null)
    val prefilledDesignHeadline: StateFlow<String?> = _prefilledDesignHeadline.asStateFlow()

    private var aiJob: Job? = null

    init {
        viewModelScope.launch {
            val lang = prefsRepository.languageFlow.first()
            val theme = prefsRepository.themeModeFlow.first()
            val result = repository.ensureUserProfile(lang.code, theme.code)
            if (result.isFailure) {
                Log.w(TAG, "Profile bootstrap failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun selectProductForReviews(productId: String?) {
        _selectedProductId.value = productId
    }

    fun prepareAiPrompt(mode: AiMode, prompt: String) {
        _prefilledAiMode.value = mode
        _prefilledAiPrompt.value = prompt
    }

    fun clearPrefilledAiPrompt() {
        _prefilledAiMode.value = null
        _prefilledAiPrompt.value = null
    }

    fun sendTextToDesign(text: String) {
        _prefilledDesignHeadline.value = text.lines().firstOrNull { it.isNotBlank() }?.take(100) ?: text.take(100)
    }

    fun clearPrefilledDesignHeadline() {
        _prefilledDesignHeadline.value = null
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            prefsRepository.setLanguage(language)
            val profile = (userProfileState.value as? UiState.Success)?.data
            if (profile != null) {
                repository.updateUserProfile(
                    displayName = profile.displayName,
                    country = profile.country,
                    language = language.code,
                    themeMode = profile.themeMode,
                    bio = profile.bio,
                    photoUrl = profile.photoUrl
                )
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            prefsRepository.setThemeMode(mode)
            val profile = (userProfileState.value as? UiState.Success)?.data
            if (profile != null) {
                repository.updateUserProfile(
                    displayName = profile.displayName,
                    country = profile.country,
                    language = profile.language,
                    themeMode = mode.code,
                    bio = profile.bio,
                    photoUrl = profile.photoUrl
                )
            }
        }
    }

    fun updateProfile(
        displayName: String,
        country: String,
        language: AppLanguage,
        themeMode: ThemeMode,
        bio: String?,
        photoUrl: String?
    ) {
        viewModelScope.launch {
            prefsRepository.setLanguage(language)
            prefsRepository.setThemeMode(themeMode)
            val res = repository.updateUserProfile(
                displayName = displayName,
                country = country,
                language = language.code,
                themeMode = themeMode.code,
                bio = bio,
                photoUrl = photoUrl
            )
            emitResultMessage(res, "Perfil atualizado", "Profile error")
        }
    }

    fun toggleLessonComplete(courseId: String, lessonId: String, totalLessons: Int, lessonTitle: String) {
        viewModelScope.launch {
            val res = repository.toggleLessonCompletion(courseId, lessonId, totalLessons, lessonTitle)
            if (res.isFailure) {
                _events.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to update progress")
            }
        }
    }

    fun saveDesignProject(
        existingId: String?,
        templateType: DesignTemplateType,
        title: String,
        headline: String,
        subheadline: String,
        ctaText: String,
        brandName: String,
        paletteId: String
    ) {
        viewModelScope.launch {
            val res = repository.saveDesignProject(
                existingId = existingId,
                templateType = templateType,
                title = title,
                headline = headline,
                subheadline = subheadline,
                ctaText = ctaText,
                brandName = brandName,
                paletteId = paletteId
            )
            emitResultMessage(res, "Projeto guardado no estúdio", "Erro ao guardar projeto")
        }
    }

    fun deleteDesignProject(projectId: String) {
        viewModelScope.launch {
            val res = repository.deleteDesignProject(projectId)
            emitResultMessage(res, "Projeto eliminado", "Erro ao eliminar projeto")
        }
    }

    fun generateDesignCopyWithAi(topic: String, templateLabel: String, language: AppLanguage) {
        if (topic.isBlank()) return
        viewModelScope.launch {
            _isGeneratingDesignCopy.value = true
            val res = aiRepository.generateDesignCopy(topic, templateLabel, language)
            _isGeneratingDesignCopy.value = false
            res.onSuccess {
                _designDraftAiCopy.value = it
            }.onFailure { err ->
                _events.tryEmit(err.localizedMessage ?: "Money AI error")
            }
        }
    }

    fun clearDesignDraftAiCopy() {
        _designDraftAiCopy.value = null
    }

    fun generateWithAi(mode: AiMode, prompt: String, language: AppLanguage) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isEmpty()) return
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _aiResponseState.value = AiResponseState.Loading
            val country = (userProfileState.value as? UiState.Success)?.data?.country ?: "Moçambique"
            var finalOutput = ""
            try {
                aiRepository.streamAdvice(mode, cleanPrompt, language, country).collect { partial ->
                    finalOutput = partial
                    _aiResponseState.value = AiResponseState.Streaming(partial)
                }
                if (finalOutput.isNotBlank()) {
                    _aiResponseState.value = AiResponseState.Success(finalOutput)
                    repository.saveAiGeneration(mode, cleanPrompt, finalOutput)
                } else {
                    _aiResponseState.value = AiResponseState.Error("Money AI não retornou resposta.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI generation timed out", e)
                _aiResponseState.value = AiResponseState.Error("Tempo limite excedido (60s). Tente novamente.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI generation failed", e)
                _aiResponseState.value = AiResponseState.Error(e.localizedMessage ?: "Erro no Money AI.")
            }
        }
    }

    fun deleteAiGeneration(id: String) {
        viewModelScope.launch {
            repository.deleteAiGeneration(id)
        }
    }

    fun publishMarketProduct(
        title: String,
        description: String,
        category: MarketCategory,
        priceUsd: Double,
        priceMzn: Double,
        affiliatePercent: Long,
        paletteId: String
    ) {
        viewModelScope.launch {
            val profile = (userProfileState.value as? UiState.Success)?.data
            val sellerName = profile?.displayName?.ifBlank { null }
                ?: Firebase.auth.currentUser?.displayName
                ?: "Criador Money Hub"
            val sellerCountry = profile?.country ?: "Moçambique"
            val res = repository.publishMarketProduct(
                sellerName = sellerName,
                sellerCountry = sellerCountry,
                title = title,
                description = description,
                category = category,
                priceUsd = priceUsd,
                priceMzn = priceMzn,
                affiliatePercent = affiliatePercent,
                paletteId = paletteId
            )
            emitResultMessage(res, "Produto publicado no Money Market", "Erro ao publicar produto")
        }
    }

    fun deleteMarketProduct(productId: String) {
        viewModelScope.launch {
            val res = repository.deleteMarketProduct(productId)
            emitResultMessage(res, "Produto removido", "Erro ao remover produto")
        }
    }

    fun submitReview(productId: String, rating: Long, comment: String) {
        viewModelScope.launch {
            val profile = (userProfileState.value as? UiState.Success)?.data
            val authorName = profile?.displayName?.ifBlank { null }
                ?: Firebase.auth.currentUser?.displayName
                ?: "Membro Money Hub"
            val res = repository.submitReview(productId, authorName, rating, comment)
            emitResultMessage(res, "Avaliação publicada", "Erro ao publicar avaliação")
        }
    }

    fun reportContent(targetType: String, targetId: String, reason: String) {
        viewModelScope.launch {
            val res = repository.submitContentReport(targetType, targetId, reason)
            emitResultMessage(res, "Denúncia enviada para moderação", "Erro ao enviar denúncia")
        }
    }

    fun addPayoutMethod(provider: PayoutProvider, label: String, maskedAccount: String, currency: String) {
        viewModelScope.launch {
            val res = repository.addPayoutMethod(provider, label, maskedAccount, currency)
            emitResultMessage(res, "Método guardado", "Erro ao guardar método")
        }
    }

    fun deletePayoutMethod(methodId: String) {
        viewModelScope.launch {
            repository.deletePayoutMethod(methodId)
        }
    }

    fun updateReportStatus(reportId: String, status: String) {
        viewModelScope.launch {
            val res = repository.updateReportStatus(reportId, status)
            emitResultMessage(res, "Estado da denúncia atualizado", "Erro na moderação")
        }
    }

    fun moderateProductStatus(productId: String, status: String) {
        viewModelScope.launch {
            val res = repository.moderateProductStatus(productId, status)
            emitResultMessage(res, "Estado do produto atualizado", "Erro na moderação")
        }
    }

    private fun <T> emitResultMessage(result: Result<T>, successMsg: String, errorFallback: String) {
        if (result.isSuccess) {
            _events.tryEmit(successMsg)
        } else {
            _events.tryEmit(result.exceptionOrNull()?.localizedMessage ?: errorFallback)
        }
    }

    private fun <T> Flow<T>.toUiStateFlow(fallbackError: String): Flow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, fallbackError, rootError)
                emit(UiState.Error(rootError.message ?: fallbackError))
            }
}
