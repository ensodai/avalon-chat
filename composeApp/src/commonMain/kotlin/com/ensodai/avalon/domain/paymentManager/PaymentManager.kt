package com.ensodai.avalon.domain.paymentManager

import com.ensodai.avalon.data.db.dto.UserTariffInfoDto
import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.shared.avalon.gifts.models.BillingProductSpecs
import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.shared.avalon.rpc.services.GiftsRpcService
import com.ensodai.shared.avalon.rpc.services.UserRpcService
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class PaymentManager(
    @Named("RuStore") private val ruStoreProvider: PaymentProvider,
    @Named("Google") private val googleProvider: PaymentProvider,
    private val userRpcService: UserRpcService,
    private val platformHelper: BillingPlatformHelper,
    @ApplicationScope private val scope: CoroutineScope,
    private val giftsRpcService: GiftsRpcService,
    private val localUserRepository: LocalUserRepository,
    private val logger: AppLogger
) {
    // Текущий активный провайдер
    private val defaultProvider = MutableStateFlow<PaymentProvider?>(null)

    // Кэш продуктов
    private val _productsFlow = MutableStateFlow<Result<SubscriptionItemResult>?>(null)
    val productsFlow = _productsFlow.asStateFlow()

    // Кэш расходных продуктов (энергии)
    private val _consumablesFlow = MutableStateFlow<Result<List<DisplayConsumableDto>>?>(null)
    val consumablesFlow = _consumablesFlow.asStateFlow()

    // Локальный источник состояний (для ошибок самого менеджера)
    private val _localBillingState = MutableSharedFlow<BillingState>(replay = 0)

    // --- ЕДИНЫЙ ПОТОК СОСТОЯНИЙ ---
    // Объединяем RuStore + Google + Ошибки Менеджера
    val billingState: SharedFlow<BillingState> = merge(
        ruStoreProvider.billingState,
        googleProvider.billingState,
        _localBillingState.asSharedFlow()
    ).shareIn(scope, SharingStarted.Eagerly, replay = 0)

    /**
     * Инициализация
     * Подписываемся на изменения юзера в БД. Как только база появится — выберем правильного провайдера.
     */
    fun initialize() {
        // Подписки временно отключены (переход на пользовательские API-ключи)
        /*
        scope.launch {
            logger.d("PaymentManager", "PaymentManager: Запуск реактивной инициализации...")

            localUserRepository
                .getUserTariffFlow()
                .collectLatest { tariffInfo ->
                    try {
                        updateProviderStrategy(tariffInfo)
                    } catch (e: Exception) {
                        logger.e(
                            "PaymentManager",
                            "PaymentManager: Ошибка обновления стратегии: ${e.stackTraceToString()}"
                        )
                    }
                }
        }
        */
    }

    /**
     * Логика выбора провайдера при старте/изменении юзера
     */
    private suspend fun updateProviderStrategy(tariffInfo: UserTariffInfoDto?) {
        val provider = if (tariffInfo != null &&
            tariffInfo.premiumStatus != PremiumStatus.FREE &&
            tariffInfo.paymentProvider != null
        ) {
            getProviderInstance(tariffInfo.paymentProvider)
        } else {
            selectProviderStrategy()
        }

        if (defaultProvider.value != provider) {
            defaultProvider.value = provider
            provider.initialize()

            // Если провайдер сменился - сбрасываем кэш и грузим жестко
            _productsFlow.value = null
            _consumablesFlow.value = null
            loadProducts(forceRefresh = true)
        } else {
            // Если провайдер тот же, просто пробуем догрузить, если пусто
            loadProducts(forceRefresh = false)
        }
    }

    /**
     * Метод покупки (Вызывается из UI)
     */
    fun buySubscription(newProductId: String) {
        scope.launch {
            try {
                // 1. Получаем текущий тариф (безопасно, даже если null)
                val rawTariff = localUserRepository.getUserTariff()
                val currentTariff = rawTariff ?: UserTariffInfoDto(PremiumStatus.FREE, null)

                // 2. Определяем, через кого платить (Sticky Logic)
                val providerToUse = resolveProviderForPurchase(currentTariff)

                // 3. Логика Апгрейда: определяем ID старого товара
                val oldProductId = if (currentTariff.premiumStatus != PremiumStatus.FREE) {
                    // Используем маппер, чтобы получить ID типа "premium" или "crystal"
                    mapStatusToProductId(currentTariff.premiumStatus)
                } else {
                    null
                }

                logger.d("PaymentManager", "Start Buy: New=$newProductId, Old=$oldProductId")

                // 4. Запускаем
                providerToUse.launchBillingFlow(newProductId, oldProductId)

            } catch (e: Exception) {
                logger.e(
                    "PaymentManager",
                    "PaymentManager: Ошибка старта покупки: ${e.stackTraceToString()}"
                )
                _localBillingState.emit(BillingState.Error("Ошибка: ${e.message}"))
            }
        }
    }

    /**
     * Метод покупки расходника (энергии) (Вызывается из UI)
     */
    fun buyConsumable(productId: String) {
        scope.launch {
            try {
                val rawTariff = localUserRepository.getUserTariff()
                val currentTariff = rawTariff ?: UserTariffInfoDto(PremiumStatus.FREE, null)
                val providerToUse = resolveProviderForPurchase(currentTariff)
                logger.d("PaymentManager", "Start Buy Consumable: $productId")
                providerToUse.launchBillingFlow(productId, null)
            } catch (e: Exception) {
                logger.e(
                    "PaymentManager",
                    "PaymentManager: Ошибка старта покупки расходника: ${e.stackTraceToString()}"
                )
                _localBillingState.emit(BillingState.Error("Ошибка: ${e.message}"))
            }
        }
    }

    /**
     * Восстановить покупки (Вызывается из UI)
     */
    fun restorePurchases() {
        scope.launch {
            try {
                val currentTariff = localUserRepository.getUserTariff()
                // Если юзер Free - берем дефолтного (по IP), если платный - его родного
                val currentInfo = currentTariff ?: UserTariffInfoDto(PremiumStatus.FREE, null)
                val provider = resolveProviderForPurchase(currentInfo)

                logger.d("PaymentManager", "Restore via provider")
                provider.sync()

            } catch (e: Exception) {
                logger.e("PaymentManager", "Restore failed: ${e.stackTraceToString()}")
                _localBillingState.emit(BillingState.Error("Ошибка восстановления"))
            }
        }
    }

    // --- Внутренние методы ---

    /**
     * Загружает товары.
     * @param forceRefresh Если true - грузим из сети, даже если есть кэш.
     */
    suspend fun loadProducts(forceRefresh: Boolean = false) {
        // 1. ПРОВЕРКА КЭША
        // Если не просим форсировать И данные уже есть (успешные) -> выходим.
        if (!forceRefresh && _productsFlow.value?.isSuccess == true && _consumablesFlow.value?.isSuccess == true) {
            return
        }

        try {
            val provider = waitForDefaultProvider()

            // 1. Бэкенд
            val backendResult = runCatching {
                giftsRpcService.getAvailableProducts()
            }
            if (backendResult.isFailure) {
                val exception = backendResult.exceptionOrNull() ?: Exception("Unknown error")
                _productsFlow.emit(Result.failure(exception))
                _consumablesFlow.emit(Result.failure(exception))
                return
            }

            val backendProducts = backendResult.getOrThrow()
            val productIds = backendProducts.subscriptions.map { it.codeName } +
                    backendProducts.consumables.map { it.codeName }

            // 2. Стор
            val storeResult = provider.getProducts(productIds)

            if (storeResult.isSuccess) {
                val storeProducts = storeResult.getOrThrow()

                // 3. Мердж подписок
                val mergedList = backendProducts.subscriptions.mapNotNull { backendItem ->
                    val storeItem = storeProducts.find { it.id == backendItem.codeName }
                    if (storeItem != null) {
                        DisplaySubscriptionDto(
                            id = storeItem.id,
                            title = backendItem.displayName,
                            description = storeItem.description,
                            priceLabel = storeItem.priceLabel,
                            dailyEnergy = backendItem.dailyEnergy,
                            maxCapacity = backendItem.maxCapacity,
                            originalSource = storeItem.originalSource,
                        )
                    } else null
                }
                val subscriptionItemResult = SubscriptionItemResult(
                    isEnablePaymentService = backendProducts.isEnablePaymentService,
                    displaySubscriptionDto = mergedList
                )
                _productsFlow.emit(Result.success(subscriptionItemResult))

                // 4. Мердж расходников
                val mergedConsumables = backendProducts.consumables.mapNotNull { backendItem ->
                    val storeItem = storeProducts.find { it.id == backendItem.codeName }
                    if (storeItem != null) {
                        DisplayConsumableDto(
                            id = storeItem.id,
                            title = backendItem.displayName,
                            description = storeItem.description,
                            priceLabel = storeItem.priceLabel,
                            energyAmount = BillingProductSpecs.mapProductIdToEnergy(backendItem.codeName),
                            originalSource = storeItem.originalSource
                        )
                    } else null
                }
                _consumablesFlow.emit(Result.success(mergedConsumables))
            } else {
                val exception = storeResult.exceptionOrNull() ?: Exception("Store error")
                _productsFlow.emit(Result.failure(exception))
                _consumablesFlow.emit(Result.failure(exception))
            }
        } catch (e: Exception) {
            _productsFlow.emit(Result.failure(e))
            _consumablesFlow.emit(Result.failure(e))
        }
    }

    private suspend fun resolveProviderForPurchase(currentTariff: UserTariffInfoDto): PaymentProvider {
        if (currentTariff.premiumStatus != PremiumStatus.FREE && currentTariff.paymentProvider != null) {
            return getProviderInstance(currentTariff.paymentProvider)
        }
        return waitForDefaultProvider()
    }

    private suspend fun selectProviderStrategy(): PaymentProvider {

        if (platformHelper.isRuStoreInstalled()) return ruStoreProvider

        val locationResult = runCatching { userRpcService.getUserLocation() }.getOrNull()
        return if (locationResult != null && locationResult.isRuStoreRecommended) {
            ruStoreProvider
        } else {
            googleProvider
        }
    }

    private suspend fun waitForDefaultProvider(): PaymentProvider {
        // Ждем, пока initialize() установит значение
        return defaultProvider.filterNotNull().first()
    }

    private fun getProviderInstance(providerEnum: SubscriptionProvider): PaymentProvider {
        return when (providerEnum) {
            SubscriptionProvider.RUSTORE -> ruStoreProvider
            SubscriptionProvider.GOOGLE -> googleProvider
            SubscriptionProvider.TEST -> ruStoreProvider
            SubscriptionProvider.SYSTEM_GRANT -> googleProvider
        }
    }

    private fun mapStatusToProductId(status: PremiumStatus): String? {
        return when (status) {
            PremiumStatus.PREMIUM -> "premium"
            PremiumStatus.CRYSTAL -> "crystal"
            PremiumStatus.ELITE -> "elite"
            else -> null
        }
    }
}
