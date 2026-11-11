package com.ensodai.avalon.domain.chatEvents

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class DozeModeAndStressIntegrationTest {

    @Test
    fun testStressFlappingAndConcurrentQueueing() = runTest {
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

        // 1. Запускаем 50 итераций резкого переключения сети (Flapping)
        repeat(50) { i ->
            val isOnline = (i % 2 == 0)
            queueManager.setNetworkAvailable(isOnline)

            // Параллельно добавляем сообщения
            queueManager.enqueueAction(
                LocalChatAction.SendChatMessage(
                    slotId = Uuid.random(),
                    contentId = Uuid.random(),
                    containerId = Uuid.random(),
                    chatVariantId = variantId,
                    chatSessionId = sessionId,
                    contentType = AvalonMessageContentType.TEXT,
                    text = "Stress msg #$i",
                    characterId = Uuid.random(),
                    llmModelCode = "gpt-4",
                    messageSystemInstruction = null
                )
            )
        }

        testScope.advanceUntilIdle()

        // 2. Включаем стабильную сеть
        queueManager.setNetworkAvailable(true)
        testScope.advanceUntilIdle()

        // 3. Все 50 сообщений должны быть успешно доставлены без потерь и дедлоков!
        assertEquals(50, dispatchedActions.size)
        repeat(50) { i ->
            assertEquals("Stress msg #$i", (dispatchedActions[i] as LocalChatAction.SendChatMessage).text)
        }
    }

    /**
     * ТЕСТ 2 (Каталог Deep Research #6 / #27): Doze Mode Sleep & High-Volume Queue Optimizer.
     * 100 действий ставятся в очередь при выключенной сети:
     * - Сообщения создаются, редактируются, удаляются.
     * - Optimizer обязан сжать очередь и доставить только валидный финальный набор действий в строгом FIFO-порядке.
     */
    @Test
    fun testDozeModeQueueOptimizationAndFifoDelivery() = runTest {
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

        // Сеть выключена (глубокий сон Doze Mode)
        queueManager.setNetworkAvailable(false)

        val slotIdToEdit = Uuid.random()
        val containerIdToEdit = Uuid.random()
        val slotIdToDelete = Uuid.random()

        // 1. Сообщение, которое потом отредактируют
        queueManager.enqueueAction(
            LocalChatAction.SendChatMessage(
                slotId = slotIdToEdit,
                contentId = Uuid.random(),
                containerId = containerIdToEdit,
                chatVariantId = variantId,
                chatSessionId = sessionId,
                contentType = AvalonMessageContentType.TEXT,
                text = "Initial draft",
                characterId = Uuid.random(),
                llmModelCode = "gpt-4",
                messageSystemInstruction = null
            )
        )

        // 2. Правка этого же сообщения (тот же containerId)
        queueManager.enqueueAction(
            LocalChatAction.EditChatMessage(
                slotId = slotIdToEdit,
                containerId = containerIdToEdit,
                chatVariantId = variantId,
                chatSessionId = sessionId,
                text = "Final edited text"
            )
        )

        // 3. Сообщение, которое затем удалят
        queueManager.enqueueAction(
            LocalChatAction.SendChatMessage(
                slotId = slotIdToDelete,
                contentId = Uuid.random(),
                containerId = Uuid.random(),
                chatVariantId = variantId,
                chatSessionId = sessionId,
                contentType = AvalonMessageContentType.TEXT,
                text = "To be deleted",
                characterId = Uuid.random(),
                llmModelCode = "gpt-4",
                messageSystemInstruction = null
            )
        )

        // 4. Удаление сообщения #3
        queueManager.enqueueAction(
            LocalChatAction.DeleteMessages(
                slotIds = listOf(slotIdToDelete),
                chatVariantId = variantId,
                chatSessionId = sessionId
            )
        )

        // 5. Ещё 20 обычных сообщений
        repeat(20) { i ->
            queueManager.enqueueAction(
                LocalChatAction.SendChatMessage(
                    slotId = Uuid.random(),
                    contentId = Uuid.random(),
                    containerId = Uuid.random(),
                    chatVariantId = variantId,
                    chatSessionId = sessionId,
                    contentType = AvalonMessageContentType.TEXT,
                    text = "Doze msg #$i",
                    characterId = Uuid.random(),
                    llmModelCode = "gpt-4",
                    messageSystemInstruction = null
                )
            )
        }

        testScope.advanceUntilIdle()
        // Пока сеть выключена — ничего не отправлено
        assertEquals(0, dispatchedActions.size)

        // Телефон выходит из Doze Mode, включается сеть:
        queueManager.setNetworkAvailable(true)
        testScope.advanceUntilIdle()

        // Проверяем работу оптимизатора:
        // 1-е сообщение объединено с правкой -> отправлен текст "Final edited text"
        // Сообщение slotIdToDelete удалено до отправки -> не попало в сеть
        // + 20 обычных сообщений = итого 21 отправленное действие
        assertEquals(21, dispatchedActions.size)
        val firstAction = dispatchedActions.first() as LocalChatAction.SendChatMessage
        assertEquals(slotIdToEdit, firstAction.slotId)
        assertEquals("Final edited text", firstAction.text)
    }

    /**
     * ТЕСТ 3 (Каталог Deep Research #16): Wi-Fi -> Cellular Handshake Switch Race.
     * Быстрое переключение сети с параллельным вбросом 30 сообщений из разных корутин.
     */
    @Test
    fun testWifiToCellularSwitchRaceCondition() = runTest {
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

        // 30 корутин спамят сообщениями
        (0 until 30).forEach { i ->
            testScope.launch {
                // Имитация переключения интерфейсов сети прямо во время отправки
                if (i % 5 == 0) queueManager.setNetworkAvailable(false)
                if (i % 7 == 0) queueManager.setNetworkAvailable(true)

                queueManager.enqueueAction(
                    LocalChatAction.SendChatMessage(
                        slotId = Uuid.random(),
                        contentId = Uuid.random(),
                        containerId = Uuid.random(),
                        chatVariantId = variantId,
                        chatSessionId = sessionId,
                        contentType = AvalonMessageContentType.TEXT,
                        text = "Concurrent switch msg #$i",
                        characterId = Uuid.random(),
                        llmModelCode = "gpt-4",
                        messageSystemInstruction = null
                    )
                )
            }
        }

        testScope.advanceUntilIdle()

        // Финально подтверждаем стабильную сотовую связь
        queueManager.setNetworkAvailable(true)
        testScope.advanceUntilIdle()

        // Все 30 сообщений без дубликатов и без потерь
        assertEquals(30, dispatchedActions.size, "Ровно 30 сообщений должны быть отправлены при переключении сотовой связи!")
    }
}
