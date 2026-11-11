package com.ensodai.avalon.data.db.models.character

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
    tableName = "character_image_entity",
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
data class CharacterImageEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Uuid,
    @ColumnInfo(name = "character_id")
    val characterId: Uuid,
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
    @ColumnInfo(name = "thumb_hash")
    val thumbHash: String? = null,
    @ColumnInfo(name = "is_private")
    val isPrivate: Boolean = false,
    @ColumnInfo(name = "energy_cost")
    val energyCost: Int = 0,
    @ColumnInfo(name = "required_level")
    val requiredLevel: Int? = null,
    @ColumnInfo(name = "is_unlocked")
    val isUnlocked: Boolean = false
)

