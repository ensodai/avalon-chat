package com.ensodai.avalon.domain.models.chat

import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import androidx.compose.runtime.Immutable

@Immutable
data class WelcomeInfo(
    val description: String?,
    val imageUrls: List<WelcomeCardImageDto>,
    val colors: WelcomeColors
)

@Immutable
data class WelcomeColors(
    val start: String,
    val end: String,
    val light: String,
    val isLightEnabled: Boolean
)