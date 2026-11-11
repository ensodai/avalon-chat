package com.ensodai.avalon.domain.repository.local

import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.CharacterAvatarResponse
import com.ensodai.shared.avalon.character.models.CharacterImageResponse
import com.ensodai.shared.avalon.character.models.WelcomeCardImageResponse
import com.ensodai.avalon.domain.useCases.editWelcomeCardUseCase.UpdateWelcomeCardConfig
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.character.models.InteractionFormat
import kotlin.uuid.Uuid

interface LocalUpdateCharacterRepository {

    suspend fun updateName(characterId: Uuid, newName: String): Result<Unit>

    suspend fun updateDescription(characterId: Uuid, description: String): Result<Unit>

    suspend fun updateFullDescription(
        characterId: Uuid,
        fullDescription: String,
        isPublicFullDescription: Boolean
    ): Result<Unit>

    suspend fun updateScenario(characterId: Uuid, scenario: String?): Result<Unit>

    suspend fun updateMesExample(characterId: Uuid, mesExample: String?): Result<Unit>

    suspend fun updatePrivacy(
        characterId: Uuid,
        characterVisibility: CharacterVisibility
    ): Result<Unit>

    suspend fun updateTemperature(characterId: Uuid, temperature: Float): Result<Unit>

    suspend fun updateFirstMessage(characterId: Uuid, message: String): Result<Unit>

    suspend fun updateWelcomeCard(
        characterId: Uuid,
        welcomeCardImages: List<WelcomeCardImageResponse>,
        welcomeCardConfig: UpdateWelcomeCardConfig
    ): Result<Unit>

    suspend fun updateImages(
        avatar: CharacterAvatarResponse?,
        images: List<CharacterImageResponse>,
        characterId: Uuid
    )

    suspend fun updateImageUnlockState(imageId: Uuid, imageUrl: String, isUnlocked: Boolean): Result<Unit>

    suspend fun updateOutreach(characterId: Uuid, isOutreachEnabled: Boolean): Result<Unit>

    suspend fun updateVoice(characterId: Uuid, voice: VoiceModel): Result<Unit>

    suspend fun updateInteractionFormat(characterId: Uuid, interactionFormat: InteractionFormat): Result<Unit>
}