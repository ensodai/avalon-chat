package com.ensodai.avalon.data.db.models.gift

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.ensodai.shared.avalon.gifts.models.*
import kotlin.uuid.Uuid

@Entity(tableName = "gift_items")
data class GiftItemEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Uuid,
    @ColumnInfo(name = "pack_id")
    val packId: Uuid,
    @ColumnInfo(name = "file_url")
    val fileUrl: String,
    @ColumnInfo(name = "price")
    val price: Int,
    @ColumnInfo(name = "action_type")
    val actionType: GiftActionType,
    @ColumnInfo(name = "gift_name")
    val giftName: String,
    @ColumnInfo(name = "gift_description")
    val giftDescription: String
)
