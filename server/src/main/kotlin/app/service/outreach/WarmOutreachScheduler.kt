package app.service.outreach

import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.users.AppUsers
import app.data.db.tables.users.UserCharacterRelationships
import app.data.models.EnergyDeductionResult
import app.data.repository.ChatRepository
import app.data.repository.UserRepository
import app.service.ChatActionType
import app.service.LlmConfigService
import app.service.LlmResult
import app.service.chat.MessageNotifier
import app.service.notification.PushNotificationService
import app.service.relationship.CharacterRelationshipService
import app.apiv4.chat.ChatEventBus
import app.utils.dbQuery
import app.utils.getUTCTimeNow
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.days
import kotlin.uuid.Uuid

data class WarmOutreachCandidate(
    val relationshipId: Uuid,
    val userId: Uuid,
    val characterId: Uuid,
    val relationshipLevel: Int,
    val outreachMorningEnabled: Boolean,
    val outreachMorningPrompt: String?,
    val outreachAfternoonEnabled: Boolean,
    val outreachAfternoonPrompt: String?,
    val outreachEveningEnabled: Boolean,
    val outreachEveningPrompt: String?,
    val lastOutreachSentAt: Instant?,
    val lastOutreachSlot: String?,
    val timezoneOffset: Int,
    val characterName: String,
    val modelCode: String
)

@Single
class WarmOutreachScheduler(
    private val userRepository: UserRepository,
    private val llmConfigService: LlmConfigService,
    private val chatEventBus: ChatEventBus,
    private val pushNotificationService: PushNotificationService,
    private val chatRepository: ChatRepository,
    private val messageNotifier: MessageNotifier,
    private val warmOutreachDirector: WarmOutreachDirector
) : KoinComponent {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val log = KotlinLogging.logger {}

    /**
     * Запускает фоновую службу планировщика проактивных сообщений близких друзей (Warm Outreach).
     * Создает бесконечный цикл в корутине с 15-минутным интервалом.
     */
    fun start() {
        log.info { "WarmOutreachScheduler: Служба планировщика теплых рассылок запущена." }
        scope.launch {
            while (isActive) {
                try {
                    runWarmOutreachCycle()
                    delay(15.minutes)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.error(e) { "WarmOutreachScheduler: Ошибка в цикле планировщика" }
                    delay(1.minutes) // При ошибке ждем минуту и пробуем снова
                }
            }
        }
    }

    /**
     * Останавливает фоновую корутину планировщика.
     */
    fun stop() {
        scope.cancel()
        log.info { "WarmOutreachScheduler: Служба остановлена." }
    }

    /**
     * Выполняет один цикл теплой проактивной рассылки.
     */
    suspend fun runWarmOutreachCycle() {
        log.info { "WarmOutreachScheduler: Запуск цикла теплых рассылок..." }
        val now = Clock.System.now()
        val sevenDaysAgo = now - 7.days

        // 1. Получаем список потенциальных кандидатов (уровень >= 5, активны за неделю, фича включена)
        val candidates = dbQuery {
            UserCharacterRelationships
                .join(AppUsers, JoinType.INNER, UserCharacterRelationships.userId, AppUsers.id)
                .join(AICharacters, JoinType.INNER, UserCharacterRelationships.characterId, AICharacters.id)
                .select(
                    UserCharacterRelationships.id,
                    UserCharacterRelationships.userId,
                    UserCharacterRelationships.characterId,
                    UserCharacterRelationships.relationshipLevel,
                    UserCharacterRelationships.outreachMorningEnabled,
                    UserCharacterRelationships.outreachMorningPrompt,
                    UserCharacterRelationships.outreachAfternoonEnabled,
                    UserCharacterRelationships.outreachAfternoonPrompt,
                    UserCharacterRelationships.outreachEveningEnabled,
                    UserCharacterRelationships.outreachEveningPrompt,
                    UserCharacterRelationships.lastOutreachSentAt,
                    UserCharacterRelationships.lastOutreachSlot,
                    AppUsers.timezoneOffset,
                    AICharacters.characterName,
                    AICharacters.geminiModel
                )
                .where {
                    (UserCharacterRelationships.relationshipLevel greaterEq 5) and
                    (UserCharacterRelationships.isOutreachEnabled eq true) and
                    (AppUsers.deletedAt.isNull()) and
                    (AppUsers.isBanned eq false) and
                    (AppUsers.isOutreachEnabled eq true) and
                    (AppUsers.lastSeenAt.isNotNull()) and
                    (AppUsers.lastSeenAt greaterEq sevenDaysAgo) and
                    (AICharacters.isBanned eq false)
                }
                .map { row ->
                    WarmOutreachCandidate(
                        relationshipId = row[UserCharacterRelationships.id].value,
                        userId = row[UserCharacterRelationships.userId].value,
                        characterId = row[UserCharacterRelationships.characterId].value,
                        relationshipLevel = row[UserCharacterRelationships.relationshipLevel],
                        outreachMorningEnabled = row[UserCharacterRelationships.outreachMorningEnabled],
                        outreachMorningPrompt = row[UserCharacterRelationships.outreachMorningPrompt],
                        outreachAfternoonEnabled = row[UserCharacterRelationships.outreachAfternoonEnabled],
                        outreachAfternoonPrompt = row[UserCharacterRelationships.outreachAfternoonPrompt],
                        outreachEveningEnabled = row[UserCharacterRelationships.outreachEveningEnabled],
                        outreachEveningPrompt = row[UserCharacterRelationships.outreachEveningPrompt],
                        lastOutreachSentAt = row[UserCharacterRelationships.lastOutreachSentAt],
                        lastOutreachSlot = row[UserCharacterRelationships.lastOutreachSlot],
                        timezoneOffset = row[AppUsers.timezoneOffset],
                        characterName = row[AICharacters.characterName],
                        modelCode = row[AICharacters.geminiModel]
                    )
                }
        }

        log.info { "WarmOutreachScheduler: Найдено ${candidates.size} связей отношений (уровень >= 5), готовых к проверке." }

        for (candidate in candidates) {
            try {
                // 2. Offline Guard: пишем только если пользователь оффлайн
                val isOnline = chatEventBus.isUserOnline(candidate.userId)
                if (isOnline) {
                    log.info { "WarmOutreachScheduler: Пользователь ${candidate.userId} сейчас онлайн. Пропуск." }
                    continue
                }

                // 3. Timezone & Slot Guard
                val localInstant = now + candidate.timezoneOffset.minutes
                val ktixInstant = kotlinx.datetime.Instant.fromEpochMilliseconds(localInstant.toEpochMilliseconds())
                val localHour = ktixInstant.toLocalDateTime(TimeZone.UTC).hour

                val currentSlot = when (localHour) {
                    in 9..11 -> "MORNING"
                    in 12..15 -> "AFTERNOON"
                    in 18..20 -> "EVENING"
                    else -> null
                }

                if (currentSlot == null) {
                    continue // Пользователь вне активных временных окон
                }

                // Проверяем, включен ли слот пользователем
                val isSlotEnabled = when (currentSlot) {
                    "MORNING" -> candidate.outreachMorningEnabled
                    "AFTERNOON" -> candidate.outreachAfternoonEnabled
                    "EVENING" -> candidate.outreachEveningEnabled
                    else -> false
                }

                if (!isSlotEnabled) {
                    continue
                }

                // Проверяем, не отправляли ли мы уже сообщение в этот же слот сегодня
                val lastSent = candidate.lastOutreachSentAt
                if (lastSent != null && candidate.lastOutreachSlot == currentSlot) {
                    val lastSentLocal = lastSent + candidate.timezoneOffset.minutes
                    val lastSentDay = lastSentLocal.toLocalDateTime(TimeZone.UTC).date
                    val currentLocalDay = localInstant.toLocalDateTime(TimeZone.UTC).date
                    if (lastSentDay == currentLocalDay) {
                        continue // Уже отправляли в этот слот сегодня
                    }
                }

                // 4. Пожелания (кастомный промпт) для текущего слота
                val userPrompt = when (currentSlot) {
                    "MORNING" -> candidate.outreachMorningPrompt
                    "AFTERNOON" -> candidate.outreachAfternoonPrompt
                    "EVENING" -> candidate.outreachEveningPrompt
                    else -> null
                }

                log.info { "WarmOutreachScheduler: Обнаружен подходящий кандидат для отправки: userId=${candidate.userId}, characterId=${candidate.characterId}, слот=$currentSlot" }

                // 5. Поиск или создание сессии и варианта чата
                val activeSessionAndVariant = dbQuery {
                    val sessionRow = ChatSessions
                        .select(ChatSessions.id, ChatSessions.activeVariantId)
                        .where { (ChatSessions.userId eq candidate.userId) and (ChatSessions.characterId eq candidate.characterId) }
                        .singleOrNull()

                    if (sessionRow != null) {
                        val sId = sessionRow[ChatSessions.id].value
                        var vId = sessionRow[ChatSessions.activeVariantId]?.value
                        if (vId == null) {
                            // Если по какой-то причине вариант null, ищем первый доступный или создаем
                            vId = ChatVariants
                                .select(ChatVariants.id)
                                .where { ChatVariants.chatSessionId eq sId }
                                .limit(1)
                                .singleOrNull()
                                ?.get(ChatVariants.id)?.value
                        }
                        if (vId != null) {
                            sId to vId
                        } else {
                            // Создаем вариант
                            val newVarId = Uuid.random()
                            chatRepository.createNewVariant(sId, newVarId)
                            sId to newVarId
                        }
                    } else {
                        // Сессии нет вовсе — генерируем и создаем
                        val newSessionId = Uuid.random()
                        val newVariantId = Uuid.random()
                        chatRepository.createSessionAndVariant(
                            userId = candidate.userId,
                            characterId = candidate.characterId,
                            chatSessionId = newSessionId,
                            chatVariantId = newVariantId
                        )
                        newSessionId to newVariantId
                    }
                }

                val (sessionId, variantId) = activeSessionAndVariant

                // 6. Списание энергии с учетом скидки
                val llmConfig = llmConfigService.getConfig(candidate.modelCode)
                val deductionResult = userRepository.deductAndGetBalance(
                    userId = candidate.userId,
                    llmConfig = llmConfig,
                    actionType = ChatActionType.REPLY,
                    contentId = null,
                    metaInfo = mapOf("character_id" to candidate.characterId.toString())
                )

                if (deductionResult !is EnergyDeductionResult.Success) {
                    log.warn { "WarmOutreachScheduler: Не удалось списать энергию для пользователя ${candidate.userId} (возможно, недостаточно средств). Пропуск." }
                    continue
                }

                val newDailyBalance = deductionResult.newBalance.dailyEnergy
                val newReserveBalance = deductionResult.newBalance.reserveEnergy

                // 7. Генерация сообщения через WarmOutreachDirector
                val generatorResult = warmOutreachDirector.generateWarmOutreach(
                    userId = candidate.userId,
                    characterId = candidate.characterId,
                    chatVariantId = variantId,
                    relationshipLevel = candidate.relationshipLevel,
                    slotName = currentSlot,
                    userPrompt = userPrompt,
                    modelCode = candidate.modelCode
                )

                if (generatorResult is LlmResult.Success) {
                    val text = generatorResult.text
                    val reaction = generatorResult.reaction
                    log.info { "WarmOutreachScheduler: Сгенерировано проактивное сообщение (длина: ${text.length} симв.)" }

                    val slotId = Uuid.random()
                    val containerId = Uuid.random()
                    val contentId = Uuid.random()

                    // 8. Сохранение сообщения в базу данных
                    val slotResponse = chatRepository.createNewMessageTurn(
                        messageRole = MessageRole.MODEL,
                        messageRarity = Rarity.COMMON,
                        slotId = slotId,
                        contentId = contentId,
                        containerId = containerId,
                        chatVariantId = variantId,
                        chatSessionId = sessionId,
                        contentType = AvalonMessageContentType.TEXT,
                        text = text,
                        messageSystemInstruction = null,
                        visualDescription = null
                    )

                    // 9. Обновление статуса отправки в связке отношений
                    dbQuery {
                        UserCharacterRelationships.update({ UserCharacterRelationships.id eq candidate.relationshipId }) {
                            it[lastOutreachSentAt] = getUTCTimeNow()
                            it[lastOutreachSlot] = currentSlot
                        }
                    }

                    // 10. Отправка WebSocket-события
                    messageNotifier.notifyUserWithMessage(
                        userId = candidate.userId,
                        message = ServerMessage.NewChatMessage(
                            slot = slotResponse,
                            characterId = candidate.characterId,
                            currentUserQuota = newDailyBalance,
                            reserveEnergy = newReserveBalance
                        )
                    )

                    // 11. Отправка Push-уведомления оффлайн пользователю
                    pushNotificationService.sendNewMessagePush(
                        userId = candidate.userId,
                        characterName = candidate.characterName,
                        text = text,
                        characterId = candidate.characterId,
                        sessionId = sessionId
                    )

                    log.info { "WarmOutreachScheduler: Проактивное сообщение успешно отправлено пользователю ${candidate.userId}." }
                } else {
                    val errMessage = (generatorResult as? LlmResult.Error)?.exception?.message ?: "Блокировка или пустой ответ"
                    log.error { "WarmOutreachScheduler: Ошибка генерации сообщения от персонажа ${candidate.characterId}: $errMessage" }
                }

            } catch (e: Exception) {
                log.error(e) { "WarmOutreachScheduler: Ошибка обработки кандидата с relationshipId=${candidate.relationshipId}" }
            }
        }
    }
}
