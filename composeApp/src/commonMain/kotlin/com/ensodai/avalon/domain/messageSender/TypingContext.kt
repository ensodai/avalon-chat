package com.ensodai.avalon.domain.messageSender

import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.avalon.utils.getUTCTimeNow
import kotlin.time.Instant

data class TypingContext(
    val rarity: Rarity,
    val startedAt: Instant = getUTCTimeNow()
)