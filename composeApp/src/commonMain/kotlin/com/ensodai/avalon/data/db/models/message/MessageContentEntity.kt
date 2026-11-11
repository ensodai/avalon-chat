package com.ensodai.avalon.data.db.models.message

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import kotlin.uuid.Uuid

@Entity(
    tableName = "message_contents",
    foreignKeys = [
        ForeignKey(
            entity = MessageContainerEntity::class,
            parentColumns = ["id"],
            childColumns = ["container_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("container_id")
    ]
)
data class MessageContentEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Uuid,
    @ColumnInfo(name = "container_id")
    val containerId: Uuid,
    @ColumnInfo(name = "avalon_message_content_type")
    val type: AvalonMessageContentType,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int
)