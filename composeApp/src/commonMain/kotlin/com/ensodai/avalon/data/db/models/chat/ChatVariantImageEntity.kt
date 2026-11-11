package com.ensodai.avalon.data.db.models.chat

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
    tableName = "chat_variant_image",
    foreignKeys = [
        ForeignKey(
            entity = ChatVariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["chat_variant_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chat_variant_id")]
)
data class ChatVariantImageEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Uuid,
    @ColumnInfo(name = "chat_variant_id")
    val chatVariantId: Uuid,
    @ColumnInfo(name = "image_url")
    val imageUrl: String,
    @ColumnInfo(name = "thumb_hash")
    val thumbHash: String? = null
)
