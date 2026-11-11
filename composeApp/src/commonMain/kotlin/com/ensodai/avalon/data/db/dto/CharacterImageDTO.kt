package com.ensodai.avalon.data.db.dto

import kotlin.uuid.Uuid

data class CharacterImageDTO(
    val imagePublicId: Uuid,
    val url: String,
    val thumbHash: String? = null,
    val isPrivate: Boolean = false,
    val energyCost: Int = 0,
    val requiredLevel: Int? = null,
    val isUnlocked: Boolean = false
)