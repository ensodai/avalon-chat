package com.ensodai.avalon.data.repository.local

import com.ensodai.avalon.data.repository.local.base.BaseRepository
import com.ensodai.avalon.data.repository.local.base.DatabaseExecutor


import com.ensodai.avalon.data.db.AppDataBase
import com.ensodai.avalon.data.db.dao.CacheDao
import com.ensodai.avalon.data.db.dao.CharacterDao
import com.ensodai.avalon.data.db.dao.ChatHistoryDao
import com.ensodai.avalon.data.db.dao.ChatVariantDao
import com.ensodai.avalon.data.db.dao.UserDao
import com.ensodai.avalon.data.db.dto.CharacterAvatarDto
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.CharacterWithShortDetails
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.data.db.models.character.CharacterAvatarEntity
import com.ensodai.avalon.data.db.models.character.CharacterDetailsWithConfigs
import com.ensodai.avalon.data.db.models.character.CharacterEditData
import com.ensodai.avalon.data.db.models.character.CharacterEntity
import com.ensodai.avalon.data.db.models.character.CharacterImageEntity
import com.ensodai.avalon.data.db.models.character.CharacterProfileTuple
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.avalon.data.db.models.character.CharacterVisualsResult
import com.ensodai.avalon.data.db.models.character.DescriptionPrivacyTuple
import com.ensodai.avalon.data.db.models.character.WelcomeCardEntity
import com.ensodai.avalon.data.db.models.character.WelcomeCardImageEntity
import com.ensodai.avalon.data.db.models.character.WelcomeCardWithImages
import com.ensodai.avalon.data.db.models.character.CharacterEventRoadmapEntity
import com.ensodai.shared.avalon.character.models.CharacterEventRoadmapDto
import com.ensodai.shared.avalon.character.models.CharacterImageResponse
import com.ensodai.avalon.data.db.models.chat.ChatVariantImageEntity
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import com.ensodai.avalon.domain.models.SaveCharacterProfileDto
import com.ensodai.avalon.domain.models.UpdateCharacterProfile
import com.ensodai.avalon.domain.models.chat.WelcomeColors
import com.ensodai.avalon.domain.models.chat.WelcomeInfo
import com.ensodai.avalon.domain.models.message.ChatHeaderInfo
import com.ensodai.avalon.domain.repository.local.CharacterNameAndAvatar
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.getUTCTimeNow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.time.Instant
import kotlin.uuid.Uuid

class LocalCharacterRepositoryImpl(
    dbExecutor: DatabaseExecutor,
    private val characterDao: CharacterDao,
    private val appDatabase: AppDataBase,
    private val datastoreManager: DatastoreManager,
    private val userDao: UserDao,
    private val chatHistoryDao: ChatHistoryDao,
    private val chatVariantDao: ChatVariantDao,
    private val cacheDao: CacheDao,
    private val logger: AppLogger
) : BaseRepository(dbExecutor), LocalCharacterRepository {

    override suspend fun upsertCharacter(
        characterId: Uuid,
        ownerNick: String,
        characterAvatar: CharacterAvatarDto?,
        characterImages: List<CharacterImageDTO>?,
        saveCharacterProfileDto: SaveCharacterProfileDto,
        welcomeCardImages: List<WelcomeCardImageDto>?
    ) {
        return dbWrite {
            logger.d("LocalCharacterRepositoryImpl", "upserting character: $characterId")
                val characterEntity = CharacterEntity(
                    id = characterId,
                    ownerNick = ownerNick,
                    name = saveCharacterProfileDto.characterName,
                    isPublicFullDescription = saveCharacterProfileDto.isPublicFullDescription,
                    visibility = saveCharacterProfileDto.visibility,
                    shortDescription = saveCharacterProfileDto.shortDescription,
                    fullDescription = saveCharacterProfileDto.fullDescription,
                    firstMessage = saveCharacterProfileDto.firstMessage,
                    canEdit = saveCharacterProfileDto.userCanEditCharacter,
                    updatedAt = saveCharacterProfileDto.updatedAt,
                    temperature = saveCharacterProfileDto.temperature,
                    unclaimedEnergy = saveCharacterProfileDto.unclaimedEnergy,
                    totalEarnedEnergy = saveCharacterProfileDto.totalEarnedEnergy,
                    isOutreachEnabled = saveCharacterProfileDto.isOutreachEnabled,
                    voice = saveCharacterProfileDto.voice,
                    relationshipLevel = saveCharacterProfileDto.relationshipLevel,
                    experiencePoints = saveCharacterProfileDto.experiencePoints,
                    eventsCount = saveCharacterProfileDto.eventsCount,
                    interactionFormat = saveCharacterProfileDto.interactionFormat,
                    scenario = saveCharacterProfileDto.scenario,
                    mesExample = saveCharacterProfileDto.mesExample
                )
                characterDao.upsertCharacter(characterEntity)

                characterDao.deleteAvatarByCharId(characterId)

                if (characterAvatar != null) {
                    characterDao.insertCharacterAvatar(
                        CharacterAvatarEntity(
                            id = characterAvatar.imagePublicId,
                            characterId = characterId,
                            imageUrl = characterAvatar.url,
                            thumbHash = characterAvatar.thumbHash
                        )
                    )
                }

                characterDao.deleteImagesByCharId(characterId)

                if (!characterImages.isNullOrEmpty()) {
                    val imageEntities = characterImages.map { image ->
                        CharacterImageEntity(
                            id = image.imagePublicId,
                            characterId = characterId,
                            imageUrl = image.url,
                            thumbHash = image.thumbHash,
                            isPrivate = image.isPrivate,
                            energyCost = image.energyCost,
                            requiredLevel = image.requiredLevel,
                            isUnlocked = image.isUnlocked
                        )
                    }
                    characterDao.insertCharacterImages(imageEntities)
                }

                characterDao.deleteWelcomeCardByCharId(characterId)
                characterDao.deleteWelcomeCardImagesByCharId(characterId)

                if (saveCharacterProfileDto.welcomeCard != null) {
                    val welcomeCard = WelcomeCardEntity(
                        id = saveCharacterProfileDto.welcomeCard.id,
                        characterId = characterId,
                        description = saveCharacterProfileDto.welcomeCard.welcomeCardDescription,
                        colorStart = saveCharacterProfileDto.welcomeCard.startColor,
                        colorEnd = saveCharacterProfileDto.welcomeCard.endColor,
                        colorLight = saveCharacterProfileDto.welcomeCard.colorLight,
                        isEnableLight = saveCharacterProfileDto.welcomeCard.isEnableLight,
                    )
                    characterDao.insertWelcomeCard(welcomeCard)

                    if (!welcomeCardImages.isNullOrEmpty()) {
                        val wcImageEntities = welcomeCardImages.map { image ->
                            WelcomeCardImageEntity(
                                id = image.imagePublicId,
                                characterId = characterId,
                                imageUrl = image.url,
                                thumbHash = image.thumbHash
                            )
                        }
                        characterDao.insertWelcomeCardImages(wcImageEntities)
                    }
                }
        }
    }

    override suspend fun deleteCharacters(characters: List<Uuid>) {
        if (characters.isNotEmpty()) {
            dbWrite { characterDao.deleteCharactersByIds(characters) }
        }
    }


    override suspend fun updateChatConfig(saveCharacterProfile: UpdateCharacterProfile) {

    }

    override suspend fun updateLastTimeUpdateSession(
        sessionId: Uuid,
        timestamp: Instant
    ) {

    }

    override suspend fun deleteCharactersByIds(ids: List<Uuid>) {
        dbWrite { characterDao.deleteCharactersByIds(ids) }
    }

    override suspend fun deleteCharacterById(id: Uuid) {
        dbWrite { characterDao.deleteCharacterById(id) }
    }


    override suspend fun deleteChatVariant(serverId: Uuid) {
    }

    override suspend fun updateChatVariantName(response: ChatVariantResponse) {
        dbWrite {
            chatVariantDao.updateVariantName(
                id = response.id,
                name = response.name,
                updatedAt = response.updatedAt
            )
        }
    }

    override suspend fun updateChatVariantImage(response: ChatVariantResponse) {
        val image = response.image
        val imageEntity = if (image != null) {
            ChatVariantImageEntity(
                id = image.id,
                chatVariantId = response.id,
                imageUrl = image.imageUrl,
                thumbHash = image.thumbHash
            )
        } else {
            null
        }

        dbWrite {
            chatVariantDao.updateVariantImageTransaction(
                variantId = response.id,
                newImage = imageEntity
            )
        }
    }

    override suspend fun getCharacterWithDetailsFlow(characterId: Uuid): Flow<CharacterDetailsWithConfigs> {
        val characterFlow = dbObserve { characterDao.getCharacterWithDetailsFlow(characterId) }
        val configsFlow = dbObserve { cacheDao.getAvailableLlmConfigsFlow() }

        return characterFlow.combine(configsFlow) { characterTuple, configs ->
            val selectedCodeName = characterTuple?.chatSession?.selectedLlmConfigCodeName
            val foundConfig = configs.find { it.codeName == selectedCodeName }

            CharacterDetailsWithConfigs(
                characterDetails = characterTuple,
                availableConfigs = configs,
                selectedLlmConfig = foundConfig
            )
        }
    }

    override fun getChatDetailsFlow(characterId: Uuid): Flow<ChatHeaderInfo?> {
        return cachedObserve("getChatDetailsFlow_${characterId}") { characterDao.getChatDetailsFlow(characterId) }
            .map { tuple ->
                if (tuple == null) return@map null

                val welcomeInfo = tuple.welcomeCard?.let { card ->
                    if (card.description.isNullOrBlank() && tuple.welcomeImages.isEmpty()) {
                        null
                    } else {
                        WelcomeInfo(
                            description = card.description,
                            imageUrls = tuple.welcomeImages.map {
                                WelcomeCardImageDto(
                                    imagePublicId = it.id,
                                    url = it.imageUrl,
                                    thumbHash = it.thumbHash
                                )
                            },
                            colors = WelcomeColors(
                                start = card.colorStart,
                                end = card.colorEnd,
                                light = card.colorLight,
                                isLightEnabled = card.isEnableLight
                            )
                        )
                    }
                }

                ChatHeaderInfo(
                    firstMessage = tuple.firstMessage,
                    welcomeInfo = welcomeInfo
                )
            }
            .distinctUntilChanged()
    }

    override suspend fun characterExists(characterId: Uuid): Boolean {
        return dbRead { characterDao.characterExists(characterId) }
    }

    override suspend fun createStubCharacter(id: Uuid) {
        val characterEntity = CharacterEntity(
            id = id,
            ownerNick = "",
            name = "",
            isPublicFullDescription = false,
            visibility = CharacterVisibility.PRIVATE,
            shortDescription = "",
            fullDescription = null,
            firstMessage = null,
            canEdit = false,
            updatedAt = getUTCTimeNow(),
            temperature = 0.7f,
            interactionFormat = InteractionFormat.LITERARY_RPG
        )
        dbWrite { characterDao.upsertCharacter(characterEntity) }
    }

    override fun getCharacterProfileFlow(characterId: Uuid): Flow<CharacterProfileTuple?> {
        return cachedObserve("getCharacterProfileFlow_${characterId}") { characterDao.getCharacterProfileFlow(characterId) }
    }

    override suspend fun getAllCharacterIds(): List<Uuid> {
        return dbRead { characterDao.getAllCharacterIds() }
    }

    override suspend fun getCharacterEditData(characterId: Uuid): Flow<CharacterEditData?> {
        return dbObserve { characterDao.getCharacterEditDataFlow(characterId) }
    }

    override fun getCharacterFullDescriptionFlow(characterId: Uuid): Flow<DescriptionPrivacyTuple?> {
        return cachedObserve("getCharacterFullDescriptionFlow_${characterId}") { characterDao.getCharacterFullDescriptionFlow(characterId) }
    }

    override fun getCharacterImagesFlow(characterId: Uuid): Flow<CharacterVisualsResult> {
        return cachedObserve("getCharacterImagesFlow_${characterId}") { characterDao.getCharacterVisualsFlow(characterId) }
    }

    override fun getCharacterTemperatureFlow(characterId: Uuid): Flow<Float?> {
        return cachedObserve("getCharacterTemperatureFlow_${characterId}") { characterDao.getCharacterTemperature(characterId) }
    }

    override fun getCharacterWelcomeCardUseCase(characterId: Uuid): Flow<WelcomeCardWithImages?> {
        return cachedObserve("getCharacterWelcomeCardUseCase_${characterId}") { characterDao.getWelcomeCardWithImagesFlow(characterId) }
    }

    override fun getUserCharactersCountFlow(): Flow<Int> {
        return dbObserve { characterDao.getUserCharactersCountFlow() }
    }

    override fun getUserCharactersPagingData(): Flow<List<CharacterWithShortDetails>> {
        return dbObserve { characterDao.getUserCharactersFlow() }
    }

    override suspend fun updateSyncAction(ids: List<Uuid>, syncAction: SyncAction) {
        dbWrite { characterDao.updateSyncAction(ids, syncAction) }
    }

    override suspend fun getCharactersBySyncAction(syncAction: SyncAction): List<CharacterEntity> {
        return dbRead { characterDao.getCharactersBySyncAction(syncAction) }
    }

    override suspend fun hardDeleteCharacters(ids: List<Uuid>) {
        dbWrite { characterDao.hardDeleteCharacters(ids) }
    }

    override suspend fun getCharactersByIds(ids: List<Uuid>): List<CharacterEntity> {
        return dbRead { characterDao.getCharactersByIds(ids) }
    }

    override suspend fun getCharacterNameAndAvatar(characterId: Uuid): CharacterNameAndAvatar? {
        return dbRead {
            val character = characterDao.getCharacterById(characterId) ?: return@dbRead null
            val avatar = characterDao.getAvatarByCharacterId(characterId)
            CharacterNameAndAvatar(
                name = character.name,
                avatarUrl = avatar?.imageUrl
            )
        }
    }

    override suspend fun updateCharacterUnclaimedEnergy(characterId: Uuid, unclaimedEnergy: Double) {
        dbWrite { characterDao.updateCharacterUnclaimedEnergy(characterId, unclaimedEnergy) }
    }

    override suspend fun updateCharacterRelationship(characterId: Uuid, level: Int, experience: Long) {
        dbWrite {
            characterDao.updateCharacterRelationship(characterId, level, experience)
            characterDao.unlockImagesUpToLevel(characterId, level)
            characterDao.unlockRoadmapUpToLevel(characterId, level)
        }
    }

    override fun getCharacterRoadmapFlow(characterId: Uuid): Flow<List<CharacterEventRoadmapDto>> {
        return dbObserve { characterDao.getRoadmapFlow(characterId) }.map { entities ->
            entities.map { entity ->
                CharacterEventRoadmapDto(
                    level = entity.level,
                    hasReward = entity.hasReward,
                    isUnlocked = entity.isUnlocked,
                    triggerScenario = entity.triggerScenario,
                    rewardPhoto = entity.rewardPhotoId?.let { photoId ->
                        CharacterImageResponse(
                            id = photoId,
                            imageUrl = entity.rewardPhotoUrl ?: "",
                            thumbHash = entity.rewardPhotoThumbHash,
                            isPrivate = entity.rewardPhotoIsPrivate,
                            energyCost = entity.rewardPhotoEnergyCost,
                            requiredLevel = entity.rewardPhotoRequiredLevel,
                            isUnlocked = entity.rewardPhotoIsUnlocked
                        )
                    }
                )
            }
        }
    }

    override suspend fun saveCharacterRoadmap(characterId: Uuid, roadmap: List<CharacterEventRoadmapDto>) {
        dbWrite {
            val character = characterDao.getCharacterById(characterId)
                val currentLevel = character?.relationshipLevel ?: 1

                characterDao.deleteRoadmap(characterId)
                val entities = roadmap.map { dto ->
                    val isUnlocked = dto.isUnlocked || dto.level <= currentLevel
                    val rewardPhotoIsUnlocked = dto.rewardPhoto?.isUnlocked ?: false ||
                            (dto.rewardPhoto?.requiredLevel?.let { it <= currentLevel } ?: false)

                    CharacterEventRoadmapEntity(
                        characterId = characterId,
                        level = dto.level,
                        hasReward = dto.hasReward,
                        isUnlocked = isUnlocked,
                        triggerScenario = dto.triggerScenario,
                        rewardPhotoId = dto.rewardPhoto?.id,
                        rewardPhotoUrl = dto.rewardPhoto?.imageUrl,
                        rewardPhotoThumbHash = dto.rewardPhoto?.thumbHash,
                        rewardPhotoIsPrivate = dto.rewardPhoto?.isPrivate ?: false,
                        rewardPhotoEnergyCost = dto.rewardPhoto?.energyCost ?: 0,
                        rewardPhotoRequiredLevel = dto.rewardPhoto?.requiredLevel,
                        rewardPhotoIsUnlocked = rewardPhotoIsUnlocked
                    )
                }
            characterDao.insertRoadmap(entities)
        }
    }

    override suspend fun upsertCharactersBulk(characters: List<com.ensodai.avalon.domain.repository.local.SaveCharacterBulkData>) {
        if (characters.isEmpty()) return

        dbWrite {
            val characterEntities = mutableListOf<CharacterEntity>()
                val characterAvatars = mutableListOf<com.ensodai.avalon.data.db.models.character.CharacterAvatarEntity>()
                val characterImages = mutableListOf<com.ensodai.avalon.data.db.models.character.CharacterImageEntity>()
                val welcomeCards = mutableListOf<com.ensodai.avalon.data.db.models.character.WelcomeCardEntity>()
                val welcomeCardImages = mutableListOf<com.ensodai.avalon.data.db.models.character.WelcomeCardImageEntity>()

                val characterIds = characters.map { it.characterId }

                characters.forEach { data ->
                    characterEntities.add(
                        CharacterEntity(
                            id = data.characterId,
                            ownerNick = data.ownerNick,
                            name = data.saveCharacterProfileDto.characterName,
                            isPublicFullDescription = data.saveCharacterProfileDto.isPublicFullDescription,
                            visibility = data.saveCharacterProfileDto.visibility,
                            shortDescription = data.saveCharacterProfileDto.shortDescription,
                            fullDescription = data.saveCharacterProfileDto.fullDescription,
                            firstMessage = data.saveCharacterProfileDto.firstMessage,
                            canEdit = data.saveCharacterProfileDto.userCanEditCharacter,
                            updatedAt = data.saveCharacterProfileDto.updatedAt,
                            temperature = data.saveCharacterProfileDto.temperature,
                            unclaimedEnergy = data.saveCharacterProfileDto.unclaimedEnergy,
                            totalEarnedEnergy = data.saveCharacterProfileDto.totalEarnedEnergy,
                            isOutreachEnabled = data.saveCharacterProfileDto.isOutreachEnabled,
                            voice = data.saveCharacterProfileDto.voice,
                            relationshipLevel = data.saveCharacterProfileDto.relationshipLevel,
                            experiencePoints = data.saveCharacterProfileDto.experiencePoints,
                            eventsCount = data.saveCharacterProfileDto.eventsCount,
                            interactionFormat = data.saveCharacterProfileDto.interactionFormat,
                            scenario = data.saveCharacterProfileDto.scenario,
                            mesExample = data.saveCharacterProfileDto.mesExample
                        )
                    )

                    data.characterAvatar?.let { avatar ->
                        characterAvatars.add(
                            com.ensodai.avalon.data.db.models.character.CharacterAvatarEntity(
                                id = avatar.imagePublicId,
                                characterId = data.characterId,
                                imageUrl = avatar.url,
                                thumbHash = avatar.thumbHash
                            )
                        )
                    }

                    data.characterImages?.forEach { image ->
                        characterImages.add(
                            com.ensodai.avalon.data.db.models.character.CharacterImageEntity(
                                id = image.imagePublicId,
                                characterId = data.characterId,
                                imageUrl = image.url,
                                thumbHash = image.thumbHash,
                                isPrivate = image.isPrivate,
                                energyCost = image.energyCost,
                                requiredLevel = image.requiredLevel,
                                isUnlocked = image.isUnlocked
                            )
                        )
                    }

                    data.saveCharacterProfileDto.welcomeCard?.let { card ->
                        welcomeCards.add(
                            com.ensodai.avalon.data.db.models.character.WelcomeCardEntity(
                                id = card.id,
                                characterId = data.characterId,
                                description = card.welcomeCardDescription,
                                colorStart = card.startColor,
                                colorEnd = card.endColor,
                                colorLight = card.colorLight,
                                isEnableLight = card.isEnableLight,
                            )
                        )
                    }

                    data.welcomeCardImages?.forEach { image ->
                        welcomeCardImages.add(
                            com.ensodai.avalon.data.db.models.character.WelcomeCardImageEntity(
                                id = image.imagePublicId,
                                characterId = data.characterId,
                                imageUrl = image.url,
                                thumbHash = image.thumbHash
                            )
                        )
                    }
                }

                characterDao.upsertCharacters(characterEntities)

                characterDao.deleteAvatarsByCharIds(characterIds)
                if (characterAvatars.isNotEmpty()) {
                    characterDao.insertCharacterAvatars(characterAvatars)
                }

                characterDao.deleteImagesByCharIds(characterIds)
                if (characterImages.isNotEmpty()) {
                    characterDao.insertCharacterImages(characterImages)
                }

                characterDao.deleteWelcomeCardsByCharIds(characterIds)
                if (welcomeCards.isNotEmpty()) {
                    characterDao.insertWelcomeCards(welcomeCards)
                }

                characterDao.deleteWelcomeCardImagesByCharIds(characterIds)
            if (welcomeCardImages.isNotEmpty()) {
                characterDao.insertWelcomeCardImages(welcomeCardImages)
            }
        }
    }
}
