package com.ensodai.avalon.data.db.models.character

import androidx.room3.Embedded
import androidx.room3.Relation
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.data.db.models.chat.ChatSessionEntity


data class CharacterDetailsWithConfigs(
    val characterDetails: CharacterWithDetailsTuple?,
    val availableConfigs: List<AvailableLlmConfigs>,
    val selectedLlmConfig: AvailableLlmConfigs?
)

data class CharacterWithDetailsTuple(
    @Embedded
    val character: CharacterEntity,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["character_id"]
    )
    val welcomeCard: WelcomeCardEntity?,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["character_id"]
    )
    val welcomeImages: List<WelcomeCardImageEntity>,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["character_id"]
    )
    val avatar: CharacterAvatarEntity?,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["character_id"]
    )
    val chatSession: ChatSessionEntity?
)