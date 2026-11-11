package com.ensodai.avalon.data.db.models

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "show_promo_flags_entity")
data class ShowPromoFlagsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val telegramReminder: Boolean = true,
    val openApiTransitionAnnouncement: Boolean = true
)
