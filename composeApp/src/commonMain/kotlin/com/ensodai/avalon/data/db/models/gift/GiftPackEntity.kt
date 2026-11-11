package com.ensodai.avalon.data.db.models.gift

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.uuid.Uuid

@Entity(tableName = "gift_packs")
data class GiftPackEntity(
    @PrimaryKey val id: Uuid,
    val authorId: Uuid?,
    val name: String,
    val description: String?,
    val coverUrl: String,
    val isPublic: Boolean,
    val price: Int,
    val isOwned: Boolean = false
)
