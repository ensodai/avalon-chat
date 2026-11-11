package app.utils.logger

import io.github.oshai.kotlinlogging.KLogger

class AppLogger(
    private val logger: KLogger,
    private val tag: String,
    private val emojiTag: String = "🔌"
) {

    private fun colorize(text: String, color: String) =
        "$color$text${Ansi.RESET}"

    private fun format(
        msg: String,
        event: String,
        eventColor: String,
        emojiEvent: String
    ): String {
        val tagBlock = colorize("[$emojiTag $tag]", Ansi.CYAN)
        val eventBlock = colorize("[$emojiEvent $event]", eventColor)
        return "$tagBlock $eventBlock $msg"
    }

    fun success(msg: String) {
        logger.info {
            format(
                msg,
                event = "SUCCESS",
                eventColor = Ansi.GREEN,
                emojiEvent = "✅"
            )
        }
    }

    fun fail(msg: String) {
        logger.error {
            format(
                msg,
                event = "FAIL",
                eventColor = Ansi.RED,
                emojiEvent = "❌"
            )
        }
    }

    fun warn(msg: String) {
        logger.warn {
            format(
                msg,
                event = "WARN",
                eventColor = Ansi.YELLOW,
                emojiEvent = "⚠️"
            )
        }
    }

    fun info(msg: String) {
        logger.info {
            format(
                msg,
                event = "INFO",
                eventColor = Ansi.BLUE,
                emojiEvent = "ℹ️"
            )
        }
    }

    fun event(
        event: String,
        emojiEvent: String,
        eventColor: String,
        msg: String
    ) {
        logger.info {
            format(msg, event, eventColor, emojiEvent)
        }
    }

    object Ansi {
        const val RESET = "\u001B[0m"
        const val RED = "\u001B[31m"
        const val GREEN = "\u001B[32m"
        const val YELLOW = "\u001B[33m"
        const val BLUE = "\u001B[34m"
        const val CYAN = "\u001B[36m"
        const val MAGENTA = "\u001B[35m"
    }
}
