package com.ensodai.avalon.domain.sessionWorker


import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.data.db.models.character.CharacterEntity
import com.ensodai.avalon.data.db.models.chat.ChatSessionEntity
import com.ensodai.avalon.data.db.models.chat.ChatVariantEntity
import com.ensodai.shared.avalon.session.models.ChatSessionsDeleteBatchRequest
import com.ensodai.shared.avalon.session.models.ChatSessionsBatchRequest
import com.ensodai.shared.avalon.chat.models.ChatUpdateItem
import com.ensodai.shared.avalon.rpc.services.*
import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.IODispatcher
import com.ensodai.avalon.utils.messageOrDefault
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

import kotlin.coroutines.cancellation.CancellationException

@Single
class PendingChangesSyncManager(
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val localCharacterRepository: LocalCharacterRepository,
    private val characterRpcService: CharacterRpcService,
    private val sessionSupportRpcService: SessionSupportRpcService,
    @ApplicationScope private val scope: CoroutineScope,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val logger: AppLogger
) {

    private val syncTrigger = Channel<Unit>(Channel.CONFLATED)

    init {
        scope.launch(IODispatcher) {
            for (signal in syncTrigger) {
                try {
                    processPendingChanges()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.e(
                        "PendingChangesSyncManager",
                        "🔥 Критическая ошибка в цикле синхронизации: ${e.stackTraceToString()}"
                    )
                }
            }
        }
    }

    fun scheduleSync() {
        syncTrigger.trySend(Unit)
    }

    private suspend fun processPendingChanges() {
        logger.d("PendingChangesSyncManager", "🚀 Начало синхронизации исходящих изменений...")

        // 1. Сначала синхронизируем персонажей (так как удаление персонажа тянет за собой сессию)
        syncCharacters()

        // 2. Затем чат-сессии
        syncChatSessions()

        logger.d("PendingChangesSyncManager", "🏁 Синхронизация завершена")
    }

    private suspend fun syncCharacters() {
        // 1. DELETE
        val charactersToDelete =
            localCharacterRepository.getCharactersBySyncAction(SyncAction.DELETE)
        if (charactersToDelete.isNotEmpty()) {
            pushCharacterDeletes(charactersToDelete)
        }

        // 2. CREATE/UPDATE (в будущем)
    }

    private suspend fun pushCharacterDeletes(characters: List<CharacterEntity>) {
        characters.forEach { character ->
            logger.d("PendingChangesSyncManager", "🗑 Удаляем персонажа: ${character.id}")

            // Перед удалением ищем сессию этого персонажа
            val session = localChatSessionsRepository.getSessionByCharacterId(character.id)
            if (session != null) {
                // Если сессия есть, чистим для неё WebSocket
                purgeMessagesForDeletedContexts(listOf(session), emptyList())
            }

            val result = runCatching {
                characterRpcService.deleteCharacter(character.id)
            }
            result.fold(
                onSuccess = {
                    localCharacterRepository.hardDeleteCharacters(listOf(character.id))
                    logger.d(
                        "PendingChangesSyncManager",
                        "✅ Персонаж ${character.id} удален с сервера и из локальной БД"
                    )
                },
                onFailure = {
                    logger.e(
                        "PendingChangesSyncManager",
                        "❌ Ошибка сети при удалении персонажа ${character.id}: ${it.messageOrDefault()}"
                    )
                }
            )
        }
    }

    private suspend fun syncChatSessions() {
        val chatSessionsToDelete =
            localChatSessionsRepository.getChatSessionsBySyncAction(SyncAction.DELETE)
        val chatVariantToDelete =
            localChatSessionsRepository.getChatVariantsBySyncAction(SyncAction.DELETE)

        if (chatSessionsToDelete.isNotEmpty() || chatVariantToDelete.isNotEmpty()) {
            purgeMessagesForDeletedContexts(chatSessionsToDelete, chatVariantToDelete)
        }

        if (chatSessionsToDelete.isNotEmpty()) {
            pushDeletes(chatSessionsToDelete)
        }

        val chatSessionsToUpdate =
            localChatSessionsRepository.getChatSessionsBySyncAction(SyncAction.UPDATE)
        if (chatSessionsToUpdate.isNotEmpty()) {
            pushUpdates(chatSessionsToUpdate)
        }
    }

    private suspend fun purgeMessagesForDeletedContexts(
        chatSessionsToDelete: List<ChatSessionEntity>,
        chatVariantToDelete: List<ChatVariantEntity>
    ) {
        chatSyncRpcClient.cancelMessagesForDeletedContexts(
            deletedSessionIds = chatSessionsToDelete.map { it.id },
            deletedVariantIds = chatVariantToDelete.map { it.id }
        )
    }

    private suspend fun pushDeletes(chatSessions: List<ChatSessionEntity>) {
        val ids = chatSessions.map { it.id }
        logger.d("PendingChangesSyncManager", "🗑 Удаляем ${ids.size} чатов: $ids")

        val request = ChatSessionsDeleteBatchRequest(ids)
        val result = runCatching {
            sessionSupportRpcService.deleteChatSessionsBatch(request)
        }

        result.fold(
            onSuccess = {
                localChatSessionsRepository.hardDeleteChatSessions(ids)
                logger.d(
                    "PendingChangesSyncManager",
                    "✅ Чаты успешно удалены с сервера и из локальной БД"
                )
            },
            onFailure = {
                logger.e(
                    "PendingChangesSyncManager",
                    "❌ Ошибка сети при удалении: ${it.messageOrDefault()}"
                )
            }
        )
    }

    private suspend fun pushUpdates(chatSessions: List<ChatSessionEntity>) {
        logger.d("PendingChangesSyncManager", "✏️ Обновляем ${chatSessions.size} чатов")

        val updateItems = chatSessions.map { chat ->
            ChatUpdateItem(
                id = chat.id,
                isArchived = chat.isArchived,
            )
        }

        val request = ChatSessionsBatchRequest(updateItems)
        val result = runCatching {
            sessionSupportRpcService.updateChatSessionsBatch(request)
        }

        result.fold(
            onSuccess = { result ->
                // Используем IDs из запроса для отметки как синхронизированные
                localChatSessionsRepository.markChatSessionsAsSynced(ids = updateItems.map { it.id })
                logger.d("PendingChangesSyncManager", "✅ Чаты успешно обновлены")
            },
            onFailure = {
                logger.e(
                    "PendingChangesSyncManager",
                    "❌ Ошибка сервера при обновлении: ${it.messageOrDefault()}"
                )
            }
        )
    }
}
