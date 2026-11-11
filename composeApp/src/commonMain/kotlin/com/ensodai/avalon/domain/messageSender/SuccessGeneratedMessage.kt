package com.ensodai.avalon.domain.messageSender

import com.ensodai.shared.avalon.chat.models.Rarity

data class SuccessGeneratedMessage(
    val text: String,
    val tokens: Int,
    val rarity: Rarity
)