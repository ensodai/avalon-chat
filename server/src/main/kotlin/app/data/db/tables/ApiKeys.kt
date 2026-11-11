package app.data.db.tables

import app.core.BaseUuidTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object ApiKeys : BaseUuidTable("api_keys") {
    val key = varchar("key", 255).uniqueIndex()

    // Блокировки
    val isBanned = bool("is_banned").default(false)
    val cooldownUntil = timestamp("cooldown_until").nullable()

    // Статистика
    val requestCount = integer("request_count").default(0)
    val failureCount = integer("failure_count").default(0)

    // Ротация. Используем timestamp.
    // defaultExpression(CurrentTimestamp) означает, что при создании ключа
    // он встает в конец очереди на использование.
    val lastSelected = timestamp("last_selected")
        .defaultExpression(CurrentTimestamp)

    init {
        // Индекс для быстрого поиска: "Дай мне активные ключи"
        index("idx_active_keys", isUnique = false, isBanned, cooldownUntil, deletedAt)
    }
}
