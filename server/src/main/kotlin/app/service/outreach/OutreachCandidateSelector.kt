package app.service.outreach

import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.users.AppUsers
import app.data.db.tables.users.CreatorAffinityTable
import app.data.db.tables.aiCharacter.OutreachHistoryTable
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import app.utils.dbQuery
import app.apiv4.chat.ChatEventBus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class CharacterInfo(
    val id: Uuid,
    val ownerId: Uuid,
    val name: String,
    val shortDescription: String,
    val fullDescription: String?,
    val temperature: Float,
    val interactionFormat: InteractionFormat,
    val harassment: Int,
    val hate: Int,
    val sexuallyExplicit: Int,
    val dangerousContent: Int,
    val topK: Int?,
    val topP: Float,
    val maxOutputTokens: Int,
    val maxContextTokens: Int
)

data class Candidate(
    val id: Uuid,
    val timezoneOffset: Int,
    val lastSeenAt: Instant,
    val dailyOutreachLimit: Int
)

data class CandidateWithAffinity(
    val candidate: Candidate,
    val messageCount: Int
)

@Single
class OutreachCandidateSelector(
    private val chatEventBus: ChatEventBus
) {
    private val log = KotlinLogging.logger {}

    /**
     * Получает список активных ИИ-персонажей, у которых включен аутрич и они не заблокированы.
     */
    suspend fun getActiveOutreachBots(): List<CharacterInfo> {
        return dbQuery {
            (AICharacters innerJoin AppUsers)
                .select(
                    AICharacters.id,
                    AICharacters.owner,
                    AICharacters.characterName,
                    AICharacters.shortDescription,
                    AICharacters.fullDescription,
                    AICharacters.temperature,
                    AICharacters.interactionFormat,
                    AICharacters.harassment,
                    AICharacters.hate,
                    AICharacters.sexuallyExplicit,
                    AICharacters.dangerousContent,
                    AICharacters.topK,
                    AICharacters.topP,
                    AICharacters.maxOutputTokens,
                    AICharacters.maxContextTokens
                )
                .where { 
                    (AICharacters.isOutreachEnabled eq true) and 
                    (AICharacters.isOutreachBlocked eq false) and 
                    (AICharacters.isBanned eq false) and
                    (AppUsers.isCreatorOutreachBlocked eq false)
                }
                .map { row ->
                    CharacterInfo(
                        id = row[AICharacters.id].value,
                        ownerId = row[AICharacters.owner].value,
                        name = row[AICharacters.characterName],
                        shortDescription = row[AICharacters.shortDescription],
                        fullDescription = row[AICharacters.fullDescription],
                        temperature = row[AICharacters.temperature],
                        interactionFormat = row[AICharacters.interactionFormat],
                        harassment = row[AICharacters.harassment],
                        hate = row[AICharacters.hate],
                        sexuallyExplicit = row[AICharacters.sexuallyExplicit],
                        dangerousContent = row[AICharacters.dangerousContent],
                        topK = row[AICharacters.topK],
                        topP = row[AICharacters.topP],
                        maxOutputTokens = row[AICharacters.maxOutputTokens],
                        maxContextTokens = row[AICharacters.maxContextTokens]
                    )
                }
        }
    }

    /**
     * Ищет лучшего пользователя-кандидата для конкретного бота.
     * Возвращает пару (пользователь, лояльность) или null, если подходящих нет.
     */
    suspend fun findBestCandidateForBot(bot: CharacterInfo, now: Instant): CandidateWithAffinity? {
        val sevenDaysAgo = now - 7.days
        val twentyFourHoursAgo = now - 24.hours

        return dbQuery {
            // А. Находим ID пользователей, у которых уже есть чат с этим ботом
            val usersWithSessions = ChatSessions
                .select(ChatSessions.userId)
                .where { ChatSessions.characterId eq bot.id }
                .map { it[ChatSessions.userId].value }
                .toSet()

            // Б. Находим ID пользователей, которым этот бот уже отправлял аутрич
            val usersWithOutreach = OutreachHistoryTable
                .select(OutreachHistoryTable.userId)
                .where { OutreachHistoryTable.characterId eq bot.id }
                .map { it[OutreachHistoryTable.userId].value }
                .toSet()

            // В. Находим количество полученных аутричей пользователями за последние 24 часа
            val recentOutreachList = OutreachHistoryTable
                .select(OutreachHistoryTable.userId)
                .where { OutreachHistoryTable.sentAt greaterEq twentyFourHoursAgo }
                .map { it[OutreachHistoryTable.userId].value }

            val recentOutreachCounts = recentOutreachList
                .groupingBy { it }
                .eachCount()

            // Г. Выбираем всех активных пользователей, заходивших за последние 7 дней, исключая автора бота
            val activeUsers = AppUsers
                .selectAll()
                .where { 
                    (AppUsers.deletedAt.isNull()) and 
                    (AppUsers.isBanned eq false) and 
                    (AppUsers.id neq bot.ownerId) and
                    (AppUsers.lastSeenAt.isNotNull()) and
                    (AppUsers.lastSeenAt greaterEq sevenDaysAgo) and
                    (AppUsers.isOutreachEnabled eq true) and
                    (AppUsers.dailyOutreachLimit greaterEq 1) and
                    (AppUsers.premiumStatus neq PremiumStatus.FREE)
                }
                .map { row ->
                    Candidate(
                        id = row[AppUsers.id].value,
                        timezoneOffset = row[AppUsers.timezoneOffset],
                        lastSeenAt = row[AppUsers.lastSeenAt]!!,
                        dailyOutreachLimit = row[AppUsers.dailyOutreachLimit]
                    )
                }

            log.info { "OutreachCandidateSelector: Кандидаты (не удалены, не забанены, не авторы бота, аутрич включен): ${activeUsers.map { it.id }}" }

            // Д. Фильтруем кандидатов по истории и Timezone Guard
            val validCandidates = activeUsers.filter { candidate ->
                candidate.id !in usersWithSessions &&
                candidate.id !in usersWithOutreach &&
                (recentOutreachCounts[candidate.id] ?: 0) < candidate.dailyOutreachLimit
            }.filter { candidate ->
                // Timezone Guard: локальное время пользователя строго между 09:00 и 22:00
                val localInstant = now + candidate.timezoneOffset.minutes
                val ktixInstant = kotlinx.datetime.Instant.fromEpochMilliseconds(localInstant.toEpochMilliseconds())
                val localHour = ktixInstant.toLocalDateTime(TimeZone.UTC).hour
                localHour in 9..21
            }

            log.info { "OutreachCandidateSelector: Прошли фильтрацию ( Timezone Guard и нет сессий с ботом): ${validCandidates.map { it.id }}" }

            if (validCandidates.isEmpty()) return@dbQuery null

            // Е. Получаем лояльность (creator_affinity) для подходящих кандидатов
            val candidateIds = validCandidates.map { it.id }
            val affinities = CreatorAffinityTable
                .select(CreatorAffinityTable.userId, CreatorAffinityTable.messageCount)
                .where { (CreatorAffinityTable.creatorId eq bot.ownerId) and (CreatorAffinityTable.userId inList candidateIds) }
                .associate { it[CreatorAffinityTable.userId].value to it[CreatorAffinityTable.messageCount] }

            // Ж. Сортируем кандидатов: сначала с теплой лояльностью (Tier 1), затем по свежести визита (Tier 4)
            val sorted = validCandidates.sortedWith(
                compareByDescending<Candidate> { affinities[it.id] ?: 0 }
                    .thenByDescending { it.lastSeenAt }
            )

            val bestCandidate = sorted.first()
            val messageCount = affinities[bestCandidate.id] ?: 0

            CandidateWithAffinity(bestCandidate, messageCount)
        }
    }
}
