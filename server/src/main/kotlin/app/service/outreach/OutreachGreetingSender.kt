package app.service.outreach

import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.aiCharacter.FeedbackStatus
import app.data.db.tables.aiCharacter.OutreachHistoryTable
import app.data.db.tables.chat.MessageContainers
import app.data.db.tables.chat.MessageContents
import app.data.db.tables.chat.MessageSlots
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.users.AppUsers
import app.service.chat.MessageNotifier
import app.utils.dbQuery
import app.utils.getUTCTimeNow
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import app.apiv4.chat.ChatEventBus
import app.service.notification.PushNotificationService
import org.koin.core.annotation.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Single
class OutreachGreetingSender(
    private val messageNotifier: MessageNotifier,
    private val chatEventBus: ChatEventBus,
    private val pushNotificationService: PushNotificationService
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val log = KotlinLogging.logger {}

    /**
     * Атомарно сохраняет сгенерированное приветствие в БД, создавая сессию и вариант диалога,
     * и отправляет реальном времени WebSocket-событие пользователю.
     */
    suspend fun saveAndSendGreeting(
        bot: CharacterInfo,
        userId: Uuid,
        text: String,
        reaction: String?,
        sessionId: Uuid,
        variantId: Uuid
    ) {
        val now = getUTCTimeNow()
        val slotId = Uuid.random()
        val containerId = Uuid.random()
        val contentId = Uuid.random()
        var userQuota = 0
        var userReserve = 0

        // 1. Атомарно сохраняем все сущности в БД
        dbQuery {
            // А. Создаем сессию чата (без activeVariantId и lastMessageId)
            ChatSessions.insert {
                it[id] = sessionId
                it[ChatSessions.userId] = userId
                it[ChatSessions.characterId] = bot.id
                it[isArchived] = false
                it[activeVariantId] = null
                it[createdAt] = now
                it[updatedAt] = now
                it[lastMessageRole] = MessageRole.MODEL
                it[lastMessageId] = null
                it[lastMessageAt] = now
                it[lastMessageType] = AvalonMessageContentType.TEXT
                it[lastMessagePreview] = text.take(200)
            }

            // Б. Создаем вариант диалога (с unread_count = 1)
            ChatVariants.insert {
                it[id] = variantId
                it[chatSessionId] = sessionId
                it[name] = "По умолчанию"
                it[unreadCount] = 1
                it[createdAt] = now
                it[updatedAt] = now
            }

            // В. Создаем слот (без activeMessageId)
            MessageSlots.insert {
                it[id] = slotId
                it[chatVariantId] = variantId
                it[activeMessageId] = null
                it[createdAt] = now
                it[updatedAt] = now
            }

            // Г. Создаем контейнер сообщения
            MessageContainers.insert {
                it[id] = containerId
                it[MessageContainers.slotId] = slotId
                it[role] = MessageRole.MODEL
                it[rarity] = Rarity.COMMON
                it[characterReaction] = reaction
                it[createdAt] = now
                it[updatedAt] = now
            }

            // Д. Записываем контент сообщения
            MessageContents.insert {
                it[id] = contentId
                it[MessageContents.containerId] = containerId
                it[type] = AvalonMessageContentType.TEXT
                it[content] = text
                it[orderIndex] = 0
                it[createdAt] = now
                it[updatedAt] = now
            }

            // Обновляем слот, связывая его с контейнером
            MessageSlots.update({ MessageSlots.id eq slotId }) {
                it[activeMessageId] = containerId
            }

            // Обновляем сессию чата, связывая её с вариантом и последним сообщением
            ChatSessions.update({ ChatSessions.id eq sessionId }) {
                it[activeVariantId] = variantId
                it[lastMessageId] = containerId
            }

            // Е. Записываем отправку в историю рассылки
            OutreachHistoryTable.insert {
                it[id] = Uuid.random()
                it[characterId] = bot.id
                it[OutreachHistoryTable.userId] = userId
                it[sentAt] = now
                it[feedbackStatus] = FeedbackStatus.PENDING
                it[createdAt] = now
                it[updatedAt] = now
            }

            // Ж. Инкрементируем счетчик отправленных аутричей у бота
            val currentSentCount = AICharacters
                .select(AICharacters.outreachSentCount)
                .where { AICharacters.id eq bot.id }
                .singleOrNull()
                ?.get(AICharacters.outreachSentCount) ?: 0

            AICharacters.update({ AICharacters.id eq bot.id }) {
                it[outreachSentCount] = currentSentCount + 1
            }

            // З. Получаем квоту и резерв пользователя для отправки в WebSocket
            val userRow = AppUsers
                .select(AppUsers.currentDailyBalance, AppUsers.reserveBalance)
                .where { AppUsers.id eq userId }
                .singleOrNull()

            userQuota = userRow?.get(AppUsers.currentDailyBalance) ?: 0
            userReserve = userRow?.get(AppUsers.reserveBalance) ?: 0
        }

        log.info { "OutreachGreetingSender: Сообщение от бота ${bot.name} сохранено в БД для пользователя $userId." }

        // 2. Формируем DTO для отправки в сокет
        val slotResponse = com.ensodai.shared.avalon.chat.models.MessageSlotResponse(
            id = slotId,
            chatVariantId = variantId,
            chatSessionId = sessionId,
            activeContainerId = containerId,
            createdAt = now,
            updatedAt = now,
            containers = listOf(
                com.ensodai.shared.avalon.chat.models.MessageContainerResponse(
                    containerId = containerId,
                    slotId = slotId,
                    role = MessageRole.MODEL,
                    rarity = Rarity.COMMON,
                    status = com.ensodai.shared.avalon.chat.models.MessageStatus.SENT,
                    userReaction = null,
                    characterReaction = reaction,
                    isRead = false,
                    showOutreachFeedback = true,
                    createdAt = now,
                    updatedAt = now,
                    contents = listOf(
                        com.ensodai.shared.avalon.chat.models.MessageContentResponse(
                            id = contentId,
                            type = AvalonMessageContentType.TEXT,
                            content = text,
                            orderIndex = 0
                        )
                    )
                )
            )
        )

        // 3. Асинхронно отправляем событие через MessageNotifier и push-уведомление (если оффлайн)
        scope.launch {
            try {
                val isOnline = chatEventBus.isUserOnline(userId)
                log.info { "OutreachGreetingSender: Проверка статуса пользователя $userId. Онлайн: $isOnline" }

                messageNotifier.notifyUserWithMessage(
                    userId = userId,
                    message = ServerMessage.NewChatMessage(
                        slot = slotResponse,
                        characterId = bot.id,
                        currentUserQuota = userQuota,
                        reserveEnergy = userReserve
                    )
                )

                if (!isOnline) {
                    log.info { "OutreachGreetingSender: Пользователь $userId оффлайн. Отправка пуш-уведомления..." }
                    pushNotificationService.sendNewMessagePush(
                        userId = userId,
                        characterName = bot.name,
                        text = text,
                        characterId = bot.id,
                        sessionId = sessionId
                    )
                } else {
                    log.info { "OutreachGreetingSender: WebSocket-событие NewChatMessage успешно отправлено онлайн-пользователю $userId." }
                }
            } catch (e: Exception) {
                log.error(e) { "OutreachGreetingSender: Ошибка отправки уведомления для пользователя $userId" }
            }
        }
    }
}
