@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.shared.avalon.character.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CharacterEventDto(
    @Contextual val id: Uuid? = null,
    val requiredLevel: Int,
    val eventName: String = "",
    val eventTriggerScenario: String,
    val eventPermanentTrait: String
)

@Serializable
data class UpdateCharacterEventsRequest(
    @Contextual val characterId: Uuid,
    val events: List<CharacterEventDto>
)
