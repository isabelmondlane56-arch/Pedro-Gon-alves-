package com.example.data

import android.util.Log
import com.example.localization.AppLanguage
import com.example.model.AiMode
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "MoneyAiRepository"

data class DesignCopySuggestion(
    val headline: String,
    val subheadline: String,
    val ctaText: String
)

class MoneyAiRepository {

    private val streamModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    private val designCopyModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "headline" to Schema.string(description = "Short punchy marketing headline under 55 characters"),
                "subheadline" to Schema.string(description = "Compelling subheadline or value proposition under 110 characters"),
                "ctaText" to Schema.string(description = "Action-oriented button text under 25 characters")
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    fun streamAdvice(
        mode: AiMode,
        userPrompt: String,
        language: AppLanguage,
        userCountry: String
    ): Flow<String> = flow {
        val systemDirective = buildSystemPrompt(mode, language, userCountry)
        val fullPrompt = "$systemDirective\n\nPedido do utilizador / User request:\n$userPrompt"
        val accumulated = StringBuilder()

        withTimeout(60_000L) {
            streamModel.generateContentStream(fullPrompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    accumulated.append(text)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("Money AI returned an empty response.")
        }
    }

    suspend fun generateDesignCopy(
        topic: String,
        templateLabel: String,
        language: AppLanguage
    ): Result<DesignCopySuggestion> {
        return try {
            val langInstruction = if (language == AppLanguage.PT) {
                "Responda em Português claro e persuasivo."
            } else {
                "Respond in crisp, persuasive English."
            }
            val prompt = """
                You are Money AI inside the Money Hub creative studio.
                Create high-converting marketing copy for a $templateLabel about: "$topic".
                $langInstruction
            """.trimIndent()

            val response = withTimeout(60_000L) {
                designCopyModel.generateContent(prompt)
            }
            val jsonText = response.text ?: error("Empty structured response")
            val json = JSONObject(jsonText)
            Result.success(
                DesignCopySuggestion(
                    headline = json.getString("headline").take(120),
                    subheadline = json.getString("subheadline").take(240),
                    ctaText = json.getString("ctaText").take(60)
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Design copy generation timed out", e)
            Result.failure(IllegalStateException("A geração expirou após 60 segundos. Tente novamente."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Design copy generation failed", e)
            Result.failure(e)
        }
    }

    private fun buildSystemPrompt(
        mode: AiMode,
        language: AppLanguage,
        userCountry: String
    ): String {
        val langRule = if (language == AppLanguage.PT) {
            "Responda em Português profissional, claro, prático e estruturado com tópicos fáceis de aplicar."
        } else {
            "Respond in clear, professional, actionable English structured with concise bullet points."
        }

        val roleContext = when (mode) {
            AiMode.BUSINESS_IDEAS -> "Act as an international digital business strategist. Suggest realistic, high-margin digital business models tailored to $userCountry and global online buyers."
            AiMode.AD_COPY -> "Act as a senior performance copywriter for Facebook Ads, Instagram Ads, and WhatsApp campaigns. Provide hook, body, and CTA variations."
            AiMode.EBOOK_IDEAS -> "Act as a bestselling digital publishing consultant. Provide compelling ebook titles, target audience, chapter-by-chapter outline, and pricing strategy."
            AiMode.PRODUCT_DESC -> "Act as an e-commerce & digital marketplace conversion specialist. Write persuasive product descriptions ready for Money Market."
            AiMode.MARKETING_STRATEGY -> "Act as a growth marketing director. Deliver a step-by-step customer acquisition and conversion plan."
            AiMode.BUSINESS_PLAN -> "Act as a startup advisor. Build a concise lean business plan: Value Proposition, Customer Segments, Channels, Revenue Streams, and First 7-Day Actions."
            AiMode.SOCIAL_CONTENT -> "Act as a viral social media strategist. Provide ready-to-post hooks, captions, and visual direction."
            AiMode.TRANSLATE -> "Act as an international business translator and localization expert. Translate the provided marketing or business text accurately into the requested languages while preserving persuasive tone."
        }
        return "$roleContext\n$langRule"
    }
}
