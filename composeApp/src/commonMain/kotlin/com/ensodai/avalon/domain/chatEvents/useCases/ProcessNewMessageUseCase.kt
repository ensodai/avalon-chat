package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.avalon.data.db.dto.chat.CreateContainerDto
import com.ensodai.avalon.data.db.dto.chat.CreateContentDto
import com.ensodai.avalon.data.db.dto.chat.CreateSlotDto
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.shared.avalon.character.models.CharacterResponse
import com.ensodai.shared.avalon.chat.models.MessageSlotResponse
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.useCases.commonUseCase.CharacterSaver
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import org.koin.core.annotation.Single

import kotlin.uuid.Uuid

@Single
class ProcessNewMessageUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val localUserRepository: LocalUserRepository,
    private val characterSaver: CharacterSaver,
    private val characterRpcService: CharacterRpcService,
    private val localCharacterRepository: LocalCharacterRepository,
    private val logger: AppLogger
) {

    suspend fun save(
        message: MessageSlotResponse,
        newQuota: Int,
        characterId: Uuid
    ) {
        // Проверка на эхо-сообщение (если сообщение отправлено юзером и уже есть локально)
        val isUserMessage = message.containers.firstOrNull()?.role == com.ensodai.shared.avalon.chat.models.MessageRole.USER
        if (isUserMessage) {
            val exists = localChatHistoryRepository.getSlotsByIds(listOf(message.id)).isNotEmpty()
            if (exists) {
                logger.d("ProcessNewMessageUseCase", "Ignoring echo message: ${message.id}")
                localUserRepository.updateUserQuota(newQuota)
                return
            }
        }

        // ШАГ 1: Проверка и Сеть (ВНЕ ТРАНЗАКЦИИ) 🌐
        // Мы не хотим блокировать БД пока ждем ответ сервера
        val characterExists = localCharacterRepository.characterExists(characterId)

        var characterToSave: CharacterResponse? = null
        var needToSaveStub = false

        if (!characterExists) {
            runCatching {
                characterRpcService.getCharacter(characterPublicId = characterId)
            }.fold(
                onSuccess = { response ->
                    characterToSave = response
                },
                onFailure = {
                    // Если сеть упала — ставим флаг, что нужно создать заглушку,
                    // иначе упадет Foreign Key при вставке сообщения.
                    logger.e(
                        "ProcessNewMessageUseCase",
                        "Failed to load character $characterId. Will use STUB: ${it.stackTraceToString()}"
                    )
                    needToSaveStub = true
                }
            )
        }

        // ШАГ 2: Запись в БД (ВНУТРИ ТРАНЗАКЦИИ) 💾
        localUserRepository.updateUserQuota(newQuota)

        // 2.1. Сохраняем персонажа (или Заглушку), если его не было
        if (characterToSave != null) {
            characterSaver.save(character = characterToSave!!)
        } else if (needToSaveStub) {
            // ВАЖНО: Метод создания заглушки.
            // Он должен вставить CharacterEntity с минимальными полями (id, name="Loading...")
            localCharacterRepository.createStubCharacter(characterId)
        }

        // 2.2. Маппинг
        val containerDtos = message.containers.map { containerResp ->
            val contentDtos = containerResp.contents.map { contentResp ->
                CreateContentDto(
                    id = contentResp.id,
                    type = contentResp.type,
                    content = contentResp.content,
                    orderIndex = contentResp.orderIndex
                )
            }

            CreateContainerDto(
                id = containerResp.containerId,
                role = containerResp.role,
                rarity = containerResp.rarity,
                status = containerResp.status,
                syncAction = SyncAction.NONE,
                userReaction = containerResp.userReaction,
                characterReaction = containerResp.characterReaction,
                createdAt = containerResp.createdAt,
                updatedAt = containerResp.updatedAt,
                contents = contentDtos
            )
        }

        val slotDto = CreateSlotDto(
            sessionId = message.chatSessionId,
            variantId = message.chatVariantId,
            slotId = message.id,
            activeContainerId = message.activeContainerId,
            syncAction = SyncAction.NONE,
            createdAt = message.createdAt,
            updatedAt = message.updatedAt,
            containers = containerDtos,
            characterId = characterId
        )

        localChatHistoryRepository.saveNewMessage(slotDto)
    }
}
