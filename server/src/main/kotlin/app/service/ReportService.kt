package app.service

import com.ensodai.shared.avalon.session.models.ReportRequest
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaPhoto
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single
import app.data.db.tables.users.Moderators
import app.data.db.tables.users.AppUser
import app.data.db.tables.aiCharacter.AICharacter
import app.data.db.tables.aiCharacter.AICharacterStats
import app.data.db.tables.aiCharacter.AICharacterLorebooks
import app.data.db.tables.aiCharacter.AICharacterLorebookEntries
import app.data.db.tables.images.CharacterAvatars
import app.data.db.tables.images.CharacterImages
import app.utils.dbQuery
import app.cache.RedisService
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Максимальный размер файла для Telegram (10 MB)
private const val MAX_FILE_SIZE = 10 * 1024 * 1024L
// Лимит подписи для медиа в Telegram
private const val CAPTION_LIMIT = 1024

data class ReportDetails(
    val characterName: String,
    val shortDescription: String,
    val fullDescription: String,
    val category: String,
    val messageCount: Int,
    val firstMessage: String,
    val formattedDate: String,
    val avatarFile: File?,
    val additionalImages: List<File>
)

data class LorebookReportInfo(
    val name: String,
    val description: String?,
    val count: Long,
    val author: String
)

@Single
class ReportService(
    private val bot: Bot,
    private val adminId: Long
) {
    suspend fun getModeratorIds(): List<Uuid> = dbQuery {
        Moderators.select(Moderators.user)
            .where { Moderators.isActive eq true }
            .map { it[Moderators.user].value }
    }

    private suspend fun fetchReportDetails(characterId: Uuid): ReportDetails? = dbQuery {
        val aiCharacter = AICharacter.findById(characterId) ?: return@dbQuery null

        val msgCount = AICharacterStats
            .select(AICharacterStats.messageCount)
            .where { AICharacterStats.characterId eq characterId }
            .singleOrNull()
            ?.get(AICharacterStats.messageCount)
            ?: 0L

        val avatarPath = CharacterAvatars
            .select(CharacterAvatars.imagePath)
            .where { CharacterAvatars.characterPublicId eq aiCharacter.id }
            .singleOrNull()
            ?.get(CharacterAvatars.imagePath)

        val imagePaths = CharacterImages
            .select(CharacterImages.imagePath)
            .where { CharacterImages.characterPublicId eq aiCharacter.id }
            .orderBy(CharacterImages.orderIndex to SortOrder.ASC)
            .map { it[CharacterImages.imagePath] }

        val createdAt = aiCharacter.createdAt
        val formattedDate = if (createdAt.toEpochMilliseconds() == 0L) {
            "Дата неизвестна"
        } else {
            val moscowZone = ZoneId.of("Europe/Moscow")
            val javaInstant = java.time.Instant.ofEpochMilli(createdAt.toEpochMilliseconds())
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(moscowZone).format(javaInstant)
        }

        ReportDetails(
            characterName = aiCharacter.characterName,
            shortDescription = aiCharacter.shortDescription,
            fullDescription = aiCharacter.fullDescription ?: "Описание отсутствует",
            category = aiCharacter.category,
            messageCount = msgCount.toInt(),
            firstMessage = aiCharacter.firstMessage ?: "Первое сообщение отсутствует",
            formattedDate = formattedDate,
            avatarFile = avatarPath?.let { File(it) },
            additionalImages = imagePaths.map { File(it) }
        )
    }

    private suspend fun sendReportTelegram(
        bot: Bot,
        adminId: Long,
        messageText: String,
        details: ReportDetails
    ) {
        val validImages = mutableListOf<File>()
        details.avatarFile?.let { file ->
            if (file.exists() && file.length() > 0 && file.length() <= MAX_FILE_SIZE) validImages.add(file)
        }
        details.additionalImages.forEach { file ->
            if (file.exists() && file.length() > 0 && file.length() <= MAX_FILE_SIZE) validImages.add(file)
        }

        val caption = if (messageText.length > CAPTION_LIMIT) {
            messageText.take(CAPTION_LIMIT - 3) + "..."
        } else {
            messageText
        }

        try {
            when {
                validImages.isEmpty() -> {
                    bot.sendMessage(
                        chatId = ChatId.fromId(adminId),
                        text = messageText,
                        parseMode = ParseMode.HTML
                    )
                }
                validImages.size == 1 -> {
                    bot.sendPhoto(
                        chatId = ChatId.fromId(adminId),
                        photo = TelegramFile.ByFile(validImages[0]),
                        caption = caption,
                        parseMode = ParseMode.HTML
                    )
                }
                else -> {
                    val mediaGroup = validImages.mapIndexed { index, file ->
                        InputMediaPhoto(
                            media = TelegramFile.ByFile(file),
                            caption = if (index == 0) caption else null
                        )
                    }.toTypedArray()

                    bot.sendMediaGroup(
                        chatId = ChatId.fromId(adminId),
                        mediaGroup = MediaGroup.from(*mediaGroup)
                    )
                }
            }
        } catch (e: Exception) {
            System.err.println("Ошибка отправки медиа в Telegram: ${e.message}")
            bot.sendMessage(
                chatId = ChatId.fromId(adminId),
                text = messageText,
                parseMode = ParseMode.HTML
            )
        }
    }

    suspend fun handleUserReport(request: ReportRequest, userId: Uuid, characterUUID: Uuid?, bot: Bot, adminId: Long) {
        val characterId = characterUUID ?: throw IllegalArgumentException("Character ID is missing")
        
        val appUserName = dbQuery {
            AppUser.findById(userId)?.let { user ->
                user.nickName ?: user.name
            } ?: "Неизвестный пользователь"
        }

        val details = fetchReportDetails(characterId)
            ?: throw IllegalStateException("Персонаж $characterId не найден")

        val messageText = buildString {
            val shortFullDescription = details.fullDescription.take(500) +
                    if (details.fullDescription.length > 500) "..." else ""
            appendLine("<b>📢 Новый репорт</b>")
            appendLine("<b>🚨 Сообщение:</b> ${request.message}")
            appendLine("-------------------------")
            appendLine("<b>👤 ID репортера:</b> <code>$userId</code>")
            appendLine("<b>🗣️ Имя репортера:</b> $appUserName")
            appendLine("-------------------------")
            appendLine("<b>🆔 ID персонажа:</b> <code>$characterId</code>")
            appendLine("<b>🧑 Имя персонажа:</b> ${details.characterName}")
            appendLine("<b>💬 Сообщений:</b> ${details.messageCount}")
            appendLine("<b>🗂 Категория:</b> ${details.category}")
            appendLine("<b>📅 Создан:</b> ${details.formattedDate}")
            appendLine("<b>🗣 Первое сообщение:</b> ${details.firstMessage}")
            appendLine("<b>📝 Краткое описание:</b> ${details.shortDescription}")
            appendLine("<b>📖 Полное описание:</b> $shortFullDescription")
        }

        sendReportTelegram(bot, adminId, messageText, details)
    }

    suspend fun handleModeratorCommand(request: ReportRequest, userId: Uuid, characterUUID: Uuid?, bot: Bot, adminId: Long): String {
        val characterId = characterUUID ?: return "Character ID is missing"
        val message = request.message.trim()

        if (message.startsWith("[ban]", ignoreCase = true)) {
            val reason = message.removePrefix("[ban]").trim().take(512)

            var categoryToInvalidate: String? = null

            dbQuery {
                val aiCharacter = AICharacter.findById(characterId)
                    ?: throw IllegalStateException("Персонаж с ID $characterId не найден для блокировки.")
                aiCharacter.isBanned = true
                aiCharacter.visibility = CharacterVisibility.PRIVATE
                categoryToInvalidate = aiCharacter.category
            }

            categoryToInvalidate?.let { category ->
                try {
                    RedisService.invalidateCategoryCache(category)
                } catch (e: Exception) {
                    System.err.println("Не удалось инвалидировать кэш категории $category в Redis: ${e.message}")
                }
            }

            val details = fetchReportDetails(characterId)
                ?: throw IllegalStateException("Персонаж $characterId не найден")

            val messageText = buildString {
                val shortFullDescription = details.fullDescription.take(500) +
                        if (details.fullDescription.length > 500) "..." else ""
                appendLine("<b>❌ ПЕРСОНАЖ ЗАБАНЕН МОДЕРАТОРОМ</b>")
                if (reason.isNotBlank()) {
                    appendLine("<b>📝 Причина:</b> $reason")
                }
                appendLine("-------------------------")
                appendLine("<b>👤 ID модератора:</b> <code>$userId</code>")
                appendLine("-------------------------")
                appendLine("<b>🆔 ID персонажа:</b> <code>$characterId</code>")
                appendLine("<b>🧑 Имя персонажа:</b> ${details.characterName}")
                appendLine("<b>💬 Сообщений:</b> ${details.messageCount}")
                appendLine("<b>🗂 Категория:</b> ${details.category}")
                appendLine("<b>📅 Создан:</b> ${details.formattedDate}")
                appendLine("<b>🗣 Первое сообщение:</b> ${details.firstMessage}")
                appendLine("<b>📝 Краткое описание:</b> ${details.shortDescription}")
                appendLine("<b>📖 Полное описание:</b> $shortFullDescription")
            }

            sendReportTelegram(bot, adminId, messageText, details)

            return "Персонаж $characterId был забанен. Причина: $reason"
        }

        // Если это модератор, но команда не бан - шлем как обычный репорт от модератора
        val details = fetchReportDetails(characterId)
            ?: throw IllegalStateException("Персонаж $characterId не найден")

        val messageText = buildString {
            val shortFullDescription = details.fullDescription.take(500) +
                    if (details.fullDescription.length > 500) "..." else ""
            appendLine("<b>🗣️ Жалоба от модератора</b>")
            appendLine("<b>🚨 Сообщение:</b> $message")
            appendLine("-------------------------")
            appendLine("<b>👤 ID модератора:</b> <code>$userId</code>")
            appendLine("-------------------------")
            appendLine("<b>🆔 ID персонажа:</b> <code>$characterId</code>")
            appendLine("<b>🧑 Имя персонажа:</b> ${details.characterName}")
            appendLine("<b>💬 Сообщений:</b> ${details.messageCount}")
            appendLine("<b>🗂 Категория:</b> ${details.category}")
            appendLine("<b>📅 Создан:</b> ${details.formattedDate}")
            appendLine("<b>🗣 Первое сообщение:</b> ${details.firstMessage}")
            appendLine("<b>📝 Краткое описание:</b> ${details.shortDescription}")
            appendLine("<b>📖 Полное описание:</b> $shortFullDescription")
        }

        sendReportTelegram(bot, adminId, messageText, details)

        return "Жалоба модератора отправлена."
    }

    suspend fun handleLorebookReport(lorebookId: Uuid, reporterUserId: Uuid, message: String): String {
        val appUserName = dbQuery {
            AppUser.findById(reporterUserId)?.let { user ->
                user.nickName ?: user.name
            } ?: "Неизвестный пользователь"
        }

        val info = dbQuery {
            val book = AICharacterLorebooks.selectAll()
                .where { AICharacterLorebooks.id eq lorebookId }
                .singleOrNull() ?: return@dbQuery null

            val count = AICharacterLorebookEntries.selectAll()
                .where { AICharacterLorebookEntries.lorebookId eq lorebookId }
                .count()

            val author = book[AICharacterLorebooks.userId]?.value?.let { authorId ->
                AppUser.findById(authorId)?.let { it.nickName ?: it.name }
            } ?: "Аноним"

            LorebookReportInfo(book[AICharacterLorebooks.name], book[AICharacterLorebooks.description], count, author)
        } ?: throw IllegalStateException("База знаний $lorebookId не найдена")

        val messageText = buildString {
            appendLine("<b>📢 Жалоба на базу знаний (Lorebook)</b>")
            appendLine("<b>🚨 Сообщение:</b> $message")
            appendLine("-------------------------")
            appendLine("<b>👤 ID репортера:</b> <code>$reporterUserId</code>")
            appendLine("<b>🗣️ Имя репортера:</b> $appUserName")
            appendLine("-------------------------")
            appendLine("<b>🆔 ID базы знаний:</b> <code>$lorebookId</code>")
            appendLine("<b>📚 Название:</b> ${info.name}")
            appendLine("<b>✍️ Автор:</b> ${info.author}")
            appendLine("<b>📄 Статей:</b> ${info.count}")
            if (!info.description.isNullOrBlank()) {
                appendLine("<b>📝 Описание:</b> ${info.description.take(500)}")
            }
        }

        try {
            bot.sendMessage(
                chatId = ChatId.fromId(adminId),
                text = messageText,
                parseMode = ParseMode.HTML
            )
        } catch (e: Exception) {
            System.err.println("Ошибка отправки жалобы на лорбук в Telegram: ${e.message}")
        }

        return "Жалоба на базу знаний отправлена модераторам"
    }
}


