package app.data.repository

import app.data.db.tables.aiCharacter.AICharacter
import app.data.db.tables.aiCharacter.AICharacters
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import app.data.db.tables.chatSessions.ChatSession
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariant
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.users.*
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import com.ensodai.shared.avalon.chat.models.ChatVariantImageResponse
import com.ensodai.shared.avalon.chat.models.ChatUpdateItem
import com.ensodai.shared.avalon.user.models.UserAccountInfoResponse
import com.ensodai.shared.avalon.persona.models.PersonaResponse
import app.data.network.models.response.ChatSessionResponse
import app.domain.models.NicknameUpdateResult
import app.service.paymentService.TariffService
import app.utils.dbQuery
import app.utils.toServerUrl
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class UserRepositoryApp(
    private val tariffService: TariffService
) {

    suspend fun findOrCreateUser(
        googleSub: String,
        name: String,
        email: String?,
        avatarUrl: String?
    ): AppUserInternalDto =
        findUserByGoogleSub(googleSub) ?: addUser(
            googleSub = googleSub,
            name = name,
            email = email,
            avatarUrl = avatarUrl
        )

    /**
     * Получает сущность AppUser по ID.
     */
    suspend fun findById(userId: Uuid): AppUser? {
        return dbQuery {
            AppUser.findById(userId)
        }
    }

    suspend fun findUserByUsername(username: String): AppUserInternalDto? {
        return dbQuery {
            AppUsers
                .selectAll()
                .where { (AppUsers.nickName eq username) and AppUsers.deletedAt.isNull() }
                .singleOrNull()
                ?.toAppUserInternalDto()
        }
    }

    suspend fun createUserWithPassword(
        username: String,
        passwordHash: String
    ): AppUserInternalDto {
        return dbQuery {
            val now = Clock.System.now()
            val newUserId = Uuid.random()

            AppUsers.insert {
                it[id] = newUserId
                it[AppUsers.googleSub] = null
                it[AppUsers.passwordHash] = passwordHash
                it[AppUsers.name] = username
                it[AppUsers.nickName] = username
                it[AppUsers.email] = null
                it[AppUsers.imagePath] = null
                it[AppUsers.currentDailyBalance] = 999999
                it[AppUsers.reserveBalance] = 999999
                it[AppUsers.premiumStatus] = com.ensodai.shared.avalon.gifts.models.PremiumStatus.ELITE
                it[AppUsers.createdAt] = now
                it[AppUsers.updatedAt] = now
            }

            UserAiSettings.insert {
                it[id] = Uuid.random()
                it[userId] = org.jetbrains.exposed.v1.core.dao.id.EntityID(newUserId, AppUsers)
                it[providerId] = com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE
                it[baseUrl] = "https://generativelanguage.googleapis.com"
                it[apiKey] = null
                it[textModel] = "gemini-2.5-flash"
                it[ttsModel] = "gemini-2.5-flash-preview-tts"
                it[imageModel] = "gemini-2.5-flash-image"
                it[imagePromptModel] = "gemini-2.5-flash"
                it[summaryModel] = "gemini-2.5-flash-lite"
                it[isCustomProviderEnabled] = false
                it[createdAt] = now
                it[updatedAt] = now
            }

            AppUserInternalDto(
                id = newUserId,
                googleSub = null,
                passwordHash = passwordHash,
                name = username,
                nickName = username,
                email = null,
                avatarUrl = null,
                currentQuotaLimit = 999999,
                lastQuotaReset = now.toEpochMilliseconds(),
                totalGeneratedMessages = 0,
                deletedAt = null,
                defaultPersona = null,
                isBanned = false,
                isShadowBanned = false,
                totalGeneratedTokens = 0,
                rarityProfileName = "CLASSIC_RPG",
                lastSeenAt = null,
                timezoneOffset = 0,
                isCreatorOutreachBlocked = false,
                isOutreachEnabled = false,
                dailyOutreachLimit = 3,
                isFlowModeEnabled = false
            )
        }
    }

    suspend fun findUserByGoogleSub(googleSub: String): AppUserInternalDto? {
        return dbQuery {
            AppUsers
                .selectAll()
                .where { AppUsers.googleSub eq googleSub }
                .singleOrNull()
                ?.toAppUserInternalDto()
        }
    }

    suspend fun addUser(
        googleSub: String,
        name: String,
        email: String?,
        avatarUrl: String?
    ): AppUserInternalDto {
        return dbQuery {
            val now = Clock.System.now()
            val newUserId = Uuid.random()
            
            AppUsers.insert {
                it[id] = newUserId
                it[AppUsers.googleSub] = googleSub
                it[AppUsers.name] = name
                it[AppUsers.email] = email
                it[AppUsers.imagePath] = avatarUrl
                it[AppUsers.nickName] = "user_${newUserId.toString().take(8)}"
                it[AppUsers.currentDailyBalance] = 0 
                it[AppUsers.reserveBalance] = 0
                it[AppUsers.createdAt] = now
                it[AppUsers.updatedAt] = now
            }

            UserAiSettings.insert {
                it[id] = Uuid.random()
                it[userId] = org.jetbrains.exposed.v1.core.dao.id.EntityID(newUserId, AppUsers)
                it[providerId] = com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE
                it[baseUrl] = "https://generativelanguage.googleapis.com"
                it[apiKey] = null
                it[textModel] = "gemini-2.5-flash"
                it[ttsModel] = "gemini-2.5-flash-preview-tts"
                it[imageModel] = "gemini-2.5-flash-image"
                it[imagePromptModel] = "gemini-2.5-flash"
                it[summaryModel] = "gemini-2.5-flash-lite"
                it[isCustomProviderEnabled] = false
                it[createdAt] = now
                it[updatedAt] = now
            }

            AppUserInternalDto(
                id = newUserId,
                googleSub = googleSub,
                name = name,
                nickName = "user_${newUserId.toString().take(8)}",
                email = email,
                avatarUrl = avatarUrl,
                currentQuotaLimit = 0,
                lastQuotaReset = now.toEpochMilliseconds(),
                totalGeneratedMessages = 0,
                deletedAt = null,
                defaultPersona = null,
                isBanned = false,
                isShadowBanned = false,
                totalGeneratedTokens = 0,
                rarityProfileName = "CLASSIC_RPG",
                lastSeenAt = now,
                timezoneOffset = 0,
                isCreatorOutreachBlocked = false,
                isOutreachEnabled = false,
                dailyOutreachLimit = 3,
                isFlowModeEnabled = false
            )
        }
    }

    suspend fun getUserAccountInfo(userId: Uuid): UserAccountInfoResponse? {
        return dbQuery {
            val user = AppUser.findById(userId) ?: return@dbQuery null
            val data = tariffService.getUserTariffInfo(userId)

            val sumExpression = AICharacters.unclaimedEnergy.sum()
            val totalUnclaimed = AICharacters
                .select(sumExpression)
                .where { AICharacters.owner eq userId }
                .singleOrNull()?.get(sumExpression) ?: 0.0

            UserAccountInfoResponse(
                name = user.name,
                nickName = user.nickName,
                id = user.id.value,
                avatarUrl = user.avatarUrl ?: "",
                currentQuota = data.currentQuota,
                maxQuota = data.maxQuota,
                reserveBalance = data.reserveBalance,
                maxReserveBalance = data.maxReserveBalance,
                premiumStatus = data.premiumStatus,
                defaultPersona = user.defaultPersona?.toResponse(),
                userPersonas = UserPersona.find { UserPersonas.userId eq userId }
                    .orderBy(UserPersonas.createdAt to SortOrder.DESC)
                    .map { it.toResponse() },
                paymentProvider = data.paymentProvider,
                nextBillingDate = data.nextBillingDate,
                totalUnclaimedCreatorEnergy = totalUnclaimed,
                isOutreachEnabled = user.isOutreachEnabled,
                dailyOutreachLimit = user.dailyOutreachLimit,
                isFlowModeEnabled = user.isFlowModeEnabled,
                isEarlySupporter = user.isEarlySupporter
            )
        }
    }

    suspend fun updateOutreachSettings(userId: Uuid, isOutreachEnabled: Boolean, dailyOutreachLimit: Int): Boolean {
        return dbQuery {
            val user = AppUser.findById(userId) ?: return@dbQuery false
            user.isOutreachEnabled = isOutreachEnabled
            user.dailyOutreachLimit = dailyOutreachLimit
            true
        }
    }

    suspend fun updateFlowModeSettings(userId: Uuid, isFlowModeEnabled: Boolean): Boolean {
        return dbQuery {
            val user = AppUser.findById(userId) ?: return@dbQuery false
            user.isFlowModeEnabled = isFlowModeEnabled
            true
        }
    }

    suspend fun isFlowModeEnabled(userId: Uuid): Boolean {
        return dbQuery {
            AppUser.findById(userId)?.isFlowModeEnabled ?: false
        }
    }

    suspend fun updateNickname(userId: Uuid, newNickname: String): NicknameUpdateResult {
        return dbQuery {
            val isTaken = AppUsers.selectAll().where { AppUsers.nickName eq newNickname }.count() > 0
            if (isTaken) return@dbQuery NicknameUpdateResult.NicknameTaken

            val updated = AppUsers.update({ AppUsers.id eq userId }) {
                it[nickName] = newNickname
                it[updatedAt] = Clock.System.now()
            }

            if (updated > 0) NicknameUpdateResult.Success else NicknameUpdateResult.UserNotFound
        }
    }

    suspend fun isNicknameAvailable(nickname: String): Boolean {
        return dbQuery {
            AppUsers.selectAll().where { AppUsers.nickName eq nickname }.count() == 0L
        }
    }

    suspend fun isUserShadowBanned(userId: Uuid): Boolean {
        return dbQuery {
            AppUsers.select(AppUsers.isShadowBanned)
                .where { AppUsers.id eq userId }
                .singleOrNull()
                ?.get(AppUsers.isShadowBanned) ?: false
        }
    }

    suspend fun applyShadowBan(nickname: String): Boolean {
        return dbQuery {
            val updated = AppUsers.update({ AppUsers.nickName eq nickname }) {
                it[isShadowBanned] = true
            }
            updated > 0
        }
    }

    suspend fun incrementUserStats(userId: Uuid, tokens: Int) {
        dbQuery {
            AppUsers.update({ AppUsers.id eq userId }) {
                it.update(totalGeneratedMessages, totalGeneratedMessages.plus(1))
                it.update(totalGeneratedTokens, totalGeneratedTokens.plus(tokens))
            }
        }
    }

    suspend fun ensureAndGetChatSession(userId: Uuid, characterId: Uuid): ChatSessionResponse {
        return dbQuery {
            val character = AICharacter.findById(characterId)
                ?: throw IllegalArgumentException("Персонаж с ID $characterId не найден")

            val isOwner = character.owner.value == userId
            if (character.visibility == CharacterVisibility.PRIVATE && !isOwner) {
                throw SecurityException("Пользователь не имеет доступа к этому приватному персонажу")
            }

            val existingSession = ChatSession.find {
                (ChatSessions.userId eq userId) and (ChatSessions.characterId eq characterId)
            }.singleOrNull()

            val chatSession = if (existingSession == null) {
                val now = Clock.System.now()
                val user = AppUser.findById(userId) ?: throw Exception("User not found")
                val newSession = ChatSession.new {
                    this.user = user
                    this.aiCharacter = character
                    this.isArchived = false
                    this.createdAt = now
                    this.updatedAt = now
                }

                ChatVariant.new {
                    this.chatSession = newSession
                    this.name = character.characterName
                    this.createdAt = now
                    this.updatedAt = now
                }
                newSession
            } else {
                existingSession
            }

            ChatSessionResponse(
                id = chatSession.id.value,
                characterId = characterId,
                isArchived = chatSession.isArchived,
                activeVariantId = chatSession.activeVariant?.id?.value,
                updatedAt = chatSession.updatedAt,
                variants = chatSession.variants.map { variant ->
                    ChatVariantResponse(
                        id = variant.id.value,
                        name = variant.name,
                        image = variant.chatVariantImage?.let {
                            ChatVariantImageResponse(
                                id = it.id.value,
                                imageUrl = it.imagePath.toServerUrl(),
                                thumbHash = it.thumbHash
                            )
                        },
                        sessionId = variant.chatSession.id.value,
                        updatedAt = variant.updatedAt,
                        createdAt = variant.createdAt
                    )
                }
            )
        }
    }

    suspend fun markAsDeleted(userId: Uuid) {
        dbQuery {
            AppUsers.update({ AppUsers.id eq userId }) {
                it[deletedAt] = Clock.System.now()
            }
        }
    }

    // --- Persona Management ---

    suspend fun createPersona(userId: Uuid, name: String, description: String): PersonaResponse {
        return dbQuery {
            val now = Clock.System.now()
            val persona = UserPersona.new {
                this.appUser = AppUser.findById(userId)!!
                this.personaName = name
                this.personaDescription = description
                this.createdAt = now
                this.updatedAt = now
            }
            persona.toResponse()
        }
    }

    suspend fun getPersonasByUserId(userId: Uuid): List<PersonaResponse> {
        return dbQuery {
            UserPersona.find { UserPersonas.userId eq userId }
                .orderBy(UserPersonas.createdAt to SortOrder.DESC)
                .map { it.toResponse() }
        }
    }

    suspend fun updatePersona(userId: Uuid, personaId: Uuid, name: String, description: String): PersonaResponse? {
        return dbQuery {
            val persona = UserPersona.findById(personaId) ?: return@dbQuery null
            if (persona.appUser.id.value != userId) return@dbQuery null

            persona.personaName = name
            persona.personaDescription = description
            persona.updatedAt = Clock.System.now()
            persona.toResponse()
        }
    }

    suspend fun deletePersona(userId: Uuid, personaId: Uuid): Boolean {
        return dbQuery {
            val persona = UserPersona.findById(personaId) ?: return@dbQuery false
            if (persona.appUser.id.value != userId) return@dbQuery false
            
            persona.deletedAt = Clock.System.now()
            true
        }
    }

    suspend fun selectPersona(userId: Uuid, personaId: Uuid?): Boolean {
        return dbQuery {
            val user = AppUser.findById(userId) ?: return@dbQuery false
            
            if (personaId != null) {
                val persona = UserPersona.findById(personaId) ?: return@dbQuery false
                if (persona.appUser.id.value != userId) return@dbQuery false
                user.defaultPersona = persona
            } else {
                user.defaultPersona = null
            }
            true
        }
    }

    // --- Chat Session Batch Operations ---

    suspend fun updateChatSessionsBatch(userId: Uuid, items: List<ChatUpdateItem>): Map<Uuid, Instant> {
        return dbQuery {
            val now = Clock.System.now()
            val results = mutableMapOf<Uuid, Instant>()
            
            items.forEach { item ->
                val updatedCount = ChatSessions.update({ (ChatSessions.id eq item.id) and (ChatSessions.userId eq userId) }) {
                    item.isArchived?.let { archived -> it[isArchived] = archived }
                    it[updatedAt] = now
                }
                if (updatedCount > 0) {
                    results[item.id] = now
                }
            }
            results
        }
    }

    suspend fun softDeleteChatSessionsBatch(userId: Uuid, sessionIds: List<Uuid>) {
        dbQuery {
            val now = Clock.System.now()
            ChatSessions.update({ (ChatSessions.id inList sessionIds) and (ChatSessions.userId eq userId) }) {
                it[deletedAt] = now
                it[updatedAt] = now
            }
        }
    }
}
