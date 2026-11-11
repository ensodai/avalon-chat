package app.data.repository

import app.core.updateAndGetTimestamp
import app.data.db.tables.aiCharacter.*
import app.data.db.tables.chat.*
import app.data.db.tables.chatSessions.ChatSession
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariant
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.images.*
import app.data.db.tables.users.AppUsers
import app.data.db.tables.users.UserCharacterRelationships
import app.data.network.models.request.UpdateCharacterRequest
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.chat.models.ImagePayload
import com.ensodai.shared.avalon.character.models.*
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import app.domain.models.DeleteVariantResult
import app.utils.toUuidOrNull
import app.utils.dbQuery
import app.utils.toServerUrl
import app.cache.RedisService
import app.utils.PopularityCalculator
import io.ktor.server.plugins.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import kotlin.math.ceil
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class CharacterRepository {

    /**
     * Выполняет полнофункциональный поиск персонажей с пагинацией.
     */
    suspend fun searchCharacters(query: String, page: Int, limit: Int = 10): PaginatedResponse<CharacterPreviewResponse> {
        return dbQuery {
            val offset = (page - 1) * limit

            val avatarAlias = CharacterAvatars.imagePath.min().alias("avatar_image")
            val avatarSubquery = CharacterAvatars
                .select(CharacterAvatars.characterPublicId, avatarAlias)
                .groupBy(CharacterAvatars.characterPublicId)
                .alias("avatar_subquery")

            val eventsCountAlias = CharacterEvents.id.count().alias("events_count")
            val eventsSubquery = CharacterEvents
                .select(CharacterEvents.characterId, eventsCountAlias)
                .groupBy(CharacterEvents.characterId)
                .alias("events_subquery")

            val userNickname = AppUsers.nickName.alias("user_nickname")

            var finalCondition: Op<Boolean> = (AICharacters.visibility eq CharacterVisibility.PUBLIC) and
                    (AICharacters.isBanned eq false) and
                    (AppUsers.deletedAt.isNull()) and
                    (AppUsers.isBanned eq false) and
                    (AppUsers.isShadowBanned eq false)

            if (query.startsWith("@") && query.length > 1) {
                val nicknameQuery = "%${query.substring(1).trim().lowercase()}%"
                finalCondition = finalCondition and (AppUsers.nickName.lowerCase() like nicknameQuery)
            } else if (query.isNotBlank()) {
                val characterNameQuery = "%${query.trim().lowercase()}%"
                finalCondition = finalCondition and (AICharacters.characterName.lowerCase() like characterNameQuery)
            }

            val baseJoin = AICharacters
                .join(AICharacterStats, JoinType.INNER, onColumn = AICharacters.id, otherColumn = AICharacterStats.characterId)
                .join(AppUsers, JoinType.INNER, onColumn = AICharacters.owner, otherColumn = AppUsers.id)

            val listJoin = baseJoin
                .join(
                    avatarSubquery,
                    JoinType.LEFT,
                    onColumn = AICharacters.id,
                    otherColumn = avatarSubquery[CharacterAvatars.characterPublicId]
                )
                .join(
                    eventsSubquery,
                    JoinType.LEFT,
                    onColumn = AICharacters.id,
                    otherColumn = eventsSubquery[CharacterEvents.characterId]
                )

            val columnsToSelect = listOf(
                AICharacters.id,
                userNickname,
                avatarSubquery[avatarAlias],
                AICharacters.characterName,
                AICharacters.shortDescription,
                AICharacterStats.messageCount,
                AICharacters.updatedAt,
                AICharacters.visibility,
                eventsSubquery[eventsCountAlias]
            )

            val itemsList = listJoin
                .select(columnsToSelect)
                .where { finalCondition }
                .orderBy(AICharacterStats.messageCount to SortOrder.DESC)
                .limit(limit)
                .offset(offset.toLong())
                .map { row ->
                    CharacterPreviewResponse(
                        id = row[AICharacters.id].value,
                        ownerNick = row[userNickname] ?: "Unknown",
                        avatarUrl = row[avatarSubquery[avatarAlias]]?.toServerUrl(),
                        characterName = row[AICharacters.characterName],
                        shortDescription = row[AICharacters.shortDescription],
                        messageCountRating = row[AICharacterStats.messageCount].toInt(),
                        visibility = row[AICharacters.visibility],
                        updatedAt = row[AICharacters.updatedAt],
                        eventsCount = (row.getOrNull(eventsSubquery[eventsCountAlias]) ?: 0L).toInt()
                    )
                }

            val totalItems = baseJoin
                .select(AICharacters.id)
                .where { finalCondition }
                .count()

            val totalPages = if (limit > 0) ceil(totalItems.toDouble() / limit).toInt() else 0

            PaginatedResponse(
                page = page,
                totalPages = totalPages,
                items = itemsList,
                limit = limit
            )
        }
    }


    private fun updateCharacter(
        characterRequest: UpdateCharacterRequest,
        publicId: Uuid,
    ): Instant? {
        val newTimestamp = AICharacters.updateAndGetTimestamp(publicId) { statement ->
            characterRequest.characterName?.let { statement[AICharacters.characterName] = it }
            characterRequest.fullDescription?.let { statement[AICharacters.fullDescription] = it }
            characterRequest.shortDescription?.let { statement[AICharacters.shortDescription] = it }
            characterRequest.temperature?.let { statement[AICharacters.temperature] = it }
            characterRequest.harassment?.let { statement[AICharacters.harassment] = it }
            characterRequest.hate?.let { statement[AICharacters.hate] = it }
            characterRequest.sexuallyExplicit?.let { statement[AICharacters.sexuallyExplicit] = it }
            characterRequest.dangerousContent?.let { statement[AICharacters.dangerousContent] = it }
            characterRequest.visibility?.let { statement[AICharacters.visibility] = it }
            characterRequest.isPublicFullDescription?.let { statement[AICharacters.isPublicFullDescription] = it }
            characterRequest.category?.let { statement[AICharacters.category] = it }
            characterRequest.firstMessage?.let { statement[AICharacters.firstMessage] = it }
        }

        newTimestamp?.let {
            characterRequest.welcomeCard?.let { welcomeCardData ->
                val existingCard = CharacterWelcomeCardEntity.findById(publicId)

                existingCard?.apply {
                    welcomeCardDescription = welcomeCardData.welcomeCardDescription
                    cardColorStart = welcomeCardData.cardColorStart
                    cardColorEnd = welcomeCardData.cardColorEnd
                    cardColorLight = welcomeCardData.cardColorLight
                    isEnableLight = welcomeCardData.isEnableLight
                }
                    ?: CharacterWelcomeCardEntity.new(publicId) {
                        welcomeCardDescription = welcomeCardData.welcomeCardDescription
                        cardColorStart = welcomeCardData.cardColorStart
                        cardColorEnd = welcomeCardData.cardColorEnd
                        cardColorLight = welcomeCardData.cardColorLight
                        isEnableLight = welcomeCardData.isEnableLight
                    }
            }
        }
        return newTimestamp
    }

    suspend fun getCharacterImages(characterPublicId: Uuid): List<CharacterImage> {
        return dbQuery {
            CharacterImage.find { CharacterImages.characterPublicId eq characterPublicId }.toList()
        }
    }

    suspend fun getWelcomeCardImages(characterPublicId: Uuid): List<WelcomeCardImage> {
        return dbQuery {
            WelcomeCardImage
                .find { WelcomeCardImages.characterPublicId eq characterPublicId }
                .orderBy(WelcomeCardImages.orderIndex to SortOrder.ASC)
                .toList()
        }
    }

    suspend fun getCharacterAvatar(characterPublicId: Uuid): CharacterAvatar? {
        return dbQuery {
            CharacterAvatar.find { CharacterAvatars.characterPublicId eq characterPublicId }.singleOrNull()
        }
    }

    private fun updateCharacterAvatar(
        characterPublicId: Uuid,
        imageId: Uuid,
        deleteImagesIds: List<Uuid>
    ) {
        val oldAvatar = CharacterAvatar.find {
            CharacterAvatars.characterPublicId eq characterPublicId
        }.singleOrNull()

        oldAvatar?.let { avatar ->
            if (deleteImagesIds.contains(avatar.id.value)) {
                avatar.delete()
            } else {
                CharacterImage.new {
                    this.characterPublicId = avatar.characterPublicId
                    this.imagePath = avatar.imagePath
                }
                avatar.delete()
            }
        }

        val newAvatarImage = CharacterImage.findById(imageId)
        if (newAvatarImage == null) return

        CharacterAvatar.new {
            this.characterPublicId = EntityID(characterPublicId, AICharacters)
            this.imagePath = newAvatarImage.imagePath
        }
        oldAvatar?.deletedAt = Clock.System.now()
    }

    suspend fun performCharacterUpdate(
        characterPublicId: Uuid,
        characterData: UpdateCharacterRequest?,
        isUserShadowBanned: Boolean,
        newAvatarFromGalleryId: Uuid?,
        newAvatarFromFilePath: String?,
        newImagePaths: List<String>,
        newWelcomeCardPaths: List<String>
    ): Instant? {
        return dbQuery {
            val oldCategory = AICharacters
                .select(AICharacters.category)
                .where { AICharacters.id eq characterPublicId }
                .singleOrNull()?.get(AICharacters.category)

            var finalUpdatedAt: Instant? = null
            var childEntitiesChanged = false

            characterData?.let { data ->
                val finalData = if (isUserShadowBanned) data.copy(visibility = CharacterVisibility.PRIVATE) else data
                finalUpdatedAt = updateCharacter(
                    characterRequest = finalData,
                    publicId = characterPublicId,
                )
            }

            newAvatarFromGalleryId?.let { imageId ->
                updateCharacterAvatar(characterPublicId, imageId, emptyList())
                childEntitiesChanged = true
            }

            newAvatarFromFilePath?.let { path ->
                CharacterAvatar.find { CharacterAvatars.characterPublicId eq characterPublicId }.singleOrNull()
                    ?.delete()
                CharacterAvatar.new {
                    this.characterPublicId = EntityID(characterPublicId, AICharacters)
                    this.imagePath = path
                }
                childEntitiesChanged = true
            }

            if (newImagePaths.isNotEmpty()) {
                newImagePaths.forEach { path ->
                    CharacterImage.new {
                        this.characterPublicId = EntityID(characterPublicId, AICharacters)
                        this.imagePath = path
                    }
                }
                childEntitiesChanged = true
            }

            if (newWelcomeCardPaths.isNotEmpty()) {
                newWelcomeCardPaths.forEach { path ->
                    WelcomeCardImage.new {
                        this.characterPublicId = EntityID(characterPublicId, AICharacters)
                        this.imagePath = path
                    }
                }
                childEntitiesChanged = true
            }

            if (childEntitiesChanged && finalUpdatedAt == null) {
                val currentTime = Clock.System.now()
                AICharacters.update({ AICharacters.id eq characterPublicId }) {
                    it[updatedAt] = currentTime
                }
                finalUpdatedAt = currentTime
            }

            if (finalUpdatedAt != null) {
                oldCategory?.let { RedisService.invalidateCategoryCache(it) }
                val newCategory = AICharacters
                    .select(AICharacters.category)
                    .where { AICharacters.id eq characterPublicId }
                    .singleOrNull()?.get(AICharacters.category)
                if (newCategory != null && newCategory != oldCategory) {
                    RedisService.invalidateCategoryCache(newCategory)
                }
            }

            finalUpdatedAt
        }
    }

    suspend fun deleteImageRecordsAndGetPaths(imageIds: List<String>): List<String> {
        val imageUUIDs = imageIds.mapNotNull { it.toUuidOrNull() }
        if (imageUUIDs.isEmpty()) return emptyList()

        return dbQuery {
            val pathsToDelete = mutableListOf<String>()
            CharacterImage.find { CharacterImages.id inList imageUUIDs }.forEach { image ->
                pathsToDelete.add(image.imagePath)
                image.deletedAt = Clock.System.now()
            }
            CharacterAvatar.find { CharacterAvatars.id inList imageUUIDs }.forEach { avatar ->
                pathsToDelete.add(avatar.imagePath)
                avatar.deletedAt = Clock.System.now()
            }
            WelcomeCardImage.find { WelcomeCardImages.id inList imageUUIDs }.forEach { wcImage ->
                pathsToDelete.add(wcImage.imagePath)
                wcImage.deletedAt = Clock.System.now()
            }
            pathsToDelete
        }
    }

    suspend fun getCharacterById(characterPublicId: Uuid, userId: Uuid): CharacterResponse {
        return dbQuery {
            val charTable = AICharacters
            val relTable = UserCharacterRelationships
            val userTable = AppUsers
            val avatarTable = CharacterAvatars
            val cardTable = CharacterWelcomeCard

            val mainRow = charTable
                .join(userTable, JoinType.INNER, charTable.owner, userTable.id)
                .join(
                    otherTable = avatarTable,
                    joinType = JoinType.LEFT,
                    onColumn = charTable.id,
                    otherColumn = avatarTable.characterPublicId,
                    additionalConstraint = { avatarTable.deletedAt.isNull() }
                )
                .join(
                    otherTable = relTable,
                    joinType = JoinType.LEFT,
                    onColumn = charTable.id,
                    otherColumn = relTable.characterId,
                    additionalConstraint = { relTable.userId eq userId }
                )
                .join(
                    otherTable = cardTable,
                    joinType = JoinType.LEFT,
                    onColumn = charTable.id,
                    otherColumn = cardTable.character,
                    additionalConstraint = { cardTable.deletedAt.isNull() }
                )
                .selectAll()
                .where {
                    (charTable.id eq characterPublicId)
                }
                .firstOrNull()
                ?: throw NotFoundException("Персонаж с ID $characterPublicId не найден")

            val isDeleted = mainRow[charTable.deletedAt] != null
            val ownerId = mainRow[charTable.owner].value
            val isOwner = ownerId == userId
            val canViewContent = isOwner || !isDeleted

            val relationshipLevel = mainRow.getOrNull(relTable.relationshipLevel) ?: 1

            val unlockedImageIds = UnlockedCharacterImages
                .select(UnlockedCharacterImages.characterImageId)
                .where { UnlockedCharacterImages.userId eq userId }
                .map { it[UnlockedCharacterImages.characterImageId].value }
                .toSet()

            val characterImagesList = if (canViewContent) {
                CharacterImages
                    .select(
                        CharacterImages.id,
                        CharacterImages.imagePath,
                        CharacterImages.thumbHash,
                        CharacterImages.isPrivate,
                        CharacterImages.energyCost,
                        CharacterImages.requiredLevel
                    )
                    .where {
                        (CharacterImages.characterPublicId eq characterPublicId) and
                                (CharacterImages.deletedAt.isNull())
                    }
                    .orderBy(CharacterImages.orderIndex, SortOrder.ASC)
                    .map { row ->
                        val imageId = row[CharacterImages.id].value
                        val isPrivateImage = row[CharacterImages.isPrivate]
                        val requiredLvl = row[CharacterImages.requiredLevel]

                        val isUnlocked = when {
                            isOwner -> true
                            requiredLvl != null -> relationshipLevel >= requiredLvl
                            isPrivateImage -> unlockedImageIds.contains(imageId)
                            else -> true
                        }

                        CharacterImageResponse(
                            id = imageId,
                            imageUrl = if (isUnlocked) row[CharacterImages.imagePath].toServerUrl() else "",
                            thumbHash = row[CharacterImages.thumbHash],
                            isPrivate = isPrivateImage,
                            energyCost = row[CharacterImages.energyCost],
                            requiredLevel = requiredLvl,
                            isUnlocked = isUnlocked
                        )
                    }
            } else emptyList()

            val welcomeCardResult = if (canViewContent) mainRow.getOrNull(cardTable.id) else null
            val welcomeImagesList = if (welcomeCardResult != null) {
                WelcomeCardImages
                    .select(WelcomeCardImages.id, WelcomeCardImages.imagePath)
                    .where {
                        (WelcomeCardImages.characterPublicId eq characterPublicId) and
                                (WelcomeCardImages.deletedAt.isNull())
                    }
                    .orderBy(WelcomeCardImages.orderIndex, SortOrder.ASC)
                    .map { row ->
                        WelcomeCardImageResponse(
                            id = row[WelcomeCardImages.id].value,
                            imageUrl = row[WelcomeCardImages.imagePath].toServerUrl()
                        )
                    }
            } else emptyList()

            val isPublicDesc = mainRow[charTable.isPublicFullDescription]
            val fullDesc = if (canViewContent && (isOwner || isPublicDesc)) {
                mainRow[charTable.fullDescription]
            } else {
                null
            }

            val sharedMediaList = if (canViewContent) {
                MessageContents
                    .join(MessageContainers, JoinType.INNER, MessageContents.containerId, MessageContainers.id)
                    .join(MessageSlots, JoinType.INNER, MessageContainers.slotId, MessageSlots.id)
                    .join(ChatVariants, JoinType.INNER, MessageSlots.chatVariantId, ChatVariants.id)
                    .join(ChatSessions, JoinType.INNER, ChatVariants.chatSessionId, ChatSessions.id)
                    .select(MessageContents.content)
                    .where {
                        (ChatSessions.userId eq userId) and
                                (ChatSessions.characterId eq characterPublicId) and
                                (MessageContents.type eq AvalonMessageContentType.IMAGE) and
                                (MessageContents.deletedAt.isNull()) and
                                (MessageContainers.deletedAt.isNull()) and
                                (MessageSlots.deletedAt.isNull()) and
                                (ChatVariants.deletedAt.isNull()) and
                                (ChatSessions.deletedAt.isNull())
                    }
                    .orderBy(MessageContainers.createdAt to SortOrder.DESC)
                    .mapNotNull { row ->
                        val contentJson = row[MessageContents.content]
                        MessageContentCodec.decodeAs<ImagePayload>(contentJson)?.url?.toServerUrl()
                    }
            } else emptyList()

            val promptDetailsRow = if (canViewContent && (isOwner || isPublicDesc)) {
                AICharacterPromptDetails
                    .select(AICharacterPromptDetails.scenario, AICharacterPromptDetails.mesExample)
                    .where { AICharacterPromptDetails.characterId eq characterPublicId }
                    .singleOrNull()
            } else null
            val scenarioText = promptDetailsRow?.get(AICharacterPromptDetails.scenario)
            val mesExampleText = promptDetailsRow?.get(AICharacterPromptDetails.mesExample)

            CharacterResponse(
                id = mainRow[charTable.id].value,
                ownerNick = mainRow[userTable.nickName],
                characterName = mainRow[charTable.characterName],
                shortDescription = mainRow[charTable.shortDescription],
                fullDescription = fullDesc,
                isPublicFullDescription = isPublicDesc,
                visibility = mainRow[charTable.visibility],
                avatar = mainRow[avatarTable.id]?.let { id ->
                    if (!canViewContent) return@let null
                    CharacterAvatarResponse(
                        id = id.value,
                        imageUrl = mainRow[avatarTable.imagePath].toServerUrl()
                    )
                },
                images = characterImagesList,
                welcomeCard = if (welcomeCardResult != null) {
                    WelcomeCardResponse(
                        id = welcomeCardResult.value,
                        welcomeCardDescription = mainRow[cardTable.welcomeCardDescription],
                        welcomeCardImages = welcomeImagesList.takeIf { it.isNotEmpty() },
                        startColor = mainRow[cardTable.cardColorStart],
                        endColor = mainRow[cardTable.cardColorEnd],
                        cardColorLight = mainRow[cardTable.cardColorLight],
                        isEnableLight = mainRow[cardTable.isEnableLight]
                    )
                } else null,
                firstMessage = if (canViewContent) mainRow[charTable.firstMessage] else null,
                canEdit = isOwner,
                updatedAt = mainRow[charTable.updatedAt],
                temperature = mainRow[charTable.temperature],
                isDeleted = isDeleted,
                isOutreachEnabled = mainRow[charTable.isOutreachEnabled],
                relationshipLevel = mainRow.getOrNull(relTable.relationshipLevel) ?: 1,
                experiencePoints = mainRow.getOrNull(relTable.experiencePoints) ?: 0L,
                eventsCount = CharacterEvents.select(CharacterEvents.id).where { CharacterEvents.characterId eq characterPublicId }.count().toInt(),
                sharedMedia = sharedMediaList,
                scenario = scenarioText,
                mesExample = mesExampleText
            )
        }
    }

    suspend fun bulkDeleteCharactersAndGetImagePaths(
        characterPublicIds: List<Uuid>,
        userId: Uuid
    ): List<String> {
        if (characterPublicIds.isEmpty()) return emptyList()
        return dbQuery {
            val charactersToDelete = AICharacter.find {
                (AICharacters.id inList characterPublicIds) and (AICharacters.owner eq userId)
            }.with(AICharacter::avatar, AICharacter::images)

            charactersToDelete.forEach { character ->
                character.avatar?.deletedAt = Clock.System.now()
                character.images.forEach { it.deletedAt = Clock.System.now() }
                // Инвалидируем кэш для удаляемой категории
                RedisService.invalidateCategoryCache(character.category)
            }

            val allPathsToDelete = charactersToDelete.flatMap { character ->
                val avatarPath = character.avatar?.imagePath
                val imagePaths = character.images.map { it.imagePath }
                listOfNotNull(avatarPath) + imagePaths
            }.distinct()

            charactersToDelete.forEach { it.deletedAt = Clock.System.now() }
            allPathsToDelete
        }
    }

    suspend fun belongsToUser(characterPublicId: Uuid, userId: Uuid): Boolean {
        return dbQuery {
            AICharacter.find {
                (AICharacters.id eq characterPublicId) and (AICharacters.owner eq userId)
            }.empty().not()
        }
    }

    suspend fun existsByPublicId(characterPublicId: Uuid): Boolean {
        return dbQuery {
            AICharacter.findById(characterPublicId) != null
        }
    }

    suspend fun deleteChatVariant(variantId: Uuid, userId: Uuid): DeleteVariantResult {
        return dbQuery {
            val variantToDelete = ChatVariant
                .find { ChatVariants.id eq variantId }
                .with(ChatVariant::chatSession, ChatSession::user)
                .singleOrNull()

            if (variantToDelete == null || variantToDelete.chatSession.user.id.value != userId) {
                return@dbQuery DeleteVariantResult.VariantNotFound
            }

            val sessionId = variantToDelete.chatSession.id
            val variantCount = ChatVariant.find { ChatVariants.chatSessionId eq sessionId }.count()
            if (variantCount <= 1) {
                return@dbQuery DeleteVariantResult.CannotDeleteLast
            }

            val imagePathToDelete = variantToDelete.chatVariantImage?.imagePath
            variantToDelete.deletedAt = Clock.System.now()
            DeleteVariantResult.Success(imagePathToDelete)
        }
    }

    suspend fun incrementStats(characterId: Uuid, tokensDelta: Long, userId: Uuid) = dbQuery {
        AppUsers.update({ AppUsers.id eq userId }) {
            it[totalGeneratedMessages] = totalGeneratedMessages + 1
            it[totalGeneratedTokens] = totalGeneratedTokens + tokensDelta.toInt()
        }

        val updatedRows = AICharacterStats.update({ AICharacterStats.characterId eq characterId }) {
            it[messageCount] = messageCount + 1
            it[totalGeneratedTokens] = totalGeneratedTokens + tokensDelta
            it[lastInteractionAt] = Clock.System.now()
        }

        if (updatedRows == 0) {
            AICharacterStats.insert {
                it[this.characterId] = characterId
                it[this.messageCount] = 1
                it[this.totalGeneratedTokens] = tokensDelta
                it[this.lastInteractionAt] = Clock.System.now()
            }
        }

        // --- Расчет популярности (Trend Score) и инвалидация кэша ---
        try {
            val usersCount = UserCharacterRelationships
                .select(UserCharacterRelationships.id)
                .where { UserCharacterRelationships.characterId eq characterId }
                .count()

            val createdAt = AICharacters
                .select(AICharacters.createdAt)
                .where { AICharacters.id eq characterId }
                .singleOrNull()?.get(AICharacters.createdAt) ?: Clock.System.now()

            val statsRow = AICharacterStats
                .select(AICharacterStats.messageCount, AICharacterStats.lastInteractionAt)
                .where { AICharacterStats.characterId eq characterId }
                .singleOrNull()

            val msgCount = statsRow?.get(AICharacterStats.messageCount) ?: 1L
            val lastInteraction = statsRow?.get(AICharacterStats.lastInteractionAt) ?: Clock.System.now()

            val score = PopularityCalculator.calculate(
                usersCount = usersCount,
                messageCount = msgCount,
                lastInteractionAt = lastInteraction,
                createdAt = createdAt
            )

            AICharacterStats.update({ AICharacterStats.characterId eq characterId }) {
                it[popularityScore] = score
            }

            // Инвалидируем кэш категории
            val cat = AICharacters
                .select(AICharacters.category)
                .where { AICharacters.id eq characterId }
                .singleOrNull()?.get(AICharacters.category)
            cat?.let { RedisService.invalidateCategoryCache(it) }
        } catch (e: Exception) {
            println("[CharacterRepository] Ошибка при обновлении статистики популярности: ${e.message}")
        }
    }

    suspend fun getCharactersEnergy(userId: Uuid): List<CharacterEnergyResponse> {
        return dbQuery {
            AICharacters
                .selectAll()
                .where { AICharacters.owner eq userId }
                .map { row ->
                    CharacterEnergyResponse(
                        characterId = row[AICharacters.id].value,
                        unclaimedEnergy = row[AICharacters.unclaimedEnergy]
                    )
                }
        }
    }

    suspend fun getEventsByCharacterId(characterId: Uuid): List<CharacterEventDto> {
        return dbQuery {
            CharacterEvents
                .selectAll()
                .where { CharacterEvents.characterId eq characterId }
                .orderBy(CharacterEvents.requiredLevel, SortOrder.ASC)
                .map { row ->
                    CharacterEventDto(
                        id = row[CharacterEvents.id].value,
                        requiredLevel = row[CharacterEvents.requiredLevel],
                        eventName = row[CharacterEvents.eventName],
                        eventTriggerScenario = row[CharacterEvents.eventTriggerScenario],
                        eventPermanentTrait = row[CharacterEvents.eventPermanentTrait]
                    )
                }
        }
    }

    suspend fun replaceCharacterEvents(characterId: Uuid, events: List<CharacterEventDto>) {
        dbQuery {
            CharacterEvents.deleteWhere { CharacterEvents.characterId eq characterId }
            events.forEach { event ->
                CharacterEvents.insert {
                    it[this.characterId] = characterId
                    it[this.requiredLevel] = event.requiredLevel
                    it[this.eventName] = event.eventName
                    it[this.eventTriggerScenario] = event.eventTriggerScenario
                    it[this.eventPermanentTrait] = event.eventPermanentTrait
                }
            }
        }
    }
}
