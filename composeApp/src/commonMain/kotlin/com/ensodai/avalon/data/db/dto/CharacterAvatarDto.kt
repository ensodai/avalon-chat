package com.ensodai.avalon.data.db.dto

import kotlin.uuid.Uuid

data class CharacterAvatarDto(
    val imagePublicId: Uuid,
    val url: String,
    val thumbHash: String? = null
)