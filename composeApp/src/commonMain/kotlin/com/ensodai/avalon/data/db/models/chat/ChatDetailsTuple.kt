package com.ensodai.avalon.data.db.models.chat

import androidx.room3.ColumnInfo
import androidx.room3.Relation
import com.ensodai.avalon.data.db.models.character.WelcomeCardEntity
import com.ensodai.avalon.data.db.models.character.WelcomeCardImageEntity
import kotlin.uuid.Uuid

data class ChatDetailsTuple(
    @ColumnInfo(name = "id")
    val characterId: Uuid,
    @ColumnInfo(name = "first_message")
    val firstMessage: String?,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["character_id"]
    )
    val welcomeCard: WelcomeCardEntity?,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["character_id"]
    )
    val welcomeImages: List<WelcomeCardImageEntity>
)