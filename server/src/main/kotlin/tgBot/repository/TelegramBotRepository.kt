package tgBot.repository

import app.data.db.tables.ApiKeys
import app.data.db.tables.Category
import app.data.db.tables.aiCharacter.AICharacter
import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.images.CharacterImage
import app.data.db.tables.users.AppUser
import app.data.db.tables.users.AppUsers
import app.utils.dbQuery
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.uuid.Uuid

data class BotUserSummary(
    val id: Uuid,
    val nickName: String,
    val name: String,
    val premiumStatus: PremiumStatus,
    val isBanned: Boolean,
    val isShadowBanned: Boolean,
    val totalMessages: Int,
    val currentBalance: Int,
    val createdAt: String
)

@Single
class TelegramBotRepository {

    suspend fun getDatabaseStatistics(): DatabaseStats {
        return dbQuery {
            val totalUsers = AppUser.all().count()
            val aliveUsers = AppUser.count(AppUsers.deletedAt.isNull())

            val usersByPremiumStatus = AppUsers
                .select(AppUsers.premiumStatus, AppUsers.id.count())
                .groupBy(AppUsers.premiumStatus)
                .associate { it[AppUsers.premiumStatus].name to it[AppUsers.id.count()] }

            val totalCharacters = AICharacter.all().count()
            val publicCharacters = AICharacter.count(AICharacters.visibility eq CharacterVisibility.PUBLIC)

            val totalCharacterImages = CharacterImage.all().count()
            val totalCategories = Category.all().count()

            val totalMessagesSum = AppUsers.totalGeneratedMessages.sum()
            val totalMessages = AppUsers
                .select(totalMessagesSum)
                .firstOrNull()
                ?.get(totalMessagesSum)?.toLong() ?: 0L

            val totalApiKeys = ApiKeys
                .select(ApiKeys.id)
                .where { ApiKeys.deletedAt.isNull() }
                .count()

            DatabaseStats(
                totalUsers = totalUsers,
                totalAliveUsers = aliveUsers,
                totalDeletedUsers = totalUsers - aliveUsers,
                usersByPremiumStatus = usersByPremiumStatus,
                totalCharacters = totalCharacters,
                totalPublicCharacters = publicCharacters,
                totalPrivateCharacters = totalCharacters - publicCharacters,
                totalCharacterImages = totalCharacterImages,
                totalCategories = totalCategories,
                totalGeneratedMessages = totalMessages,
                totalApiKeys = totalApiKeys
            )
        }
    }

    suspend fun getRecentUsers(limit: Int = 10): List<BotUserSummary> {
        return dbQuery {
            AppUsers
                .selectAll()
                .where { AppUsers.deletedAt.isNull() }
                .orderBy(AppUsers.createdAt to SortOrder.DESC)
                .limit(limit)
                .map { row ->
                    BotUserSummary(
                        id = row[AppUsers.id].value,
                        nickName = row[AppUsers.nickName],
                        name = row[AppUsers.name],
                        premiumStatus = row[AppUsers.premiumStatus],
                        isBanned = row[AppUsers.isBanned],
                        isShadowBanned = row[AppUsers.isShadowBanned],
                        totalMessages = row[AppUsers.totalGeneratedMessages],
                        currentBalance = row[AppUsers.currentDailyBalance],
                        createdAt = row[AppUsers.createdAt].toString().take(10)
                    )
                }
        }
    }

    suspend fun findUserById(userId: Uuid): BotUserSummary? {
        return dbQuery {
            AppUsers
                .selectAll()
                .where { AppUsers.id eq userId }
                .singleOrNull()
                ?.let { row ->
                    BotUserSummary(
                        id = row[AppUsers.id].value,
                        nickName = row[AppUsers.nickName],
                        name = row[AppUsers.name],
                        premiumStatus = row[AppUsers.premiumStatus],
                        isBanned = row[AppUsers.isBanned],
                        isShadowBanned = row[AppUsers.isShadowBanned],
                        totalMessages = row[AppUsers.totalGeneratedMessages],
                        currentBalance = row[AppUsers.currentDailyBalance],
                        createdAt = row[AppUsers.createdAt].toString().take(10)
                    )
                }
        }
    }

    suspend fun findUserByQuery(query: String): BotUserSummary? {
        val trimmed = query.trim()
        val uuidAttempt = try { Uuid.parse(trimmed) } catch (_: Exception) { null }

        return dbQuery {
            val condition = if (uuidAttempt != null) {
                AppUsers.id eq uuidAttempt
            } else {
                (AppUsers.nickName eq trimmed) or (AppUsers.name eq trimmed)
            }

            AppUsers
                .selectAll()
                .where { condition and AppUsers.deletedAt.isNull() }
                .firstOrNull()
                ?.let { row ->
                    BotUserSummary(
                        id = row[AppUsers.id].value,
                        nickName = row[AppUsers.nickName],
                        name = row[AppUsers.name],
                        premiumStatus = row[AppUsers.premiumStatus],
                        isBanned = row[AppUsers.isBanned],
                        isShadowBanned = row[AppUsers.isShadowBanned],
                        totalMessages = row[AppUsers.totalGeneratedMessages],
                        currentBalance = row[AppUsers.currentDailyBalance],
                        createdAt = row[AppUsers.createdAt].toString().take(10)
                    )
                }
        }
    }

    suspend fun toggleBan(userId: Uuid): Boolean? {
        return dbQuery {
            val user = AppUser.findById(userId) ?: return@dbQuery null
            val newStatus = !user.isBanned
            user.isBanned = newStatus
            newStatus
        }
    }

    suspend fun updateUserPremium(userId: Uuid, status: PremiumStatus): Boolean {
        return dbQuery {
            val updated = AppUsers.update({ AppUsers.id eq userId }) {
                it[premiumStatus] = status
                it[updatedAt] = Clock.System.now()
            }
            updated > 0
        }
    }
}