package com.ensodai.avalon.data.db.models.character

import androidx.room3.Embedded
import androidx.room3.Relation

data class WelcomeCardWithImages(
    @Embedded
    val card: WelcomeCardEntity,
    @Relation(
        parentColumns = ["character_id"],
        entityColumns = ["character_id"]
    )
    val images: List<WelcomeCardImageEntity>
)