package com.ensodai.avalon.data.db.models.chat

import androidx.room3.Embedded
import androidx.room3.Relation
import com.ensodai.avalon.data.db.models.message.MessageContainerEntity
import com.ensodai.avalon.data.db.models.message.MessageSlotEntity

data class SlotWithContainers(
    @Embedded
    val slot: MessageSlotEntity,

    @Relation(
        entity = MessageContainerEntity::class,
        parentColumns = ["id"],
        entityColumns = ["slot_id"]
    )
    val containers: List<ContainerWithContent>
)