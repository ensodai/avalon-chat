package com.ensodai.shared.avalon.user.models

import kotlinx.serialization.Serializable

@Serializable
enum class AiProviderType(
    val id: String,
    val displayName: String,
    val defaultBaseUrl: String
) {
    GOOGLE("google", "Google AI Studio", "https://generativelanguage.googleapis.com"),
    OPENROUTER("openrouter", "OpenRouter", "https://openrouter.ai/api/v1"),
    OPENAI("openai", "OpenAI Direct", "https://api.openai.com/v1"),
    DEEPSEEK("deepseek", "DeepSeek", "https://api.deepseek.com/v1"),
    GROQ("groq", "Groq", "https://api.groq.com/openai/v1"),
    CUSTOM("custom", "Пользовательский (Custom URL)", "");

    companion object {
        fun fromId(id: String?): AiProviderType =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
                ?: GOOGLE
    }
}

@Serializable
enum class AiModelCategory {
    TEXT,
    TTS,
    IMAGE,
    IMAGE_PROMPT,
    SUMMARY;

    companion object {
        fun fromString(value: String?): AiModelCategory =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: TEXT
    }
}

@Serializable
data class AiModelCatalogItem(
    val id: String,
    val name: String,
    val provider: AiProviderType,
    val category: AiModelCategory,
    val contextWindow: String? = null,
    val promptPrice: String? = null,
    val completionPrice: String? = null,
    val description: String? = null,
    val isRecommended: Boolean = false,
    val badge: String? = null
)

@Serializable
data class GetAiCatalogModelsRequest(
    val provider: AiProviderType = AiProviderType.GOOGLE,
    val category: AiModelCategory = AiModelCategory.TEXT
)

@Serializable
data class GetAiCatalogModelsResponse(
    val provider: AiProviderType,
    val category: AiModelCategory,
    val models: List<AiModelCatalogItem> = emptyList()
)
