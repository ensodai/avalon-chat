package com.ensodai.avalon.domain.chatEvents

import com.ensodai.shared.avalon.chat.models.*
import com.ensodai.shared.avalon.chat.wss.*
import com.ensodai.shared.avalon.gifts.models.GiftEffectType
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.avalon.domain.global.GlobalEventBus
import com.ensodai.avalon.domain.global.GlobalEvent
import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.domain.messageSender.TypingStatusManager
import com.ensodai.avalon.domain.chatEvents.useCases.*
import com.ensodai.avalon.domain.sessionWorker.SyncManager
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

/**
 * Отвечает за обработку всех входящих сообщений (событий) от сервера.
 * Это "мозг" реактивной части чата.
 */
@Single
class ChatEventHandler(
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val syncManager: SyncManager,
    private val processNewMessage: ProcessNewMessageUseCase,
    private val processMessageUpdated: ProcessMessageUpdatedUseCase,
    private val processMessagesDeleted: ProcessMessagesDeletedUseCase,
    private val processNewContainer: ProcessNewContainerUseCase,
    private val processActiveContainerChanged: ProcessActiveContainerChangedUseCase,
    private val processMessageAck: ProcessMessageAckUseCase,
    private val processMessageAckError: ProcessMessageAckErrorUseCase,
    private val processMessageReaction: ProcessMessageReactionUseCase,
    private val processCharacterDeleted: ProcessCharacterDeletedUseCase,
    private val processHardDeleteMessageSlots: ProcessHardDeleteMessageSlotsUseCase,
    private val processMessagesRead: ProcessMessagesReadUseCase,
    private val processMessageContentAdded: ProcessMessageContentAddedUseCase,
    private val processRelationshipLevelUp: ProcessRelationshipLevelUpUseCase,
    private val processMessageStreamChunk: ProcessMessageStreamChunkUseCase,
    private val typingStatusManager: TypingStatusManager,
    private val globalEventBus: GlobalEventBus,
    @ApplicationScope private val externalScope: CoroutineScope,
    private val messageStreamManager: MessageStreamManager,
    private val logger: AppLogger
) {

    private val scope = CoroutineScope(SupervisorJob() + IODispatcher)

    init {
        // Подписка на поток сообщений от клиента с неограниченным буфером для последовательной обработки в фоне
        externalScope.launch(IODispatcher) {
            chatSyncRpcClient.incomingMessages
                .buffer(Channel.UNLIMITED)
                .collect { event ->
                    try {
                        handleEvent(event)
                    } catch (e: Exception) {
                        logger.e(
                            "ChatEventHandler",
                            "Error handling event: ${e.stackTraceToString()}"
                        )
                    }
                }
        }

        // Сброс статусов печати при потере соединения
        scope.launch {
            chatSyncRpcClient.connectionState.collectLatest { state ->
                if (state !is RpcConnectionState.Connected) {
                    typingStatusManager.clearAll()
                    messageStreamManager.clearAll()
                }
            }
        }
    }

    private suspend fun handleEvent(event: ServerMessage) {
        when (event) {
            is ServerMessage.Error -> {
                logger.e("ChatEventHandler", "🔥 Server error: ${event.message}")
            }

            is ServerMessage.MessageAck -> {
                logger.d("ChatEventHandler", "✅ Message ACK: ${event.containerId}")
                processMessageAck.confirm(event)
            }

            is ServerMessage.MessageAckError -> {
                logger.e("ChatEventHandler", "❌ Message ACK ERROR: ${event.containerId}")
                processMessageAckError.confirm(
                    containerId = event.containerId,
                    serverTimestamp = event.timestamp,
                    slotId = event.slotId
                )
            }

            is ServerMessage.NewChatMessage -> {
                logger.d("ChatEventHandler", "📩 New message in slot: ${event.slot.id}")

                val isUserMessage = event.slot.containers.firstOrNull()?.role == MessageRole.USER
                if (!isUserMessage) {
                    typingStatusManager.clearTyping(event.slot.chatSessionId)
                }

                val isGiftMessage = event.slot.containers.firstOrNull()?.contents
                    ?.firstOrNull { it.type == AvalonMessageContentType.GIFT }
                if (isGiftMessage != null) {
                    try {
                        val giftPayload = MessageContentCodec.decode(
                            type = AvalonMessageContentType.GIFT,
                            contentString = isGiftMessage.content
                        ) as? StickerGiftPayload
                        if (giftPayload != null && giftPayload.effectType != GiftEffectType.NONE) {
                            scope.launch {
                                globalEventBus.emit(
                                    GlobalEvent.GiftEffectTriggered(
                                        characterId = event.characterId,
                                        effectType = giftPayload.effectType
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        logger.e("ChatEventHandler", "Failed to decode gift payload: ${e.message}")
                    }
                }

                processNewMessage.save(
                    message = event.slot,
                    newQuota = event.currentUserQuota,
                    characterId = event.characterId
                )
            }

            is ServerMessage.MessageUpdated -> {
                logger.d("ChatEventHandler", "✏️ Message updated: ${event.containerId}")
                processMessageUpdated.update(event)
            }

            is ServerMessage.MessagesDeleted -> {
                logger.d("ChatEventHandler", "🗑 Messages deleted: ${event.slotIds}")
                processMessagesDeleted.deleteMessages(event)
            }

            is ServerMessage.NewContainerAdded -> {
                logger.d("ChatEventHandler", "➕ New container added to slot: ${event.container.slotId}")
                processNewContainer.add(
                    container = event.container,
                    sessionId = event.sessionId,
                    newQuota = event.currentUserQuota
                )
            }

            is ServerMessage.ActiveContainerChanged -> {
                logger.d("ChatEventHandler", "🔄 Active container changed in slot: ${event.slotId}")
                processActiveContainerChanged.update(event)
            }

            is ServerMessage.SyncTrigger -> {
                logger.d("ChatEventHandler", "🔔 Received SyncTrigger for entity: ${event.entity}")
                // Логика может быть умнее: если "chats" -> обновляем только чаты.
                // Но пока делаем надежный Catch-Up всего.
                syncManager.performCatchUpSync()
            }

            is ServerMessage.SendUserReaction -> {
                logger.d("ChatEventHandler", "❤️ User reaction: ${event.reaction}")
                processMessageReaction.setUserReaction(
                    containerId = event.messageContainerId,
                    reaction = event.reaction
                )
            }

            is ServerMessage.SendCharacterReaction -> {
                logger.d("ChatEventHandler", "❤️ Character reaction: ${event.reaction}")
                processMessageReaction.setCharacterReaction(
                    containerId = event.messageContainerId,
                    reaction = event.reaction
                )
            }

            is ServerMessage.DeleteCharacter -> {
                logger.d("ChatEventHandler", "🗑 Character deleted by server: ${event.characterID}")
                processCharacterDeleted.delete(event.characterID)
            }

            is ServerMessage.MessagesDeleteError -> {
                logger.e("ChatEventHandler", "❌ Messages delete error: ${event.error}")
                processHardDeleteMessageSlots.delete(event.slotIds)
            }

            is ServerMessage.MessageEditError -> {
                logger.e("ChatEventHandler", "❌ Message edit error: ${event.error}")
                processMessageAckError.confirm(
                    containerId = event.containerId,
                    serverTimestamp = event.timestamp,
                    slotId = event.slotId
                )
            }

            is ServerMessage.MessagesRead -> {
                logger.d("ChatEventHandler", "👀 Messages read: ${event.messageContainerIds}")
                processMessagesRead.markAsRead(event.messageContainerIds)
            }

            is ServerMessage.MessageContentAdded -> {
                logger.d("ChatEventHandler", "➕ Message content added: container=${event.containerId}, type=${event.contentType}")
                processMessageContentAdded.add(event)
            }

            is ServerMessage.RelationshipLevelUp -> {
                logger.d("ChatEventHandler", "🏆 Relationship Level Up: character=${event.characterId}, level=${event.newLevel}")
                processRelationshipLevelUp.updateRelationship(
                    characterId = event.characterId,
                    oldLevel = event.oldLevel,
                    level = event.newLevel,
                    experience = event.currentExperience
                )
            }

            is ServerMessage.MessageStreamChunk -> {
                logger.d("ChatEventHandler", "🌊 Message stream chunk: ${event.containerId}")
                processMessageStreamChunk.process(event)
            }
        }
    }
}
