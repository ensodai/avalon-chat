package app.plugins

import app.utils.EnvProvider
import app.utils.logger
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.BotCommand
import com.github.kotlintelegrambot.entities.ChatId
import io.ktor.server.application.*
import io.ktor.util.*
import org.koin.ktor.ext.inject

val TelegramBotKey = AttributeKey<Bot>("TelegramBot")
val TelegramAdminIdKey = AttributeKey<Long>("TelegramAdminId")

fun Application.telegramBot() {

    val token = EnvProvider.getTelegramBotToken().trim()
    val adminId = EnvProvider.getTelegramAdminId()
    if (token.isBlank() || token == "null" || adminId <= 0L) {
        logger.info("Telegram бот отключен: TELEGRAM_BOT_TOKEN или TELEGRAM_ADMIN_ID не настроены.")
        return
    }

    val bot: Bot by inject()

    logger.info("Конфигурация Telegram прочитана успешно. ID админа: $adminId")

    val commands = listOf(
        BotCommand("start", "Главное меню и дашборд"),
        BotCommand("menu", "Панель управления сервером"),
        BotCommand("create_user", "Создать аккаунт пользователя"),
        BotCommand("users", "Список последних пользователей")
    )

    bot.setMyCommands(commands).fold(
        ifSuccess = {
            logger.info("SUCCESS: Команды Telegram-бота успешно зарегистрированы.")
        },
        ifError = { error ->
            logger.warn("Не удалось обновить команды бота: $error")
        }
    )

    // 3. Запускаем бота
    bot.startPolling()
    attributes.put(TelegramBotKey, bot)
    attributes.put(TelegramAdminIdKey, adminId)
}