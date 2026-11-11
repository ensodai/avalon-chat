package com.ensodai.avalon.data.db.models

import androidx.room3.ColumnInfo

data class VariantInfo(
    @ColumnInfo(name = "id") val localId: Int,
    @ColumnInfo(name = "chat_session_id") val sessionId: Int,
    @ColumnInfo(name = "active_chat_variant_id") val activeVariantIdInSession: Int?
)