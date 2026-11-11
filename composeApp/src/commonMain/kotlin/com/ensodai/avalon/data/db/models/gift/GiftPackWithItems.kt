package com.ensodai.avalon.data.db.models.gift

import androidx.room3.Embedded
import androidx.room3.Relation

data class GiftPackWithItems(
    @Embedded val pack: GiftPackEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["pack_id"]
    )
    val items: List<GiftItemEntity>
)
