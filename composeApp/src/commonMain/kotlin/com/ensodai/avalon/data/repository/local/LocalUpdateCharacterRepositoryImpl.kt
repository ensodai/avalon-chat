package com.ensodai.avalon.data.repository.local

import com.ensodai.avalon.data.repository.local.base.BaseRepository
import com.ensodai.avalon.data.repository.local.base.DatabaseExecutor

import com.ensodai.avalon.data.db.dao.UpdateCharacterDao
import com.ensodai.avalon.data.db.models.character.CharacterAvatarEntity
import com.ensodai.avalon.data.db.models.character.CharacterImageEntity
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.data.db.models.character.WelcomeCardImageEntity
import com.ensodai.shared.avalon.character.models.CharacterAvatarResponse
import com.ensodai.shared.avalon.character.models.CharacterImageResponse
import com.ensodai.shared.avalon.character.models.WelcomeCardImageResponse
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import com.ensodai.avalon.domain.useCases.editWelcomeCardUseCase.UpdateWelcomeCardConfig
import kotlin.uuid.Uuid

class LocalUpdateCharacterRepositoryImpl(
    dbExecutor: DatabaseExecutor,
    private val updateCharacterDao: UpdateCharacterDao
) : BaseRepository(dbExecutor), LocalUpdateCharacterRepository {

    override suspend fun updateName(
        characterId: Uuid,
        newName: String
    ): Result<Unit> = runCatching {
        dbWrite { updateCharacterDao.updateName(characterId, newName) }
    }

    override suspend fun updateDescription(
        characterId: Uuid,
        description: String
    ): Result<Unit> = runCatching {
        dbWrite { updateCharacterDao.updateDescription(characterId, description) }
    }

    override suspend fun updateFullDescription(
        characterId: Uuid,
        fullDescription: String,
        isPublicFullDescription: Boolean
    ): Result<Unit> = runCatching {
        dbWrite {
            updateCharacterDao.updateFullDescription(
                characterId = characterId,
                fullDescription = fullDescription,
                isPublicFullDescription = isPublicFullDescription
            )
        }
    }

    override suspend fun updateScenario(
        characterId: Uuid,
        scenario: String?
    ): Result<Unit> = runCatching {
        dbWrite {
            updateCharacterDao.updateScenario(characterId, scenario)
        }
    }

    override suspend fun updateMesExample(
        characterId: Uuid,
        mesExample: String?
    ): Result<Unit> = runCatching {
        dbWrite {
            updateCharacterDao.updateMesExample(characterId, mesExample)
        }
    }

    override suspend fun updatePrivacy(
        characterId: Uuid,
        characterVisibility: CharacterVisibility
    ): Result<Unit> = runCatching {
        dbWrite { updateCharacterDao.updatePrivacy(characterId, characterVisibility) }
    }

    override suspend fun updateTemperature(
        characterId: Uuid,
        temperature: Float
    ): Result<Unit> = runCatching {
        dbWrite { updateCharacterDao.updateTemperature(characterId, temperature) }
    }

    override suspend fun updateFirstMessage(
        characterId: Uuid,
        message: String
    ): Result<Unit> = runCatching {
        dbWrite { updateCharacterDao.updateFirstMessage(characterId, message) }
    }

    override suspend fun updateWelcomeCard(
        characterId: Uuid,
        welcomeCardImages: List<WelcomeCardImageResponse>,
        welcomeCardConfig: UpdateWelcomeCardConfig
    ): Result<Unit> = runCatching {

        val imageEntities = welcomeCardImages.map { imageDto ->
            WelcomeCardImageEntity(
                id = imageDto.id,
                characterId = characterId,
                imageUrl = imageDto.imageUrl,
                thumbHash = imageDto.thumbHash
            )
        }

        dbWrite {
            updateCharacterDao.updateWelcomeCardTransaction(
                characterId = characterId,
                config = welcomeCardConfig,
                newImages = imageEntities
            )
        }
    }

    override suspend fun updateImages(
        avatar: CharacterAvatarResponse?,
        images: List<CharacterImageResponse>,
        characterId: Uuid
    ) {
        dbWrite {
            updateCharacterDao.deleteAvatarByCharId(characterId)

            if (avatar != null) {
                updateCharacterDao.insertCharacterAvatar(
                    CharacterAvatarEntity(
                        id = avatar.id,
                        characterId = characterId,
                        imageUrl = avatar.imageUrl,
                        thumbHash = avatar.thumbHash
                    )
                )
            }

            updateCharacterDao.deleteImagesByCharId(characterId)

            if (images.isNotEmpty()) {
                val imageEntities = images.map { image ->
                    CharacterImageEntity(
                        id = image.id,
                        characterId = characterId,
                        imageUrl = image.imageUrl,
                        thumbHash = image.thumbHash,
                        isPrivate = image.isPrivate,
                        energyCost = image.energyCost,
                        requiredLevel = image.requiredLevel,
                        isUnlocked = image.isUnlocked
                    )
                }
                updateCharacterDao.insertCharacterImages(imageEntities)
            }
        }
    }

    override suspend fun updateImageUnlockState(
        imageId: Uuid,
        imageUrl: String,
        isUnlocked: Boolean
    ): Result<Unit> = runCatching {
        dbWrite { updateCharacterDao.updateImageUnlockState(imageId, imageUrl, isUnlocked) }
    }

    override suspend fun updateOutreach(
        characterId: Uuid,
        isOutreachEnabled: Boolean
    ): Result<Unit> = runCatching {
        dbWrite { updateCharacterDao.updateOutreach(characterId, isOutreachEnabled) }
    }

    override suspend fun updateVoice(
        characterId: Uuid,
        voice: VoiceModel
    ): Result<Unit> = runCatching {
        dbWrite { updateCharacterDao.updateVoice(characterId, voice) }
    }

    override suspend fun updateInteractionFormat(
        characterId: Uuid,
        interactionFormat: InteractionFormat
    ): Result<Unit> = runCatching {
        dbWrite { updateCharacterDao.updateInteractionFormat(characterId, interactionFormat) }
    }
}
