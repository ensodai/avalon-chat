package app.data.db.tables.chat

import app.core.BaseUuidTable
import app.data.db.tables.users.AppUsers
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

/**
 * # MessageReadReceiptsTable (Квитанции о прочтении сообщений)
 *
 * Хранит записи о прочтении сообщений (контейнеров) пользователями.
 * Отсутствие записи для пары (userId, messageContainerId) означает, что сообщение непрочитано.
 */
object MessageReadReceiptsTable : BaseUuidTable("message_read_receipts") {
    val messageContainerId = reference("message_container_id", MessageContainers, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE)
    val readAt = timestamp("read_at").defaultExpression(CurrentTimestamp)

    init {
        // Уникальный составной индекс для молниеносного поиска по паре "Пользователь + Сообщение"
        // и гарантированного исключения дубликатов квитанций в СУБД.
        index("idx_read_receipts_unique", isUnique = true, userId, messageContainerId)
    }
}
