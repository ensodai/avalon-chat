package com.ensodai.avalon.data.db.models.character

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.ensodai.avalon.data.db.models.SyncAction
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

import com.ensodai.shared.avalon.character.models.*
import com.ensodai.shared.avalon.liveCall.models.VoiceModel

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "character_entity")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Uuid,
    @ColumnInfo(name = "is_public_full_description")
    val isPublicFullDescription: Boolean,
    @ColumnInfo(name = "visibility")
    val visibility: CharacterVisibility,
    @ColumnInfo(name = "owner_nick")
    val ownerNick: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "short_description")
    val shortDescription: String,
    @ColumnInfo(name = "full_description")
    val fullDescription: String?,
    @ColumnInfo(name = "first_message")
    val firstMessage: String?,
    @ColumnInfo(name = "can_edit")
    val canEdit: Boolean,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    @ColumnInfo(name = "temperature")
    val temperature: Float,
    @ColumnInfo(name = "unclaimed_energy")
    val unclaimedEnergy: Double? = null,
    @ColumnInfo(name = "total_earned_energy")
    val totalEarnedEnergy: Double? = null,
    @ColumnInfo(name = "is_outreach_enabled")
    val isOutreachEnabled: Boolean = false,
    @ColumnInfo(name = "voice")
    val voice: VoiceModel = VoiceModel.ZEPHYR,
    @ColumnInfo(name = "relationship_level")
    val relationshipLevel: Int = 1,
    @ColumnInfo(name = "experience_points")
    val experiencePoints: Long = 0L,
    @ColumnInfo(name = "events_count")
    val eventsCount: Int = 0,
    @ColumnInfo(name = "interaction_format")
    val interactionFormat: InteractionFormat = InteractionFormat.LITERARY_RPG,
    @ColumnInfo(name = "scenario")
    val scenario: String? = null,
    @ColumnInfo(name = "mes_example")
    val mesExample: String? = null,
    @ColumnInfo(name = "sync_state")
    val syncAction: SyncAction = SyncAction.NONE
)
