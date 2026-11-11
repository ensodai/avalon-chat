package com.ensodai.avalon.domain.useCases.commonUseCase

import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.sessionWorker.PendingChangesSyncManager
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ArchiveChatSessionsUseCase(
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val pendingChangesSyncManager: PendingChangesSyncManager
) {

    suspend fun archive(chatSessionsIds: List<Uuid>) {
        localChatSessionsRepository.archiveChatSessions(chatSessionsIds)
        pendingChangesSyncManager.scheduleSync()
    }

    suspend fun unarchive(chatSessionsIds: List<Uuid>) {
        localChatSessionsRepository.unArchiveChatSessions(chatSessionsIds)
        pendingChangesSyncManager.scheduleSync()
    }
}