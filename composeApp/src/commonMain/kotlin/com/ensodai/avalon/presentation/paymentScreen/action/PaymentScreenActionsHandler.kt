package com.ensodai.avalon.presentation.paymentScreen.action
import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.avalon.domain.paymentManager.BillingState
import com.ensodai.avalon.domain.paymentManager.PaymentManager
import com.ensodai.avalon.domain.useCases.paymentSreenUseCase.GetUserTariffUseCase
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.feature_bank_energy
import com.ensodai.avalon.generated.resources.feature_early_access
import com.ensodai.avalon.generated.resources.feature_extended_memory
import com.ensodai.avalon.generated.resources.feature_golden_glow
import com.ensodai.avalon.generated.resources.voice_streaming_feature
import com.ensodai.avalon.presentation.paymentScreen.PaymentScreenViewModel
import com.ensodai.avalon.presentation.paymentScreen.viewState.PaymentUiStatus
import com.ensodai.avalon.presentation.paymentScreen.viewState.SubscriptionPlan
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.utils.IODispatcher
import com.ensodai.avalon.utils.KermitLogger
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

fun PaymentScreenViewModel.updateProducts(
    paymentManager: PaymentManager,
    getUserTariff: GetUserTariffUseCase,
    resourceWrapper: ResourceWrapper
) {
    viewModelScope.launch(IODispatcher) {
        paymentManager.productsFlow.collect { result ->
            if (result == null) return@collect

            // 1. Получаем текущий статус
            val currentInfo = getUserTariff.get()
            val currentStatus = currentInfo?.premiumStatus ?: PremiumStatus.FREE
            val currentTariff = when (currentStatus) {
                PremiumStatus.FREE -> UserTariff.Free
                PremiumStatus.SUPPORTER -> UserTariff.Supporter
                PremiumStatus.PREMIUM -> UserTariff.Premium
                PremiumStatus.CRYSTAL -> UserTariff.Crystal
                PremiumStatus.ELITE -> UserTariff.Elite
                PremiumStatus.SPECIAL -> UserTariff.Special
            }

            result.fold(
                onSuccess = { successResult ->
                    KermitLogger().d(
                        "TestPayment",
                        "PaymentScreenViewModel successResult $successResult"
                    )
                    val products = successResult.displaySubscriptionDto.mapNotNull { tariff ->


                        val tariffStatus = when (tariff.id) {
                            "supporter" -> PremiumStatus.SUPPORTER
                            "premium" -> PremiumStatus.PREMIUM
                            "crystal" -> PremiumStatus.CRYSTAL
                            "elite" -> PremiumStatus.ELITE
                            else -> null
                        }

                        if (tariffStatus == null) return@mapNotNull null

                        if (tariffStatus.ordinal <= currentStatus.ordinal) {
                            return@mapNotNull null
                        }

                        // 4. Маппинг в UI модель
                        when (tariff.id) {
                            "supporter" -> SubscriptionPlan(
                                id = tariff.id,
                                tariff = UserTariff.Supporter,
                                title = tariff.title,
                                price = tariff.priceLabel,
                                dailyEnergy = tariff.dailyEnergy,
                                maxCapacity = tariff.maxCapacity,
                                features = listOf(
                                    resourceWrapper.getString(
                                        Res.string.feature_bank_energy,
                                        tariff.maxCapacity
                                    )
                                )
                            )

                            "premium" -> SubscriptionPlan(
                                id = tariff.id,
                                tariff = UserTariff.Premium,
                                title = tariff.title,
                                price = tariff.priceLabel,
                                dailyEnergy = tariff.dailyEnergy,
                                maxCapacity = tariff.maxCapacity,
                                features = listOf(
                                    resourceWrapper.getString(
                                        Res.string.feature_bank_energy,
                                        tariff.maxCapacity
                                    ),
                                    resourceWrapper.getString(Res.string.voice_streaming_feature)
                                )
                            )

                            "crystal" -> SubscriptionPlan(
                                id = tariff.id,
                                tariff = UserTariff.Crystal,
                                title = tariff.title,
                                price = tariff.priceLabel,
                                dailyEnergy = tariff.dailyEnergy,
                                maxCapacity = tariff.maxCapacity,
                                features = listOf(
                                    resourceWrapper.getString(
                                        Res.string.feature_bank_energy,
                                        tariff.maxCapacity
                                    ),
                                    resourceWrapper.getString(Res.string.voice_streaming_feature),
                                    resourceWrapper.getString(Res.string.feature_extended_memory)
                                )
                            )

                            "elite" -> SubscriptionPlan(
                                id = tariff.id,
                                tariff = UserTariff.Elite,
                                title = tariff.title,
                                price = tariff.priceLabel,
                                dailyEnergy = tariff.dailyEnergy,
                                maxCapacity = tariff.maxCapacity,
                                features = listOf(
                                    resourceWrapper.getString(
                                        Res.string.feature_bank_energy,
                                        tariff.maxCapacity
                                    ),
                                    resourceWrapper.getString(Res.string.voice_streaming_feature),
                                    resourceWrapper.getString(Res.string.feature_extended_memory),
                                    resourceWrapper.getString(Res.string.feature_golden_glow),
                                    resourceWrapper.getString(Res.string.feature_early_access)
                                )
                            )

                            else -> null
                        }
                    }
                    KermitLogger().d(
                        "TestPayment",
                        "PaymentScreenViewModel products size ${products.size}"
                    )
                    
                    val premiumIndex = products.indexOfFirst { it?.tariff == UserTariff.Premium }
                    val defaultSelectedIndex = if (premiumIndex != -1) premiumIndex else 0
                    
                    updateViewState {
                        it.copy(
                            currentTariff = currentTariff,
                            products = products,
                            selectedPlanIndex = defaultSelectedIndex,
                            initialPage = if (products.size < 2) 0 else 1,
                            isEnablePaymentService = successResult.isEnablePaymentService,
                            isMaxTariff = currentStatus == PremiumStatus.ELITE || currentStatus == PremiumStatus.SPECIAL
                        )
                    }
                },
                onFailure = {
                    KermitLogger().d("TestPayment", "PaymentScreenViewModel exception $it")
                }
            )
        }
    }
}

fun PaymentScreenViewModel.onPurchase(
    tariff: SubscriptionPlan?,
    paymentManager: PaymentManager
) {
    if (tariff == null) return
    viewModelScope.launch(IODispatcher) {

        paymentManager.buySubscription(
            newProductId = tariff.id
        )
    }
}

fun PaymentScreenViewModel.observePaymentEvents(
    paymentManager: PaymentManager
) {
    viewModelScope.launch {
        paymentManager.billingState
            .distinctUntilChanged()
            .collect { billingState ->

                KermitLogger().d(
                    "TestPayment",
                    "PaymentScreenViewModel Billing State changed: $billingState"
                )

                when (billingState) {
                    is BillingState.Idle -> {
                        updateViewState { it.copy(paymentStatus = PaymentUiStatus.IDLE) }
                    }

                    is BillingState.Loading -> {
                        updateViewState { it.copy(paymentStatus = PaymentUiStatus.PROCESSING) }
                    }

                    is BillingState.Success -> {
                        val productId = billingState.productId

                        val currentState = viewState.value
                        val boughtPlan = currentState.products.find { it?.id == productId }

                        if (boughtPlan != null) {
                            updateViewState {
                                it.copy(
                                    paymentStatus = PaymentUiStatus.SUCCESS,
                                    purchasedPlan = boughtPlan
                                )
                            }
                        } else {
                            KermitLogger().e(
                                "TestPayment",
                                "PaymentScreenViewModel Plan not found locally for id: $productId"
                            )
                            updateViewState { it.copy(paymentStatus = PaymentUiStatus.IDLE) }
                        }
                    }

                    is BillingState.Error -> {
                        KermitLogger().e(
                            "TestPayment",
                            "PaymentScreenViewModel Purchase error: ${billingState.message}"
                        )

                        updateViewState { it.copy(paymentStatus = PaymentUiStatus.IDLE) }

                        // TODO: Тут можно кинуть SingleEvent (SideEffect) чтобы показать Toast/Snackbar
                        // sendEvent(PaymentScreenEvent.ShowError(billingState.message))
                    }
                }
            }
    }
}
