package app.scheduler

import app.data.db.tables.aiCharacter.*
import app.data.db.tables.users.UserCharacterRelationships
import app.utils.PopularityCalculator
import app.utils.dbQuery
import app.cache.RedisService
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.select
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

@Single
class PopularityScheduler : KoinComponent {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val log = KotlinLogging.logger {}

    /**
     * Запускает фоновый цикл периодического пересчета популярности ботов (раз в 15 минут).
     */
    fun start() {
        log.info { "PopularityScheduler: Служба расчета популярности персонажей запущена." }
        scope.launch {
            while (isActive) {
                try {
                    recalculatePopularityCycle()
                    delay(15.minutes)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.error(e) { "PopularityScheduler: Ошибка в цикле расчета популярности" }
                    delay(1.minutes) // При ошибке ждем минуту и пробуем снова
                }
            }
        }
    }

    /**
     * Останавливает фоновую корутину планировщика.
     */
    fun stop() {
        scope.cancel()
        log.info { "PopularityScheduler: Служба остановлена." }
    }

    /**
     * Выполняет один цикл пересчета Trend Score для всех публичных незабаненных ботов.
     */
    suspend fun recalculatePopularityCycle() {
        log.info { "PopularityScheduler: Начало цикла пересчета популярности персонажей..." }
        val now = Clock.System.now()

        dbQuery {
            // 1. Получаем агрегированное количество уникальных собеседников по каждому боту
            val userCounts = UserCharacterRelationships
                .select(UserCharacterRelationships.characterId, UserCharacterRelationships.id.count())
                .groupBy(UserCharacterRelationships.characterId)
                .associate { it[UserCharacterRelationships.characterId].value to it[UserCharacterRelationships.id.count()] }

            // 2. Получаем список всех публичных незабаненных персонажей с их категориями и датой создания
            val activeCharacters = AICharacters
                .select(AICharacters.id, AICharacters.createdAt, AICharacters.category)
                .where { (AICharacters.visibility eq CharacterVisibility.PUBLIC) and (AICharacters.isBanned eq false) }
                .map { 
                    val id = it[AICharacters.id].value
                    val createdAt = it[AICharacters.createdAt]
                    val category = it[AICharacters.category]
                    CharacterInfo(id, createdAt, category)
                }

            // 3. Получаем всю текущую статистику (сообщения и дату последнего использования)
            val stats = AICharacterStats
                .selectAll()
                .associate { it[AICharacterStats.characterId].value to (it[AICharacterStats.messageCount] to it[AICharacterStats.lastInteractionAt]) }

            log.info { "PopularityScheduler: Пересчет для ${activeCharacters.size} персонажей." }

            val categoriesToInvalidate = mutableSetOf<String>()

            // 4. Пересчитываем в памяти и обновляем БД
            activeCharacters.forEach { char ->
                val userCount = userCounts[char.id] ?: 0L
                val (msgCount, lastInteraction) = stats[char.id] ?: (0L to char.createdAt)

                val score = PopularityCalculator.calculate(
                    usersCount = userCount,
                    messageCount = msgCount,
                    lastInteractionAt = lastInteraction,
                    createdAt = char.createdAt,
                    now = now
                )

                // Обновляем статистику популярности в БД
                AICharacterStats.update({ AICharacterStats.characterId eq char.id }) {
                    it[popularityScore] = score
                }

                categoriesToInvalidate.add(char.category)
            }

            // 5. Массовая инвалидация кэша измененных категорий в Redis
            categoriesToInvalidate.forEach { category ->
                RedisService.invalidateCategoryCache(category)
            }

            log.info { "PopularityScheduler: Цикл успешно завершен. Инвалидировано ${categoriesToInvalidate.size} категорий." }
        }
    }

    private data class CharacterInfo(
        val id: kotlin.uuid.Uuid,
        val createdAt: kotlin.time.Instant,
        val category: String
    )
}
