package app.utils.logger

import io.github.oshai.kotlinlogging.KotlinLogging

object DebugLogger {
    // Тег "🚧" (Construction) — сразу видно, что это временный код
    private val base = AppLogger(KotlinLogging.logger("DEBUG"), "DEBUG", "🚧")

    // Используем MAGENTA для всех отладочных сообщений, чтобы они выделялись
    // среди синих (Info) и зеленых (Success) логов.
    private const val COLOR = AppLogger.Ansi.MAGENTA

    /**
     * Просто вывести сообщение (аналог println, но красиво)
     * Эмодзи: 👀 (смотрю)
     */
    fun log(msg: String) =
        base.event("LOOK", "👀", COLOR, msg)

    /**
     * Для проверки значений переменных.
     * Пример: DebugLogger.watch("userId", 12345)
     * Вывод: [🚧 DEBUG] [📦 VAR] userId = 12345
     */
    fun watch(variableName: String, value: Any?) =
        base.event("VAR", "📦", COLOR, "$variableName = $value")

    /**
     * Для отметки чекпоинтов (дошел код сюда или нет)
     * Эмодзи: 📍 (пимпочка на карте)
     */
    fun trace(msg: String = "Code reached here") =
        base.event("TRACE", "📍", COLOR, msg)

    /**
     * ОЧЕНЬ важное сообщение, которое нужно увидеть 100%
     * Использует WARN уровень логгера, чтобы точно попасть в консоль
     * Эмодзи: 🔥
     */
    fun highlight(msg: String) {
        // Добавляем визуальные разделители
        base.event("ATTN", "🔥", AppLogger.Ansi.YELLOW, ">>> $msg <<<")
    }

    /**
     * Дамп списка или сложного объекта
     */
    fun dump(title: String, data: Any?) {
        val text = data.toString()
        base.event("DUMP", "💩", COLOR, "$title: $text")
    }
}