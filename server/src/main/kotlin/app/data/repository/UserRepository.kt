package app.data.repository
import com.ensodai.shared.avalon.gifts.models.*
import kotlin.math.roundToInt

import app.data.db.tables.LlmConfig
import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.users.AppUser
import app.data.db.tables.users.AppUsers
import app.data.db.tables.users.EnergyOperationType
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import app.service.ChatActionType
import app.service.EnergyService
import app.data.models.EnergyDeductionResult
import app.data.models.UserEnergyBalance
import app.utils.dbQuery
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single
import app.service.relationship.CharacterRelationshipService
import com.ensodai.shared.avalon.relationship.RelationshipLevelCalculator

@Single
class UserRepository(
    private val energyService: EnergyService,
    private val characterRelationshipService: CharacterRelationshipService
) {


    suspend fun deductAndGetBalance(
        userId: Uuid,
        llmConfig: LlmConfig,
        actionType: ChatActionType,
        contentId: Uuid?,
        metaInfo: Map<String, String>
    ): EnergyDeductionResult {
        return dbQuery {
            // 0. Вычисляем скидку на основе уровня отношений
            val characterId = getCharacterIdFromMetaInfo(metaInfo)
            val relationshipLevel = if (characterId != null) {
                characterRelationshipService.getRelationshipLevel(userId, characterId)
            } else 1

            val discountFactor = RelationshipLevelCalculator.getEnergyDiscountFactor(relationshipLevel)
            val actualCost = (llmConfig.energyCost * discountFactor).roundToInt().coerceAtLeast(1)

            // 1. Блокируем строку
            val userRow = AppUsers
                .select(AppUsers.currentDailyBalance, AppUsers.reserveBalance)
                .where { AppUsers.id eq userId }
                .forUpdate()
                .singleOrNull()

            if (userRow == null) {
                return@dbQuery EnergyDeductionResult.UserNotFound
            }

            val currentReserve = userRow[AppUsers.reserveBalance]
            val currentDaily = userRow[AppUsers.currentDailyBalance]

            // СЦЕНАРИЙ: Недостаточно средств
            if ((currentReserve + currentDaily) < actualCost) {
                return@dbQuery EnergyDeductionResult.InsufficientFunds(
                    currentBalance = UserEnergyBalance(
                        dailyEnergy = currentDaily,
                        reserveEnergy = currentReserve
                    ),
                    requiredCost = actualCost
                )
            }

            // 3. Логика списания (Математика)
            var newReserve = currentReserve
            var newDaily = currentDaily
            var remainingCost = actualCost

            // Сначала списываем из резерва
            if (newReserve >= remainingCost) {
                newReserve -= remainingCost
                remainingCost = 0
            } else {
                remainingCost -= newReserve
                newReserve = 0
            }

            // Остаток из дневной квоты
            if (remainingCost > 0) {
                newDaily -= remainingCost
            }

            // 4. Обновляем базу
            AppUsers.update({ AppUsers.id eq userId }) {
                it[reserveBalance] = newReserve
                it[currentDailyBalance] = newDaily
            }

            // 5. ПИШЕМ ЛОГ
            val energyHistoryRecordId = energyService.spendForChatAction(
                userId = userId,
                cost = actualCost,
                actionType = actionType,
                modelName = llmConfig.name,
                contextId = contentId,
                metaInfo = metaInfo,
                actualDaily = newDaily,
                actualReserve = newReserve
            )

            // Начисление бонуса создателю персонажа
            if (characterId != null) {
                tryRewardCreator(userId, actualCost, characterId)
                characterRelationshipService.addExperience(userId, characterId, actualCost.toLong())
            }

            EnergyDeductionResult.Success(
                newBalance = UserEnergyBalance(newDaily, newReserve),
                energyHistoryRecordId = energyHistoryRecordId
            )
        }
    }

    suspend fun deductEnergyForGift(
        userId: Uuid,
        cost: Int,
        giftId: Uuid,
        contentId: Uuid?,
        metaInfo: Map<String, String>
    ): EnergyDeductionResult {
        return dbQuery {
            val userRow = AppUsers
                .select(AppUsers.currentDailyBalance, AppUsers.reserveBalance)
                .where { AppUsers.id eq userId }
                .forUpdate()
                .singleOrNull()

            if (userRow == null) {
                return@dbQuery EnergyDeductionResult.UserNotFound
            }

            val currentReserve = userRow[AppUsers.reserveBalance]
            val currentDaily = userRow[AppUsers.currentDailyBalance]

            if ((currentReserve + currentDaily) < cost) {
                return@dbQuery EnergyDeductionResult.InsufficientFunds(
                    currentBalance = UserEnergyBalance(currentDaily, currentReserve),
                    requiredCost = cost
                )
            }

            var newReserve = currentReserve
            var newDaily = currentDaily
            var remainingCost = cost

            if (newReserve >= remainingCost) {
                newReserve -= remainingCost
                remainingCost = 0
            } else {
                remainingCost -= newReserve
                newReserve = 0
            }

            if (remainingCost > 0) {
                newDaily -= remainingCost
            }

            AppUsers.update({ AppUsers.id eq userId }) {
                it[reserveBalance] = newReserve
                it[currentDailyBalance] = newDaily
            }

            val energyHistoryRecordId = energyService.spendForChatAction(
                userId = userId,
                cost = cost,
                actionType = ChatActionType.GIFT,
                modelName = "GIFT",
                contextId = contentId,
                metaInfo = metaInfo,
                actualDaily = newDaily,
                actualReserve = newReserve
            )

            // Начисление бонуса создателю персонажа
            val characterId = getCharacterIdFromMetaInfo(metaInfo)
            if (characterId != null) {
                tryRewardCreator(userId, cost, characterId)
                characterRelationshipService.addExperience(userId, characterId, cost.toLong())
            }

            EnergyDeductionResult.Success(
                newBalance = UserEnergyBalance(newDaily, newReserve),
                energyHistoryRecordId = energyHistoryRecordId
            )
        }
    }

    suspend fun deductLiveCallTurn(
        userId: Uuid,
        llmConfig: LlmConfig,
        metaInfo: Map<String, String>
    ): EnergyDeductionResult {
        return dbQuery {
            val userRow = AppUsers
                .select(AppUsers.currentDailyBalance, AppUsers.reserveBalance)
                .where { AppUsers.id eq userId }
                .forUpdate()
                .singleOrNull()

            if (userRow == null) {
                return@dbQuery EnergyDeductionResult.UserNotFound
            }

            val currentReserve = userRow[AppUsers.reserveBalance]
            val currentDaily = userRow[AppUsers.currentDailyBalance]

            if ((currentReserve + currentDaily) < llmConfig.energyCost) {
                return@dbQuery EnergyDeductionResult.InsufficientFunds(
                    currentBalance = UserEnergyBalance(
                        dailyEnergy = currentDaily,
                        reserveEnergy = currentReserve
                    ),
                    requiredCost = llmConfig.energyCost
                )
            }

            var newReserve = currentReserve
            var newDaily = currentDaily
            var remainingCost = llmConfig.energyCost

            if (newReserve >= remainingCost) {
                newReserve -= remainingCost
                remainingCost = 0
            } else {
                remainingCost -= newReserve
                newReserve = 0
            }

            if (remainingCost > 0) {
                newDaily -= remainingCost
            }

            AppUsers.update({ AppUsers.id eq userId }) {
                it[reserveBalance] = newReserve
                it[currentDailyBalance] = newDaily
            }

            val energyHistoryRecordId = energyService.spendForChatAction(
                userId = userId,
                cost = llmConfig.energyCost,
                actionType = ChatActionType.LIVE_CALL_TURN,
                modelName = llmConfig.name,
                contextId = null,
                metaInfo = metaInfo,
                actualDaily = newDaily,
                actualReserve = newReserve
            )

            // Начисление бонуса создателю персонажа
            val characterId = getCharacterIdFromMetaInfo(metaInfo)
            if (characterId != null) {
                tryRewardCreator(userId, llmConfig.energyCost, characterId)
                characterRelationshipService.addExperience(userId, characterId, llmConfig.energyCost.toLong())
            }

            EnergyDeductionResult.Success(
                newBalance = UserEnergyBalance(newDaily, newReserve),
                energyHistoryRecordId = energyHistoryRecordId
            )
        }
    }

    suspend fun deductEnergyForTts(
        userId: Uuid,
        voiceModelName: String,
        metaInfo: Map<String, String>
    ): EnergyDeductionResult {
        return dbQuery {
            val userRow = AppUsers
                .select(AppUsers.currentDailyBalance, AppUsers.reserveBalance)
                .where { AppUsers.id eq userId }
                .forUpdate()
                .singleOrNull()

            if (userRow == null) {
                return@dbQuery EnergyDeductionResult.UserNotFound
            }

            val currentReserve = userRow[AppUsers.reserveBalance]
            val currentDaily = userRow[AppUsers.currentDailyBalance]

            val cost = 1

            if ((currentReserve + currentDaily) < cost) {
                return@dbQuery EnergyDeductionResult.InsufficientFunds(
                    currentBalance = UserEnergyBalance(
                        dailyEnergy = currentDaily,
                        reserveEnergy = currentReserve
                    ),
                    requiredCost = cost
                )
            }

            var newReserve = currentReserve
            var newDaily = currentDaily
            var remainingCost = cost

            if (newReserve >= remainingCost) {
                newReserve -= remainingCost
                remainingCost = 0
            } else {
                remainingCost -= newReserve
                newReserve = 0
            }

            if (remainingCost > 0) {
                newDaily -= remainingCost
            }

            AppUsers.update({ AppUsers.id eq userId }) {
                it[reserveBalance] = newReserve
                it[currentDailyBalance] = newDaily
            }

            val energyHistoryRecordId = energyService.spendForChatAction(
                userId = userId,
                cost = cost,
                actionType = ChatActionType.TTS,
                modelName = voiceModelName,
                contextId = null,
                metaInfo = metaInfo,
                actualDaily = newDaily,
                actualReserve = newReserve
            )

            // Начисление бонуса создателю персонажа
            val characterId = getCharacterIdFromMetaInfo(metaInfo)
            if (characterId != null) {
                tryRewardCreator(userId, cost, characterId)
                characterRelationshipService.addExperience(userId, characterId, cost.toLong())
            }

            EnergyDeductionResult.Success(
                newBalance = UserEnergyBalance(newDaily, newReserve),
                energyHistoryRecordId = energyHistoryRecordId
            )
        }
    }

    suspend fun findById(userId: Uuid): AppUser? {
        return dbQuery {
            AppUser.findById(userId)
        }
    }

    suspend fun getUserEnergyBalance(userId: Uuid): UserEnergyBalance {
        return dbQuery {
            AppUsers
                .select(AppUsers.currentDailyBalance, AppUsers.reserveBalance)
                .where { AppUsers.id eq userId }
                .map { row ->
                    UserEnergyBalance(
                        dailyEnergy = row[AppUsers.currentDailyBalance],
                        reserveEnergy = row[AppUsers.reserveBalance]
                    )
                }
                .first()
        }
    }

    suspend fun getUserPremiumStatus(userId: Uuid): PremiumStatus {
        return dbQuery {
            AppUsers
                .select(AppUsers.premiumStatus)
                .where { AppUsers.id eq userId }
                .map { it[AppUsers.premiumStatus] }
                .singleOrNull()
                ?: PremiumStatus.FREE
        }
    }

    private fun getCharacterIdFromMetaInfo(metaInfo: Map<String, String>): Uuid? {
        val chatSessionIdStr = metaInfo["chat_session_id"]
        val chatVariantIdStr = metaInfo["chat_variant_id"]

        val chatSessionId = when {
            chatSessionIdStr != null -> {
                try { Uuid.parse(chatSessionIdStr) } catch (e: Exception) { null }
            }
            chatVariantIdStr != null -> {
                val variantId = try { Uuid.parse(chatVariantIdStr) } catch (e: Exception) { null }
                variantId?.let { vId ->
                    ChatVariants
                        .select(ChatVariants.chatSessionId)
                        .where { ChatVariants.id eq vId }
                        .map { it[ChatVariants.chatSessionId].value }
                        .singleOrNull()
                }
            }
            else -> null
        }

        return chatSessionId?.let { sId ->
            ChatSessions
                .select(ChatSessions.characterId)
                .where { ChatSessions.id eq sId }
                .map { it[ChatSessions.characterId].value }
                .singleOrNull()
        }
    }

    private fun tryRewardCreator(
        userId: Uuid,
        spentEnergy: Int,
        characterId: Uuid
    ) {
        val premiumStatus = AppUsers
            .select(AppUsers.premiumStatus)
            .where { AppUsers.id eq userId }
            .map { it[AppUsers.premiumStatus] }
            .singleOrNull() ?: PremiumStatus.FREE

        if (premiumStatus == PremiumStatus.FREE) return

        val charRow = AICharacters
            .select(AICharacters.owner, AICharacters.unclaimedEnergy, AICharacters.totalEarnedEnergy)
            .where { AICharacters.id eq characterId }
            .singleOrNull() ?: return

        val creatorId = charRow[AICharacters.owner].value

        if (userId == creatorId) return

        val bonus = spentEnergy * 0.20
        if (bonus <= 0.0) return

        val currentUnclaimed = charRow[AICharacters.unclaimedEnergy]
        val currentTotal = charRow[AICharacters.totalEarnedEnergy]

        AICharacters.update({ AICharacters.id eq characterId }) {
            it[unclaimedEnergy] = currentUnclaimed + bonus
            it[totalEarnedEnergy] = currentTotal + bonus
        }

        energyService.logBonusOperation(
            userId = creatorId,
            type = EnergyOperationType.ACCUMULATE_BONUS,
            amountChange = 0,
            dailyAfter = 0,
            reserveAfter = 0,
            meta = "bonus:$bonus;from_user:$userId",
            refId = characterId
        )
    }

    suspend fun deductEnergyForPhotoUnlock(
        userId: Uuid,
        cost: Int,
        characterImageId: Uuid,
        characterId: Uuid
    ): EnergyDeductionResult {
        return dbQuery {
            val userRow = AppUsers
                .select(AppUsers.currentDailyBalance, AppUsers.reserveBalance)
                .where { AppUsers.id eq userId }
                .forUpdate()
                .singleOrNull()

            if (userRow == null) {
                return@dbQuery EnergyDeductionResult.UserNotFound
            }

            val currentReserve = userRow[AppUsers.reserveBalance]
            val currentDaily = userRow[AppUsers.currentDailyBalance]

            if ((currentReserve + currentDaily) < cost) {
                return@dbQuery EnergyDeductionResult.InsufficientFunds(
                    currentBalance = UserEnergyBalance(currentDaily, currentReserve),
                    requiredCost = cost
                )
            }

            var newReserve = currentReserve
            var newDaily = currentDaily
            var remainingCost = cost

            if (newReserve >= remainingCost) {
                newReserve -= remainingCost
                remainingCost = 0
            } else {
                remainingCost -= newReserve
                newReserve = 0
            }

            if (remainingCost > 0) {
                newDaily -= remainingCost
            }

            AppUsers.update({ AppUsers.id eq userId }) {
                it[reserveBalance] = newReserve
                it[currentDailyBalance] = newDaily
            }

            val energyHistoryRecordId = energyService.spendForChatAction(
                userId = userId,
                cost = cost,
                actionType = ChatActionType.PHOTO_UNLOCK,
                modelName = "PHOTO_UNLOCK",
                contextId = characterImageId,
                metaInfo = mapOf("character_id" to characterId.toString()),
                actualDaily = newDaily,
                actualReserve = newReserve
            )

            // Начисление бонуса создателю персонажа
            tryRewardCreator(userId, cost, characterId)

            EnergyDeductionResult.Success(
                newBalance = UserEnergyBalance(newDaily, newReserve),
                energyHistoryRecordId = energyHistoryRecordId
            )
        }
    }
}
