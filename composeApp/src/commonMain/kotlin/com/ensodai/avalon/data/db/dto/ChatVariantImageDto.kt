package com.ensodai.avalon.data.db.dto

import kotlin.uuid.Uuid

data class ChatVariantImageDto(
    val id: Uuid,
    val imageUrl: String,
    val thumbHash: String? = null
)