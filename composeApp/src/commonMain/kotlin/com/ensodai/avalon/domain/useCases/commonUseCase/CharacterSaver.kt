package com.ensodai.avalon.domain.useCases.commonUseCase

import com.ensodai.avalon.data.db.dto.CharacterAvatarDto
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.WelcomeCardDto
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.shared.avalon.character.models.CharacterResponse
import com.ensodai.avalon.domain.models.SaveCharacterProfileDto
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import org.koin.core.annotation.Single

@Single
class CharacterSaver(
    private val localCharacterRepository: LocalCharacterRepository,
) {

    suspend fun save(character: CharacterResponse) {
        val avatar = character.avatar?.let {
            CharacterAvatarDto(
                imagePublicId = it.id,
                url = it.imageUrl,
                thumbHash = it.thumbHash
            )
        }
        val images = character.images.map {
            CharacterImageDTO(
                imagePublicId = it.id,
                url = it.imageUrl,
                thumbHash = it.thumbHash,
                isPrivate = it.isPrivate,
                energyCost = it.energyCost,
                requiredLevel = it.requiredLevel,
                isUnlocked = it.isUnlocked
            )
        }
        val welcomeCardImages = character.welcomeCard?.welcomeCardImages?.map {
            WelcomeCardImageDto(
                imagePublicId = it.id,
                url = it.imageUrl,
                thumbHash = it.thumbHash
            )
        }
        val welcomeCard = character.welcomeCard?.let { welcomeCard ->
            WelcomeCardDto(
                id = welcomeCard.id,
                welcomeCardDescription = welcomeCard.welcomeCardDescription,
                welcomeCardImages = welcomeCardImages,
                startColor = welcomeCard.startColor,
                endColor = welcomeCard.endColor,
                colorLight = welcomeCard.cardColorLight,
                isEnableLight = welcomeCard.isEnableLight
            )
        }

        val saveCharacterProfileDto = SaveCharacterProfileDto(
            characterName = character.characterName,
            fullDescription = character.fullDescription,
            shortDescription = character.shortDescription,
            updatedAt = character.updatedAt,
            visibility = character.visibility,
            firstMessage = character.firstMessage,
            welcomeCard = welcomeCard,
            userCanEditCharacter = character.canEdit,
            isPublicFullDescription = character.isPublicFullDescription,
            temperature = character.temperature,
            unclaimedEnergy = character.unclaimedEnergy,
            totalEarnedEnergy = character.totalEarnedEnergy,
            isOutreachEnabled = character.isOutreachEnabled,
            voice = character.voice,
            relationshipLevel = character.relationshipLevel,
            experiencePoints = character.experiencePoints,
            eventsCount = character.eventsCount,
            interactionFormat = character.interactionFormat,
            scenario = character.scenario,
            mesExample = character.mesExample
        )

        localCharacterRepository.upsertCharacter(
            characterId = character.id,
            ownerNick = character.ownerNick,
            characterAvatar = avatar,
            characterImages = images,
            saveCharacterProfileDto = saveCharacterProfileDto,
            welcomeCardImages = welcomeCardImages,
        )
    }
}
