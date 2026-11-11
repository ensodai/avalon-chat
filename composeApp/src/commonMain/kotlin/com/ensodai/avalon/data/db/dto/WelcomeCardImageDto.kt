package com.ensodai.avalon.data.db.dto

import kotlin.uuid.Uuid
import androidx.compose.runtime.Immutable

@Immutable
data class WelcomeCardImageDto(
    val imagePublicId: Uuid,
    val url: String,
    val thumbHash: String? = null
)