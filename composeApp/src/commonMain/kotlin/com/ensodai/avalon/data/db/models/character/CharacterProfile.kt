package com.ensodai.avalon.data.db.models.character

import androidx.room3.Embedded
import androidx.room3.Relation
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import kotlin.uuid.Uuid

data class CharacterProfile(
    val id: Uuid,
    val name: String,
    val ownerNick: String,
    val canEdit: Boolean,
    val isPublicFullDescription: Boolean,
    val shortDescription: String,
    val fullDescription: String?,
    val avatarUrl: String?,
    val images: List<CharacterImageEntity>,
    val relationshipLevel: Int,
    val experiencePoints: Long,
    val visibility: CharacterVisibility = CharacterVisibility.PUBLIC,
    val scenario: String? = null,
    val mesExample: String? = null
)

data class CharacterProfileTuple(
    @Embedded
    val character: CharacterEntity,
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