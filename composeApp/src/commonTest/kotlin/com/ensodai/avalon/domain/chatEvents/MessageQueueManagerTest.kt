package com.ensodai.avalon.domain.chatEvents

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class MessageQueueManagerTest {

    @Test
    fun testOfflineQueueBufferingAndInstantFlushOnNetworkAvailable() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val dispatchedActions = mutableListOf<LocalChatAction>()
        val queueManager = MessageQueueManager(
            messageQueueOptimizer = MessageQueueOptimizer(),
            scope = testScope,
            sendAction = { action ->
                dispatchedActions.add(action)
            }
        )

        val sessionId = Uuid.random()
        val variantId = Uuid.random()

        // 1. Очередь оффлайн
        queueManager.setNetworkAvailable(false)
        testScope.advanceUntilIdle()

        // 2. Пользователь отправляет 3 сообщения в оффлайне
        val action1 = createSendAction("Msg 1", sessionId, variantId)
        val action2 = createSendAction("Msg 2", sessionId, variantId)
        val action3 = createSendAction("Msg 3", sessionId, variantId)

        queueManager.enqueueAction(action1)
        queueManager.enqueueAction(action2)
        queueManager.enqueueAction(action3)

        testScope.advanceUntilIdle()
        // В оффлайне ничего не отправлено
        assertEquals(0, dispatchedActions.size)

        // 3. Сеть появляется (сокет подключен)
        queueManager.setNetworkAvailable(true)
        testScope.advanceUntilIdle()

        // 4. Все 3 сообщения моментально отправлены в правильном порядке
        assertEquals(3, dispatchedActions.size)
        assertEquals("Msg 1", (dispatchedActions[0] as LocalChatAction.SendChatMessage).text)
        assertEquals("Msg 2", (dispatchedActions[1] as LocalChatAction.SendChatMessage).text)
        assertEquals("Msg 3", (dispatchedActions[2] as LocalChatAction.SendChatMessage).text)
    }

    @Test
    fun testPurgeMessagesForDeletedContexts() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val dispatchedActions = mutableListOf<LocalChatAction>()
        val queueManager = MessageQueueManager(
            messageQueueOptimizer = MessageQueueOptimizer(),
            scope = testScope,
            sendAction = { action ->
                dispatchedActions.add(action)
            }
        )

        val sessionToDelete = Uuid.random()
        val sessionToKeep = Uuid.random()
        val variantId = Uuid.random()

        queueManager.setNetworkAvailable(false)
        testScope.advanceUntilIdle()

        queueManager.enqueueAction(createSendAction("Keep me", sessionToKeep, variantId))
        queueManager.enqueueAction(createSendAction("Delete me", sessionToDelete, variantId))

        testScope.advanceUntilIdle()

        // Удаляем контекст sessionToDelete
        queueManager.purgeMessagesForDeletedContexts(
            deletedSessionIds = listOf(sessionToDelete),
            deletedVariantIds = emptyList()
        )

        queueManager.setNetworkAvailable(true)
        testScope.advanceUntilIdle()

        // Только "Keep me" должно быть отправлено
        assertEquals(1, dispatchedActions.size)
        assertEquals("Keep me", (dispatchedActions[0] as LocalChatAction.SendChatMessage).text)
    }

    private fun createSendAction(text: String, sessionId: Uuid, variantId: Uuid): LocalChatAction.SendChatMessage {
        return LocalChatAction.SendChatMessage(
            slotId = Uuid.random(),
            contentId = Uuid.random(),
            containerId = Uuid.random(),
            chatVariantId = variantId,
            chatSessionId = sessionId,
            contentType = AvalonMessageContentType.TEXT,
            text = text,
            characterId = Uuid.random(),
            llmModelCode = "gpt-4",
            messageSystemInstruction = null
        )
    }
}
