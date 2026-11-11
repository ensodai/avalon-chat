package com.ensodai.avalon.domain.repository.local

import com.ensodai.avalon.domain.chatEvents.LocalChatAction

interface MessageQueueRestorerRepository {

    suspend fun getAllPendingEvents(): List<LocalChatAction>

    suspend fun clearLocalOnlyDeletes(): Int

}
