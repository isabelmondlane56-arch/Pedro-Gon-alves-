package com.example.model

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ThemeMode(val code: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light");

    companion object {
        fun fromCode(raw: String?): ThemeMode =
            entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: DARK
    }
}

enum class AccountPlan(val code: String) {
    FREE("FREE"),
    PREMIUM("PREMIUM");

    companion object {
        fun fromCode(raw: String?): AccountPlan =
            entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: FREE
    }
}

enum class UserRole(val code: String) {
    USER("USER"),
    ADMIN("ADMIN");

    companion object {
        fun fromCode(raw: String?): UserRole =
            entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: USER
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val country: String = "Moçambique",
    val language: String = "pt",
    val themeMode: String = "dark",
    val plan: String = "FREE",
    val role: String = "USER",
    val bio: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AcademyProgress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val completedLessonIds: List<String> = emptyList(),
    val progressPercent: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class DesignTemplateType(val code: String, val labelPt: String, val labelEn: String) {
    POSTER("POSTER", "Cartaz Publicitário", "Ad Poster"),
    AD("AD", "Anúncio Comercial", "Commercial Ad"),
    EBOOK_COVER("EBOOK_COVER", "Capa de Ebook", "Ebook Cover"),
    SOCIAL_POST("SOCIAL_POST", "Post Redes Sociais", "Social Media Post"),
    LOGO("LOGO", "Logótipo", "Brand Logo"),
    BANNER("BANNER", "Banner Digital", "Store Banner"),
    PRODUCT_CARD("PRODUCT_CARD", "Cartão de Produto", "Product Card");

    companion object {
        fun fromCode(raw: String?): DesignTemplateType =
            entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: POSTER
    }
}

data class DesignPalette(
    val id: String,
    val namePt: String,
    val nameEn: String,
    val backgroundStart: Color,
    val backgroundEnd: Color,
    val accent: Color,
    val textColor: Color
)

val DesignPalettes = listOf(
    DesignPalette(
        id = "gold_navy",
        namePt = "Ouro & Azul Noite",
        nameEn = "Gold & Midnight Navy",
        backgroundStart = Color(0xFF07090F),
        backgroundEnd = Color(0xFF13293D),
        accent = Color(0xFFE5B84B),
        textColor = Color(0xFFF4F6FA)
    ),
    DesignPalette(
        id = "obsidian_emerald",
        namePt = "Obsidiana & Esmeralda",
        nameEn = "Obsidian & Emerald",
        backgroundStart = Color(0xFF0A1412),
        backgroundEnd = Color(0xFF10382C),
        accent = Color(0xFF2EC486),
        textColor = Color(0xFFF3FAF7)
    ),
    DesignPalette(
        id = "royal_sapphire",
        namePt = "Safira Executiva",
        nameEn = "Executive Sapphire",
        backgroundStart = Color(0xFF0C1736),
        backgroundEnd = Color(0xFF1F3E82),
        accent = Color(0xFFF4CE6A),
        textColor = Color(0xFFFFFFFF)
    ),
    DesignPalette(
        id = "ivory_luxury",
        namePt = "Marfim & Dourado",
        nameEn = "Ivory & Gold",
        backgroundStart = Color(0xFFFDFBF5),
        backgroundEnd = Color(0xFFEDE4CC),
        accent = Color(0xFF9A7209),
        textColor = Color(0xFF0D1B2A)
    ),
    DesignPalette(
        id = "crimson_promo",
        namePt = "Impacto Comercial",
        nameEn = "Promo Impact",
        backgroundStart = Color(0xFF1E0B14),
        backgroundEnd = Color(0xFF54142B),
        accent = Color(0xFFFFC857),
        textColor = Color(0xFFFFF8F9)
    )
)

@Keep
@IgnoreExtraProperties
data class DesignProject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val templateType: String = DesignTemplateType.POSTER.code,
    val title: String = "",
    val headline: String = "",
    val subheadline: String = "",
    val ctaText: String = "",
    val brandName: String = "",
    val paletteId: String = "gold_navy",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class AiMode(
    val code: String,
    val labelPt: String,
    val labelEn: String,
    val samplePromptPt: String,
    val samplePromptEn: String
) {
    BUSINESS_IDEAS(
        "BUSINESS_IDEAS",
        "Ideias de Negócio",
        "Business Ideas",
        "5 ideias de negócios digitais de baixo custo para começar em Moçambique",
        "5 low-cost digital business ideas to launch this month"
    ),
    AD_COPY(
        "AD_COPY",
        "Textos Publicitários",
        "Ad Copy",
        "Cria 3 textos persuasivos para anúncio no Instagram sobre um curso de tráfego pago",
        "Write 3 high-converting Instagram ad copy variations for an online course"
    ),
    EBOOK_IDEAS(
        "EBOOK_IDEAS",
        "Ideias de Ebooks",
        "Ebook Ideas",
        "Estrutura completa de capítulos e título vendedor para um ebook de finanças pessoais",
        "Complete chapter outline and compelling title for a personal finance ebook"
    ),
    PRODUCT_DESC(
        "PRODUCT_DESC",
        "Descrições Comerciais",
        "Product Copy",
        "Descrição comercial pronta para publicar no Money Market para um kit de templates",
        "Commercial marketplace description for a digital marketing template kit"
    ),
    MARKETING_STRATEGY(
        "MARKETING_STRATEGY",
        "Estratégias Marketing",
        "Marketing Strategy",
        "Plano de lançamento em 14 dias para vender o meu primeiro produto digital",
        "14-day launch strategy to sell my first digital product online"
    ),
    BUSINESS_PLAN(
        "BUSINESS_PLAN",
        "Plano de Negócio",
        "Business Plan",
        "Mini plano de negócio com proposta de valor, público-alvo, canais e preços",
        "Lean business plan with value proposition, target audience, channels, and pricing"
    ),
    SOCIAL_CONTENT(
        "SOCIAL_CONTENT",
        "Redes Sociais",
        "Social Content",
        "Calendário de 5 publicações virais para atrair clientes para uma loja virtual",
        "5-post viral content calendar to attract buyers to an online storefront"
    ),
    TRANSLATE(
        "TRANSLATE",
        "Tradução Multilíngue",
        "Multilingual Translate",
        "Traduz para Inglês, Francês e Espanhol: 'Transforma o teu conhecimento em rendimento digital.'",
        "Translate to Portuguese, French, and Spanish: 'Turn your digital skills into global income.'"
    );

    companion object {
        fun fromCode(raw: String?): AiMode =
            entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: BUSINESS_IDEAS
    }
}

@Keep
@IgnoreExtraProperties
data class AiGeneration(
    @DocumentId val id: String = "",
    val userId: String = "",
    val mode: String = AiMode.BUSINESS_IDEAS.code,
    val prompt: String = "",
    val response: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ActivityLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val actionType: String = "",
    val title: String = "",
    val detail: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

enum class PayoutProvider(
    val code: String,
    val label: String,
    val regionTag: String
) {
    MPESA("MPESA", "M-Pesa (Vodacom MZ)", "Moçambique"),
    EMOLA("EMOLA", "e-Mola (Movitel MZ)", "Moçambique"),
    BANK_MZ("BANK_MZ", "Conta Bancária / NIB", "Moçambique & CPLP"),
    PAYPAL("PAYPAL", "PayPal Global", "Internacional"),
    STRIPE("STRIPE", "Stripe / Cartão", "Internacional");

    companion object {
        fun fromCode(raw: String?): PayoutProvider =
            entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: MPESA
    }
}

@Keep
@IgnoreExtraProperties
data class PayoutMethod(
    @DocumentId val id: String = "",
    val userId: String = "",
    val provider: String = PayoutProvider.MPESA.code,
    val accountLabel: String = "",
    val maskedAccount: String = "",
    val currency: String = "MZN",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class MarketCategory(val code: String, val labelPt: String, val labelEn: String) {
    EBOOK("EBOOK", "Ebooks", "Ebooks"),
    COURSE("COURSE", "Cursos Digitais", "Courses"),
    SERVICE("SERVICE", "Serviços", "Services"),
    DESIGN_KIT("DESIGN_KIT", "Kits & Lojas", "Kits & Stores"),
    AFFILIATE("AFFILIATE", "Afiliados", "Affiliates");

    companion object {
        fun fromCode(raw: String?): MarketCategory =
            entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: EBOOK
    }
}

@Keep
@IgnoreExtraProperties
data class MarketProduct(
    @DocumentId val id: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerCountry: String = "Moçambique",
    val title: String = "",
    val description: String = "",
    val category: String = MarketCategory.EBOOK.code,
    val priceUsd: Double = 0.0,
    val priceMzn: Double = 0.0,
    val affiliatePercent: Long = 20L,
    val ratingAvg: Double = 0.0,
    val reviewCount: Long = 0L,
    val paletteId: String = "gold_navy",
    val status: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ProductReview(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val rating: Long = 5L,
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ContentReport(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val targetType: String = "MARKET_PRODUCT",
    val targetId: String = "",
    val reason: String = "",
    val status: String = "OPEN",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AiResponseState {
    data object Idle : AiResponseState
    data object Loading : AiResponseState
    data class Streaming(val text: String) : AiResponseState
    data class Success(val text: String) : AiResponseState
    data class Error(val message: String) : AiResponseState
}
