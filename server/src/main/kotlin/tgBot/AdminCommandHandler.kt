package tgBot

import app.data.repository.UserRepositoryApp
import app.service.AdminSubscriptionService
import app.service.paymentService.PaymentService
import app.utils.EnvProvider
import app.utils.PasswordHasher
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import tgBot.repository.BotUserSummary
import tgBot.repository.DatabaseStats
import tgBot.repository.TelegramBotRepository
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

object AdminCommandHandler {
    private const val DIVIDER = "━━━━━━━━━━━━━━━━━━━━━"

    private val logger = LoggerFactory.getLogger(AdminCommandHandler::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private sealed interface AdminState {
        data object Idle : AdminState
        data object AwaitingUsername : AdminState
        data object AwaitingSearch : AdminState
    }

    private val adminStates = ConcurrentHashMap<Long, AdminState>()

    fun register(
        adminId: Long,
        dispatcher: Dispatcher,
        repository: TelegramBotRepository,
        userRepository: UserRepositoryApp,
        adminSubscriptionService: AdminSubscriptionService,
        paymentService: PaymentService
    ) {
        fun isAuthorized(chatId: Long): Boolean = chatId == adminId

        fun sendOrEditMessage(
            bot: Bot,
            chatId: Long,
            messageId: Long?,
            text: String,
            replyMarkup: ReplyMarkup? = null,
            parseMode: ParseMode? = ParseMode.HTML
        ) {
            if (messageId != null) {
                val (editResponse, _) = bot.editMessageText(
                    chatId = ChatId.fromId(chatId),
                    messageId = messageId,
                    text = text,
                    parseMode = parseMode,
                    replyMarkup = replyMarkup
                )

                if (editResponse?.isSuccessful == true) {
                    return
                }
                try {
                    bot.deleteMessage(ChatId.fromId(chatId), messageId)
                } catch (_: Exception) {}
            }

            var sendSucceeded = false
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = text,
                parseMode = parseMode,
                replyMarkup = replyMarkup
            ).fold(
                ifSuccess = { sendSucceeded = true },
                ifError = { sendSucceeded = false }
            )

            if (!sendSucceeded && parseMode != null) {
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = text,
                    parseMode = null,
                    replyMarkup = replyMarkup
                )
            }
        }

        fun buildDashboardText(stats: DatabaseStats): String {
            val timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            val regStatus = if (EnvProvider.isPublicRegistrationAllowed) {
                "🟢 <b>ОТКРЫТА</b>"
            } else {
                "🔴 <b>ЗАКРЫТА (Только по инвайтам/админом)</b>"
            }

            val premiumPlans = stats.usersByPremiumStatus.entries.joinToString(" | ") { (k, v) -> "$k: $v" }
                .ifBlank { "нет данных" }

            return buildString {
                appendLine("🛡 <b>Avalon Chat • Панель управления</b>")
                appendLine(DIVIDER)
                appendLine("👥 <b>Пользователи:</b> <code>${stats.totalUsers}</code> (активных: <code>${stats.totalAliveUsers}</code>)")
                appendLine("💎 <b>Тарифы:</b> $premiumPlans")
                appendLine("🤖 <b>Персонажи:</b> <code>${stats.totalCharacters}</code> (публичных: <code>${stats.totalPublicCharacters}</code>)")
                appendLine("💬 <b>Сообщения:</b> <code>${stats.totalGeneratedMessages}</code>")
                appendLine("🔑 <b>API Ключи:</b> <code>${stats.totalApiKeys}</code>")
                appendLine(DIVIDER)
                appendLine("🔒 <b>Регистрация в приложении:</b> $regStatus")
                append("🕒 <i>Обновлено: $timeStr</i>")
            }
        }

        fun buildDashboardKeyboard(): InlineKeyboardMarkup {
            val regButtonText = if (EnvProvider.isPublicRegistrationAllowed) {
                "🔒 Закрыть регистрацию"
            } else {
                "🔓 Открыть регистрацию"
            }

            return InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData("➕ Создать пользователя", "create_user_btn"),
                    InlineKeyboardButton.CallbackData("👥 Пользователи", "list_users")
                ),
                listOf(
                    InlineKeyboardButton.CallbackData("🔍 Поиск юзера", "search_user_btn"),
                    InlineKeyboardButton.CallbackData("🔄 Обновить", "refresh_dashboard")
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(regButtonText, "toggle_registration")
                )
            )
        }

        suspend fun sendUserCard(bot: Bot, chatId: Long, messageId: Long?, user: BotUserSummary) {
            val statusIcon = if (user.isBanned) "🔴 Заблокирован" else "🟢 Активен"
            val shadowIcon = if (user.isShadowBanned) " | 👻 ShadowBan" else ""

            val text = buildString {
                appendLine("👤 <b>Карточка пользователя: ${user.nickName}</b>")
                appendLine(DIVIDER)
                appendLine("🆔 <b>ID:</b> <code>${user.id}</code>")
                appendLine("📛 <b>Имя:</b> <code>${user.name}</code>")
                appendLine("🚦 <b>Статус:</b> $statusIcon$shadowIcon")
                appendLine("💎 <b>Тариф:</b> <b>${user.premiumStatus.name}</b>")
                appendLine("⚡ <b>Баланс:</b> <code>${user.currentBalance}</code>")
                appendLine("💬 <b>Сообщений:</b> <code>${user.totalMessages}</code>")
                appendLine("📅 <b>Создан:</b> <code>${user.createdAt}</code>")
            }

            val banActionText = if (user.isBanned) "🟢 Разбанить" else "🚫 Забанить"
            val keyboard = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(banActionText, "toggle_ban:${user.id}"),
                    InlineKeyboardButton.CallbackData("👻 Shadow Ban", "toggle_shadow_ban:${user.id}")
                ),
                listOf(
                    InlineKeyboardButton.CallbackData("💎 ELITE", "set_plan:${user.id}:ELITE"),
                    InlineKeyboardButton.CallbackData("🌟 PREMIUM", "set_plan:${user.id}:PREMIUM"),
                    InlineKeyboardButton.CallbackData("🆓 FREE", "set_plan:${user.id}:FREE")
                ),
                listOf(
                    InlineKeyboardButton.CallbackData("👥 К списку", "list_users"),
                    InlineKeyboardButton.CallbackData("📊 Главное меню", "menu")
                )
            )

            sendOrEditMessage(bot, chatId, messageId, text, keyboard)
        }

        suspend fun sendUsersList(bot: Bot, chatId: Long, messageId: Long?) {
            val users = repository.getRecentUsers(10)
            if (users.isEmpty()) {
                val emptyText = buildString {
                    appendLine("👥 <b>Список пользователей пуст</b>")
                    appendLine(DIVIDER)
                    append("Нажмите кнопку ниже, чтобы создать первого пользователя:")
                }
                val markup = InlineKeyboardMarkup.create(
                    listOf(InlineKeyboardButton.CallbackData("➕ Создать пользователя", "create_user_btn")),
                    listOf(InlineKeyboardButton.CallbackData("📊 Главное меню", "menu"))
                )
                sendOrEditMessage(bot, chatId, messageId, emptyText, markup)
                return
            }

            val text = buildString {
                appendLine("👥 <b>Последние пользователи (${users.size})</b>")
                appendLine(DIVIDER)
                append("Выберите пользователя для управления:")
            }

            val userButtons = users.map { u ->
                val icon = if (u.isBanned) "🔴" else "🟢"
                listOf(
                    InlineKeyboardButton.CallbackData(
                        "$icon ${u.nickName} (${u.premiumStatus.name})",
                        "user_view:${u.id}"
                    )
                )
            }

            val navButtons = listOf(
                listOf(
                    InlineKeyboardButton.CallbackData("➕ Создать пользователя", "create_user_btn"),
                    InlineKeyboardButton.CallbackData("🔍 Поиск", "search_user_btn")
                ),
                listOf(
                    InlineKeyboardButton.CallbackData("📊 Главное меню", "menu")
                )
            )

            sendOrEditMessage(bot, chatId, messageId, text, InlineKeyboardMarkup.create(userButtons + navButtons))
        }

        suspend fun executeUserCreation(
            bot: Bot,
            chatId: Long,
            messageId: Long?,
            rawInput: String
        ) {
            val parts = rawInput.split("\\s+".toRegex()).filter { it.isNotBlank() }
            if (parts.isEmpty()) return

            val username = parts[0].trim()
            val password = if (parts.size > 1) parts[1].trim() else UUID.randomUUID().toString().take(8)

            if (username.length !in 3..20) {
                sendOrEditMessage(
                    bot = bot,
                    chatId = chatId,
                    messageId = messageId,
                    text = "⚠️ <b>Некорректный логин!</b>\nЛогин должен содержать от 3 до 20 символов.",
                    replyMarkup = InlineKeyboardMarkup.createSingleRowKeyboard(
                        InlineKeyboardButton.CallbackData("❌ Отмена", "cancel")
                    )
                )
                return
            }

            val existing = userRepository.findUserByUsername(username)
            if (existing != null) {
                sendOrEditMessage(
                    bot = bot,
                    chatId = chatId,
                    messageId = messageId,
                    text = "⚠️ Пользователь с логином <code>$username</code> уже существует! Попробуйте другой ник.",
                    replyMarkup = InlineKeyboardMarkup.createSingleRowKeyboard(
                        InlineKeyboardButton.CallbackData("❌ Отмена", "cancel")
                    )
                )
                return
            }

            val hash = PasswordHasher.hash(password)
            val newUser = userRepository.createUserWithPassword(username, hash)
            adminStates[chatId] = AdminState.Idle

            val successText = buildString {
                appendLine("✅ <b>Пользователь успешно создан!</b>")
                appendLine(DIVIDER)
                appendLine("👤 <b>Логин:</b> <code>${newUser.nickName}</code>")
                appendLine("🔑 <b>Пароль:</b> <code>$password</code>")
                appendLine("💎 <b>Тариф:</b> <b>ELITE</b> (безлимитный)")
                appendLine(DIVIDER)
                appendLine("📱 <i>Передайте логин и пароль пользователю для входа в Avalon Chat.</i>")
            }

            val keyboard = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData("👤 Карточка пользователя", "user_view:${newUser.id}"),
                    InlineKeyboardButton.CallbackData("📊 В меню", "menu")
                )
            )
            sendOrEditMessage(bot, chatId, messageId, successText, keyboard)
        }

        // --- COMMANDS ---

        dispatcher.command("start") {
            val chatId = update.message?.chat?.id ?: return@command
            if (!isAuthorized(chatId)) return@command
            adminStates[chatId] = AdminState.Idle

            scope.launch {
                val stats = repository.getDatabaseStatistics()
                sendOrEditMessage(
                    bot = bot,
                    chatId = chatId,
                    messageId = null,
                    text = buildDashboardText(stats),
                    replyMarkup = buildDashboardKeyboard()
                )
            }
        }

        dispatcher.command("menu") {
            val chatId = update.message?.chat?.id ?: return@command
            if (!isAuthorized(chatId)) return@command
            adminStates[chatId] = AdminState.Idle

            scope.launch {
                val stats = repository.getDatabaseStatistics()
                sendOrEditMessage(
                    bot = bot,
                    chatId = chatId,
                    messageId = null,
                    text = buildDashboardText(stats),
                    replyMarkup = buildDashboardKeyboard()
                )
            }
        }

        dispatcher.command("users") {
            val chatId = update.message?.chat?.id ?: return@command
            if (!isAuthorized(chatId)) return@command
            adminStates[chatId] = AdminState.Idle

            scope.launch {
                sendUsersList(bot, chatId, null)
            }
        }

        dispatcher.command("create_user") {
            val chatId = update.message?.chat?.id ?: return@command
            if (!isAuthorized(chatId)) return@command

            val text = update.message?.text?.trim().orEmpty()
            val args = text.split("\\s+".toRegex()).drop(1)

            if (args.isNotEmpty()) {
                scope.launch {
                    executeUserCreation(bot, chatId, null, args.joinToString(" "))
                }
            } else {
                adminStates[chatId] = AdminState.AwaitingUsername
                val promptText = buildString {
                    appendLine("👤 <b>Создание нового пользователя</b>")
                    appendLine(DIVIDER)
                    appendLine("Отправьте в ответном сообщении логин и пароль через пробел:")
                    appendLine("Например: <code>alex MyPassword123</code>")
                    append("<i>Если пароль не указан, он будет сгенерирован автоматически.</i>")
                }
                sendOrEditMessage(
                    bot = bot,
                    chatId = chatId,
                    messageId = null,
                    text = promptText,
                    replyMarkup = InlineKeyboardMarkup.createSingleRowKeyboard(
                        InlineKeyboardButton.CallbackData("❌ Отмена", "cancel")
                    )
                )
            }
        }

        dispatcher.command("shadow_ban") {
            val chatId = update.message?.chat?.id ?: return@command
            if (!isAuthorized(chatId)) return@command

            val args = update.message?.text?.split("\\s+".toRegex())?.drop(1)
            if (args.isNullOrEmpty()) {
                bot.sendMessage(ChatId.fromId(chatId), text = "⚠️ Использование: /shadow_ban <user_nick>")
                return@command
            }
            val nick = args.joinToString(" ")
            scope.launch {
                val success = userRepository.applyShadowBan(nick)
                val reply = if (success) "✅ Юзер '$nick' в теневом бане." else "❌ Юзер '$nick' не найден."
                bot.sendMessage(ChatId.fromId(chatId), text = reply)
            }
        }

        // --- TEXT INPUT ---

        dispatcher.text {
            val chatId = message.chat.id
            if (!isAuthorized(chatId)) return@text
            val text = message.text?.trim().orEmpty()
            if (text.startsWith("/")) return@text

            when (adminStates[chatId]) {
                is AdminState.AwaitingUsername -> {
                    scope.launch {
                        executeUserCreation(bot, chatId, null, text)
                    }
                }
                is AdminState.AwaitingSearch -> {
                    adminStates[chatId] = AdminState.Idle
                    scope.launch {
                        val user = repository.findUserByQuery(text)
                        if (user != null) {
                            sendUserCard(bot, chatId, null, user)
                        } else {
                            val notFoundText = "❌ Пользователь с логином или ID '<code>$text</code>' не найден."
                            val markup = InlineKeyboardMarkup.create(
                                listOf(InlineKeyboardButton.CallbackData("🔍 Попробовать снова", "search_user_btn")),
                                listOf(InlineKeyboardButton.CallbackData("📊 В главное меню", "menu"))
                            )
                            sendOrEditMessage(bot, chatId, null, notFoundText, markup)
                        }
                    }
                }
                else -> {
                    scope.launch {
                        val stats = repository.getDatabaseStatistics()
                        sendOrEditMessage(
                            bot = bot,
                            chatId = chatId,
                            messageId = null,
                            text = buildDashboardText(stats),
                            replyMarkup = buildDashboardKeyboard()
                        )
                    }
                }
            }
        }

        // --- CALLBACK QUERY ---

        dispatcher.callbackQuery {
            val chatId = callbackQuery.from.id
            if (!isAuthorized(chatId)) return@callbackQuery

            val data = callbackQuery.data
            val messageId = callbackQuery.message?.messageId
            val callbackId = callbackQuery.id

            bot.answerCallbackQuery(callbackQueryId = callbackId)

            scope.launch {
                when {
                    data == "menu" || data == "refresh_dashboard" -> {
                        adminStates[chatId] = AdminState.Idle
                        val stats = repository.getDatabaseStatistics()
                        sendOrEditMessage(
                            bot = bot,
                            chatId = chatId,
                            messageId = messageId,
                            text = buildDashboardText(stats),
                            replyMarkup = buildDashboardKeyboard()
                        )
                    }

                    data == "toggle_registration" -> {
                        EnvProvider.isPublicRegistrationAllowed = !EnvProvider.isPublicRegistrationAllowed
                        val stats = repository.getDatabaseStatistics()
                        sendOrEditMessage(
                            bot = bot,
                            chatId = chatId,
                            messageId = messageId,
                            text = buildDashboardText(stats),
                            replyMarkup = buildDashboardKeyboard()
                        )
                    }

                    data == "create_user_btn" -> {
                        adminStates[chatId] = AdminState.AwaitingUsername
                        val promptText = buildString {
                            appendLine("👤 <b>Создание нового пользователя</b>")
                            appendLine(DIVIDER)
                            appendLine("Отправьте в ответном сообщении логин и пароль через пробел:")
                            appendLine("Например: <code>alex MyPassword123</code>")
                            append("<i>Если пароль не указан, он будет сгенерирован автоматически.</i>")
                        }
                        sendOrEditMessage(
                            bot = bot,
                            chatId = chatId,
                            messageId = messageId,
                            text = promptText,
                            replyMarkup = InlineKeyboardMarkup.createSingleRowKeyboard(
                                InlineKeyboardButton.CallbackData("❌ Отмена", "cancel")
                            )
                        )
                    }

                    data == "search_user_btn" -> {
                        adminStates[chatId] = AdminState.AwaitingSearch
                        val searchText = buildString {
                            appendLine("🔍 <b>Поиск пользователя</b>")
                            appendLine(DIVIDER)
                            append("Введите никнейм или UUID пользователя в ответном сообщении:")
                        }
                        sendOrEditMessage(
                            bot = bot,
                            chatId = chatId,
                            messageId = messageId,
                            text = searchText,
                            replyMarkup = InlineKeyboardMarkup.createSingleRowKeyboard(
                                InlineKeyboardButton.CallbackData("❌ Отмена", "cancel")
                            )
                        )
                    }

                    data == "cancel" -> {
                        adminStates[chatId] = AdminState.Idle
                        val stats = repository.getDatabaseStatistics()
                        sendOrEditMessage(
                            bot = bot,
                            chatId = chatId,
                            messageId = messageId,
                            text = buildDashboardText(stats),
                            replyMarkup = buildDashboardKeyboard()
                        )
                    }

                    data == "list_users" -> {
                        adminStates[chatId] = AdminState.Idle
                        sendUsersList(bot, chatId, messageId)
                    }

                    data.startsWith("user_view:") -> {
                        val userIdStr = data.removePrefix("user_view:")
                        val uuid = try { Uuid.parse(userIdStr) } catch (_: Exception) { null }
                        if (uuid != null) {
                            val user = repository.findUserById(uuid)
                            if (user != null) {
                                sendUserCard(bot, chatId, messageId, user)
                            } else {
                                sendUsersList(bot, chatId, messageId)
                            }
                        }
                    }

                    data.startsWith("toggle_ban:") -> {
                        val userIdStr = data.removePrefix("toggle_ban:")
                        val uuid = try { Uuid.parse(userIdStr) } catch (_: Exception) { null }
                        if (uuid != null) {
                            repository.toggleBan(uuid)
                            val user = repository.findUserById(uuid)
                            if (user != null) {
                                sendUserCard(bot, chatId, messageId, user)
                            }
                        }
                    }

                    data.startsWith("toggle_shadow_ban:") -> {
                        val userIdStr = data.removePrefix("toggle_shadow_ban:")
                        val uuid = try { Uuid.parse(userIdStr) } catch (_: Exception) { null }
                        if (uuid != null) {
                            val user = repository.findUserById(uuid)
                            if (user != null) {
                                userRepository.applyShadowBan(user.nickName)
                                val updatedUser = repository.findUserById(uuid)
                                if (updatedUser != null) {
                                    sendUserCard(bot, chatId, messageId, updatedUser)
                                }
                            }
                        }
                    }

                    data.startsWith("set_plan:") -> {
                        val parts = data.removePrefix("set_plan:").split(":")
                        val userIdStr = parts.getOrNull(0)
                        val planStr = parts.getOrNull(1)
                        val uuid = try { userIdStr?.let { Uuid.parse(it) } } catch (_: Exception) { null }
                        val plan = try { planStr?.let { PremiumStatus.valueOf(it) } } catch (_: Exception) { null }

                        if (uuid != null && plan != null) {
                            repository.updateUserPremium(uuid, plan)
                            val user = repository.findUserById(uuid)
                            if (user != null) {
                                sendUserCard(bot, chatId, messageId, user)
                            }
                        }
                    }
                }
            }
        }
    }
}