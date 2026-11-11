package com.ensodai.avalon.data.db.models.character

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity(
    tableName = "character_event_roadmap_entity",
    primaryKeys = ["character_id", "level"],
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["character_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CharacterEventRoadmapEntity(
    @ColumnInfo(name = "character_id")
    val characterId: Uuid,
    @ColumnInfo(name = "level")
    val level: Int,
    @ColumnInfo(name = "has_reward")
    val hasReward: Boolean,
    @ColumnInfo(name = "is_unlocked")
    val isUnlocked: Boolean,
    @ColumnInfo(name = "trigger_scenario")
    val triggerScenario: String?,
    
    // Поля вложенной сущности rewardPhoto (CharacterImageResponse)
    @ColumnInfo(name = "reward_photo_id")
    val rewardPhotoId: Uuid?,
    @ColumnInfo(name = "reward_photo_url")
    val rewardPhotoUrl: String?,
    @ColumnInfo(name = "reward_photo_thumb_hash")
    val rewardPhotoThumbHash: String?,
    @ColumnInfo(name = "reward_photo_is_private")
    val rewardPhotoIsPrivate: Boolean = false,
    @ColumnInfo(name = "reward_photo_energy_cost")
    val rewardPhotoEnergyCost: Int = 0,
    @ColumnInfo(name = "reward_photo_required_level")
    val rewardPhotoRequiredLevel: Int? = null,
    @ColumnInfo(name = "reward_photo_is_unlocked")
    val rewardPhotoIsUnlocked: Boolean = false
)
