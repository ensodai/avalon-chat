package com.ensodai.avalon.domain.useCases.commonUseCase

import com.ensodai.avalon.data.db.dto.CharacterAvatarDto
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.ChatSessionPreviewDto
import com.ensodai.avalon.data.db.dto.ChatVariantDto
import com.ensodai.avalon.data.db.dto.ChatVariantImageDto
import com.ensodai.avalon.data.db.dto.WelcomeCardDto
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.shared.avalon.character.models.CharacterPromptDetailsInput
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.SaveCharacterRequest
import com.ensodai.shared.avalon.character.models.WelcomeCardRequest
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.avalon.domain.models.SaveCharacterProfileDto
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.shared.avalon.character.models.PublishCharacterRequest
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.utils.AppLogger
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class WelcomeCardConfig(
    val welcomeCardImages: List<WelcomeCardImageDto>?,
    val welcomeCardDescription: String?,
    val startColor: String,
    val endColor: String,
    val colorLight: String,
    val isEnableLight: Boolean
)

@Single
class CreateNewCharacterUseCase(
    private val localCharacterRepository: LocalCharacterRepository,
    private val characterRpcService: CharacterRpcService,
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val logger: AppLogger
) {

    @OptIn(ExperimentalUuidApi::class)
    suspend fun create(
        characterName: String,
        fullDescription: String?,
        shortDescription: String,
        temperature: Float,
        visibility: CharacterVisibility,
        isPublicFullDescription: Boolean,
        category: String?,
        firstMessage: String?,
        avatarToken: String?,
        imageTokens: List<String>,
        welcomeCardImageTokens: List<String>,
        welcomeCardConfig: WelcomeCardConfig,
        isOutreachEnabled: Boolean,
        voice: VoiceModel,
        interactionFormat: InteractionFormat,
        scenario: String? = null,
        mesExample: String? = null,
        attachedLorebookIds: List<Uuid> = emptyList()
    ): Result<Unit> {
        return try {
            logger.d("CreateNewCharacter", "Publishing character with pre-uploaded images")

            val request = SaveCharacterRequest(
                characterName = characterName,
                fullDescription = fullDescription,
                shortDescription = shortDescription,
                temperature = temperature,
                visibility = visibility,
                isPublicFullDescription = isPublicFullDescription,
                category = category,
                firstMessage = firstMessage,
                welcomeCard = WelcomeCardRequest(
                    welcomeCardDescription = welcomeCardConfig.welcomeCardDescription,
                    cardColorStart = welcomeCardConfig.startColor,
                    cardColorEnd = welcomeCardConfig.endColor,
                    cardColorLight = welcomeCardConfig.colorLight,
                    isEnableLight = welcomeCardConfig.isEnableLight
                ),
                isOutreachEnabled = isOutreachEnabled,
                voice = voice,
                interactionFormat = interactionFormat,
                promptDetails = if (!scenario.isNullOrBlank() || !mesExample.isNullOrBlank()) {
                    CharacterPromptDetailsInput(
                        scenario = scenario,
                        mesExample = mesExample
                    )
                } else null,
                attachedLorebookIds = attachedLorebookIds
            )

            logger.d("CreateNewCharacter", "Sending RPC request to save character...")

            val result = runCatching {
                characterRpcService.publishCharacter(
                    PublishCharacterRequest(
                        characterData = request,
                        imageTokens = imageTokens.ifEmpty { null },
                        avatarToken = avatarToken,
                        welcomeCardImageTokens = welcomeCardImageTokens.ifEmpty { null }
                    )
                )
            }

            result.fold(
                onSuccess = { response ->
                    // response это RpcResponse.CharacterPublished
                    // Работаем напрямую с response.character, response.chatSession, response.chatVariant
                    logger.d(
                        "CreateNewCharacter",
                        "Character published successfully: ${response.character.id}"
                    )

                    // ====================================================================
                    // ШАГ 3: Сохраняем данные в локальную БД
                    // ====================================================================

                    val characterImages = response.character.images.map { image ->
                        CharacterImageDTO(
                            imagePublicId = image.id,
                            url = image.imageUrl,
                            thumbHash = image.thumbHash,
                            isPrivate = image.isPrivate,
                            energyCost = image.energyCost,
                            requiredLevel = image.requiredLevel,
                            isUnlocked = image.isUnlocked
                        )
                    }
                    val welcomeCardImages =
                        response.character.welcomeCard?.welcomeCardImages?.map { image ->
                            WelcomeCardImageDto(
                                imagePublicId = image.id,
                                url = image.imageUrl,
                                thumbHash = image.thumbHash
                            )
                        }
                    val characterAvatarDto = response.character.avatar?.let { image ->
                        CharacterAvatarDto(
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
                        scenario = response.character.scenario ?: scenario,
                        mesExample = response.character.mesExample ?: mesExample
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

                    logger.d("CreateNewCharacter", "Local DB transaction completed")

                    Result.success(Unit)
                },
                onFailure = { error ->
                    logger.e(
                        "CreateNewCharacter",
                        "Failed to publish character: ${error.stackTraceToString()}"
                    )
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            logger.e(
                "CreateNewCharacter",
                "Unexpected error during character creation: ${e.stackTraceToString()}"
            )
            Result.failure(e)
        }
    }

}
