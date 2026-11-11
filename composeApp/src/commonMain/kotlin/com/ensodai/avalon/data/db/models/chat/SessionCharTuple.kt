package com.ensodai.avalon.data.db.models.chat

import androidx.room3.ColumnInfo
import kotlin.uuid.Uuid

data class SessionCharTuple(
    @ColumnInfo(name = "id") val sessionId: Uuid,
    @ColumnInfo(name = "character_id") val characterId: Uuid
)