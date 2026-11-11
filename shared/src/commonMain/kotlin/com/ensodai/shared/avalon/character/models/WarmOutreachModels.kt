@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.shared.avalon.character.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class WarmOutreachSettingsDto(
    val isOutreachEnabled: Boolean,
    val outreachMorningEnabled: Boolean,
    val outreachMorningPrompt: String?,
    val outreachAfternoonEnabled: Boolean,
    val outreachAfternoonPrompt: String?,
    val outreachEveningEnabled: Boolean,
    val outreachEveningPrompt: String?
)

@Serializable
data class UpdateWarmOutreachSettingsRequest(
    @Contextual val characterId: Uuid,
    val settings: WarmOutreachSettingsDto
)
