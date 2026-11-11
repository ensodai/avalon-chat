package app.service

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import app.domain.models.DomainMessage
import app.domain.models.DomainMessageContent
import app.service.lorebook.model.LorebookDepthEntry
import app.utils.logger.LlmLogger
import kotlin.random.Random
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

/**
 * Стандартная реализация [app.syncSocket.domain.services.models.HistoryBuilder].
 * Применяет набор правил, специфичных для API Gemini, для подготовки истории чата.
 */
@Single
class HistoryBuilder {

    fun build(
        firstMessage: String?,
        conversationSummary: String?,
        rawHistory: List<DomainMessage>,
        maxHistorySize: Int,
        lorebookAtDepth: List<LorebookDepthEntry> = emptyList()
    ): List<DomainMessage> {
        val workingHistory = rawHistory.toMutableList()

        val hasModelFirstMessage = workingHistory.firstOrNull()?.role == MessageRole.MODEL
        val hasFirstMessage = !hasModelFirstMessage && !firstMessage.isNullOrBlank()
        val hasSummary = !conversationSummary.isNullOrBlank()

        var historyLimit = maxHistorySize
        if (hasFirstMessage) historyLimit--
        if (hasSummary) historyLimit--

        truncateHistory(workingHistory, historyLimit)

        if (hasFirstMessage) {
            addFirstCharacterMessage(workingHistory, firstMessage)
        }

        if (hasSummary) {
            val summaryText = """
                <conversation_summary>
                Here is a summary of the older archived messages from our conversation. You MUST remember these events and context:
                $conversationSummary
                </conversation_summary>
            """.trimIndent()

            if (workingHistory.isNotEmpty() && workingHistory.first().role == MessageRole.USER) {
                val firstMsg = workingHistory.first()
                val updatedContents = mutableListOf<DomainMessageContent>()
                updatedContents.add(
                    DomainMessageContent(
                        type = AvalonMessageContentType.TEXT,
                        content = summaryText
                    )
                )
                updatedContents.addAll(firstMsg.contents)
                workingHistory[0] = firstMsg.copy(contents = updatedContents)
            } else {
                val contents = listOf(
                    DomainMessageContent(
                        type = AvalonMessageContentType.TEXT,
                        content = summaryText
                    )
                )
                val summaryMessage = DomainMessage(
                    role = MessageRole.USER,
                    contents = contents,
                    id = Uuid.random()
                )
                workingHistory.add(0, summaryMessage)
            }
        }

        // Инъекция статей лорбука на заданную глубину истории (AT_DEPTH)
        if (lorebookAtDepth.isNotEmpty() && workingHistory.isNotEmpty()) {
            val byDepth = lorebookAtDepth.groupBy { it.depth }
            for ((depth, entries) in byDepth) {
                val targetIdx = (workingHistory.size - 1 - depth).coerceIn(0, workingHistory.size - 1)
                val targetMsg = workingHistory[targetIdx]
                val depthLoreText = """
                    <context_reminder>
                    ${entries.joinToString("\n\n") { it.content }}
                    </context_reminder>
                """.trimIndent()

                val updatedContents = mutableListOf<DomainMessageContent>()
                updatedContents.add(
                    DomainMessageContent(
                        type = AvalonMessageContentType.TEXT,
                        content = depthLoreText
                    )
                )
                updatedContents.addAll(targetMsg.contents)
                workingHistory[targetIdx] = targetMsg.copy(contents = updatedContents)
            }
        }

        ensureStartsWithUserRole(workingHistory)
        ensureEndsWithUserRole(workingHistory)
        handleEmptyHistory(workingHistory)
//        addRandomMetaTag(workingHistory)
        LlmLogger.inf("Создано ${workingHistory.size} сообщений истории")
        return workingHistory
    }

    /**
     * Добавляет первое сообщение персонажа в начало истории, если оно задано.
     * Это помогает задать "тон" и контекст для LLM с самого начала.
     */
    private fun addFirstCharacterMessage(history: MutableList<DomainMessage>, firstMessage: String?) {
        if (!firstMessage.isNullOrBlank()) {
            val contents = listOf(
                DomainMessageContent(
                    type = AvalonMessageContentType.TEXT,
                    content = "first character message = {$firstMessage}"
                )
            )
            val message = DomainMessage(
                role = MessageRole.MODEL,
                contents = contents,
                id = Uuid.random()
            )
            history.add(0, message)
        }
    }

    /**
     * Обрезает историю до [maxHistorySize], оставляя только самые последние сообщения.
     */
    private fun truncateHistory(history: MutableList<DomainMessage>, maxHistorySize: Int) {
        if (history.size > maxHistorySize) {
            val truncated = history.takeLast(maxHistorySize)
            history.clear()
            history.addAll(truncated)
        }
    }

    /**
     * Гарантирует, что история начинается с сообщения роли USER, как того требует API Gemini.
     * Если первое сообщение от MODEL, вставляет в начало пустое сообщение от USER.
     */
    private fun ensureStartsWithUserRole(history: MutableList<DomainMessage>) {
        if (history.isNotEmpty() && history.first().role == MessageRole.MODEL) {
            val contents = listOf(
                DomainMessageContent(
                    type = AvalonMessageContentType.TEXT,
                    content = ""
                )
            )
            val emptyUserMessage = DomainMessage(
                role = MessageRole.USER,
                contents = contents,
                id = Uuid.random()
            )
            history.add(0, emptyUserMessage)
        }
    }

    /**
     * Гарантирует, что история заканчивается сообщением роли USER.
     * Используется для сценариев "продолжить генерацию", когда последнее сообщение было от модели.
     */
    private fun ensureEndsWithUserRole(history: MutableList<DomainMessage>) {
        if (history.isNotEmpty() && history.last().role == MessageRole.MODEL) {
            val contents = listOf(
                DomainMessageContent(
                    type = AvalonMessageContentType.TEXT,
                    content = "{meta tag: user clicked Continue Generation}"
                )
            )
            val metaMessage = DomainMessage(
                role = MessageRole.USER,
                contents = contents,
                id = Uuid.random()
            )
            history.add(metaMessage)
        }
    }

    /**
     * Обрабатывает случай, когда история полностью пуста (самое первое сообщение в чате).
     * Вставляет мета-сообщение, которое инструктирует модель начать диалог.
     */
    private fun handleEmptyHistory(history: MutableList<DomainMessage>) {
        if (history.isEmpty()) {
            val contents = listOf(
                DomainMessageContent(
                    type = AvalonMessageContentType.TEXT,
                    content = "{meta tag: user clicked Continue generation. user wants you to write the first message in the chat."
                )
            )
            val metaMessage = DomainMessage(
                role = MessageRole.USER,
                contents = contents,
                id = Uuid.random()
            )
            history.add(metaMessage)
        }
    }

    /**
     * Добавляет случайное число в мета-теге к последнему сообщению пользователя.
     * Это "хак" для борьбы с кэшированием ответов LLM и для повышения вариативности генерации.
     */
    private fun addRandomMetaTag(history: MutableList<DomainMessage>) {
        if (history.isNotEmpty()) {
            val lastIndex = history.lastIndex
            val lastMessage = history[lastIndex]

            if (lastMessage.role == MessageRole.USER) {
                val randomNumber = Random.Default.nextInt(1, 101)
                val metaTag = "{meta tag: random number = {$randomNumber}}"
                val newContents = lastMessage.contents.map {
                    if (it.type == AvalonMessageContentType.TEXT) {
                        it.copy(content = it.content + metaTag)
                    } else it
                }

                history[lastIndex] = lastMessage.copy(contents = newContents)
            }
        }
    }

}