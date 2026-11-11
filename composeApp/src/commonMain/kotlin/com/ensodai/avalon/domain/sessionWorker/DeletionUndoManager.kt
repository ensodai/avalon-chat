package com.ensodai.avalon.domain.sessionWorker


import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.chatEvents.LocalChatAction
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.utils.currentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

data class PendingDeletionItem(
    val id: Uuid = Uuid.random(), // Уникальный ID самой операции
    val payloadIds: List<Uuid>,       // Список ID удаленных сущностей
    val type: DeletionType,
    val displayText: String,                 // Текст для UI
    val originalSyncActions: Map<Uuid, SyncAction>, // Карта ID -> его статус до удаления
    val characterId: Uuid? = null,    // Для сообщений: ID персонажа, чтобы найти сессию
    val deletionTimestamp: Long = currentTimeMillis(),
    val duration: Long
)

enum class DeletionType { CHARACTER, CHAT_SESSION, MESSAGE }

@Single
class DeletionUndoManager(
    private val localChatRepository: LocalChatSessionsRepository,
    private val localCharacterRepository: LocalCharacterRepository,
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val syncManager: PendingChangesSyncManager,
    @ApplicationScope private val scope: CoroutineScope
) {

    companion object {
        private const val DEFAULT_UNDO_DURATION = 3000L
    }

    private val _pendingDeletions = MutableStateFlow<List<PendingDeletionItem>>(emptyList())
    val pendingDeletions = _pendingDeletions.asStateFlow()

    private val deletionJobs = mutableMapOf<Uuid, Job>()

    /**
     * Точка входа для удаления.
     */
    fun startDeletion(
        ids: List<Uuid>,
        type: DeletionType,
        displayText: String,
        characterId: Uuid? = null // Передаем при удалении сообщений
    ) {
        scope.launch {
            val originalActions = getOriginalSyncActions(type, ids)

            softDeleteInDb(type, ids)

            val item = PendingDeletionItem(
                payloadIds = ids,
                type = type,
                displayText = displayText,
                originalSyncActions = originalActions,
                characterId = characterId,
                duration = DEFAULT_UNDO_DURATION
            )

            _pendingDeletions.value = _pendingDeletions.value + item

            val job = launch {
                delay(item.duration.milliseconds)
                commitDeletion(item)
            }

            deletionJobs[item.id] = job
        }
    }

    fun undoDeletion(item: PendingDeletionItem) {
        val job = deletionJobs[item.id]
        if (job?.isActive == true) {
            job.cancel()
            deletionJobs.remove(item.id)

            scope.launch {
                restoreInDb(item.type, item.originalSyncActions)
                removePendingItem(item)
            }
        }
    }

    fun forceCommitDeletion(item: PendingDeletionItem) {
        deletionJobs[item.id]?.cancel()
        commitDeletion(item)
    }

    private fun commitDeletion(item: PendingDeletionItem) {
        if (item !in _pendingDeletions.value) return

        removePendingItem(item)
        deletionJobs.remove(item.id)

        scope.launch {
            if (item.type == DeletionType.MESSAGE) {
                // Для сообщений используем WebSocket/RPC Action
                val charId = item.characterId ?: return@launch
                val session = localChatRepository.getSessionByCharacterId(charId)
                val activeVariantId = session?.activeChatVariantId

                if (session != null && activeVariantId != null) {
                    val action = LocalChatAction.DeleteMessages(
                        slotIds = item.payloadIds,
                        chatVariantId = activeVariantId,
                        chatSessionId = session.id,
                    )
                    chatSyncRpcClient.sendAction(action)
                }
            } else {
                // Для персонажей и сессий используем обычный SyncManager
                syncManager.scheduleSync()
            }
        }
    }

    private fun removePendingItem(item: PendingDeletionItem) {
        _pendingDeletions.value = _pendingDeletions.value - item
    }

    fun commitAllImmediately() {
        deletionJobs.values.forEach { it.cancel() }
        deletionJobs.clear()
        _pendingDeletions.value = emptyList()
        syncManager.scheduleSync()
    }

    // --- Вспомогательные методы ---

    private suspend fun getOriginalSyncActions(
        type: DeletionType,
        ids: List<Uuid>
    ): Map<Uuid, SyncAction> {
        return when (type) {
            DeletionType.CHARACTER -> {
                localCharacterRepository.getCharactersByIds(ids)
                    .associate { it.id to it.syncAction }
            }

            DeletionType.CHAT_SESSION -> {
                localChatRepository.getChatSessionsByIds(ids)
                    .associate { it.id to it.syncAction }
            }

            DeletionType.MESSAGE -> {
                localChatHistoryRepository.getSlotsByIds(ids)
                    .associate { it.id to it.syncAction }
            }
        }
    }

    private suspend fun softDeleteInDb(type: DeletionType, ids: List<Uuid>) {
        when (type) {
            DeletionType.CHAT_SESSION -> localChatRepository.updateSyncAction(
                ids,
                SyncAction.DELETE
            )

            DeletionType.CHARACTER -> {
                localChatRepository.updateSyncActionChatSessionByCharacterId(ids, SyncAction.DELETE)
                localCharacterRepository.updateSyncAction(ids, SyncAction.DELETE)
            }

            DeletionType.MESSAGE -> {
                localChatHistoryRepository.updateSlotsSyncAction(ids, SyncAction.DELETE)
            }
        }
    }

    private suspend fun restoreInDb(type: DeletionType, originalActions: Map<Uuid, SyncAction>) {
        val groupedActions = originalActions.entries
            .groupBy { it.value }
            .mapValues { it.value.map { entry -> entry.key } }

        groupedActions.forEach { (action, ids) ->
            val targetAction = if (action == SyncAction.DELETE) SyncAction.UPDATE else action

            when (type) {
                DeletionType.CHAT_SESSION -> localChatRepository.updateSyncAction(ids, targetAction)
                DeletionType.CHARACTER -> {
                    localCharacterRepository.updateSyncAction(ids, targetAction)
                    localChatRepository.updateSyncActionChatSessionByCharacterId(ids, targetAction)
                }

                DeletionType.MESSAGE -> {
                    localChatHistoryRepository.updateSlotsSyncAction(ids, targetAction)
                }
            }
        }
    }
}
