package com.ensodai.avalon.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.room3.Update
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.data.db.models.character.CharacterAvatarEntity
import com.ensodai.avalon.data.db.models.character.CharacterImageEntity
import com.ensodai.avalon.data.db.models.character.WelcomeCardImageEntity
import com.ensodai.avalon.domain.useCases.editWelcomeCardUseCase.UpdateWelcomeCardConfig
import kotlin.uuid.Uuid

@Dao
abstract class UpdateCharacterDao {

    @Query("UPDATE character_entity SET name = :newName WHERE id = :characterId")
    abstract suspend fun updateName(characterId: Uuid, newName: String)

    @Query("UPDATE character_entity SET short_description = :description WHERE id = :characterId")
    abstract suspend fun updateDescription(characterId: Uuid, description: String)

    @Query("UPDATE character_entity SET full_description = :fullDescription, is_public_full_description = :isPublicFullDescription WHERE id = :characterId")
    abstract suspend fun updateFullDescription(
        characterId: Uuid,
        fullDescription: String,
        isPublicFullDescription: Boolean
    )

    @Query("UPDATE character_entity SET scenario = :scenario WHERE id = :characterId")
    abstract suspend fun updateScenario(characterId: Uuid, scenario: String?)

    @Query("UPDATE character_entity SET mes_example = :mesExample WHERE id = :characterId")
    abstract suspend fun updateMesExample(characterId: Uuid, mesExample: String?)

    @Query("UPDATE character_entity SET visibility = :characterVisibility WHERE id = :characterId")
    abstract suspend fun updatePrivacy(characterId: Uuid, characterVisibility: CharacterVisibility)

    @Query("UPDATE character_entity SET first_message = :message WHERE id = :characterId")
    abstract suspend fun updateFirstMessage(characterId: Uuid, message: String)

    @Query("DELETE FROM character_avatar_entity WHERE character_id = :charId")
    abstract suspend fun deleteAvatarByCharId(charId: Uuid)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertCharacterAvatar(avatar: CharacterAvatarEntity)

    @Update
    protected abstract suspend fun _update_insertCharacterAvatar(avatar: CharacterAvatarEntity)

    @Transaction
    open suspend fun insertCharacterAvatar(avatar: CharacterAvatarEntity) {
        _insert_insertCharacterAvatar(avatar)
        _update_insertCharacterAvatar(avatar)
    }// --- Images (Чистка) ---

    @Query("DELETE FROM character_image_entity WHERE character_id = :charId")
    abstract suspend fun deleteImagesByCharId(charId: Uuid)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertCharacterImages(images: List<CharacterImageEntity>)

    @Update
    protected abstract suspend fun _update_insertCharacterImages(images: List<CharacterImageEntity>)

    @Transaction
    open suspend fun insertCharacterImages(images: List<CharacterImageEntity>) {
        _insert_insertCharacterImages(images)
        _update_insertCharacterImages(images)
    }

    @Query("UPDATE character_image_entity SET image_url = :imageUrl, is_unlocked = :isUnlocked WHERE id = :imageId")
    abstract suspend fun updateImageUnlockState(
        imageId: Uuid,
        imageUrl: String,
        isUnlocked: Boolean
    )

    @Query("UPDATE character_entity SET temperature = :temperature WHERE id = :characterId")
    abstract suspend fun updateTemperature(characterId: Uuid, temperature: Float)

    @Query("UPDATE character_entity SET is_outreach_enabled = :isOutreachEnabled WHERE id = :characterId")
    abstract suspend fun updateOutreach(characterId: Uuid, isOutreachEnabled: Boolean)

    @Query("UPDATE character_entity SET voice = :voice WHERE id = :characterId")
    abstract suspend fun updateVoice(characterId: Uuid, voice: VoiceModel)

    @Query("UPDATE character_entity SET interaction_format = :interactionFormat WHERE id = :characterId")
    abstract suspend fun updateInteractionFormat(
        characterId: Uuid,
        interactionFormat: InteractionFormat
    )


    @Query(
        """
        UPDATE welcome_card_entity 
        SET description = :description,
            color_start = :colorStart,
            color_end = :colorEnd,
            color_light = :colorLight,
            is_enable_light = :isEnableLight
        WHERE character_id = :characterId
    """
    )
    abstract suspend fun updateWelcomeCardDetails(
        characterId: Uuid,
        description: String?,
        colorStart: String,
        colorEnd: String,
        colorLight: String,
        isEnableLight: Boolean
    )

    @Query("DELETE FROM welcome_card_image_entity WHERE character_id = :characterId")
    abstract suspend fun deleteAllWelcomeCardImages(characterId: Uuid)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertWelcomeCardImages(images: List<WelcomeCardImageEntity>)

    @Update
    protected abstract suspend fun _update_insertWelcomeCardImages(images: List<WelcomeCardImageEntity>)

    @Transaction
    open suspend fun insertWelcomeCardImages(images: List<WelcomeCardImageEntity>) {
        _insert_insertWelcomeCardImages(images)
        _update_insertWelcomeCardImages(images)
    }

    @Transaction
    open suspend fun updateWelcomeCardTransaction(
        characterId: Uuid,
        config: UpdateWelcomeCardConfig,
        newImages: List<WelcomeCardImageEntity>
    ) {

        updateWelcomeCardDetails(
            characterId = characterId,
            description = config.welcomeCardDescription,
            colorStart = config.startColor,
            colorEnd = config.endColor,
            colorLight = config.colorLight,
            isEnableLight = config.isEnableLight
        )

        deleteAllWelcomeCardImages(characterId)

        if (newImages.isNotEmpty()) {
            insertWelcomeCardImages(newImages)
        }
    }
}