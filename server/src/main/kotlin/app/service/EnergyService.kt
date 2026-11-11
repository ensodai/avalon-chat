
package app.service

import app.data.db.tables.LlmModels
import app.data.db.tables.users.AppUsers
import app.data.db.tables.users.EnergyHistory
import app.data.db.tables.users.EnergyOperationType
import app.data.db.tables.users.SubscriptionTiers
import app.utils.dbQuery
import app.utils.logger.WssLogger
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

enum class ChatActionType {
    REPLY,  // Обычный ответ
    FORCE,  // Кнопка "продолжить"
    REGEN,  // Свайп
    LIVE_CALL_TURN, // Завершённый live-call turn
    GIFT,    // Отправка подарка
    TTS,     // Озвучивание текста (TTS)
    PHOTO_UNLOCK // Разблокировка приватного фото
}

@Single
class EnergyService {

    class InsufficientEnergyException(message: String) : RuntimeException(message)

    /**
     * Универсальный метод списания за действия в чате.
     * Возвращает ID созданной записи в EnergyHistory для последующего обновления токенов.
     *
     * @param contextId - ID связанной сущности.
     *    - Для REPLY: ID сообщения юзера.
     *    - Для REGEN: ID сообщения, которое свайпаем.
     *    - Для FORCE: Можно передать NULL или ID чата (если база позволяет хранить не message UUID).
     * @param totalTokens - общее количество токенов (для метрик, можно 0 если ещё неизвестно)
     * @param promptTokens - токены промпта
     * @param outputTokens - токены ответа
     * @param cachedTokens - кэшированные токены
     * @param thoughtsTokens - токены размышлений (chain of thought)
     * @return ID записи в EnergyHistory
     */
    fun spendForChatAction(
        userId: Uuid,
        cost: Int,
        actionType: ChatActionType,
        modelName: String,
        contextId: Uuid? = null,
        metaInfo: Map<String, String> = emptyMap(),
        // Добавили аргументы, чтобы записать точный остаток после операции
        actualDaily: Int,
        actualReserve: Int,
        // Параметры для метрик токенов (могут быть 0 если ещё неизвестно)
        totalTokens: Long = 0,
        promptTokens: Long = 0,
        outputTokens: Long = 0,
        cachedTokens: Long = 0,
        thoughtsTokens: Long = 0
    ): Uuid {
        // Мапим тип действия
        val dbOperationType = when (actionType) {
            ChatActionType.REPLY -> EnergyOperationType.SPEND_CHAT_REPLY
            ChatActionType.FORCE -> EnergyOperationType.SPEND_CHAT_FORCE
            ChatActionType.REGEN -> EnergyOperationType.SPEND_CHAT_REGEN
            ChatActionType.LIVE_CALL_TURN -> EnergyOperationType.SPEND_LIVE_CALL_TURN
            ChatActionType.GIFT -> EnergyOperationType.SPEND_GIFT
            ChatActionType.TTS -> EnergyOperationType.SPEND_CHAT_TTS
            ChatActionType.PHOTO_UNLOCK -> EnergyOperationType.SPEND_PHOTO_UNLOCK
        }
        val idValue = Uuid.random()
        // Формируем мету
        val finalMeta = (metaInfo + ("model" to modelName))
            .entries.joinToString("; ") { "${it.key}:${it.value}" }

        transaction {
            EnergyHistory.insert {
                it[id] = idValue
                it[user] = userId
                it[operationType] = dbOperationType
                it[amount] = -cost
                it[dailyBalanceAfter] = actualDaily
                it[reserveBalanceAfter] = actualReserve
                it[metaData] = finalMeta
                it[this.relatedMessageId] = contextId
                // Статистика по токенам (может быть обновлена позже)
                it[this.totalTokens] = totalTokens
                it[this.promptTokens] = promptTokens
                it[this.outputTokens] = outputTokens
                it[this.cachedTokens] = cachedTokens
                it[this.thoughtsTokens] = thoughtsTokens
            }
        }
        return idValue
    }

    /**
     * Обновление статистики по токенам в существующей записи EnergyHistory.
     * Вызывается после получения ответа от LLM.
     * Также рассчитывает реальную стоимость в долларах.
     */
    fun updateTokenStats(
        recordId: Uuid,
        totalTokens: Long,
        promptTokens: Long,
        outputTokens: Long,
        cachedTokens: Long = 0,
        thoughtsTokens: Long = 0,
        modelName: String? = null,
        cost: Int? = null,
        modelCode: String? = null,  // Код модели для расчёта стоимости
        fixedCostUsd: BigDecimal? = null,  // Фиксированная стоимость (для Imagen)
        relatedMessageId: Uuid? = null, openRouterCostUsd: BigDecimal? = null
    ) {
        transaction {
            WssLogger.info("updateTokenStats: recordId=$recordId, modelName=$modelName, modelCode=$modelCode, cost=$cost, relatedMessageId=$relatedMessageId")

            // Сначала читаем текущую мету если нужно обновить модель
            val currentMetaForUpdate = if (modelName != null) {
                EnergyHistory.selectAll()
                    .where { EnergyHistory.id eq recordId }
                    .singleOrNull()?.get(EnergyHistory.metaData) ?: ""
            } else {
                null
            }

            // Читаем цены из llm_models для расчёта реальной стоимости
            val priceInput = modelCode?.let { code ->
                LlmModels.selectAll()
                    .where { LlmModels.code eq code }
                    .singleOrNull()?.get(LlmModels.pricePerMillionInput)
            } ?: BigDecimal.ZERO

            val priceOutput = modelCode?.let { code ->
                LlmModels.selectAll()
                    .where { LlmModels.code eq code }
                    .singleOrNull()?.get(LlmModels.pricePerMillionOutput)
            } ?: BigDecimal.ZERO

            // Считаем реальную стоимость в долларах
            val realCostUsd = if (openRouterCostUsd != null) {
                openRouterCostUsd
            } else if (fixedCostUsd != null) {
                // Для Imagen: токены подзапросов (Gemini) + фиксированная цена за изображение
                val tokenCost =
                    if (priceInput.compareTo(BigDecimal.ZERO) > 0 || priceOutput.compareTo(BigDecimal.ZERO) > 0) {
                        val inputCost = BigDecimal(promptTokens).divide(BigDecimal(1_000_000), 6, RoundingMode.HALF_UP)
                            .multiply(priceInput)
                        val outputCost = BigDecimal(outputTokens).divide(BigDecimal(1_000_000), 6, RoundingMode.HALF_UP)
                            .multiply(priceOutput)
                        inputCost.add(outputCost)
                    } else {
                        BigDecimal.ZERO
                    }
                tokenCost.add(fixedCostUsd)
            } else if (priceInput.compareTo(BigDecimal.ZERO) > 0 || priceOutput.compareTo(BigDecimal.ZERO) > 0) {
                // Для текстовых моделей - цена за токены
                val inputCost =
                    BigDecimal(promptTokens).divide(BigDecimal(1_000_000), 6, RoundingMode.HALF_UP).multiply(priceInput)
                val outputCost = BigDecimal(outputTokens).divide(BigDecimal(1_000_000), 6, RoundingMode.HALF_UP)
                    .multiply(priceOutput)
                inputCost.add(outputCost)
            } else {
                BigDecimal.ZERO
            }

            EnergyHistory.update({ EnergyHistory.id eq recordId }) {
                it[this.totalTokens] = totalTokens
                it[this.promptTokens] = promptTokens
                it[this.outputTokens] = outputTokens
                it[this.cachedTokens] = cachedTokens
                it[this.thoughtsTokens] = thoughtsTokens
                // Обновляем реальную стоимость
                it[this.realCostUsd] = realCostUsd
                // Обновляем модель если передана (для генерации картинок)
                modelName?.let { newModel ->
                    // Парсим текущую мету в мапу (так же как в spendForChatAction)
                    val metaMap = (currentMetaForUpdate ?: "").split("; ")
                        .filter { it.isNotBlank() }
                        .associate { pair ->
                            val parts = pair.split(":", limit = 2)
                            if (parts.size == 2) parts[0].trim() to parts[1].trim() else "" to ""
                        }.toMutableMap()
                    // Добавляем/обновляем модель
                    metaMap["model"] = newModel
                    // Формируем строку так же как в spendForChatAction
                    it[metaData] = metaMap.entries.joinToString("; ") { "${it.key}:${it.value}" }
                }
                // Обновляем стоимость если передана (для генерации картинок)
                cost?.let { newCost ->
                    it[amount] = -newCost
                }
                // Обновляем relatedMessageId если передан (для подарков)
                relatedMessageId?.let { newRelatedMessageId ->
                    it[this.relatedMessageId] = newRelatedMessageId
                }
            }

            WssLogger.info("updateTokenStats: realCostUsd=$realCostUsd (input=$priceInput, output=$priceOutput)")
        }
    }

    /**
     * 2 & 3. DAILY_RESET_GRANT и DAILY_ROLLOVER
     */
    suspend fun processDailyReset(userId: Uuid) {
        dbQuery {

            val userRow = AppUsers.selectAll()
                .where { AppUsers.id eq userId }
                .forUpdate()
                .singleOrNull() ?: return@dbQuery

            val planCode = userRow[AppUsers.premiumStatus]

            // Отключение восстановления квоты для бесплатных пользователей в связи с переходом на OpenRouter
            if (planCode == PremiumStatus.FREE) {
                return@dbQuery
            }

            val tierRow = SubscriptionTiers.selectAll()
                .where { SubscriptionTiers.code eq planCode }
                .singleOrNull() ?: return@dbQuery

            val dailyLimit = tierRow[SubscriptionTiers.dailyEnergy]

            val maxReserve = tierRow[SubscriptionTiers.maxCapacity]

            val oldDaily = userRow[AppUsers.currentDailyBalance]
            val oldReserve = userRow[AppUsers.reserveBalance]

            var amountToRollover = 0

            if (oldDaily > 0 && maxReserve > 0) {
                val spaceInReserve = maxReserve - oldReserve
                amountToRollover = maxOf(0, minOf(oldDaily, spaceInReserve))
            }

            val newReserve = oldReserve + amountToRollover

            if (amountToRollover > 0) {
                logHistory(
                    userId = userId,
                    type = EnergyOperationType.DAILY_ROLLOVER,
                    amountChange = amountToRollover,
                    dailyAfter = 0,
                    reserveAfter = newReserve,
                    meta = "Rollover capped at max: $maxReserve"
                )
            }

            AppUsers.update({ AppUsers.id eq userId }) {
                it[currentDailyBalance] = dailyLimit
                it[reserveBalance] = newReserve
                it[lastQuotaReset] = System.currentTimeMillis()
            }

            logHistory(
                userId = userId,
                type = EnergyOperationType.DAILY_RESET_GRANT,
                amountChange = dailyLimit,
                dailyAfter = dailyLimit,
                reserveAfter = newReserve,
                meta = "Daily reset tier: $planCode"
            )
        }
    }

    fun logBonusOperation(
        userId: Uuid,
        type: EnergyOperationType,
        amountChange: Int,
        dailyAfter: Int,
        reserveAfter: Int,
        meta: String? = null,
        refId: Uuid? = null
    ) {
        transaction {
            EnergyHistory.insert {
                it[user] = userId
                it[operationType] = type
                it[amount] = amountChange
                it[dailyBalanceAfter] = dailyAfter
                it[reserveBalanceAfter] = reserveAfter
                it[metaData] = meta
                it[relatedMessageId] = refId
            }
        }
    }

    // Вспомогательный метод
    private fun logHistory(
        userId: Uuid,
        type: EnergyOperationType,
        amountChange: Int,
        dailyAfter: Int,
        reserveAfter: Int,
        meta: String? = null,
        refId: String? = null
    ) {
        EnergyHistory.insert {
            it[user] = userId
            it[operationType] = type
            it[amount] = amountChange
            it[dailyBalanceAfter] = dailyAfter
            it[reserveBalanceAfter] = reserveAfter
            it[metaData] = meta
            it[relatedMessageId] = try {
                refId?.let { uuid -> Uuid.parse(uuid) }
            } catch (e: Exception) {
                null
            }
        }
    }
}
