package app.data.network.models.response

import kotlinx.serialization.Serializable

@Serializable
data class CharacterMainRulesResponse(
    val mainRules: String,
    val defaultGeminiModel: String,
    val maxOutputTokens: Int
)
