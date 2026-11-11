package com.ensodai.avalon.data.db.models.character

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
    tableName = "welcome_card_entity",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["character_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("character_id")]
)
data class WelcomeCardEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Uuid,
    @ColumnInfo(name = "character_id")
    val characterId: Uuid,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "color_start")
    val colorStart: String,
    @ColumnInfo(name = "color_end")
    val colorEnd: String,
    @ColumnInfo(name = "color_light")
    val colorLight: String,
    @ColumnInfo(name = "is_enable_light")
    val isEnableLight: Boolean
)
