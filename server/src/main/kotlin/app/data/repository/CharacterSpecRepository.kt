package app.data.repository

import app.cache.RedisService
import app.data.db.tables.aiCharacter.AICharacterAlternateGreetingEntity
import app.data.db.tables.aiCharacter.AICharacterAlternateGreetings
import app.data.db.tables.aiCharacter.AICharacterLorebookBindings
import app.data.db.tables.aiCharacter.AICharacterLorebookEntries
import app.data.db.tables.aiCharacter.AICharacterLorebookEntity
import app.data.db.tables.aiCharacter.AICharacterLorebookEntryEntity
import app.data.db.tables.aiCharacter.AICharacterLorebooks
import app.data.db.tables.aiCharacter.AICharacterPromptDetailEntity
import app.data.db.tables.aiCharacter.AICharacterPromptDetails
import app.data.db.tables.aiCharacter.AICharacterTags
import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.aiCharacter.UserCharacterLorebooks
import app.data.db.tables.aiCharacter.UserSavedLorebooks
import app.data.db.tables.users.AppUsers
import app.utils.dbQuery
import com.ensodai.shared.avalon.character.models.*
import com.ensodai.shared.avalon.character.spec.CharacterBookDto
import com.ensodai.shared.avalon.character.spec.CharacterBookEntryDto
import com.ensodai.shared.avalon.character.spec.characterCardJson
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Single
import kotlin.math.ceil
import kotlin.uuid.Uuid

@Single
class CharacterSpecRepository {

    // =========================================================================
    // Prompt Details (1:1)
    // =========================================================================

    suspend fun savePromptDetails(characterId: Uuid, input: CharacterPromptDetailsInput): Unit = dbQuery {
        AICharacterPromptDetails.insert {
            it[this.characterId] = characterId
            it[this.scenario] = input.scenario
            it[this.mesExample] = input.mesExample
            it[this.customSystemPrompt] = input.customSystemPrompt
            it[this.postHistoryInstructions] = input.postHistoryInstructions
            it[this.creator] = input.creator
            it[this.characterVersion] = input.characterVersion
            it[this.creatorNotes] = input.creatorNotes
            it[this.isImported] = input.isImported
        }
    }

    suspend fun getPromptDetails(characterId: Uuid): CharacterPromptDetailsDto? = dbQuery {
        AICharacterPromptDetails.selectAll()
            .where { AICharacterPromptDetails.characterId eq characterId }
            .singleOrNull()
            ?.let { row ->
                CharacterPromptDetailsDto(
                    characterId = characterId,
                    scenario = row[AICharacterPromptDetails.scenario],
                    mesExample = row[AICharacterPromptDetails.mesExample],
                    customSystemPrompt = row[AICharacterPromptDetails.customSystemPrompt],
                    postHistoryInstructions = row[AICharacterPromptDetails.postHistoryInstructions],
                    creator = row[AICharacterPromptDetails.creator],
                    characterVersion = row[AICharacterPromptDetails.characterVersion],
                    creatorNotes = row[AICharacterPromptDetails.creatorNotes],
                    isImported = row[AICharacterPromptDetails.isImported]
                )
            }
    }

    suspend fun updatePromptDetails(request: UpdateCharacterPromptDetailsRequest): Unit = dbQuery {
        val updated = AICharacterPromptDetails.update({ AICharacterPromptDetails.characterId eq request.characterId }) {
            it[scenario] = request.scenario
            it[mesExample] = request.mesExample
            it[customSystemPrompt] = request.customSystemPrompt
            it[postHistoryInstructions] = request.postHistoryInstructions
            it[creator] = request.creator
            it[characterVersion] = request.characterVersion
            it[creatorNotes] = request.creatorNotes
        }
        if (updated == 0) {
            AICharacterPromptDetails.insert {
                it[characterId] = request.characterId
                it[scenario] = request.scenario
                it[mesExample] = request.mesExample
                it[customSystemPrompt] = request.customSystemPrompt
                it[postHistoryInstructions] = request.postHistoryInstructions
                it[creator] = request.creator
                it[characterVersion] = request.characterVersion
                it[creatorNotes] = request.creatorNotes
            }
        }
    }

    suspend fun updateScenario(characterId: Uuid, scenarioText: String?): Unit = dbQuery {
        val updated = AICharacterPromptDetails.update({ AICharacterPromptDetails.characterId eq characterId }) {
            it[scenario] = scenarioText
        }
        if (updated == 0) {
            AICharacterPromptDetails.insert {
                it[AICharacterPromptDetails.characterId] = characterId
                it[scenario] = scenarioText
            }
        }
    }

    suspend fun updateMesExample(characterId: Uuid, mesExampleText: String?): Unit = dbQuery {
        val updated = AICharacterPromptDetails.update({ AICharacterPromptDetails.characterId eq characterId }) {
            it[mesExample] = mesExampleText
        }
        if (updated == 0) {
            AICharacterPromptDetails.insert {
                it[AICharacterPromptDetails.characterId] = characterId
                it[mesExample] = mesExampleText
            }
        }
    }

    // =========================================================================
    // Alternate Greetings (1:N)
    // =========================================================================

    suspend fun saveAlternateGreetings(characterId: Uuid, greetings: List<String>): Unit = dbQuery {
        if (greetings.isEmpty()) return@dbQuery
        AICharacterAlternateGreetings.batchInsert(greetings.withIndex()) { (index, text) ->
            this[AICharacterAlternateGreetings.characterId] = characterId
            this[AICharacterAlternateGreetings.greetingText] = text
            this[AICharacterAlternateGreetings.orderIndex] = index
        }
    }

    suspend fun getAlternateGreetings(characterId: Uuid): List<CharacterAlternateGreetingDto> = dbQuery {
        AICharacterAlternateGreetings.selectAll()
            .where { AICharacterAlternateGreetings.characterId eq characterId }
            .orderBy(AICharacterAlternateGreetings.orderIndex to SortOrder.ASC)
            .map { row ->
                CharacterAlternateGreetingDto(
                    id = row[AICharacterAlternateGreetings.id].value,
                    characterId = characterId,
                    greetingText = row[AICharacterAlternateGreetings.greetingText],
                    title = row[AICharacterAlternateGreetings.title],
                    orderIndex = row[AICharacterAlternateGreetings.orderIndex]
                )
            }
    }

    suspend fun updateAlternateGreetings(request: UpdateAlternateGreetingsRequest): Unit = dbQuery {
        AICharacterAlternateGreetings.deleteWhere { AICharacterAlternateGreetings.characterId eq request.characterId }
        if (request.greetings.isNotEmpty()) {
            AICharacterAlternateGreetings.batchInsert(request.greetings.withIndex()) { (index, greeting) ->
                this[AICharacterAlternateGreetings.characterId] = request.characterId
                this[AICharacterAlternateGreetings.greetingText] = greeting.greetingText
                this[AICharacterAlternateGreetings.title] = greeting.title
                this[AICharacterAlternateGreetings.orderIndex] = if (greeting.orderIndex != 0) greeting.orderIndex else index
            }
        }
    }

    // =========================================================================
    // Tags (M:N)
    // =========================================================================

    suspend fun saveTags(characterId: Uuid, tags: List<String>): Unit = dbQuery {
        val distinctTags = tags.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (distinctTags.isEmpty()) return@dbQuery
        AICharacterTags.batchInsert(distinctTags) { tag ->
            this[AICharacterTags.characterId] = characterId
            this[AICharacterTags.tag] = tag
        }
    }

    suspend fun getTags(characterId: Uuid): List<String> = dbQuery {
        AICharacterTags.selectAll()
            .where { AICharacterTags.characterId eq characterId }
            .map { it[AICharacterTags.tag] }
    }

    // =========================================================================
    // Lorebook & Entries (1:N & 1:N)
    // =========================================================================

    suspend fun saveLorebook(characterId: Uuid, input: CharacterLorebookInput): Unit = dbQuery {
        val lorebookId = Uuid.random()
        AICharacterLorebooks.insert {
            it[id] = lorebookId
            it[this.characterId] = characterId
            it[name] = input.name
            it[description] = input.description
            it[scanDepth] = input.scanDepth
            it[tokenBudget] = input.tokenBudget
            it[recursiveScanning] = input.recursiveScanning
        }

        if (input.entries.isNotEmpty()) {
            AICharacterLorebookEntries.batchInsert(input.entries.withIndex()) { (index, entry) ->
                this[AICharacterLorebookEntries.lorebookId] = lorebookId
                this[AICharacterLorebookEntries.keys] = entry.keys.joinToString("\n")
                this[AICharacterLorebookEntries.secondaryKeys] = entry.secondaryKeys?.joinToString("\n")
                this[AICharacterLorebookEntries.content] = entry.content
                this[AICharacterLorebookEntries.comment] = entry.comment
                this[AICharacterLorebookEntries.enabled] = entry.enabled
                this[AICharacterLorebookEntries.constant] = entry.constant
                this[AICharacterLorebookEntries.selective] = entry.selective
                this[AICharacterLorebookEntries.insertionOrder] = if (entry.insertionOrder != 0) entry.insertionOrder else index
                this[AICharacterLorebookEntries.position] = entry.position
                this[AICharacterLorebookEntries.useRegex] = entry.useRegex
                this[AICharacterLorebookEntries.priority] = entry.priority
                this[AICharacterLorebookEntries.probability] = entry.probability
                this[AICharacterLorebookEntries.scanDepthOverride] = entry.scanDepthOverride
                this[AICharacterLorebookEntries.stickyTurns] = entry.stickyTurns
                this[AICharacterLorebookEntries.cooldownTurns] = entry.cooldownTurns
                this[AICharacterLorebookEntries.delayMessages] = entry.delayMessages
                this[AICharacterLorebookEntries.secondaryLogic] = entry.secondaryLogic
                this[AICharacterLorebookEntries.excludeRecursion] = entry.excludeRecursion
                this[AICharacterLorebookEntries.preventRecursion] = entry.preventRecursion
                this[AICharacterLorebookEntries.delayUntilRecursion] = entry.delayUntilRecursion
                this[AICharacterLorebookEntries.depth] = entry.depth
            }
        }

        AICharacterLorebookBindings.insert {
            it[id] = Uuid.random()
            it[this.characterId] = characterId
            it[this.lorebookId] = lorebookId
            it[this.isPrimary] = true
            it[this.isEnabled] = true
            it[this.priority] = 0
        }
    }

    suspend fun getLorebookById(lorebookId: Uuid, currentUserId: Uuid? = null): CharacterLorebookDetailsDto? = dbQuery {
        val lorebookRow = AICharacterLorebooks.selectAll()
            .where { AICharacterLorebooks.id eq lorebookId }
            .singleOrNull() ?: return@dbQuery null

        val entries = AICharacterLorebookEntries.selectAll()
            .where { AICharacterLorebookEntries.lorebookId eq lorebookId }
            .orderBy(AICharacterLorebookEntries.insertionOrder to SortOrder.ASC)
            .map { row ->
                CharacterLorebookEntryItemDto(
                    id = row[AICharacterLorebookEntries.id].value,
                    keys = row[AICharacterLorebookEntries.keys].split("\n").filter { it.isNotBlank() },
                    secondaryKeys = row[AICharacterLorebookEntries.secondaryKeys]?.split("\n")?.filter { it.isNotBlank() },
                    content = row[AICharacterLorebookEntries.content],
                    comment = row[AICharacterLorebookEntries.comment],
                    enabled = row[AICharacterLorebookEntries.enabled],
                    constant = row[AICharacterLorebookEntries.constant],
                    selective = row[AICharacterLorebookEntries.selective],
                    insertionOrder = row[AICharacterLorebookEntries.insertionOrder],
                    position = row[AICharacterLorebookEntries.position],
                    useRegex = row[AICharacterLorebookEntries.useRegex],
                    priority = row[AICharacterLorebookEntries.priority],
                    probability = row[AICharacterLorebookEntries.probability],
                    scanDepthOverride = row[AICharacterLorebookEntries.scanDepthOverride],
                    stickyTurns = row[AICharacterLorebookEntries.stickyTurns],
                    cooldownTurns = row[AICharacterLorebookEntries.cooldownTurns],
                    delayMessages = row[AICharacterLorebookEntries.delayMessages],
                    secondaryLogic = row[AICharacterLorebookEntries.secondaryLogic],
                    excludeRecursion = row[AICharacterLorebookEntries.excludeRecursion],
                    preventRecursion = row[AICharacterLorebookEntries.preventRecursion],
                    delayUntilRecursion = row[AICharacterLorebookEntries.delayUntilRecursion],
                    depth = row[AICharacterLorebookEntries.depth]
                )
            }

        val bookUserId = lorebookRow[AICharacterLorebooks.userId]?.value
        val isOwner = if (currentUserId != null && bookUserId != null) bookUserId == currentUserId else true
        val creatorName = if (bookUserId != null) {
            AppUsers.selectAll().where { AppUsers.id eq bookUserId }.singleOrNull()?.get(AppUsers.nickName)
        } else null

        CharacterLorebookDetailsDto(
            id = lorebookId,
            characterId = lorebookRow[AICharacterLorebooks.characterId]?.value,
            name = lorebookRow[AICharacterLorebooks.name],
            description = lorebookRow[AICharacterLorebooks.description],
            scanDepth = lorebookRow[AICharacterLorebooks.scanDepth],
            tokenBudget = lorebookRow[AICharacterLorebooks.tokenBudget],
            recursiveScanning = lorebookRow[AICharacterLorebooks.recursiveScanning],
            isPublic = lorebookRow[AICharacterLorebooks.isPublic],
            isOwner = isOwner,
            creatorName = creatorName,
            avatarUrl = lorebookRow[AICharacterLorebooks.avatarUrl],
            entries = entries
        )
    }

    suspend fun getLorebook(characterId: Uuid, currentUserId: Uuid? = null): CharacterLorebookDetailsDto? = dbQuery {
        val binding = AICharacterLorebookBindings.selectAll()
            .where { (AICharacterLorebookBindings.characterId eq characterId) and (AICharacterLorebookBindings.isPrimary eq true) }
            .singleOrNull() ?: AICharacterLorebookBindings.selectAll()
            .where { AICharacterLorebookBindings.characterId eq characterId }
            .firstOrNull()

        val lorebookId = binding?.get(AICharacterLorebookBindings.lorebookId)?.value
            ?: AICharacterLorebooks.selectAll()
                .where { AICharacterLorebooks.characterId eq characterId }
                .firstOrNull()?.get(AICharacterLorebooks.id)?.value
            ?: return@dbQuery null

        getLorebookById(lorebookId, currentUserId)
    }

    suspend fun ensureLorebook(characterId: Uuid, userId: Uuid? = null): Uuid = dbQuery {
        val existingBinding = AICharacterLorebookBindings.selectAll()
            .where { (AICharacterLorebookBindings.characterId eq characterId) and (AICharacterLorebookBindings.isPrimary eq true) }
            .singleOrNull() ?: AICharacterLorebookBindings.selectAll()
            .where { AICharacterLorebookBindings.characterId eq characterId }
            .firstOrNull()

        if (existingBinding != null) {
            return@dbQuery existingBinding[AICharacterLorebookBindings.lorebookId].value
        }

        val existingRow = AICharacterLorebooks.selectAll()
            .where { AICharacterLorebooks.characterId eq characterId }
            .firstOrNull()

        if (existingRow != null) {
            val lbId = existingRow[AICharacterLorebooks.id].value
            AICharacterLorebookBindings.insert {
                it[id] = Uuid.random()
                it[this.characterId] = characterId
                it[this.lorebookId] = lbId
                it[this.isPrimary] = true
                it[this.isEnabled] = true
                it[this.priority] = 0
            }
            return@dbQuery lbId
        }

        val newId = Uuid.random()
        AICharacterLorebooks.insert {
            it[id] = newId
            it[this.characterId] = characterId
            it[this.userId] = userId
            it[name] = "Default Lorebook"
            it[scanDepth] = 5
            it[recursiveScanning] = false
        }
        AICharacterLorebookBindings.insert {
            it[id] = Uuid.random()
            it[this.characterId] = characterId
            it[this.lorebookId] = newId
            it[this.isPrimary] = true
            it[this.isEnabled] = true
            it[this.priority] = 0
        }
        newId
    }

    private fun checkLorebookOwnership(lorebookId: Uuid, userId: Uuid) {
        val row = AICharacterLorebooks.selectAll()
            .where { AICharacterLorebooks.id eq lorebookId }
            .singleOrNull() ?: return
        val bookUserId = row[AICharacterLorebooks.userId]?.value
        if (bookUserId != null && bookUserId != userId) {
            throw IllegalAccessException("У вас нет прав на редактирование этой книги знаний")
        }
    }

    suspend fun getCharacterLorebooks(characterId: Uuid, currentUserId: Uuid): List<CharacterLorebookBindingDto> = dbQuery {
        val canonJoin = AICharacterLorebookBindings
            .join(AICharacterLorebooks, JoinType.INNER, AICharacterLorebookBindings.lorebookId, AICharacterLorebooks.id)
            .join(AppUsers, JoinType.LEFT, AICharacterLorebooks.userId, AppUsers.id)

        val canonRows = canonJoin.selectAll()
            .where { AICharacterLorebookBindings.characterId eq characterId }
            .orderBy(AICharacterLorebookBindings.priority to SortOrder.DESC, AICharacterLorebookBindings.createdAt to SortOrder.ASC)
            .toList()

        val userJoin = UserCharacterLorebooks
            .join(AICharacterLorebooks, JoinType.INNER, UserCharacterLorebooks.lorebookId, AICharacterLorebooks.id)
            .join(AppUsers, JoinType.LEFT, AICharacterLorebooks.userId, AppUsers.id)

        val userRows = userJoin.selectAll()
            .where {
                (UserCharacterLorebooks.characterId eq characterId) and
                (UserCharacterLorebooks.userId eq currentUserId) and
                (UserCharacterLorebooks.deletedAt.isNull())
            }
            .orderBy(UserCharacterLorebooks.priority to SortOrder.DESC, UserCharacterLorebooks.createdAt to SortOrder.ASC)
            .toList()

        val allLorebookIds = (canonRows.map { it[AICharacterLorebooks.id].value } + userRows.map { it[AICharacterLorebooks.id].value }).distinct()
        val entriesCounts = if (allLorebookIds.isNotEmpty()) {
            AICharacterLorebookEntries.selectAll()
                .where { AICharacterLorebookEntries.lorebookId inList allLorebookIds }
                .groupBy { it[AICharacterLorebookEntries.lorebookId].value }
                .mapValues { it.value.size }
        } else {
            emptyMap()
        }

        val canonBindings = canonRows.map { row ->
            val lbId = row[AICharacterLorebooks.id].value
            val bookUserId = row[AICharacterLorebooks.userId]?.value
            val isOwner = bookUserId == null || bookUserId == currentUserId
            val creatorName = row.getOrNull(AppUsers.nickName)

            CharacterLorebookBindingDto(
                bindingId = row[AICharacterLorebookBindings.id].value,
                characterId = characterId,
                lorebookId = lbId,
                name = row[AICharacterLorebooks.name],
                description = row[AICharacterLorebooks.description],
                isEnabled = row[AICharacterLorebookBindings.isEnabled],
                isPrimary = row[AICharacterLorebookBindings.isPrimary],
                priority = row[AICharacterLorebookBindings.priority],
                entriesCount = entriesCounts[lbId] ?: 0,
                isOwner = isOwner,
                isPublic = row[AICharacterLorebooks.isPublic],
                creatorName = creatorName,
                avatarUrl = row[AICharacterLorebooks.avatarUrl],
                isCanon = true
            )
        }

        val userBindings = userRows.map { row ->
            val lbId = row[AICharacterLorebooks.id].value
            val bookUserId = row[AICharacterLorebooks.userId]?.value
            val isOwner = bookUserId == null || bookUserId == currentUserId
            val creatorName = row.getOrNull(AppUsers.nickName)

            CharacterLorebookBindingDto(
                bindingId = row[UserCharacterLorebooks.id].value,
                characterId = characterId,
                lorebookId = lbId,
                name = row[AICharacterLorebooks.name],
                description = row[AICharacterLorebooks.description],
                isEnabled = row[UserCharacterLorebooks.isEnabled],
                isPrimary = false,
                priority = row[UserCharacterLorebooks.priority],
                entriesCount = entriesCounts[lbId] ?: 0,
                isOwner = isOwner,
                isPublic = row[AICharacterLorebooks.isPublic],
                creatorName = creatorName,
                avatarUrl = row[AICharacterLorebooks.avatarUrl],
                isCanon = false
            )
        }

        canonBindings + userBindings
    }

    suspend fun attachLorebook(
        characterId: Uuid,
        lorebookId: Uuid,
        isEnabled: Boolean = true,
        isPrimary: Boolean = false,
        priority: Int = 0,
        userId: Uuid
    ): CharacterLorebookBindingDto = dbQuery {
        val bookRow = AICharacterLorebooks.selectAll()
            .where { AICharacterLorebooks.id eq lorebookId }
            .singleOrNull() ?: throw NoSuchElementException("Книга знаний не найдена")

        val bookUserId = bookRow[AICharacterLorebooks.userId]?.value
        val isPublic = bookRow[AICharacterLorebooks.isPublic]
        if (bookUserId != null && bookUserId != userId && !isPublic) {
            throw IllegalAccessException("У вас нет доступа к этой приватной книге знаний")
        }

        if (isPrimary) {
            AICharacterLorebookBindings.update({ AICharacterLorebookBindings.characterId eq characterId }) {
                it[this.isPrimary] = false
            }
        }

        val existingBinding = AICharacterLorebookBindings.selectAll()
            .where { (AICharacterLorebookBindings.characterId eq characterId) and (AICharacterLorebookBindings.lorebookId eq lorebookId) }
            .singleOrNull()

        val bindingId = if (existingBinding != null) {
            val id = existingBinding[AICharacterLorebookBindings.id].value
            AICharacterLorebookBindings.update({ AICharacterLorebookBindings.id eq id }) {
                it[this.isEnabled] = isEnabled
                it[this.isPrimary] = isPrimary
                it[this.priority] = priority
            }
            id
        } else {
            val id = Uuid.random()
            AICharacterLorebookBindings.insert {
                it[this.id] = id
                it[this.characterId] = characterId
                it[this.lorebookId] = lorebookId
                it[this.isEnabled] = isEnabled
                it[this.isPrimary] = isPrimary
                it[this.priority] = priority
            }
            id
        }

        val entriesCount = AICharacterLorebookEntries.selectAll()
            .where { AICharacterLorebookEntries.lorebookId eq lorebookId }
            .count()
            .toInt()

        val isOwner = bookUserId == null || bookUserId == userId
        val creatorName = if (bookUserId != null) {
            AppUsers.selectAll().where { AppUsers.id eq bookUserId }.singleOrNull()?.get(AppUsers.nickName)
        } else null

        CharacterLorebookBindingDto(
            bindingId = bindingId,
            characterId = characterId,
            lorebookId = lorebookId,
            name = bookRow[AICharacterLorebooks.name],
            description = bookRow[AICharacterLorebooks.description],
            isEnabled = isEnabled,
            isPrimary = isPrimary,
            priority = priority,
            entriesCount = entriesCount,
            isOwner = isOwner,
            isPublic = isPublic,
            creatorName = creatorName,
            avatarUrl = bookRow[AICharacterLorebooks.avatarUrl]
        )
    }

    suspend fun detachLorebook(characterId: Uuid, lorebookId: Uuid): Unit = dbQuery {
        val deleted = AICharacterLorebookBindings.selectAll()
            .where { (AICharacterLorebookBindings.characterId eq characterId) and (AICharacterLorebookBindings.lorebookId eq lorebookId) }
            .singleOrNull() ?: return@dbQuery

        val wasPrimary = deleted[AICharacterLorebookBindings.isPrimary]

        AICharacterLorebookBindings.deleteWhere {
            (AICharacterLorebookBindings.characterId eq characterId) and (AICharacterLorebookBindings.lorebookId eq lorebookId)
        }

        if (wasPrimary) {
            val next = AICharacterLorebookBindings.selectAll()
                .where { AICharacterLorebookBindings.characterId eq characterId }
                .firstOrNull()
            if (next != null) {
                AICharacterLorebookBindings.update({ AICharacterLorebookBindings.id eq next[AICharacterLorebookBindings.id].value }) {
                    it[isPrimary] = true
                }
            }
        }
    }

    suspend fun toggleLorebookBinding(characterId: Uuid, lorebookId: Uuid, isEnabled: Boolean): Unit = dbQuery {
        AICharacterLorebookBindings.update({
            (AICharacterLorebookBindings.characterId eq characterId) and (AICharacterLorebookBindings.lorebookId eq lorebookId)
        }) {
            it[this.isEnabled] = isEnabled
        }
    }

    suspend fun setPrimaryLorebook(characterId: Uuid, lorebookId: Uuid): Unit = dbQuery {
        AICharacterLorebookBindings.update({ AICharacterLorebookBindings.characterId eq characterId }) {
            it[isPrimary] = false
        }
        AICharacterLorebookBindings.update({
            (AICharacterLorebookBindings.characterId eq characterId) and (AICharacterLorebookBindings.lorebookId eq lorebookId)
        }) {
            it[isPrimary] = true
        }
    }

    suspend fun attachUserLorebook(
        characterId: Uuid,
        lorebookId: Uuid,
        userId: Uuid
    ): CharacterLorebookBindingDto = dbQuery {
        val bookRow = AICharacterLorebooks.selectAll()
            .where { AICharacterLorebooks.id eq lorebookId }
            .singleOrNull() ?: throw NoSuchElementException("Книга знаний не найдена")

        val bookUserId = bookRow[AICharacterLorebooks.userId]?.value
        val isPublic = bookRow[AICharacterLorebooks.isPublic]
        val isSaved = UserSavedLorebooks.selectAll()
            .where { (UserSavedLorebooks.userId eq userId) and (UserSavedLorebooks.lorebookId eq lorebookId) }
            .count() > 0

        if (bookUserId != null && bookUserId != userId && !isPublic && !isSaved) {
            throw IllegalAccessException("У вас нет доступа к этой книге знаний")
        }

        val existing = UserCharacterLorebooks.selectAll()
            .where {
                (UserCharacterLorebooks.userId eq userId) and
                (UserCharacterLorebooks.characterId eq characterId) and
                (UserCharacterLorebooks.lorebookId eq lorebookId)
            }
            .singleOrNull()

        val bindingId = if (existing != null) {
            val id = existing[UserCharacterLorebooks.id].value
            UserCharacterLorebooks.update({ UserCharacterLorebooks.id eq id }) {
                it[this.isEnabled] = true
                it[this.deletedAt] = null
            }
            id
        } else {
            val id = Uuid.random()
            UserCharacterLorebooks.insert {
                it[this.id] = id
                it[this.userId] = userId
                it[this.characterId] = characterId
                it[this.lorebookId] = lorebookId
                it[this.isEnabled] = true
                it[this.priority] = 0
            }
            id
        }

        val entriesCount = AICharacterLorebookEntries.selectAll()
            .where { AICharacterLorebookEntries.lorebookId eq lorebookId }
            .count()
            .toInt()

        val isOwner = bookUserId == null || bookUserId == userId
        val creatorName = if (bookUserId != null) {
            AppUsers.selectAll().where { AppUsers.id eq bookUserId }.singleOrNull()?.get(AppUsers.nickName)
        } else null

        CharacterLorebookBindingDto(
            bindingId = bindingId,
            characterId = characterId,
            lorebookId = lorebookId,
            name = bookRow[AICharacterLorebooks.name],
            description = bookRow[AICharacterLorebooks.description],
            isEnabled = true,
            isPrimary = false,
            priority = 0,
            entriesCount = entriesCount,
            isOwner = isOwner,
            isPublic = isPublic,
            creatorName = creatorName,
            avatarUrl = bookRow[AICharacterLorebooks.avatarUrl],
            isCanon = false
        )
    }

    suspend fun detachUserLorebook(
        characterId: Uuid,
        lorebookId: Uuid,
        userId: Uuid
    ): Unit = dbQuery {
        UserCharacterLorebooks.deleteWhere {
            (UserCharacterLorebooks.userId eq userId) and
            (UserCharacterLorebooks.characterId eq characterId) and
            (UserCharacterLorebooks.lorebookId eq lorebookId)
        }
    }

    suspend fun getUserLorebooksLibrary(userId: Uuid): List<UserLorebookSummaryDto> = dbQuery {
        val ownedRows = AICharacterLorebooks.selectAll()
            .where { AICharacterLorebooks.userId eq userId }
            .toList()

        val savedJoin = UserSavedLorebooks
            .join(AICharacterLorebooks, JoinType.INNER, UserSavedLorebooks.lorebookId, AICharacterLorebooks.id)
        val savedRows = savedJoin.selectAll()
            .where { UserSavedLorebooks.userId eq userId }
            .toList()

        val allBookIds = (ownedRows.map { it[AICharacterLorebooks.id].value } + savedRows.map { it[AICharacterLorebooks.id].value }).distinct()
        if (allBookIds.isEmpty()) return@dbQuery emptyList()

        val entriesCounts = AICharacterLorebookEntries.selectAll()
            .where { AICharacterLorebookEntries.lorebookId inList allBookIds }
            .groupBy { it[AICharacterLorebookEntries.lorebookId].value }
            .mapValues { it.value.size }

        val books = (ownedRows + savedRows).distinctBy { it[AICharacterLorebooks.id].value }
        val creatorUserIds = books.mapNotNull { it[AICharacterLorebooks.userId]?.value }.distinct()
        val userNames = if (creatorUserIds.isNotEmpty()) {
            AppUsers.selectAll()
                .where { AppUsers.id inList creatorUserIds }
                .associate { it[AppUsers.id].value to it[AppUsers.nickName] }
        } else emptyMap()

        books.map { row ->
            val id = row[AICharacterLorebooks.id].value
            val bookUserId = row[AICharacterLorebooks.userId]?.value
            val isOwner = bookUserId == userId
            val creatorName = if (bookUserId != null) userNames[bookUserId] else null
            val createdAtStr = row[AICharacterLorebooks.createdAt].toString()
            val isSaved = !isOwner

            UserLorebookSummaryDto(
                id = id,
                name = row[AICharacterLorebooks.name],
                description = row[AICharacterLorebooks.description],
                isPublic = row[AICharacterLorebooks.isPublic],
                isOwner = isOwner,
                entriesCount = entriesCounts[id] ?: 0,
                creatorName = creatorName,
                createdAt = createdAtStr,
                isSavedToLibrary = isSaved,
                avatarUrl = row[AICharacterLorebooks.avatarUrl]
            )
        }
    }

    suspend fun createStandaloneLorebook(
        userId: Uuid,
        request: CreateStandaloneLorebookRequest
    ): CharacterLorebookDetailsDto = dbQuery {
        val newId = Uuid.random()
        AICharacterLorebooks.insert {
            it[id] = newId
            it[this.userId] = userId
            it[this.characterId] = request.attachToCharacterId
            it[name] = request.name
            it[description] = request.description
            it[scanDepth] = request.scanDepth
            it[tokenBudget] = request.tokenBudget
            it[recursiveScanning] = request.recursiveScanning
            it[isPublic] = request.isPublic
            it[avatarUrl] = request.avatarUrl
        }

        if (request.entries.isNotEmpty()) {
            for ((index, entry) in request.entries.withIndex()) {
                val order = if (entry.insertionOrder != 0) entry.insertionOrder else index
                AICharacterLorebookEntries.insert {
                    it[id] = Uuid.random()
                    it[this.lorebookId] = newId
                    it[keys] = entry.keys.joinToString("\n")
                    it[secondaryKeys] = entry.secondaryKeys?.joinToString("\n")
                    it[content] = entry.content
                    it[comment] = entry.comment
                    it[enabled] = entry.enabled
                    it[constant] = entry.constant
                    it[selective] = entry.selective
                    it[insertionOrder] = order
                    it[position] = entry.position
                    it[useRegex] = entry.useRegex
                    it[priority] = entry.priority
                    it[probability] = entry.probability
                    it[scanDepthOverride] = entry.scanDepthOverride
                    it[stickyTurns] = entry.stickyTurns
                    it[cooldownTurns] = entry.cooldownTurns
                    it[delayMessages] = entry.delayMessages
                    it[secondaryLogic] = entry.secondaryLogic
                    it[excludeRecursion] = entry.excludeRecursion
                    it[preventRecursion] = entry.preventRecursion
                    it[delayUntilRecursion] = entry.delayUntilRecursion
                    it[depth] = entry.depth
                }
            }
        }

        if (request.attachToCharacterId != null) {
            val charId = request.attachToCharacterId!!
            val hasBindings = AICharacterLorebookBindings.selectAll()
                .where { AICharacterLorebookBindings.characterId eq charId }
                .count() > 0
            val isPrimary = !hasBindings
            AICharacterLorebookBindings.insert {
                it[id] = Uuid.random()
                it[this.characterId] = charId
                it[this.lorebookId] = newId
                it[this.isEnabled] = true
                it[this.isPrimary] = isPrimary
                it[this.priority] = 0
            }
        }

        val created = getLorebookById(newId, userId) ?: throw IllegalStateException("Не удалось загрузить созданную книгу знаний")
        if (request.isPublic) {
            RedisService.invalidateLorebooksCache()
        }
        created
    }

    suspend fun forkLorebook(
        lorebookId: Uuid,
        userId: Uuid,
        attachToCharacterId: Uuid? = null
    ): CharacterLorebookDetailsDto = dbQuery {
        val source = AICharacterLorebooks.selectAll()
            .where { AICharacterLorebooks.id eq lorebookId }
            .singleOrNull() ?: throw NoSuchElementException("Книга знаний не найдена")

        val sourceEntries = AICharacterLorebookEntries.selectAll()
            .where { AICharacterLorebookEntries.lorebookId eq lorebookId }
            .toList()

        val newId = Uuid.random()
        AICharacterLorebooks.insert {
            it[id] = newId
            it[this.userId] = userId
            it[this.characterId] = attachToCharacterId
            it[name] = "${source[AICharacterLorebooks.name]} (Copy)"
            it[description] = source[AICharacterLorebooks.description]
            it[scanDepth] = source[AICharacterLorebooks.scanDepth]
            it[tokenBudget] = source[AICharacterLorebooks.tokenBudget]
            it[recursiveScanning] = source[AICharacterLorebooks.recursiveScanning]
            it[isPublic] = false
            it[avatarUrl] = source[AICharacterLorebooks.avatarUrl]
        }

        for (entry in sourceEntries) {
            AICharacterLorebookEntries.insert {
                it[id] = Uuid.random()
                it[this.lorebookId] = newId
                it[keys] = entry[AICharacterLorebookEntries.keys]
                it[secondaryKeys] = entry[AICharacterLorebookEntries.secondaryKeys]
                it[content] = entry[AICharacterLorebookEntries.content]
                it[comment] = entry[AICharacterLorebookEntries.comment]
                it[enabled] = entry[AICharacterLorebookEntries.enabled]
                it[constant] = entry[AICharacterLorebookEntries.constant]
                it[selective] = entry[AICharacterLorebookEntries.selective]
                it[insertionOrder] = entry[AICharacterLorebookEntries.insertionOrder]
                it[position] = entry[AICharacterLorebookEntries.position]
                it[useRegex] = entry[AICharacterLorebookEntries.useRegex]
                it[priority] = entry[AICharacterLorebookEntries.priority]
                it[probability] = entry[AICharacterLorebookEntries.probability]
                it[scanDepthOverride] = entry[AICharacterLorebookEntries.scanDepthOverride]
                it[stickyTurns] = entry[AICharacterLorebookEntries.stickyTurns]
                it[cooldownTurns] = entry[AICharacterLorebookEntries.cooldownTurns]
                it[delayMessages] = entry[AICharacterLorebookEntries.delayMessages]
                it[secondaryLogic] = entry[AICharacterLorebookEntries.secondaryLogic]
                it[excludeRecursion] = entry[AICharacterLorebookEntries.excludeRecursion]
                it[preventRecursion] = entry[AICharacterLorebookEntries.preventRecursion]
                it[delayUntilRecursion] = entry[AICharacterLorebookEntries.delayUntilRecursion]
                it[depth] = entry[AICharacterLorebookEntries.depth]
            }
        }

        if (attachToCharacterId != null) {
            val charId = attachToCharacterId!!
            val hasBindings = AICharacterLorebookBindings.selectAll()
                .where { AICharacterLorebookBindings.characterId eq charId }
                .count() > 0
            val isPrimary = !hasBindings
            AICharacterLorebookBindings.insert {
                it[id] = Uuid.random()
                it[this.characterId] = charId
                it[this.lorebookId] = newId
                it[this.isEnabled] = true
                it[this.isPrimary] = isPrimary
                it[this.priority] = 0
            }
        }

        getLorebookById(newId, userId) ?: throw IllegalStateException("Не удалось загрузить форкнутую книгу")
    }

    suspend fun deleteLorebook(lorebookId: Uuid, userId: Uuid): Unit = dbQuery {
        val book = AICharacterLorebooks.selectAll()
            .where { AICharacterLorebooks.id eq lorebookId }
            .singleOrNull() ?: return@dbQuery

        val bookUserId = book[AICharacterLorebooks.userId]?.value
        if (bookUserId != null && bookUserId != userId) {
            throw IllegalAccessException("Вы можете удалять только созданные вами книги знаний")
        }

        AICharacterLorebooks.deleteWhere { AICharacterLorebooks.id eq lorebookId }
        RedisService.invalidateLorebooksCache()
    }

    suspend fun saveLorebookToLibrary(lorebookId: Uuid, userId: Uuid): Unit = dbQuery {
        val book = AICharacterLorebooks.selectAll()
            .where { AICharacterLorebooks.id eq lorebookId }
            .singleOrNull() ?: throw NoSuchElementException("Книга знаний не найдена")

        if (!book[AICharacterLorebooks.isPublic] && book[AICharacterLorebooks.userId]?.value != userId) {
            throw IllegalAccessException("Нельзя сохранить приватную книгу знаний")
        }

        val existing = UserSavedLorebooks.selectAll()
            .where { (UserSavedLorebooks.userId eq userId) and (UserSavedLorebooks.lorebookId eq lorebookId) }
            .singleOrNull()

        if (existing == null) {
            UserSavedLorebooks.insert {
                it[id] = Uuid.random()
                it[this.userId] = userId
                it[this.lorebookId] = lorebookId
            }
        }
    }

    suspend fun removeLorebookFromLibrary(lorebookId: Uuid, userId: Uuid): Unit = dbQuery {
        UserSavedLorebooks.deleteWhere {
            (UserSavedLorebooks.userId eq userId) and (UserSavedLorebooks.lorebookId eq lorebookId)
        }
    }

    suspend fun getActiveLorebooksForCharacter(characterId: Uuid): List<CharacterLorebookDetailsDto> = dbQuery {
        val characterOwnerId = AICharacters.selectAll()
            .where { AICharacters.id eq characterId }
            .singleOrNull()
            ?.get(AICharacters.owner)?.value

        val join = AICharacterLorebookBindings
            .join(AICharacterLorebooks, JoinType.INNER, AICharacterLorebookBindings.lorebookId, AICharacterLorebooks.id)

        val activeBindings = join.selectAll()
            .where {
                (AICharacterLorebookBindings.characterId eq characterId) and
                (AICharacterLorebookBindings.isEnabled eq true) and
                (AICharacterLorebooks.deletedAt.isNull()) and
                (
                    (AICharacterLorebooks.isPublic eq true) or
                    (AICharacterLorebooks.userId eq characterOwnerId) or
                    (AICharacterLorebooks.userId.isNull())
                )
            }
            .orderBy(AICharacterLorebookBindings.priority to SortOrder.DESC, AICharacterLorebookBindings.createdAt to SortOrder.ASC)
            .toList()

        if (activeBindings.isEmpty()) {
            val legacy = getLorebook(characterId)
            return@dbQuery if (legacy != null) listOf(legacy) else emptyList()
        }

        activeBindings.mapNotNull { row ->
            val lbId = row[AICharacterLorebooks.id].value
            getLorebookById(lbId)
        }
    }

    suspend fun getActiveLorebooksForUserAndCharacter(
        characterId: Uuid,
        userId: Uuid
    ): List<CharacterLorebookDetailsDto> = dbQuery {
        val canonBooks = getActiveLorebooksForCharacter(characterId)

        val userJoin = UserCharacterLorebooks
            .join(AICharacterLorebooks, JoinType.INNER, UserCharacterLorebooks.lorebookId, AICharacterLorebooks.id)

        val userBookIds = userJoin.selectAll()
            .where {
                (UserCharacterLorebooks.userId eq userId) and
                (UserCharacterLorebooks.characterId eq characterId) and
                (UserCharacterLorebooks.isEnabled eq true) and
                (UserCharacterLorebooks.deletedAt.isNull()) and
                (AICharacterLorebooks.deletedAt.isNull())
            }
            .orderBy(UserCharacterLorebooks.priority to SortOrder.DESC, UserCharacterLorebooks.createdAt to SortOrder.ASC)
            .map { it[AICharacterLorebooks.id].value }

        val userBooks = userBookIds.mapNotNull { getLorebookById(it) }

        (canonBooks + userBooks).distinctBy { it.id }
    }

    suspend fun updateLorebookSettings(request: UpdateLorebookSettingsRequest, userId: Uuid): Unit = dbQuery {
        checkLorebookOwnership(request.lorebookId, userId)
        AICharacterLorebooks.update({ AICharacterLorebooks.id eq request.lorebookId }) {
            it[name] = request.name
            it[description] = request.description
            it[scanDepth] = request.scanDepth
            it[tokenBudget] = request.tokenBudget
            it[recursiveScanning] = request.recursiveScanning
            it[isPublic] = request.isPublic
            it[avatarUrl] = request.avatarUrl
        }
        RedisService.invalidateLorebooksCache()
    }

    suspend fun saveLorebookEntry(
        lorebookId: Uuid,
        entry: CharacterLorebookEntryItemDto,
        userId: Uuid
    ): CharacterLorebookEntryItemDto = dbQuery {
        checkLorebookOwnership(lorebookId, userId)

        val existingEntry = AICharacterLorebookEntries.selectAll()
            .where { (AICharacterLorebookEntries.lorebookId eq lorebookId) and (AICharacterLorebookEntries.id eq entry.id) }
            .singleOrNull()

        if (existingEntry != null) {
            AICharacterLorebookEntries.update({ AICharacterLorebookEntries.id eq entry.id }) {
                it[keys] = entry.keys.joinToString("\n")
                it[secondaryKeys] = entry.secondaryKeys?.joinToString("\n")
                it[content] = entry.content
                it[comment] = entry.comment
                it[enabled] = entry.enabled
                it[constant] = entry.constant
                it[selective] = entry.selective
                it[insertionOrder] = entry.insertionOrder
                it[position] = entry.position
                it[useRegex] = entry.useRegex
                it[priority] = entry.priority
                it[probability] = entry.probability
                it[scanDepthOverride] = entry.scanDepthOverride
                it[stickyTurns] = entry.stickyTurns
                it[cooldownTurns] = entry.cooldownTurns
                it[delayMessages] = entry.delayMessages
                it[secondaryLogic] = entry.secondaryLogic
                it[excludeRecursion] = entry.excludeRecursion
                it[preventRecursion] = entry.preventRecursion
                it[delayUntilRecursion] = entry.delayUntilRecursion
                it[depth] = entry.depth
            }
        } else {
            AICharacterLorebookEntries.insert {
                it[id] = entry.id
                it[this.lorebookId] = lorebookId
                it[keys] = entry.keys.joinToString("\n")
                it[secondaryKeys] = entry.secondaryKeys?.joinToString("\n")
                it[content] = entry.content
                it[comment] = entry.comment
                it[enabled] = entry.enabled
                it[constant] = entry.constant
                it[selective] = entry.selective
                it[insertionOrder] = entry.insertionOrder
                it[position] = entry.position
                it[useRegex] = entry.useRegex
                it[priority] = entry.priority
                it[probability] = entry.probability
                it[scanDepthOverride] = entry.scanDepthOverride
                it[stickyTurns] = entry.stickyTurns
                it[cooldownTurns] = entry.cooldownTurns
                it[delayMessages] = entry.delayMessages
                it[secondaryLogic] = entry.secondaryLogic
                it[excludeRecursion] = entry.excludeRecursion
                it[preventRecursion] = entry.preventRecursion
                it[delayUntilRecursion] = entry.delayUntilRecursion
                it[depth] = entry.depth
            }
        }

        entry.also {
            RedisService.invalidateLorebooksCache()
        }
    }

    suspend fun deleteLorebookEntry(lorebookId: Uuid, entryId: Uuid, userId: Uuid): Unit = dbQuery {
        checkLorebookOwnership(lorebookId, userId)
        AICharacterLorebookEntries.deleteWhere {
            (AICharacterLorebookEntries.lorebookId eq lorebookId) and (AICharacterLorebookEntries.id eq entryId)
        }
        RedisService.invalidateLorebooksCache()
    }

    suspend fun toggleLorebookEntry(
        lorebookId: Uuid,
        entryId: Uuid,
        enabled: Boolean,
        userId: Uuid
    ): Unit = dbQuery {
        checkLorebookOwnership(lorebookId, userId)
        AICharacterLorebookEntries.update({
            (AICharacterLorebookEntries.lorebookId eq lorebookId) and (AICharacterLorebookEntries.id eq entryId)
        }) {
            it[AICharacterLorebookEntries.enabled] = enabled
        }
        RedisService.invalidateLorebooksCache()
    }

    suspend fun updateLorebook(request: UpdateCharacterLorebookRequest, userId: Uuid): Unit = dbQuery {
        checkLorebookOwnership(request.lorebookId, userId)
        AICharacterLorebooks.update({ AICharacterLorebooks.id eq request.lorebookId }) {
            it[name] = request.name
            it[description] = request.description
            it[scanDepth] = request.scanDepth
            it[tokenBudget] = request.tokenBudget
            it[recursiveScanning] = request.recursiveScanning
            it[isPublic] = request.isPublic
            it[avatarUrl] = request.avatarUrl
        }

        // Smart diff:
        val existingEntries = AICharacterLorebookEntries.selectAll()
            .where { AICharacterLorebookEntries.lorebookId eq request.lorebookId }
            .associateBy { it[AICharacterLorebookEntries.id].value }

        val requestEntryIds = request.entries.map { it.id }.toSet()

        // 1. Delete removed
        val idsToDelete = existingEntries.keys - requestEntryIds
        if (idsToDelete.isNotEmpty()) {
            AICharacterLorebookEntries.deleteWhere {
                (AICharacterLorebookEntries.lorebookId eq request.lorebookId) and (AICharacterLorebookEntries.id inList idsToDelete)
            }
        }

        // 2. Update existing and insert new
        for ((index, entry) in request.entries.withIndex()) {
            val order = if (entry.insertionOrder != 0) entry.insertionOrder else index
            if (entry.id in existingEntries) {
                AICharacterLorebookEntries.update({ AICharacterLorebookEntries.id eq entry.id }) {
                    it[keys] = entry.keys.joinToString("\n")
                    it[secondaryKeys] = entry.secondaryKeys?.joinToString("\n")
                    it[content] = entry.content
                    it[comment] = entry.comment
                    it[enabled] = entry.enabled
                    it[constant] = entry.constant
                    it[selective] = entry.selective
                    it[insertionOrder] = order
                    it[position] = entry.position
                    it[useRegex] = entry.useRegex
                }
            } else {
                AICharacterLorebookEntries.insert {
                    it[id] = entry.id
                    it[this.lorebookId] = request.lorebookId
                    it[keys] = entry.keys.joinToString("\n")
                    it[secondaryKeys] = entry.secondaryKeys?.joinToString("\n")
                    it[content] = entry.content
                    it[comment] = entry.comment
                    it[enabled] = entry.enabled
                    it[constant] = entry.constant
                    it[selective] = entry.selective
                    it[insertionOrder] = order
                    it[position] = entry.position
                    it[useRegex] = entry.useRegex
                }
            }
        }
        RedisService.invalidateLorebooksCache()
    }

    suspend fun getPublicLorebooks(
        query: String?,
        page: Int,
        limit: Int,
        currentUserId: Uuid
    ): PaginatedResponse<UserLorebookSummaryDto> {
        val safePage = if (page < 1) 1 else page
        val safeLimit = if (limit < 1) 20 else limit.coerceAtMost(50)
        val version = RedisService.getLorebooksGlobalVersion()

        val cached = RedisService.getCachedPaginatedLorebooks(query, version, safePage, safeLimit)
        val baseResponse: PaginatedResponse<UserLorebookSummaryDto> = if (cached != null) {
            cached
        } else {
            val fetched = dbQuery {
                val cleanQuery = query?.trim()?.lowercase()
                var condition: Op<Boolean> = (AICharacterLorebooks.isPublic eq true) and
                        (AICharacterLorebooks.deletedAt.isNull())

                if (!cleanQuery.isNullOrBlank()) {
                    val searchPattern = "%$cleanQuery%"
                    condition = condition and (
                        (AICharacterLorebooks.name.lowerCase() like searchPattern) or
                        (AICharacterLorebooks.description.lowerCase() like searchPattern)
                    )
                }

                val totalItems = AICharacterLorebooks.selectAll()
                    .where { condition }
                    .count()

                val totalPages = if (safeLimit > 0) ceil(totalItems.toDouble() / safeLimit).toInt() else 0
                val offset = ((safePage - 1) * safeLimit).toLong()

                val rows = AICharacterLorebooks.selectAll()
                    .where { condition }
                    .orderBy(AICharacterLorebooks.createdAt to SortOrder.DESC)
                    .limit(safeLimit)
                    .offset(offset)
                    .toList()

                if (rows.isEmpty()) {
                    return@dbQuery PaginatedResponse(
                        items = emptyList(),
                        page = safePage,
                        totalPages = totalPages,
                        limit = safeLimit
                    )
                }

                val bookIds = rows.map { it[AICharacterLorebooks.id].value }
                val entriesCounts = AICharacterLorebookEntries.selectAll()
                    .where { AICharacterLorebookEntries.lorebookId inList bookIds }
                    .groupBy { it[AICharacterLorebookEntries.lorebookId].value }
                    .mapValues { it.value.size }

                val creatorUserIds = rows.mapNotNull { it[AICharacterLorebooks.userId]?.value }.distinct()
                val userNames = if (creatorUserIds.isNotEmpty()) {
                    AppUsers.selectAll()
                        .where { AppUsers.id inList creatorUserIds }
                        .associate { it[AppUsers.id].value to (it[AppUsers.nickName] ?: it[AppUsers.name]) }
                } else emptyMap()

                val items = rows.map { row ->
                    val id = row[AICharacterLorebooks.id].value
                    val bookUserId = row[AICharacterLorebooks.userId]?.value
                    val creatorName = if (bookUserId != null) userNames[bookUserId] else null
                    val createdAtStr = row[AICharacterLorebooks.createdAt].toString()

                    UserLorebookSummaryDto(
                        id = id,
                        name = row[AICharacterLorebooks.name],
                        description = row[AICharacterLorebooks.description],
                        isPublic = true,
                        isOwner = false,
                        entriesCount = entriesCounts[id] ?: 0,
                        creatorName = creatorName,
                        createdAt = createdAtStr,
                        isSavedToLibrary = false,
                        avatarUrl = row[AICharacterLorebooks.avatarUrl]
                    )
                }

                PaginatedResponse(
                    items = items,
                    page = safePage,
                    totalPages = totalPages,
                    limit = safeLimit
                )
            }
            RedisService.cachePaginatedLorebooks(query, version, safePage, safeLimit, fetched)
            fetched
        }

        if (baseResponse.items.isEmpty()) {
            return baseResponse
        }

        return dbQuery {
            val itemIds = baseResponse.items.map { it.id }
            val savedBookIds = UserSavedLorebooks.selectAll()
                .where { (UserSavedLorebooks.userId eq currentUserId) and (UserSavedLorebooks.lorebookId inList itemIds) }
                .map { it[UserSavedLorebooks.lorebookId].value }
                .toSet()

            val ownedBookIds = AICharacterLorebooks.selectAll()
                .where { (AICharacterLorebooks.id inList itemIds) and (AICharacterLorebooks.userId eq currentUserId) }
                .map { it[AICharacterLorebooks.id].value }
                .toSet()

            val enrichedItems = baseResponse.items.map { item ->
                item.copy(
                    isOwner = item.id in ownedBookIds,
                    isSavedToLibrary = item.id in savedBookIds
                )
            }

            baseResponse.copy(items = enrichedItems)
        }
    }

    suspend fun exportLorebookToJson(lorebookId: Uuid, userId: Uuid): String {
        val lorebook = getLorebookById(lorebookId, userId)
            ?: throw NoSuchElementException("Книга знаний не найдена")

        if (!lorebook.isOwner && !lorebook.isPublic) {
            throw IllegalAccessException("Экспорт приватной книги знаний стороннего автора запрещен")
        }

        val bookDto = CharacterBookDto(
            name = lorebook.name,
            description = lorebook.description,
            scanDepth = lorebook.scanDepth,
            tokenBudget = lorebook.tokenBudget,
            recursiveScanning = lorebook.recursiveScanning,
            entries = lorebook.entries.map { entry ->
                CharacterBookEntryDto(
                    keys = entry.keys,
                    secondaryKeys = entry.secondaryKeys,
                    content = entry.content,
                    comment = entry.comment,
                    enabled = entry.enabled,
                    insertionOrder = entry.insertionOrder,
                    constant = entry.constant,
                    selective = entry.selective,
                    position = when (entry.position) {
                        LorebookEntryPosition.BEFORE_CHAR -> "before_char"
                        LorebookEntryPosition.AFTER_CHAR -> "after_char"
                        LorebookEntryPosition.BEFORE_DESC -> "before_desc"
                        LorebookEntryPosition.AFTER_DESC -> "after_desc"
                        LorebookEntryPosition.PERSONALITY -> "personality"
                        LorebookEntryPosition.SCENARIO -> "scenario"
                        LorebookEntryPosition.AT_DEPTH -> "at_depth"
                    },
                    depth = entry.depth,
                    useRegex = entry.useRegex
                )
            }
        )

        return characterCardJson.encodeToString(CharacterBookDto.serializer(), bookDto)
    }
}
