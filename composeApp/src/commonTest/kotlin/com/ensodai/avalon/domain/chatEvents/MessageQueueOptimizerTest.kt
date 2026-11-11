package com.ensodai.avalon.domain.chatEvents

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class MessageQueueOptimizerTest {

    private val optimizer = MessageQueueOptimizer()

    @Test
    fun testPairwiseEditReplacesSendChatMessage() {
        val queue = ArrayDeque<LocalChatAction>()
        val containerId = Uuid.random()
        val slotId = Uuid.random()
        val sessionId = Uuid.random()
        val variantId = Uuid.random()

        val sendAction = LocalChatAction.SendChatMessage(
            slotId = slotId,
            contentId = Uuid.random(),
            containerId = containerId,
            chatVariantId = variantId,
            chatSessionId = sessionId,
            contentType = AvalonMessageContentType.TEXT,
            text = "Original message text",
            characterId = Uuid.random(),
            llmModelCode = "gpt-4",
            messageSystemInstruction = null
        )

        val editAction = LocalChatAction.EditChatMessage(
            slotId = slotId,
            containerId = containerId,
            chatVariantId = variantId,
            chatSessionId = sessionId,
            text = "Edited message text",
            messageSystemInstruction = null
        )

        // 1. Добавляем отправку
        optimizer.optimizeAndAdd(queue, sendAction)
        assertEquals(1, queue.size)

        // 2. Добавляем редактирование еще не отправленного сообщения
        optimizer.optimizeAndAdd(queue, editAction)
        assertEquals(1, queue.size) // Размер очереди не должен увеличиться

        val optimizedItem = queue.first()
        assertTrue(optimizedItem is LocalChatAction.SendChatMessage)
        assertEquals("Edited message text", optimizedItem.text)
    }

    @Test
    fun testStopGenerationAnnihilatesPendingForceGeneration() {
        val queue = ArrayDeque<LocalChatAction>()
        val sessionId = Uuid.random()
        val variantId = Uuid.random()
        val charId = Uuid.random()

        val forceGen = LocalChatAction.ForceStartGenerateResponse(
            chatSessionId = sessionId,
            chatVariantId = variantId,
            characterId = charId,
            llmModelCode = "gpt-4"
        )

        val stopGen = LocalChatAction.StopGenerateResponse(
            chatSessionId = sessionId,
            chatVariantId = variantId
        )

        // 1. Добавляем старт генерации
        optimizer.optimizeAndAdd(queue, forceGen)
        assertEquals(1, queue.size)

        // 2. Добавляем остановку генерации для той же сессии
        optimizer.optimizeAndAdd(queue, stopGen)

        // Обе команды должны взаимно аннигилировать (ничего не отправляем на сервер)
        assertEquals(0, queue.size)
    }

    @Test
    fun testBatchDeleteWipesOutPendingMessagesForSlot() {
        val queue = ArrayDeque<LocalChatAction>()
        val slot1 = Uuid.random()
        val slot2 = Uuid.random()
        val sessionId = Uuid.random()
        val variantId = Uuid.random()

        val msg1 = LocalChatAction.SendChatMessage(
            slotId = slot1,
            contentId = Uuid.random(),
            containerId = Uuid.random(),
            chatVariantId = variantId,
            chatSessionId = sessionId,
            contentType = AvalonMessageContentType.TEXT,
            text = "Message in slot 1",
            characterId = Uuid.random(),
            llmModelCode = "gpt-4",
            messageSystemInstruction = null
        )

        val msg2 = LocalChatAction.SendChatMessage(
            slotId = slot2,
            contentId = Uuid.random(),
            containerId = Uuid.random(),
            chatVariantId = variantId,
            chatSessionId = sessionId,
            contentType = AvalonMessageContentType.TEXT,
            text = "Message in slot 2",
            characterId = Uuid.random(),
            llmModelCode = "gpt-4",
            messageSystemInstruction = null
        )

        optimizer.optimizeAndAdd(queue, msg1)
        optimizer.optimizeAndAdd(queue, msg2)
        assertEquals(2, queue.size)

        // Удаляем slot1 (он локальный, на сервере его нет, поэтому DeleteMessages для него тоже не должен идти)
        val deleteAction = LocalChatAction.DeleteMessages(
            slotIds = listOf(slot1),
            chatVariantId = variantId,
            chatSessionId = sessionId
        )

        optimizer.optimizeAndAdd(queue, deleteAction)

        // msg1 удален, msg2 остался, deleteAction аннигилирован т.к. slot1 был чисто локальным
        assertEquals(1, queue.size)
        assertEquals(slot2, (queue.first() as LocalChatAction.SendChatMessage).slotId)
    }
}
