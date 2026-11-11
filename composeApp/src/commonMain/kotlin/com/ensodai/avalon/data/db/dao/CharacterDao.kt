package com.ensodai.avalon.data.db.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.RewriteQueriesToDropUnusedColumns
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.room3.Update
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.CharacterWithShortDetails
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.data.db.models.character.CharacterAvatarEntity
import com.ensodai.avalon.data.db.models.character.CharacterEditData
import com.ensodai.avalon.data.db.models.character.CharacterEntity
import com.ensodai.avalon.data.db.models.character.CharacterImageEntity
import com.ensodai.avalon.data.db.models.character.CharacterProfileTuple
import com.ensodai.avalon.data.db.models.character.CharacterVisualsRelation
import com.ensodai.avalon.data.db.models.character.CharacterVisualsResult
import com.ensodai.avalon.data.db.models.character.CharacterWithDetailsTuple
import com.ensodai.avalon.data.db.models.character.DescriptionPrivacyTuple
import com.ensodai.avalon.data.db.models.character.WelcomeCardEntity
import com.ensodai.avalon.data.db.models.character.WelcomeCardImageEntity
import com.ensodai.avalon.data.db.models.character.WelcomeCardWithImages
import com.ensodai.avalon.data.db.models.character.CharacterEventRoadmapEntity
import com.ensodai.avalon.data.db.models.chat.ChatDetailsTuple
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

@Dao
abstract class CharacterDao {

    // --- Character ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_upsertCharacter(character: CharacterEntity)

    @Update
    protected abstract suspend fun _update_upsertCharacter(character: CharacterEntity)

    @Transaction
    open suspend fun upsertCharacter(character: CharacterEntity) {
        _insert_upsertCharacter(character)
        _update_upsertCharacter(character)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_upsertCharacters(characters: List<CharacterEntity>)

    @Update
    protected abstract suspend fun _update_upsertCharacters(characters: List<CharacterEntity>)

    @Transaction
    open suspend fun upsertCharacters(characters: List<CharacterEntity>) {
        _insert_upsertCharacters(characters)
        _update_upsertCharacters(characters)
    }// --- Avatar (Чистка) ---

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
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertCharacterAvatars(avatars: List<CharacterAvatarEntity>)

    @Update
    protected abstract suspend fun _update_insertCharacterAvatars(avatars: List<CharacterAvatarEntity>)

    @Transaction
    open suspend fun insertCharacterAvatars(avatars: List<CharacterAvatarEntity>) {
        _insert_insertCharacterAvatars(avatars)
        _update_insertCharacterAvatars(avatars)
    }

    @Query("DELETE FROM character_avatar_entity WHERE character_id IN (:charIds)")
    abstract suspend fun deleteAvatarsByCharIds(charIds: List<Uuid>)

    // --- Images (Чистка) ---
    @Query("DELETE FROM character_image_entity WHERE character_id = :charId")
    abstract suspend fun deleteImagesByCharId(charId: Uuid)

    @Query("DELETE FROM character_image_entity WHERE character_id IN (:charIds)")
    abstract suspend fun deleteImagesByCharIds(charIds: List<Uuid>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertCharacterImages(images: List<CharacterImageEntity>)

    @Update
    protected abstract suspend fun _update_insertCharacterImages(images: List<CharacterImageEntity>)

    @Transaction
    open suspend fun insertCharacterImages(images: List<CharacterImageEntity>) {
        _insert_insertCharacterImages(images)
        _update_insertCharacterImages(images)
    }// --- Welcome Card (Чистка) ---

    @Query("DELETE FROM welcome_card_entity WHERE character_id = :charId")
    abstract suspend fun deleteWelcomeCardByCharId(charId: Uuid)

    @Query("DELETE FROM welcome_card_entity WHERE character_id IN (:charIds)")
    abstract suspend fun deleteWelcomeCardsByCharIds(charIds: List<Uuid>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertWelcomeCard(card: WelcomeCardEntity)

    @Update
    protected abstract suspend fun _update_insertWelcomeCard(card: WelcomeCardEntity)

    @Transaction
    open suspend fun insertWelcomeCard(card: WelcomeCardEntity) {
        _insert_insertWelcomeCard(card)
        _update_insertWelcomeCard(card)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertWelcomeCards(cards: List<WelcomeCardEntity>)

    @Update
    protected abstract suspend fun _update_insertWelcomeCards(cards: List<WelcomeCardEntity>)

    @Transaction
    open suspend fun insertWelcomeCards(cards: List<WelcomeCardEntity>) {
        _insert_insertWelcomeCards(cards)
        _update_insertWelcomeCards(cards)
    }// --- Welcome Card Images (Чистка) ---

    @Query("DELETE FROM welcome_card_image_entity WHERE character_id = :charId")
    abstract suspend fun deleteWelcomeCardImagesByCharId(charId: Uuid)

    @Query("DELETE FROM welcome_card_image_entity WHERE character_id IN (:charIds)")
    abstract suspend fun deleteWelcomeCardImagesByCharIds(charIds: List<Uuid>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertWelcomeCardImages(images: List<WelcomeCardImageEntity>)

    @Update
    protected abstract suspend fun _update_insertWelcomeCardImages(images: List<WelcomeCardImageEntity>)

    @Transaction
    open suspend fun insertWelcomeCardImages(images: List<WelcomeCardImageEntity>) {
        _insert_insertWelcomeCardImages(images)
        _update_insertWelcomeCardImages(images)
    }

    @Query("DELETE FROM character_entity WHERE id IN (:ids)")
    abstract suspend fun deleteCharactersByIds(ids: List<Uuid>)

    @Delete
    abstract suspend fun deleteCharacter(character: CharacterEntity)

    @Query("DELETE FROM character_entity WHERE id = :id")
    abstract suspend fun deleteCharacterById(id: Uuid)

    @Query("SELECT * FROM character_image_entity WHERE character_id = :id")
    abstract suspend fun getImagesByCharacterId(id: Uuid): List<CharacterImageEntity>

    @Query("SELECT * FROM character_avatar_entity WHERE character_id = :id LIMIT 1")
    abstract suspend fun getAvatarByCharacterId(id: Uuid): CharacterAvatarEntity?

    /**
     * Жёстко удаляет чужих персонажей по ID: CASCADE потянет Avatars, Images, WelcomeCards/Images — чисто!
     * @param ids Список ID персонажей (только для !canEdit, батч для мигания).
     * @return Int — кол-во удалённых (проверь == ids.size для full wipe).
     */
    @Query("DELETE FROM character_entity WHERE id IN (:ids)")
    abstract suspend fun hardDeleteCharacters(ids: List<Uuid>): Int

    /**
     * Получает персонажа по ID для проверки canEdit (быстро, один).
     * @param id ID персонажа из сессии.
     * @return CharacterEntity? (null если не нашёл, но не должно быть).
     */
    @Query("SELECT * FROM character_entity WHERE id = :id LIMIT 1")
    abstract suspend fun getCharacterById(id: Uuid): CharacterEntity?

    @Query("SELECT * FROM character_entity WHERE id IN (:ids)")
    abstract suspend fun getCharactersByIds(ids: List<Uuid>): List<CharacterEntity>

    @Transaction
    @Query("SELECT * FROM character_entity WHERE id = :characterId")
    abstract fun getCharacterWithDetailsFlow(characterId: Uuid): Flow<CharacterWithDetailsTuple?>

    @Transaction // Обязательно, так как используем @Relation
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT id, name, first_message FROM character_entity WHERE id = :characterId")
    abstract fun getChatDetailsFlow(characterId: Uuid): Flow<ChatDetailsTuple?>

    @Query("SELECT EXISTS(SELECT 1 FROM character_entity WHERE id = :characterId)")
    abstract suspend fun characterExists(characterId: Uuid): Boolean

    @Transaction
    @Query("SELECT * FROM character_entity WHERE id = :characterId")
    abstract fun getCharacterProfileFlow(characterId: Uuid): Flow<CharacterProfileTuple?>

    @Query("SELECT id FROM character_entity")
    abstract suspend fun getAllCharacterIds(): List<Uuid>

    @Transaction
    @Query(
        """
        SELECT 
            id, 
            name, 
            short_description, 
            full_description, 
            first_message, 
            visibility,
            temperature,
            is_outreach_enabled,
            voice,
            interaction_format
        FROM character_entity 
        WHERE id = :characterId
    """
    )
    abstract fun getCharacterEditDataFlow(characterId: Uuid): Flow<CharacterEditData?>

    @Query("SELECT full_description, is_public_full_description FROM character_entity WHERE id = :characterId")
    abstract fun getCharacterFullDescriptionFlow(characterId: Uuid): Flow<DescriptionPrivacyTuple?>

    @Transaction
    @Query("SELECT * FROM character_entity WHERE id = :characterId")
    abstract fun getVisualsRelation(characterId: Uuid): Flow<CharacterVisualsRelation>

    open fun getCharacterVisualsFlow(characterId: Uuid): Flow<CharacterVisualsResult> {
        return getVisualsRelation(characterId).map { relation ->
            CharacterVisualsResult(
                avatar = relation.avatarEntity?.let { entity ->
                    CharacterImageDTO(
                        imagePublicId = entity.id,
                        url = entity.imageUrl ?: "",
                        thumbHash = entity.thumbHash
                    )
                },
                images = relation.imageEntities.map { entity ->
                    CharacterImageDTO(
                        imagePublicId = entity.id,
                        url = entity.imageUrl ?: "",
                        thumbHash = entity.thumbHash,
                        isPrivate = entity.isPrivate,
                        energyCost = entity.energyCost,
                        requiredLevel = entity.requiredLevel,
                        isUnlocked = entity.isUnlocked
                    )
                }
            )
        }
    }

    @Query("SELECT temperature FROM character_entity WHERE id = :characterId")
    abstract fun getCharacterTemperature(characterId: Uuid): Flow<Float?>

    @Transaction
    @Query("SELECT * FROM welcome_card_entity WHERE character_id = :characterId")
    abstract fun getWelcomeCardWithImagesFlow(characterId: Uuid): Flow<WelcomeCardWithImages?>

    @Query("SELECT COUNT(*) FROM character_entity WHERE can_edit = 1 AND sync_state != 'DELETE'")
    abstract fun getUserCharactersCountFlow(): Flow<Int>

    @Transaction
    @Query(
        """
        SELECT 
            c.id, 
            c.name, 
            c.unclaimed_energy AS unclaimedEnergy,
            c.total_earned_energy AS totalEarnedEnergy,
            a.id AS imagePublicId, 
            a.image_url AS url,
            a.thumb_hash AS thumbHash
        FROM character_entity c
        LEFT JOIN character_avatar_entity a 
            ON c.id = a.character_id 
            AND a.image_url IS NOT NULL
        WHERE c.can_edit = 1 AND c.sync_state != 'DELETE'
        ORDER BY c.updated_at DESC
    """
    )
    abstract fun getUserCharactersFlow(): Flow<List<CharacterWithShortDetails>>

    @Query("UPDATE character_entity SET unclaimed_energy = :unclaimedEnergy WHERE id = :characterId")
    abstract suspend fun updateCharacterUnclaimedEnergy(characterId: Uuid, unclaimedEnergy: Double)

    @Query("UPDATE character_entity SET relationship_level = :level, experience_points = :experience WHERE id = :characterId")
    abstract suspend fun updateCharacterRelationship(
        characterId: Uuid,
        level: Int,
        experience: Long
    )

    @Query("UPDATE character_entity SET sync_state = :syncAction WHERE id IN (:ids)")
    abstract suspend fun updateSyncAction(ids: List<Uuid>, syncAction: SyncAction)

    @Query("SELECT * FROM character_entity WHERE sync_state = :syncAction")
    abstract suspend fun getCharactersBySyncAction(syncAction: SyncAction): List<CharacterEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertRoadmap(items: List<CharacterEventRoadmapEntity>)

    @Update
    protected abstract suspend fun _update_insertRoadmap(items: List<CharacterEventRoadmapEntity>)

    @Transaction
    open suspend fun insertRoadmap(items: List<CharacterEventRoadmapEntity>) {
        _insert_insertRoadmap(items)
        _update_insertRoadmap(items)
    }

    @Query("DELETE FROM character_event_roadmap_entity WHERE character_id = :characterId")
    abstract suspend fun deleteRoadmap(characterId: Uuid)

    @Query("SELECT * FROM character_event_roadmap_entity WHERE character_id = :characterId ORDER BY level ASC")
    abstract fun getRoadmapFlow(characterId: Uuid): Flow<List<CharacterEventRoadmapEntity>>

    @Query("UPDATE character_image_entity SET is_unlocked = 1 WHERE character_id = :characterId AND required_level <= :newLevel")
    abstract suspend fun unlockImagesUpToLevel(characterId: Uuid, newLevel: Int)

    @Query(
        """
        UPDATE character_event_roadmap_entity 
        SET is_unlocked = 1,
            reward_photo_is_unlocked = CASE WHEN reward_photo_required_level <= :newLevel THEN 1 ELSE reward_photo_is_unlocked END
        WHERE character_id = :characterId AND level <= :newLevel
        """
    )
    abstract suspend fun unlockRoadmapUpToLevel(characterId: Uuid, newLevel: Int)
}
