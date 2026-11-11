@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.avalon.domain.useCases.importCharacterCardUseCase

import com.ensodai.shared.avalon.character.models.CharacterPublishedResponse
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.ImportCharacterCardRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.data.db.dto.CharacterAvatarDto
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.ChatSessionPreviewDto
import com.ensodai.avalon.data.db.dto.ChatVariantDto
import com.ensodai.avalon.data.db.dto.ChatVariantImageDto
import com.ensodai.avalon.data.db.dto.WelcomeCardDto
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.avalon.domain.models.SaveCharacterProfileDto
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.utils.AppLogger
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ImportCharacterCardUseCase(
    private val localCharacterRepository: LocalCharacterRepository,
    private val characterRpcService: CharacterRpcService,
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val logger: AppLogger
) {
    suspend fun execute(
        fileBase64: String,
        visibility: CharacterVisibility = CharacterVisibility.PRIVATE
    ): Result<Uuid> {
        return runCatching {
            logger.d("ImportCharacterCardUseCase", "Отправка запроса импорта карточки на сервер...")
            val request = ImportCharacterCardRequest(
                fileBase64 = fileBase64,
                visibility = visibility
            )
            val response: CharacterPublishedResponse = characterRpcService.importCharacterCard(request)

            val characterAvatarDto = response.character.avatar?.let { avatar ->
                CharacterAvatarDto(
                    imagePublicId = avatar.id,
                    url = avatar.imageUrl,
                    thumbHash = avatar.thumbHash
                )
            }

            val characterImages = response.character.images?.map { image ->
                CharacterImageDTO(
                    isPrivate = image.isPrivate,
                    energyCost = image.energyCost,
                    requiredLevel = image.requiredLevel,
                    imagePublicId = image.id,
                    url = image.imageUrl,
                    thumbHash = image.thumbHash
                )
            }

            val welcomeCardImages = response.character.welcomeCard?.welcomeCardImages?.map { image ->
                WelcomeCardImageDto(
                    imagePublicId = image.id,
                    url = image.imageUrl,
                    thumbHash = image.thumbHash
                )
            }

            val welcomeCard = response.character.welcomeCard?.let {
                WelcomeCardDto(
                    id = it.id,
                    welcomeCardDescription = it.welcomeCardDescription,
                    welcomeCardImages = welcomeCardImages,
                    startColor = it.startColor,
                    endColor = it.endColor,
                    colorLight = it.cardColorLight,
                    isEnableLight = it.isEnableLight
                )
            }

            val chatVariantImageDto = response.chatVariant.image?.let {
                ChatVariantImageDto(
                    id = it.id,
                    imageUrl = it.imageUrl,
                    thumbHash = it.thumbHash
                )
            }

            val characterDto = SaveCharacterProfileDto(
                characterName = response.character.characterName,
                fullDescription = response.character.fullDescription,
                shortDescription = response.character.shortDescription,
                updatedAt = response.character.updatedAt,
                visibility = response.character.visibility,
                firstMessage = response.character.firstMessage,
                welcomeCard = welcomeCard,
                userCanEditCharacter = response.character.canEdit,
                isPublicFullDescription = response.character.isPublicFullDescription,
                temperature = response.character.temperature,
                unclaimedEnergy = response.character.unclaimedEnergy,
                totalEarnedEnergy = response.character.totalEarnedEnergy,
                isOutreachEnabled = response.character.isOutreachEnabled,
                voice = response.character.voice,
                relationshipLevel = response.character.relationshipLevel,
                experiencePoints = response.character.experiencePoints,
                eventsCount = response.character.eventsCount,
                interactionFormat = response.character.interactionFormat,
                scenario = response.character.scenario,
                mesExample = response.character.mesExample
            )

            val chatVariantDto = ChatVariantDto(
                id = response.chatVariant.id,
                name = response.chatVariant.name,
                image = chatVariantImageDto,
                sessionId = response.chatVariant.sessionId,
                unreadCount = response.chatVariant.unreadCount,
                createdAt = response.chatVariant.createdAt,
                updatedAt = response.chatVariant.updatedAt
            )

            val chatSessionDto = ChatSessionPreviewDto(
                id = response.chatSession.id,
                characterId = response.chatSession.characterId,
                isArchived = response.chatSession.isArchived,
                createdAt = response.chatSession.createdAt,
                updatedAt = response.chatSession.updatedAt,
                activeChatVariantId = null,
                lastMessage = null
            )

            localCharacterRepository.upsertCharacter(
                characterId = response.character.id,
                ownerNick = response.character.ownerNick,
                characterAvatar = characterAvatarDto,
                characterImages = characterImages,
                saveCharacterProfileDto = characterDto,
                welcomeCardImages = welcomeCardImages,
            )
            localChatSessionsRepository.upsertChatSessions(listOf(chatSessionDto))
            localChatSessionsRepository.upsertChatVariants(listOf(chatVariantDto))
            localChatSessionsRepository.updateActiveSessionVariant(
                sessionId = response.chatSession.id,
                variantId = response.chatVariant.id
            )

            logger.d("ImportCharacterCardUseCase", "Импорт успешно завершен, characterId=${response.character.id}")
            response.character.id
        }
    }
}
