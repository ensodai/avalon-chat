package com.ensodai.avalon.domain.models

import com.ensodai.avalon.data.db.dto.WelcomeCardDto
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import kotlin.time.Instant

data class SaveCharacterProfileDto(
    val characterName: String,
    val fullDescription: String?,
    val shortDescription: String,
    val updatedAt: Instant,
    val isPublicFullDescription: Boolean,
    val visibility: CharacterVisibility,
    val firstMessage: String?,
    val welcomeCard: WelcomeCardDto?,
    val userCanEditCharacter: Boolean,
    val temperature: Float,
    val unclaimedEnergy: Double? = null,
    val totalEarnedEnergy: Double? = null,
    val isOutreachEnabled: Boolean = false,
    val voice: VoiceModel = VoiceModel.ZEPHYR,
    val relationshipLevel: Int = 1,
    val experiencePoints: Long = 0L,
    val eventsCount: Int = 0,
    val interactionFormat: InteractionFormat = InteractionFormat.LITERARY_RPG,
    val scenario: String? = null,
    val mesExample: String? = null
)