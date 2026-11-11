package com.ensodai.avalon.presentation.characterProfileScreen.viewState

import kotlin.uuid.Uuid

data class CharacterAvatarWithSeed(
    val id: Uuid? = null,
    val imageUrl: String?,
    val seed: String,
    val thumbHash: String? = null,
    val isPrivate: Boolean = false,
    val energyCost: Int = 0,
    val requiredLevel: Int? = null,
    val isUnlocked: Boolean = false
)