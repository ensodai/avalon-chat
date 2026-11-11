package com.ensodai.shared.avalon.user.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class AiProviderResponse(
    val id: String,
    val displayName: String,
    val isDefault: Boolean = false
)

@Serializable
data class UserAiSettingsResponse(
    val userId: Uuid,
    val providerId: String = "google",
    val availableProviders: List<AiProviderResponse> = emptyList(),
    val baseUrl: String = "https://generativelanguage.googleapis.com",
    val apiKey: String?,
    val textModel: String = "gemini-3.1-flash-lite",
    val ttsModel: String = "gemini-2.5-flash-preview-tts",
    val imageModel: String = "gemini-2.5-flash-image",
    val imagePromptModel: String = "gemini-3.1-flash-lite",
    val summaryModel: String = "gemini-3.1-flash-lite",
    val isCustomProviderEnabled: Boolean = false
) {
    val providerType: AiProviderType get() = AiProviderType.fromId(providerId)
}

@Serializable
data class UpdateUserAiSettingsRequest(
    val providerId: String = "google",
    val baseUrl: String? = null,
    val apiKey: String? = null,
    val textModel: String? = null,
    val ttsModel: String? = null,
    val imageModel: String? = null,
    val imagePromptModel: String? = null,
    val summaryModel: String? = null,
    val isCustomProviderEnabled: Boolean = false
)

@Serializable
data class TestUserAiConnectionRequest(
    val providerId: String = "google",
    val baseUrl: String? = null,
    val apiKey: String,
    val model: String = "gemini-2.5-flash"
)

@Serializable
data class TestUserAiConnectionResponse(
    val success: Boolean,
    val message: String
)
