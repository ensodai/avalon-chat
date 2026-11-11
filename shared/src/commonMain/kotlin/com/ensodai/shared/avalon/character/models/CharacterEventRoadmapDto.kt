@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.shared.avalon.character.models

import kotlinx.serialization.Serializable

@Serializable
data class CharacterEventRoadmapDto(
    val level: Int,
    val hasReward: Boolean,
    val isUnlocked: Boolean,
    val triggerScenario: String? = null,
    val rewardPhoto: CharacterImageResponse? = null
)
