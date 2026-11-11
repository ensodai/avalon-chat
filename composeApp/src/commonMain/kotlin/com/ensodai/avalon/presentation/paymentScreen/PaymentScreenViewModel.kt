package com.ensodai.avalon.presentation.paymentScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.avalon.domain.paymentManager.PaymentManager
import com.ensodai.avalon.domain.useCases.paymentSreenUseCase.GetUserTariffUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.paymentScreen.action.PaymentScreenActions
import com.ensodai.avalon.presentation.paymentScreen.action.observePaymentEvents
import com.ensodai.avalon.presentation.paymentScreen.action.onPurchase
import com.ensodai.avalon.presentation.paymentScreen.action.updateProducts
import com.ensodai.avalon.presentation.paymentScreen.viewState.PaymentScreenViewState
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PaymentScreenViewModel(
    private val paymentManager: PaymentManager,
    private val getUserTariff: GetUserTariffUseCase,
    private val resourceWrapper: ResourceWrapper
) : BaseViewModel<PaymentScreenViewState, PaymentScreenActions>(
    initialState = PaymentScreenViewState()
) {

    override val actions = PaymentScreenActions(
        onPurchase = { tariff ->
            onPurchase(
                tariff = tariff,
                paymentManager = paymentManager
            )
        },
        onRestorePurchase = {
            paymentManager.restorePurchases()
        },
        onSelectPlan = { index ->
            updateViewState { it.copy(selectedPlanIndex = index) }
        }
    )

    init {
        updateProducts(
            paymentManager = paymentManager,
            getUserTariff = getUserTariff,
            resourceWrapper = resourceWrapper
        )
        observePaymentEvents(paymentManager)
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch(IODispatcher) {
            paymentManager.loadProducts(forceRefresh = true)
        }
    }
}
