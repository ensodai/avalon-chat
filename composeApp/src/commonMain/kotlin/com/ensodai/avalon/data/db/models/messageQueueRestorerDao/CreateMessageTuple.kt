package com.ensodai.avalon.data.db.models.messageQueueRestorerDao

import androidx.room3.ColumnInfo
import kotlin.uuid.Uuid

data class CreateMessageTuple(
    @ColumnInfo(name = "container_id") val containerId: Uuid,
    @ColumnInfo(name = "slot_id") val slotId: Uuid,
    @ColumnInfo(name = "content_id") val contentId: Uuid,
    @ColumnInfo(name = "text_content") val text: String,
    @ColumnInfo(name = "chat_variant_id") val variantId: Uuid,
    @ColumnInfo(name = "chat_session_id") val sessionId: Uuid,
    @ColumnInfo(name = "character_id") val characterId: Uuid
)