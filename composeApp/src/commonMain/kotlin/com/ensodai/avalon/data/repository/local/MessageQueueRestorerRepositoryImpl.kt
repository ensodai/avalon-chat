package com.ensodai.avalon.data.repository.local

import com.ensodai.avalon.data.repository.local.base.BaseRepository
import com.ensodai.avalon.data.repository.local.base.DatabaseExecutor

import com.ensodai.avalon.data.db.dao.MessageQueueRestorerDao
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.avalon.domain.chatEvents.LocalChatAction
import com.ensodai.avalon.domain.repository.local.MessageQueueRestorerRepository

class MessageQueueRestorerRepositoryImpl(
    dbExecutor: DatabaseExecutor,
    private val messageQueueRestorerDao: MessageQueueRestorerDao
) : BaseRepository(dbExecutor), MessageQueueRestorerRepository {

    override suspend fun getAllPendingEvents(): List<LocalChatAction> = dbRead {
        val events = mutableListOf<LocalChatAction>()

        // 1. Создание
        val creates = messageQueueRestorerDao.getPendingCreates()
        creates.forEach { tuple ->
            events.add(
                LocalChatAction.SendChatMessage(
                    slotId = tuple.slotId,
                    contentId = tuple.contentId,
                    containerId = tuple.containerId,
                    chatVariantId = tuple.variantId,
                    chatSessionId = tuple.sessionId,
                    contentType = AvalonMessageContentType.TEXT,
                    text = tuple.text,
                    characterId = tuple.characterId,
                    llmModelCode = null
                )
            )
        }

        // 2. Редактирование
        val updates = messageQueueRestorerDao.getPendingUpdates()
        updates.forEach { tuple ->
            events.add(
                LocalChatAction.EditChatMessage(
                    slotId = tuple.slotId,
                    containerId = tuple.containerId,
                    chatVariantId = tuple.variantId,
                    chatSessionId = tuple.sessionId,
                    text = tuple.text
                )
            )
        }

        // 3. Смена активного контейнера
        val swipes = messageQueueRestorerDao.getPendingSwipes()
        swipes.forEach { tuple ->
            events.add(
                LocalChatAction.ChangeActiveContainer(
                    chatVariantId = tuple.variantId,
                    chatSessionId = tuple.sessionId,
                    slotId = tuple.slotId,
                    newActiveContainerId = tuple.newContainerId
                )
            )
        }

        // 4. Удаление
        val deletes = messageQueueRestorerDao.getPendingDeletes()

        val deletesByVariant = deletes.groupBy { it.variantId }

        deletesByVariant.forEach { (variantId, items) ->
            if (items.isNotEmpty()) {

                val sessionId = items.first().sessionId

                events.add(
                    LocalChatAction.DeleteMessages(
                        slotIds = items.map { it.slotId },
                        chatVariantId = variantId,
                        chatSessionId = sessionId
                    )
                )
            }
        }

        events
    }

    override suspend fun clearLocalOnlyDeletes(): Int {
        return dbWrite { messageQueueRestorerDao.clearLocalOnlyDeletes() }
    }

}
