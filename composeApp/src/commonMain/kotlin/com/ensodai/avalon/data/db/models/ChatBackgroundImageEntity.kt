package com.ensodai.avalon.data.db.models

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.uuid.Uuid

@Entity(tableName = "chat_background_images")
data class ChatBackgroundImageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Uuid,
    @ColumnInfo(name = "url")
    val url: String
)
