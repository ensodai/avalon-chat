package com.ensodai.avalon.domain.useCases.editWelcomeCardUseCase

import kotlin.uuid.Uuid

data class UpdateWelcomeCardConfig(
    val deletedImages: List<Uuid>,
    val welcomeCardDescription: String?,
    val startColor: String,
    val endColor: String,
    val colorLight: String,
    val isEnableLight: Boolean
)
