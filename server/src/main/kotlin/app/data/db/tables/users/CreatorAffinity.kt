package app.data.db.tables.users

import app.core.BaseUuidTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

/**
 * # CreatorAffinityTable (Таблица лояльности к автору)
 *
 * Хранит очки лояльности пользователей к создателям персонажей.
 * Используется для приоритизации рассылки новых ботов среди вовлеченной аудитории автора.
 *
 * Наследуется от [BaseUuidTable] (поля id, created_at, updated_at, deleted_at создаются автоматически).
 */
object CreatorAffinityTable : BaseUuidTable("creator_affinity") {
    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE)
    val creatorId = reference("creator_id", AppUsers, onDelete = ReferenceOption.CASCADE)
    val messageCount = integer("message_count").default(0)
    val lastInteractedAt = timestamp("last_interacted_at").defaultExpression(CurrentTimestamp)

    init {
        index("idx_creator_affinity_lookup", isUnique = true, userId, creatorId)
    }
}
