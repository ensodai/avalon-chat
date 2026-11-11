package com.ensodai.avalon.domain.models

import com.ensodai.avalon.domain.useCases.commonUseCase.WelcomeCardConfig
import kotlin.time.Instant

data class UpdateCharacterProfile(
    val localId: Int,
    val publicId: String?,
    val characterName: String,
    val fullDescription: String?,
    val shortDescription: String,
    val characterAvatar: CharacterAvatar,
    val temperature: Float,
    val characterImages: List<CharacterImage>?,
    val isPublicCharacter: Boolean,
    val isPublicFullDescription: Boolean,
    val firstMessage: String?,
    val welcomeCardImages: List<CharacterImage>?,
    val welcomeCardConfig: WelcomeCardConfig?,
    val characterUpdatedAt: Instant?
)
