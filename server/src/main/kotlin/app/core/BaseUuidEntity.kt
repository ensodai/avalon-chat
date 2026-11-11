package app.core

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.time.Instant
import kotlin.uuid.Uuid

// Абстрактный базовый класс для DAO-сущностей
abstract class BaseUuidEntity(id: EntityID<Uuid>, table: BaseUuidTable) : UuidEntity(id) {
    var createdAt: Instant by table.createdAt
    var updatedAt: Instant by table.updatedAt
    var deletedAt: Instant? by table.deletedAt
}

// Абстрактный базовый класс для companion object-ов сущностей
// Это нужно, чтобы обобщить логику работы с BaseUuidTable
abstract class BaseUuidEntityClass<E : BaseUuidEntity>(table: BaseUuidTable) : UuidEntityClass<E>(table)
