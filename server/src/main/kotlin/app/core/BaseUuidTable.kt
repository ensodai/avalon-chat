package app.core

import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

// Абстрактный базовый класс для всех таблиц с аудитом и Uuid
abstract class BaseUuidTable(name: String) : UuidTable(name) {
    // Метка времени создания записи.
    // Устанавливается базой данных при вставке (INSERT).
    val createdAt = timestamp("created_at")
        .defaultExpression(CurrentTimestamp)

    // Метка времени последнего обновления.
    // Устанавливается базой данных при вставке и ОБНОВЛЯЕТСЯ триггером при UPDATE.
    val updatedAt = timestamp("updated_at")
        .defaultExpression(CurrentTimestamp)

    // Метка времени для "мягкого удаления".
    // Если null - запись активна. Если не null - запись удалена.
    val deletedAt = timestamp("deleted_at").nullable()
}
