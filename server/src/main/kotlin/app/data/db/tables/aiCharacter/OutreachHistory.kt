package app.data.db.tables.aiCharacter

import app.core.BaseUuidTable
import app.data.db.tables.users.AppUsers
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

/**
 * Статусы фидбека по первому AI-контакту (аутричу).
 */
enum class FeedbackStatus {
    PENDING,        // Сообщение отправлено, ожидается реакция пользователя
    INTERESTED,     // Пользователь нажал "Интересно" или ответил текстовым сообщением
    NOT_INTERESTED  // Пользователь нажал "Нет" или проигнорировал (таймаут 24 часа)
}

/**
 * # OutreachHistoryTable (История первого контакта/аутрича)
 *
 * Хранит историю приветственных сообщений, отправленных ботами пользователям,
 * и отслеживает реакцию на них для предотвращения повторного спама и расчета лимитов.
 *
 * Наследуется от [BaseUuidTable] (поля id, created_at, updated_at, deleted_at создаются автоматически).
 */
object OutreachHistoryTable : BaseUuidTable("outreach_history") {
    val characterId = reference("character_id", AICharacters, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE)
    val sentAt = timestamp("sent_at").defaultExpression(CurrentTimestamp)
    val feedbackStatus = enumerationByName<FeedbackStatus>("feedback_status", 20).default(FeedbackStatus.PENDING)

    init {
        index("idx_outreach_history_lookup", isUnique = true, characterId, userId)
    }
}
