package tgBot

import app.data.repository.UserRepositoryApp
import app.service.AdminSubscriptionService
import app.service.paymentService.PaymentService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import tgBot.repository.TelegramBotRepository

object BotConfig {

    fun createBot(
        botToken: String,
        adminId: Long,
        repository: TelegramBotRepository,
        userRepository: UserRepositoryApp,
        adminSubscriptionService: AdminSubscriptionService,
        paymentService: PaymentService

    ): Bot {
        val bot = bot {
            timeout = 30
            token = botToken.ifBlank { "0000000000:AAPlaceholderTokenForEmptyConfig00000" }
            logLevel = LogLevel.None

            if (botToken.isNotBlank() && adminId > 0L) {
                dispatch {
                    AdminCommandHandler.register(
                        adminId = adminId,
                        dispatcher = this,
                        repository = repository,
                        userRepository = userRepository,
                        adminSubscriptionService = adminSubscriptionService,
                        paymentService = paymentService
                    )
                }
            }
        }

        if (adminId > 0L && botToken.isNotBlank() && botToken != "null") {
            try {
                bot.sendMessage(
                    chatId = ChatId.fromId(adminId),
                    text = "🚀 Сервер Avalon Chat успешно запущен и готов к работе!"
                )
            } catch (_: Exception) {}
        }

        return bot
    }
}