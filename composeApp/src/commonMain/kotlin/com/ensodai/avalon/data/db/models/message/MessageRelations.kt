package com.ensodai.avalon.data.db.models.message

import androidx.room3.Embedded
import androidx.room3.Relation

/**
 * Промежуточный класс: Контейнер + Его Контент
 */
data class ContainerWithContentsRelation(
    @Embedded
    val container: MessageContainerEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["container_id"]
    )
    val contents: List<MessageContentEntity>
)

/**
 * Промежуточный класс: Слот + Его Контейнеры (каждый со своим контентом)
 */
data class SlotWithContainersRelation(
    @Embedded
    val slot: MessageSlotEntity,
    @Relation(
        entity = MessageContainerEntity::class,
        parentColumns = ["id"],
        entityColumns = ["slot_id"]
    )
    val containers: List<ContainerWithContentsRelation>
)