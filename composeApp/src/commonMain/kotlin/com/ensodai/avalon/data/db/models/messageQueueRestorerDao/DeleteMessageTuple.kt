package com.ensodai.avalon.data.db.models.messageQueueRestorerDao

import androidx.room3.ColumnInfo
import kotlin.uuid.Uuid

data class DeleteMessageTuple(
    @ColumnInfo(name = "slot_id") val slotId: Uuid,
    @ColumnInfo(name = "chat_variant_id") val variantId: Uuid,
    @ColumnInfo(name = "chat_session_id") val sessionId: Uuid
)
