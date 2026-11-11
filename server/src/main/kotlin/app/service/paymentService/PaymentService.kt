package app.service.paymentService
import com.ensodai.shared.avalon.gifts.models.*

import com.ensodai.shared.avalon.gifts.models.*
import app.data.db.tables.users.AppUsers
import app.data.db.tables.users.*
import app.data.repository.GlobalSettingsRepository
import app.domain.utils.ErrorType
import app.domain.utils.ServiceResult
import app.utils.dbQuery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Single
import java.math.BigDecimal
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val paymentLog = KotlinLogging.logger("Payments")

class PaymentService(
    private val validator: BillingValidatorManager,
    private val tariffService: TariffService,
    private val globalSettingsRepository: GlobalSettingsRepository,
    private val notificationService: PaymentNotificationService
) {

    // --- 1. ОБРАБОТКА ПОДПИСОК ---
    suspend fun validateSubscription(
        userId: Uuid,
        purchaseId: String,
        productId: String,
        provider: SubscriptionProvider,
        isSandbox: Boolean
    ): ServiceResult<SubscriptionStatusResponse> {

        paymentLog.info { "🔍 [VALIDATE_SUB] User: $userId, Product: $productId" }

        val validation = validator.validateSubscription(
            purchaseId = purchaseId,
            productId = productId,
            provider = provider,
            isSandbox = isSandbox
        )

        if (validation.isTestPurchase) paymentLog.warn { "🧪 TEST PURCHASE" }
        if (validation.countryCode != null) paymentLog.info { "🌍 REGION: ${validation.countryCode}" }

        // Проверяем существование подписки ДО обработки (для определения isRenewal)
        val existingSubRow = dbQuery {
            Subscriptions.selectAll()
                .where { Subscriptions.purchaseId eq purchaseId }
                .singleOrNull()
        }
        
        val isRenewal = existingSubRow != null

        val result = dbQuery {
            val now = Clock.System.now()

            // 1. ЛОГИКА ПЕРЕНОСА (TRANSFER)
            if (existingSubRow != null) {
                val ownerId = existingSubRow[Subscriptions.user]
                if (ownerId.value != userId) {
                    paymentLog.warn { "🔀 [TRANSFER] Token linked to User $ownerId. Moving to User $userId." }

//                    AppUsers.update({ AppUsers.id eq ownerId }) {
//                        it[premiumStatus] = PremiumStatus.FREE
//                    }

//                    Subscriptions.update({ Subscriptions.purchaseId eq purchaseId }) {
//                        it[user] = userId
//                    }
                }
            }

            if (!validation.isValid) {
                // 2. ЗАЩИТА ОТ СБОЕВ СЕТИ
                if (validation.isNetworkError) {
                    paymentLog.warn { "⚠️ [VALIDATE_SUB] Network error. Checking DB fallback." }

                    if (existingSubRow != null) {
                        val dbExpireDate = existingSubRow[Subscriptions.expiresAt]

                        if (dbExpireDate > now) {
                            paymentLog.info { "🛡️ [RECOVERY] DB says valid until $dbExpireDate. Restoring Premium." }

                            val cachedPlan = existingSubRow[Subscriptions.plan]
                            val tierConfig = tariffService.getConfig(cachedPlan)

                            Subscriptions.update({ Subscriptions.purchaseId eq purchaseId }) {
                                it[status] = SubscriptionStatus.ACTIVE
                            }

                            // --- ПОЛУЧАЕМ ТЕКУЩИЕ ДАННЫЕ ЮЗЕРА ---
                            val currentUserRow = AppUsers.selectAll()
                                .where { AppUsers.id eq userId }
                                .single()

                            val currentStatusInDb = currentUserRow[AppUsers.premiumStatus]
                            // Запоминаем текущий баланс из базы
                            var finalBalance = currentUserRow[AppUsers.currentDailyBalance]

                            AppUsers.update({ AppUsers.id eq userId }) {
                                it[premiumStatus] = cachedPlan

                                // Обновляем баланс ТОЛЬКО если статус изменился
                                if (currentStatusInDb != cachedPlan) {
                                    it[currentDailyBalance] = tierConfig.maxDailyEnergy
                                    finalBalance = tierConfig.maxDailyEnergy // Обновляем переменную для ответа
                                    paymentLog.info { "🔋 [RECOVERY] Status changed ($currentStatusInDb -> $cachedPlan). Energy refilled." }
                                }
                            }

                            return@dbQuery ServiceResult.Success(
                                SubscriptionStatusResponse(
                                    userId = userId,
                                    isActive = true,
                                    expirationDate = dbExpireDate,
                                    isAutoRenewing = existingSubRow[Subscriptions.isAutoRenewing],
                                    premiumStatus = cachedPlan,
                                    maxDailyEnergy = tierConfig.maxDailyEnergy,
                                    // ВАЖНО: Возвращаем актуальный баланс (старый или сброшенный)
                                    currentDailyEnergy = finalBalance,
                                    maxReserveBalance = tierConfig.maxReserveBalance,
                                    paymentProvider = provider,
                                    nextBillingDate = dbExpireDate
                                )
                            )
                        }
                    }
                    return@dbQuery ServiceResult.Error(ErrorType.SERVER_ERROR, "Validation service unavailable")
                }

                // 3. DOWNGRADE (Если реально истекла)
                return@dbQuery processInvalidSubscription(
                    userId = userId,
                    purchaseId = purchaseId,
                    existingSub = existingSubRow,
                    provider = provider
                )
            }

            // 4. ВАЛИДАЦИЯ УСПЕШНА
            return@dbQuery processValidSubscription(
                userId = userId,
                purchaseId = purchaseId,
                productId = productId,
                provider = provider,
                validation = validation,
                existingSub = existingSubRow
            )
        }

        // Отправляем уведомление после успешной обработки (вне транзакции)
        if (result is ServiceResult.Success) {
            val newExpirationDate = result.data.expirationDate ?: Clock.System.now()
            val oldExpirationDate = existingSubRow?.get(Subscriptions.expiresAt)

            // Сравниваем даты с точностью до секунд, чтобы избежать багов с миллисекундами
            val datesAreDifferent = oldExpirationDate?.toEpochMilliseconds() != newExpirationDate.toEpochMilliseconds()

            val isBrandNewPurchase = existingSubRow == null
            val isActualRenewal = existingSubRow != null && datesAreDifferent

            // Отправляем уведомление ТОЛЬКО если это реально новая покупка или сдвинулась дата (продление)
            if (isBrandNewPurchase || isActualRenewal) {
                notificationService.sendNewSubscriptionNotification(
                    userId = userId,
                    plan = result.data.premiumStatus,
                    amount = microsToBigDecimal(validation.priceAmountMicros),
                    currency = validation.priceCurrencyCode,
                    provider = provider,
                    expirationDate = newExpirationDate,
                    isAutoRenewing = validation.isAutoRenewing,
                    isTestPurchase = validation.isTestPurchase,
                    countryCode = validation.countryCode,
                    isRenewal = isActualRenewal // Передаем правильный флаг
                )
            } else {
                paymentLog.info { "ℹ️ [VALIDATE_SUB] Routine check for User: $userId. Expiry unchanged, no notification sent." }
            }
        }

        return result
    }
    // --- СЦЕНАРИЙ 1: ПОДПИСКА НЕВАЛИДНА (ИСТЕКЛА/ОТМЕНЕНА) ---
    private fun processInvalidSubscription(
        userId: Uuid,
        purchaseId: String,
        existingSub: ResultRow?,
        provider: SubscriptionProvider
    ): ServiceResult<SubscriptionStatusResponse> {

        if (existingSub == null) {
            paymentLog.warn { "⛔ [VALIDATE_SUB] Invalid token for User: $userId (Not found in DB)" }
            return ServiceResult.Error(ErrorType.BAD_REQUEST, "Invalid subscription token")
        }

        val now = Clock.System.now()

        // 1. Метим подписку как истекшую
        Subscriptions.update({ Subscriptions.purchaseId eq purchaseId }) {
            it[status] = SubscriptionStatus.EXPIRED
            it[lastValidationDate] = now
        }

        // 2. Ищем другие активные подписки пользователя
        val activeSubs = Subscriptions.selectAll()
            .where {
                (Subscriptions.user eq userId) and
                (Subscriptions.status eq SubscriptionStatus.ACTIVE) and
                (Subscriptions.expiresAt greater now)
            }
            .toList()

        val maxActiveSub = activeSubs.maxByOrNull { it[Subscriptions.plan].ordinal }

        val (finalStatus, config) = if (maxActiveSub != null) {
            val plan = maxActiveSub[Subscriptions.plan]
            paymentLog.info { "🛡️ [DOWNGRADE_PREVENTED] User: $userId has another active subscription: $plan (expires at ${maxActiveSub[Subscriptions.expiresAt]})" }
            plan to tariffService.getConfig(plan)
        } else {
            paymentLog.info { "📉 [DOWNGRADE] User: $userId has no other active subscriptions. Downgrading to FREE." }
            PremiumStatus.FREE to tariffService.getConfig(PremiumStatus.FREE)
        }

        AppUsers.update({ AppUsers.id eq userId }) {
            it[premiumStatus] = finalStatus
            it[currentDailyBalance] = config.maxDailyEnergy
            it[reserveBalance] = config.maxReserveBalance
        }

        return ServiceResult.Success(
            SubscriptionStatusResponse(
                userId = userId,
                isActive = maxActiveSub != null,
                expirationDate = maxActiveSub?.get(Subscriptions.expiresAt),
                isAutoRenewing = maxActiveSub?.get(Subscriptions.isAutoRenewing) ?: false,
                premiumStatus = finalStatus,
                maxDailyEnergy = config.maxDailyEnergy,
                currentDailyEnergy = config.maxDailyEnergy,
                maxReserveBalance = config.maxReserveBalance,
                paymentProvider = maxActiveSub?.get(Subscriptions.provider) ?: provider,
                nextBillingDate = maxActiveSub?.get(Subscriptions.expiresAt)
            )
        )
    }

    // --- СЦЕНАРИЙ 2: ПОДПИСКА ВАЛИДНА ---
    private fun processValidSubscription(
        userId: Uuid,
        purchaseId: String,
        productId: String,
        provider: SubscriptionProvider,
        validation: PaymentValidationResult,
        existingSub: ResultRow?
    ): ServiceResult<SubscriptionStatusResponse> {

        val now = Clock.System.now()
        val newExpirationDate = validation.expirationDate ?: now.plus(30.days)
        val planType = tariffService.getConfigByCodeName(productId)

        if (existingSub != null) {
            // Это продление (Renewal)
            processRenewal(
                userId = userId,
                purchaseId = purchaseId,
                productId = productId,
                planType = planType.status,
                validation = validation,
                existingSub = existingSub,
                newExpirationDate = newExpirationDate,
                provider = provider
            )
        } else {
            // Это новая покупка или апгрейд
            processNewPurchaseOrUpgrade(
                userId = userId,
                purchaseId = purchaseId,
                productId = productId,
                planType = planType.status,
                validation = validation,
                newExpirationDate = newExpirationDate,
                provider = provider
            )
        }

        return ServiceResult.Success(
            SubscriptionStatusResponse(
                userId = userId,
                isActive = true,
                expirationDate = newExpirationDate,
                isAutoRenewing = validation.isAutoRenewing,
                premiumStatus = planType.status,
                maxDailyEnergy = planType.maxDailyEnergy,
                currentDailyEnergy = planType.maxDailyEnergy,
                maxReserveBalance = planType.maxReserveBalance,
                paymentProvider = provider,
                nextBillingDate = newExpirationDate,
            )
        )
    }

    // --- ПОД-СЦЕНАРИЙ 2.1: ПРОДЛЕНИЕ ---
    private fun processRenewal(
        userId: Uuid,
        purchaseId: String,
        productId: String,
        planType: PremiumStatus,
        validation: PaymentValidationResult,
        existingSub: ResultRow,
        newExpirationDate: Instant,
        provider: SubscriptionProvider
    ) {
        val oldExpiry = existingSub[Subscriptions.expiresAt]
        val now = Clock.System.now()

        // Обновляем данные подписки
        Subscriptions.update({ Subscriptions.purchaseId eq purchaseId }) {
            it[expiresAt] = newExpirationDate
            it[lastOrderId] = validation.linkedOrderId
            it[lastValidationDate] = now
            it[status] = SubscriptionStatus.ACTIVE
            it[plan] = planType
            it[this.country] = validation.countryCode
            it[this.isTestPurchase] = validation.isTestPurchase
        }

        // Получаем текущего пользователя для проверки статуса
        val currentUserRow = AppUsers.selectAll()
            .where { AppUsers.id eq userId }
            .singleOrNull()

        val currentStatusInDb = currentUserRow?.get(AppUsers.premiumStatus)

        // Логируем транзакцию только если дата сдвинулась (реальное продление)
        if (oldExpiry != newExpirationDate) {
            paymentLog.info { "✅ [RENEWAL] User: $userId. Plan: $planType. Old: $oldExpiry -> New: $newExpirationDate" }

            val amount = microsToBigDecimal(validation.priceAmountMicros)
            val currency = validation.priceCurrencyCode

            recordTransaction(
                userId = userId,
                type = TransactionType.SUBSCRIPTION_RENEWAL,
                provider = provider,
                productId = productId,
                transactionId = validation.linkedOrderId,
                relatedToken = purchaseId,
                amount = amount,
                currency = currency,
                country = validation.countryCode,
                isTestPurchase = validation.isTestPurchase
            )

            // При продлении обновляем только Daily Balance
            val tierConfig = tariffService.getConfig(planType)
            AppUsers.update({ AppUsers.id eq userId }) {
                it[premiumStatus] = planType
                it[currentDailyBalance] = tierConfig.maxDailyEnergy
            }
        } else if (currentStatusInDb != planType) {
            // Если дата окончания не изменилась (рутинная проверка), но статус пользователя в БД сбился (например, стал FREE)
            paymentLog.info { "🛡️ [RESTORE_STATUS] User: $userId status mismatch. DB: $currentStatusInDb, Expected: $planType. Restoring status." }
            val tierConfig = tariffService.getConfig(planType)
            AppUsers.update({ AppUsers.id eq userId }) {
                it[premiumStatus] = planType
                it[currentDailyBalance] = tierConfig.maxDailyEnergy
            }
        }
    }

    // --- ПОД-СЦЕНАРИЙ 2.2: НОВАЯ ПОКУПКА / АПГРЕЙД ---
    private fun processNewPurchaseOrUpgrade(
        userId: Uuid,
        purchaseId: String,
        productId: String,
        planType: PremiumStatus,
        validation: PaymentValidationResult,
        newExpirationDate: Instant,
        provider: SubscriptionProvider
    ) {
        val now = Clock.System.now()

        // 1. Проверяем Апгрейд (Linked Token)
        if (validation.linkedPurchaseToken != null) {
            handleUpgradeLink(validation.linkedPurchaseToken)
        }

        // 2. Вставляем новую подписку
        Subscriptions.insert {
            it[user] = userId
            it[status] = SubscriptionStatus.ACTIVE
            it[this.provider] = provider
            it[this.plan] = planType
            it[this.productId] = productId
            it[this.purchaseId] = purchaseId
            it[lastOrderId] = validation.linkedOrderId
            it[startDate] = now
            it[expiresAt] = newExpirationDate
            it[isAutoRenewing] = validation.isAutoRenewing
            it[this.country] = validation.countryCode
            it[this.isTestPurchase] = validation.isTestPurchase
        }

        paymentLog.info { "🎉 [NEW_SUB/UPGRADE] User: $userId. Plan: $planType. Expires: $newExpirationDate" }

        // 3. Пишем транзакцию
        val amount = microsToBigDecimal(validation.priceAmountMicros)
        val currency = validation.priceCurrencyCode

        recordTransaction(
            userId = userId,
            type = TransactionType.SUBSCRIPTION_PURCHASE,
            provider = provider,
            productId = productId,
            transactionId = validation.linkedOrderId,
            relatedToken = purchaseId,
            amount = amount,
            currency = currency,
            country = validation.countryCode,
            isTestPurchase = validation.isTestPurchase
        )

        // 4. Обновляем юзера (Full Reset)
        val tierConfig = tariffService.getConfig(planType)
        AppUsers.update({ AppUsers.id eq userId }) {
            it[premiumStatus] = planType
            it[currentDailyBalance] = tierConfig.maxDailyEnergy
        }
    }

    // Хелпер для отключения старой подписки
    private fun handleUpgradeLink(linkedToken: String) {
        paymentLog.info { "🔄 [UPGRADE DETECTED] Replaces: $linkedToken" }

        val oldSub = Subscriptions.selectAll()
            .where { Subscriptions.purchaseId eq linkedToken }
            .singleOrNull()

        if (oldSub != null) {
            Subscriptions.update({ Subscriptions.purchaseId eq linkedToken }) {
                it[status] = SubscriptionStatus.EXPIRED
                it[lastValidationDate] = Clock.System.now()
            }
            paymentLog.info { "✅ [UPGRADE] Old subscription deactivated." }
        } else {
            paymentLog.warn { "⚠️ [UPGRADE] Linked token not found in DB." }
        }
    }

    // Хелпер для записи транзакции (чтобы не дублировать insert код)
    private fun recordTransaction(
        userId: Uuid,
        type: TransactionType,
        provider: SubscriptionProvider,
        productId: String,
        transactionId: String?,
        relatedToken: String,
        amount: BigDecimal?,
        currency: String?,
        country: String?,
        isTestPurchase: Boolean
    ) {
        Transactions.insert {
            it[user] = userId
            it[this.type] = type
            it[this.provider] = provider
            it[this.productId] = productId
            it[this.transactionId] = transactionId ?: Uuid.random().toString()
            it[relatedSubscriptionId] = relatedToken
            it[this.amount] = amount
            it[this.currency] = currency
            it[this.country] = country
            it[this.isTestPurchase] = isTestPurchase
        }
    }

    // --- 2. ОБРАБОТКА РАЗОВЫХ ПОКУПОК(их пока нет и это просто заготовка) ---
    suspend fun consumeProduct(
        userId: Uuid,
        transactionId: String,
        productId: String,
        provider: SubscriptionProvider,
        isSandbox: Boolean
    ): ServiceResult<ConsumableResultResponse> {

        val isProcessed = dbQuery {
            Transactions.selectAll()
                .where { Transactions.transactionId eq transactionId }
                .count() > 0
        }

        if (isProcessed) {
            paymentLog.info { "🔁 [CONSUME] Duplicate TxId: $transactionId. Skipping." }
            return ServiceResult.Success(ConsumableResultResponse(true, "Already processed"))
        }

        val validation = validator.validateConsumable(transactionId, productId, provider, isSandbox)

        if (!validation.isValid) {
            paymentLog.warn { "⛔ [CONSUME] Validation failed: $transactionId" }
            return ServiceResult.Error(ErrorType.BAD_REQUEST, "Invalid transaction signature")
        }

        val result = dbQuery {
            val alreadyExists = Transactions.selectAll()
                .where { Transactions.transactionId eq transactionId }
                .count() > 0

            if (alreadyExists) {
                return@dbQuery ServiceResult.Success(ConsumableResultResponse(true, "Already processed"))
            }

            val finalAmount = microsToBigDecimal(validation.priceAmountMicros)
            val currencyCode = validation.priceCurrencyCode
            val energyToAdd = BillingProductSpecs.mapProductIdToEnergy(productId)

            Transactions.insert {
                it[user] = userId
                it[type] = TransactionType.CONSUMABLE
                it[this.provider] = provider
                it[this.productId] = productId
                it[this.transactionId] = transactionId
                it[amount] = finalAmount
                it[currency] = currencyCode
            }

            val userRow = AppUsers.selectAll()
                .where { AppUsers.id eq userId }
                .single()

            val currentReserve = userRow[AppUsers.reserveBalance]
            val newBalance = currentReserve + energyToAdd

            AppUsers.update({ AppUsers.id eq userId }) {
                it[AppUsers.reserveBalance] = newBalance
            }

            paymentLog.info { "💎 [CONSUME] User: $userId +$energyToAdd energy. TxId: $transactionId" }

            ServiceResult.Success(
                ConsumableResultResponse(success = true, newBalance = newBalance)
            )
        }

        if (result is ServiceResult.Success) {
            notificationService.sendConsumablePurchaseNotification(
                userId = userId,
                productId = productId,
                amount = microsToBigDecimal(validation.priceAmountMicros),
                currency = validation.priceCurrencyCode,
                provider = provider,
                isTestPurchase = validation.isTestPurchase,
                countryCode = validation.countryCode
            )
        }

        return result
    }

    // --- ХЕЛПЕРЫ ---
    private fun microsToBigDecimal(micros: Long?): BigDecimal? {
        return micros?.let {
            BigDecimal.valueOf(it).divide(BigDecimal.valueOf(1_000_000))
        }
    }

//    private fun mapProductIdToPlan(productId: String): PremiumStatus {
//        return when {
//            productId.contains("elite", ignoreCase = true) -> PremiumStatus.ELITE
//            productId.contains("crystal", ignoreCase = true) -> PremiumStatus.CRYSTAL
//            productId.contains("premium", ignoreCase = true) -> PremiumStatus.PREMIUM
//            else -> PremiumStatus.PREMIUM
//        }
//    }

    suspend fun getAvailableProducts(): ServiceResult<ProductsListResponse> {
        val subscriptionProducts = PremiumStatus.entries
            .filter { it != PremiumStatus.FREE && it != PremiumStatus.SPECIAL }
            .map { status ->
                val productId = status.name.lowercase()
                val subscriptionStats = tariffService.getConfig(status)
                SubscriptionDto(
                    displayName = status.name,
                    codeName = productId,
                    dailyEnergy = subscriptionStats.maxDailyEnergy.toString(),
                    maxCapacity = subscriptionStats.maxReserveBalance.toString()
                )
            }

        val consumableProducts = BillingProductSpecs.consumables

        return ServiceResult.Success(
            ProductsListResponse(
                subscriptions = subscriptionProducts,
                consumables = consumableProducts,
                isEnablePaymentService = globalSettingsRepository.isPaymentServiceEnabled()
            )
        )
    }
}