package com.ensodai.shared.avalon.gifts.models

import kotlinx.serialization.Serializable

@Serializable
data class ImageGenerationConfig(
    val imagePrompt: String,
    val visualDescription: String,
    val era: String,
    val archetype: String
)
