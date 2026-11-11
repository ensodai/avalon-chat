package com.ensodai.avalon.data.db.dto

import androidx.room3.Embedded
import kotlin.uuid.Uuid

data class CharacterWithShortDetails(
    val id: Uuid,
    val name: String,
    val unclaimedEnergy: Double? = null,
    val totalEarnedEnergy: Double? = null,
    @Embedded
    val avatar: CharacterAvatarDto?
)