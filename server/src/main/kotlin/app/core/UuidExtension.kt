package app.core

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.updateReturning
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Обновляет запись и возвращает новое значение updatedAt.
 */
fun <T : BaseUuidTable> T.updateAndGetTimestamp(
    id: Uuid,
    body: T.(UpdateStatement) -> Unit
): Instant? {
    return this.updateReturning(
        returning = listOf(this.updatedAt),
        where = { this.id eq id }
    ) {
        this.body(it)
    }.singleOrNull()?.get(this.updatedAt)
}

/**
 * Универсальная функция для "мягкого удаления" сущности.
 */
fun <EntityType : BaseUuidEntity> BaseUuidEntityClass<EntityType>.softDelete(id: Uuid): Int {
    val table = this.table as BaseUuidTable
    val now = Clock.System.now()

    return table.update({ table.id eq id }) {
        it[deletedAt] = now
    }
}

/**
 * Результат операции мягкого удаления.
 */
data class SoftDeleteResult(
    val deletedAt: Instant,
    val updatedAt: Instant
)

/**
 * Универсальная функция для "мягкого удаления" с возвратом временных меток.
 */
fun <EntityType : BaseUuidEntity> BaseUuidEntityClass<EntityType>.softDeleteAndGetTimestamps(
    id: Uuid
): SoftDeleteResult? {
    val table = this.table as BaseUuidTable
    val now = Clock.System.now()

    return table.updateReturning(
        returning = listOf(table.deletedAt, table.updatedAt),
        where = { table.id eq id }
    ) {
        it[deletedAt] = now
    }.singleOrNull()?.let {
        SoftDeleteResult(
            deletedAt = it[table.deletedAt]!!,
            updatedAt = it[table.updatedAt]
        )
    }
}
