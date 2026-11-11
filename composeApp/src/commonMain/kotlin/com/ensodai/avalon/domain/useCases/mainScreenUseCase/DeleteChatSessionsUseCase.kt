package com.ensodai.avalon.domain.useCases.mainScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.sessionWorker.PendingChangesSyncManager
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class DeleteChatSessionsUseCase(
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val pendingChangesSyncManager: PendingChangesSyncManager
) {

    suspend fun delete(chatSessionsIds: List<Uuid>) {
        localChatSessionsRepository.softDeleteChatSessions(chatSessionsIds)
        pendingChangesSyncManager.scheduleSync()
    }
}
