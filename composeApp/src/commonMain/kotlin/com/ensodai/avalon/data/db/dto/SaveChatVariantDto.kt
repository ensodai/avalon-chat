package com.ensodai.avalon.data.db.dto

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import kotlin.uuid.Uuid

data class ChatVariant(
    val id: Uuid,
    val name: String?,
    val messageText: String?,
    @Embedded(prefix = "default_")
    val defaultImage: CharacterImage?,
    @Embedded
    val image: CharacterImage?
)

// CharacterImage оставляем как был
data class CharacterImage(
    @ColumnInfo(name = "image_id")
    val id: Uuid?,
    @ColumnInfo(name = "image_url")
    val url: String?,
    @ColumnInfo(name = "thumb_hash")
    val thumbHash: String? = null
)