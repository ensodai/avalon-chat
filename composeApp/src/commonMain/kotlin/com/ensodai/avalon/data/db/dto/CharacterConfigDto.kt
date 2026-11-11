package com.ensodai.avalon.data.db.dto

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class CharacterConfigDto(
    val publicCharacterId: Uuid,
    val localCharacterId: Int?,
    val ownerNick: String,
    val name: String,
    val firstMessage: String?,
    val shortDescription: String,
    val fullDescription: String?,
    val avatar: CharacterImageDTO?,
    val images: List<CharacterImageDTO>?,
    val isPublicFullDescription: Boolean,
    val userCanEditCharacter: Boolean,
    val welcomeCard: WelcomeCardDto?,
    val updatedAt: Instant
)

data class WelcomeCardDto(
    val id: Uuid,
    val welcomeCardDescription: String?,
    val welcomeCardImages: List<WelcomeCardImageDto>?,
    val startColor: String,
    val endColor: String,
    val colorLight: String,
    val isEnableLight: Boolean
)