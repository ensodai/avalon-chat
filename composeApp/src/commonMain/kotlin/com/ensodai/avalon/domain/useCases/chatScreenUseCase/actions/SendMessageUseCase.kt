package com.ensodai.avalon.domain.useCases.chatScreenUseCase.actions

import com.ensodai.avalon.data.db.dto.chat.CreateContainerDto
import com.ensodai.avalon.data.db.dto.chat.CreateContentDto
import com.ensodai.avalon.data.db.dto.chat.CreateSlotDto
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.shared.avalon.chat.models.*
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.chatEvents.LocalChatAction
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.chat.models.StickerGiftPayload
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.getUTCTimeNow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import kotlin.concurrent.Volatile
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class SendMessageUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val logger: AppLogger,
) {
    private val sendMutex = Mutex()

    @Volatile
    private var lastUsedTime: Instant? = null

    /**
     * @param characterId - ID персонажа, которому пишем (точка входа)
     * @param text - текст сообщения
     * @param llmModelCode - код модели
     * @param systemInstruction - системная инструкция к сообщению
     */
    suspend fun send(
        characterId: Uuid,
        text: String,
        llmModelCode: String,
        systemInstruction: String?,
        contentType: AvalonMessageContentType,
        gift: Gift? = null
    ) {
        val finalActionText = gift?.id?.toString() ?: text
        sendMutex.withLock {
            logger.d(
                "SendMessageUseCase",
                "Отправка сообщения $text characterId: $characterId instruction: $systemInstruction"
            )

            val session = localChatSessionsRepository.getSessionByCharacterId(characterId)

            val sessionId: Uuid
            val variantId: Uuid
            val deviceTime = getUTCTimeNow()

            if (session == null) {
                // СЦЕНАРИЙ 1: Полностью новый чат
                sessionId = Uuid.random()
                variantId = Uuid.random()

                logger.d("SendMessageUseCase", "✨ Creating NEW Session and Variant locally (Lazy)")

                localChatSessionsRepository.createChatSessionAndChatVariant(
                    chatSessionId = sessionId,
                    chatVariantId = variantId,
                    characterId = characterId,
                    createdAt = deviceTime
                )

            } else if (session.activeChatVariantId == null) {
                // СЦЕНАРИЙ 2: Сессия есть, но активный вариант потерян (Edge Case)
                sessionId = session.id
                variantId = Uuid.random()

                logger.d("SendMessageUseCase", "🛠 Session exists, but creating NEW Variant locally")

                localChatSessionsRepository.createLocalChatVariantAndSetAsActive(
                    sessionId = sessionId,
                    newVariantId = variantId,
                    createdAt = deviceTime
                )

            } else {
                // СЦЕНАРИЙ 3: Всё есть, штатный режим
                sessionId = session.id
                variantId = session.activeChatVariantId
            }

            if (lastUsedTime == null) {
                lastUsedTime = localChatHistoryRepository.getLastMessageTime(variantId)
            }

            val now = getUTCTimeNow()

            val safeCreatedAt = if (lastUsedTime != null && now <= lastUsedTime!!) {
                lastUsedTime!!.plus(1.milliseconds)
            } else {
                now
            }

            lastUsedTime = safeCreatedAt

            logger.d("TimeFix", "Msg Time: $safeCreatedAt")

            val slotId = Uuid.random()
            val containerId = Uuid.random()
            val contentId = Uuid.random()
            val messageTextContent = if (gift != null) {
                val giftPayload = StickerGiftPayload(
                    giftId = gift.id,
                    packId = gift.packId,
                    giftName = gift.giftName,
                    url = gift.fileUrl,
                    actionType = gift.actionType
                )
                MessageContentCodec.encode(
                    type = AvalonMessageContentType.GIFT,
                    payload = giftPayload
                )
            } else {
                text
            }

            val contents = mutableListOf(
                CreateContentDto(
                    id = contentId,
                    type = contentType,
                    content = messageTextContent,
                    orderIndex = 0
                )
            )

            if (!systemInstruction.isNullOrBlank()) {
                contents.add(
                    CreateContentDto(
                        id = Uuid.random(),
                        type = AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION,
                        content = systemInstruction,
                        orderIndex = 1
                    )
                )
            }

            val containerDto = CreateContainerDto(
                id = containerId,
                role = MessageRole.USER,
                rarity = Rarity.COMMON,
                status = MessageStatus.PENDING,
                syncAction = SyncAction.CREATE,
                createdAt = safeCreatedAt,
                updatedAt = safeCreatedAt,
                contents = contents,
                userReaction = null,
                characterReaction = null
            )

            val slotDto = CreateSlotDto(
                sessionId = sessionId,
                variantId = variantId,
                slotId = slotId,
                activeContainerId = containerId,
                syncAction = SyncAction.CREATE,
                createdAt = safeCreatedAt,
                updatedAt = safeCreatedAt,
                containers = listOf(containerDto),
                characterId = characterId
            )

            localChatHistoryRepository.saveNewMessage(slotDto)

            // Используем LocalChatAction вместо сетевого ClientMessage
            val action = LocalChatAction.SendChatMessage(
                containerId = containerId,
                slotId = slotId,
                contentId = contentId,
                chatVariantId = variantId,
                chatSessionId = sessionId,
                contentType = contentType,
                text = finalActionText,
                messageSystemInstruction = systemInstruction,
                characterId = characterId,
                llmModelCode = llmModelCode
            )

            // Отправляем локальный экшен в очередь
            chatSyncRpcClient.sendAction(action)
        }
    }
}
