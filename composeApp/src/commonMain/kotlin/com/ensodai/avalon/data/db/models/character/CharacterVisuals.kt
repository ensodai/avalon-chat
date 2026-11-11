package com.ensodai.avalon.data.db.models.character

import androidx.room3.Embedded
import androidx.room3.Relation
import com.ensodai.avalon.data.db.dto.CharacterImageDTO

data class CharacterVisualsResult(
    val avatar: CharacterImageDTO?,
    val images: List<CharacterImageDTO>
)

data class CharacterVisualsRelation(
    @Embedded
    val character: CharacterEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["character_id"]
    )
    val avatarEntity: CharacterAvatarEntity?,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["character_id"]
    )
    val imageEntities: List<CharacterImageEntity>
)