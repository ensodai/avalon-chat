package com.ensodai.avalon.data.db.models.chat

import androidx.room3.Embedded
import androidx.room3.Relation
import com.ensodai.avalon.data.db.models.message.MessageContainerEntity
import com.ensodai.avalon.data.db.models.message.MessageContentEntity

data class ContainerWithContent(
    @Embedded
    val container: MessageContainerEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["container_id"]
    )
    val contents: List<MessageContentEntity>
)