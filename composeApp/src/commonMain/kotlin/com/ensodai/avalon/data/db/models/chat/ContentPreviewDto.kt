package com.ensodai.avalon.data.db.models.chat

import androidx.room3.ColumnInfo
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType

data class ContentPreviewDto(
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "avalon_message_content_type")
    val type: AvalonMessageContentType
)