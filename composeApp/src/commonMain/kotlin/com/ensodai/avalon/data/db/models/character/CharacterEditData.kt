package com.ensodai.avalon.data.db.models.character

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Relation
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import kotlin.uuid.Uuid

data class CharacterCoreData(
    @ColumnInfo(name = "id")
    val id: Uuid,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "short_description")
    val shortDescription: String,
    @ColumnInfo(name = "full_description")
    val fullDescription: String?,
    @ColumnInfo(name = "first_message")
    val firstMessage: String?,
    @ColumnInfo(name = "visibility")
    val visibility: CharacterVisibility,
    @ColumnInfo(name = "temperature")
    val temperature: Float,
    @ColumnInfo(name = "is_outreach_enabled")
    val isOutreachEnabled: Boolean,
    @ColumnInfo(name = "voice")
    val voice: VoiceModel,
    @ColumnInfo(name = "interaction_format")
    val interactionFormat: InteractionFormat
)

data class CharacterEditData(
    @Embedded
    val core: CharacterCoreData,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["character_id"]
    )
    val avatar: CharacterAvatarEntity?,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["character_id"]
    )
    val images: List<CharacterImageEntity>
)