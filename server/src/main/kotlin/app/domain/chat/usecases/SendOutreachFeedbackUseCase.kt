package app.domain.chat.usecases

import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.aiCharacter.FeedbackStatus
import app.data.db.tables.aiCharacter.OutreachHistoryTable
import app.data.db.tables.chatSessions.ChatSessions
import app.service.chat.MessageNotifier
import app.utils.dbQuery
import app.utils.getUTCTimeNow
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

private val log = KotlinLogging.logger {}

/**
 * # SendOutreachFeedbackUseCase (Обратная связь на приветствия)
 *
 * Сценарий использования для обработки реакции пользователя (👍 "Интересно" / 👎 "Нет")
 * на полученное приветственное сообщение (Outreach).
 */
@Factory
class SendOutreachFeedbackUseCase(
    private val messageNotifier: MessageNotifier
) {
    /**
     * Выполняет обработку обратной связи.
     *
     * @param userId Идентификатор пользователя, отправляющего фидбек
     * @param historyId Идентификатор записи в истории рассылки приветствий
     * @param isPositive true - 👍 "Интересно", false - 👎 "Нет"
     */
    suspend fun execute(userId: Uuid, characterId: Uuid, isPositive: Boolean) {
        val result = dbQuery {
            // 1. Ищем запись в истории аутрича
            val outreachRow = OutreachHistoryTable
                .select(OutreachHistoryTable.id, OutreachHistoryTable.feedbackStatus)
                .where { (OutreachHistoryTable.characterId eq characterId) and (OutreachHistoryTable.userId eq userId) }
                .singleOrNull()

            if (outreachRow == null) {
                log.warn { "SendOutreachFeedbackUseCase: Запись истории аутрича не найдена: characterId=$characterId, userId=$userId" }
                return@dbQuery null
            }

            val historyId = outreachRow[OutreachHistoryTable.id].value
            val currentStatus = outreachRow[OutreachHistoryTable.feedbackStatus]
            if (currentStatus != FeedbackStatus.PENDING) {
                log.warn { "SendOutreachFeedbackUseCase: Обратная связь уже была обработана ранее: historyId=$historyId, status=$currentStatus" }
                return@dbQuery null
            }

            val botId = outreachRow[OutreachHistoryTable.characterId].value
            val now = getUTCTimeNow()

            if (isPositive) {
                // 👍 "Интересно"
                // Обновляем статус истории на INTERESTED
                OutreachHistoryTable.update({ OutreachHistoryTable.id eq historyId }) {
                    it[feedbackStatus] = FeedbackStatus.INTERESTED
                    it[updatedAt] = now
                }

                // Считываем текущие счетчики бота
                val botRow = AICharacters
                    .select(AICharacters.outreachRepliedCount)
                    .where { AICharacters.id eq botId }
                    .singleOrNull()

                val currentRepliedCount = botRow?.get(AICharacters.outreachRepliedCount) ?: 0

                // Инкрементируем успешные контакты, сбрасываем счетчик подряд идущих отказов в 0
                AICharacters.update({ AICharacters.id eq botId }) {
                    it[outreachRepliedCount] = currentRepliedCount + 1
                    it[consecutiveFailuresCount] = 0
                }

                log.info { "SendOutreachFeedbackUseCase: Пользователь $userId одобрил аутрич от бота $botId. Успехи бота обновлены." }
                botId to false
            } else {
                // 👎 "Нет"
                // Обновляем статус истории на NOT_INTERESTED
                OutreachHistoryTable.update({ OutreachHistoryTable.id eq historyId }) {
                    it[feedbackStatus] = FeedbackStatus.NOT_INTERESTED
                    it[updatedAt] = now
                }

                // Считываем счетчик отказов бота
                val botRow = AICharacters
                    .select(AICharacters.consecutiveFailuresCount)
                    .where { AICharacters.id eq botId }
                    .singleOrNull()

                val currentFailures = botRow?.get(AICharacters.consecutiveFailuresCount) ?: 0
                val newFailures = currentFailures + 1

                // Обновляем бота: увеличиваем счетчик отказов, при 10 подряд — блокируем
                AICharacters.update({ AICharacters.id eq botId }) {
                    it[consecutiveFailuresCount] = newFailures
                    if (newFailures >= 10) {
                        it[isOutreachBlocked] = true
                        log.warn { "SendOutreachFeedbackUseCase: Бот $botId заблокирован в рассылках из-за 10 отказов подряд!" }
                    }
                }

                // Архивируем чат-сессию для этого пользователя и бота
                val updatedCount = ChatSessions.update({ (ChatSessions.userId eq userId) and (ChatSessions.characterId eq botId) }) {
                    it[isArchived] = true
                    it[updatedAt] = now
                }

                log.info { "SendOutreachFeedbackUseCase: Пользователь $userId отклонил аутрич от бота $botId. Сессия архивирована: $updatedCount." }
                botId to (updatedCount > 0)
            }
        }

        // 3. Если сессия была архивирована, триггерим синхронизацию у клиента
        if (result != null && result.second) {
            messageNotifier.notifyUserWithMessage(
                userId = userId,
                message = ServerMessage.SyncTrigger(entity = "chats")
            )
        }
    }
}
