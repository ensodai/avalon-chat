package app.utils.logger

import io.github.oshai.kotlinlogging.KotlinLogging

object LlmLogger {
    // Используем "мозг" как тег, так как это нейронка
    private val base = AppLogger(KotlinLogging.logger("LLM"), "LLM", "🧠")

    /**
     * Лог входящего промпта (вопроса)
     * Не логирует сам текст сообщения в целях приватности.
     */
    fun prompt(id: String, text: String) =
        base.event("ASK", "🗣️", AppLogger.Ansi.BLUE, "($id) → Prompt length: ${text.length} chars")

    /**
     * Лог начала генерации (отправка в модель)
     * Цвет: Желтый (процесс/ожидание)
     * Эмодзи: 💭 (мыслительный процесс)
     */
    fun processing(id: String, modelName: String) =
        base.event("THINK", "💭", AppLogger.Ansi.YELLOW, "($id) Sending to $modelName...")

    /**
     * Лог успешного ответа от нейронки
     * Цвет: Зеленый (успех)
     * Эмодзи: 🤖 (ответ робота)
     */
    fun response(id: String, text: String) =
        base.event("ANS", "🤖", AppLogger.Ansi.GREEN, "($id) ← $text")

    /**
     * Лог статистики (токены, время) - опционально, но полезно
     * Цвет: Циан (мета-информация)
     * Эмодзи: 📊 (график/статистика)
     */
    fun stats(id: String, inputTokens: Int, outputTokens: Int, durationMs: Long) =
        base.event(
            "STATS",
            "📊",
            AppLogger.Ansi.CYAN,
            "($id) ${durationMs}ms | In: $inputTokens, Out: $outputTokens"
        )

    /**
     * Лог ошибки генерации
     */
    fun error(id: String, error: String) =
        base.fail("($id) $error")

    /**
     * Промежуточные логи (загрузка контекста, подсчет символов и т.д.)
     * Цвет: CYAN (техническая информация)
     * Эмодзи: ⚙️ (процесс/настройки) или ℹ️
     */
    fun inf(id: String, msg: String) =
        base.event("INFO", "⚙️", AppLogger.Ansi.CYAN, "($id) $msg")

    // Если нужно без ID (общие логи сервиса)
    fun inf(msg: String) =
        base.event("INFO", "⚙️", AppLogger.Ansi.CYAN, msg)
}