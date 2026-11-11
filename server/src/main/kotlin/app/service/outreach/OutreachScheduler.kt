package app.service.outreach

import app.service.LlmResult
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.days
import kotlin.uuid.Uuid

@Single
class OutreachScheduler(
    private val selector: OutreachCandidateSelector,
    private val generator: OutreachGreetingGenerator,
    private val sender: OutreachGreetingSender
) : KoinComponent {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val log = KotlinLogging.logger {}

    /**
     * Запускает фоновую службу планировщика рассылок приветствий.
     * Создает бесконечный цикл в корутине с 15-минутным интервалом.
     */
    fun start() {
        scope.launch {
            while (isActive) {
                try {
                    runOutreachCycle()
                    delay(15.minutes)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.error(e) { "OutreachScheduler: Ошибка в цикле планировщика" }
                    delay(1.minutes) // При ошибке ждем минуту и пробуем снова
                }
            }
        }
    }

    /**
     * Останавливает фоновую корутину планировщика рассылок.
     */
    fun stop() {
        scope.cancel()
    }

    /**
     * Выполняет один цикл рассылки приветствий.
     * Сканирует активных ботов и подбирает для них наиболее подходящих пользователей.
     */
    suspend fun runOutreachCycle() {
        val now = Clock.System.now()

        // 1. Получаем список активных персонажей, у которых включен Outreach
        val characters = selector.getActiveOutreachBots()

        for (bot in characters) {
            try {
                // 10% вероятность на каждом тике планировщика (раз в 15 минут)
                if (kotlin.random.Random.nextFloat() >= 0.10f) {
                    continue
                }

                // 2. Ищем лучшего пользователя-кандидата для этого бота
                val candidateWithAffinity = selector.findBestCandidateForBot(bot, now)
                if (candidateWithAffinity != null) {
                    val (candidate, messageCount) = candidateWithAffinity
                    sendOutreachGreeting(bot, candidate.id, messageCount)
                }
            } catch (e: Exception) {
                log.error(e) { "OutreachScheduler: Ошибка обработки бота ${bot.name}" }
            }
        }
    }

    /**
     * Генерирует уникальное приветствие и отправляет его пользователю.
     */
    private suspend fun sendOutreachGreeting(bot: CharacterInfo, userId: Uuid, messageCount: Int) {
        val llmResult = generator.generateGreeting(bot, userId, messageCount)

        if (llmResult is LlmResult.Success) {
            val text = llmResult.text
            val reaction = llmResult.reaction

            val sessionId = Uuid.random()
            val variantId = Uuid.random()

            // Сохраняем в БД и транслируем WebSocket
            sender.saveAndSendGreeting(
                bot = bot,
                userId = userId,
                text = text,
                reaction = reaction,
                sessionId = sessionId,
                variantId = variantId
            )
        } else {
            val errMessage = (llmResult as? LlmResult.Error)?.exception?.message ?: "Блокировка или пустой ответ"
            log.error { "OutreachScheduler: Ошибка генерации приветствия для бота ${bot.name}: $errMessage" }
        }
    }
}
