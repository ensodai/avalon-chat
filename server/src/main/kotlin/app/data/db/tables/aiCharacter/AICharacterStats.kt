package app.data.db.tables.aiCharacter

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object AICharacterStats : Table("ai_character_stats") {
    // Используем ID персонажа как Primary Key (отношение 1:1)
    val characterId = reference("character_id", AICharacters, onDelete = ReferenceOption.CASCADE)

    // Счетчики
    val messageCount = long("message_count").default(0)
    val totalGeneratedTokens = long("total_generated_tokens").default(0)
    val popularityScore = double("popularity_score").default(0.0)

    // Опционально: дата последнего использования (для чистки старых данных)
    val lastInteractionAt = timestamp("last_interaction_at")
        .defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(characterId)

    init {
        // Добавляем индекс для быстрой сортировки по популярности
        index("idx_ai_stats_message_count", isUnique = false, messageCount)
        index("idx_ai_stats_popularity_score", isUnique = false, popularityScore)
    }
}
