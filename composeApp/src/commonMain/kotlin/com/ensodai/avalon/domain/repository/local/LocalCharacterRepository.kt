package com.ensodai.avalon.domain.repository.local

import com.ensodai.avalon.data.db.dto.CharacterAvatarDto
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.CharacterWithShortDetails
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.data.db.models.character.CharacterDetailsWithConfigs
import com.ensodai.avalon.data.db.models.character.CharacterEditData
import com.ensodai.avalon.data.db.models.character.CharacterEntity
import com.ensodai.avalon.data.db.models.character.CharacterProfileTuple
import com.ensodai.avalon.data.db.models.character.CharacterVisualsResult
import com.ensodai.avalon.data.db.models.character.DescriptionPrivacyTuple
import com.ensodai.avalon.data.db.models.character.WelcomeCardWithImages
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import com.ensodai.shared.avalon.character.models.CharacterEventRoadmapDto
import com.ensodai.avalon.domain.models.SaveCharacterProfileDto
import com.ensodai.avalon.domain.models.UpdateCharacterProfile
import com.ensodai.avalon.domain.models.message.ChatHeaderInfo
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface LocalCharacterRepository {

    suspend fun upsertCharacter(
        characterId: Uuid,
        ownerNick: String,
        characterAvatar: CharacterAvatarDto?,
        characterImages: List<CharacterImageDTO>?,
        saveCharacterProfileDto: SaveCharacterProfileDto,
        welcomeCardImages: List<WelcomeCardImageDto>?
    )

    suspend fun deleteCharacters(characters: List<Uuid>)

    suspend fun updateChatConfig(saveCharacterProfile: UpdateCharacterProfile)

    suspend fun updateLastTimeUpdateSession(
        sessionId: Uuid,
        timestamp: Instant
    )

    suspend fun deleteCharactersByIds(ids: List<Uuid>)

    suspend fun deleteCharacterById(id: Uuid)

    suspend fun deleteChatVariant(serverId: Uuid)

    suspend fun updateChatVariantName(response: ChatVariantResponse)

    suspend fun updateChatVariantImage(response: ChatVariantResponse)

    suspend fun getCharacterWithDetailsFlow(characterId: Uuid): Flow<CharacterDetailsWithConfigs>

    fun getChatDetailsFlow(characterId: Uuid): Flow<ChatHeaderInfo?>

    suspend fun characterExists(characterId: Uuid): Boolean

    suspend fun createStubCharacter(id: Uuid)

    fun getCharacterProfileFlow(characterId: Uuid): Flow<CharacterProfileTuple?>

    suspend fun getAllCharacterIds(): List<Uuid>

    suspend fun getCharacterEditData(characterId: Uuid): Flow<CharacterEditData?>

    fun getCharacterFullDescriptionFlow(characterId: Uuid): Flow<DescriptionPrivacyTuple?>

    fun getCharacterImagesFlow(characterId: Uuid): Flow<CharacterVisualsResult>

    fun getCharacterTemperatureFlow(characterId: Uuid): Flow<Float?>

    fun getCharacterWelcomeCardUseCase(characterId: Uuid): Flow<WelcomeCardWithImages?>

    fun getUserCharactersCountFlow(): Flow<Int>

    fun getUserCharactersPagingData(): Flow<List<CharacterWithShortDetails>>

    suspend fun updateSyncAction(ids: List<Uuid>, syncAction: SyncAction)

    suspend fun getCharactersBySyncAction(syncAction: SyncAction): List<CharacterEntity>

    suspend fun hardDeleteCharacters(ids: List<Uuid>)

    suspend fun getCharactersByIds(ids: List<Uuid>): List<CharacterEntity>

    suspend fun getCharacterNameAndAvatar(characterId: Uuid): CharacterNameAndAvatar?

    suspend fun updateCharacterUnclaimedEnergy(characterId: Uuid, unclaimedEnergy: Double)

    suspend fun updateCharacterRelationship(characterId: Uuid, level: Int, experience: Long)

    fun getCharacterRoadmapFlow(characterId: Uuid): Flow<List<CharacterEventRoadmapDto>>

    suspend fun saveCharacterRoadmap(characterId: Uuid, roadmap: List<CharacterEventRoadmapDto>)

    suspend fun upsertCharactersBulk(characters: List<SaveCharacterBulkData>)
}

data class SaveCharacterBulkData(
    val characterId: Uuid,
    val ownerNick: String,
    val characterAvatar: CharacterAvatarDto?,
    val characterImages: List<CharacterImageDTO>?,
    val saveCharacterProfileDto: SaveCharacterProfileDto,
    val welcomeCardImages: List<WelcomeCardImageDto>?
)

data class CharacterNameAndAvatar(
    val name: String,
    val avatarUrl: String?
)
