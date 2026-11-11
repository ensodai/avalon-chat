package com.ensodai.avalon.data.db.models.message

import androidx.room3.ColumnInfo
import com.ensodai.shared.avalon.chat.models.MessageRole
import kotlin.uuid.Uuid

data class SlotInfo(
    @ColumnInfo(name = "slotId") val slotId: Uuid,
    @ColumnInfo(name = "role") val role: MessageRole
)