package com.ensodai.avalon.presentation.paymentScreen.viewState

import com.ensodai.avalon.presentation.core.BaseViewState

data class PaymentScreenViewState(
    val currentTariff: UserTariff = UserTariff.Free,
    val products: List<SubscriptionPlan?> = emptyList(),
    val selectedPlanIndex: Int = 0,
    val initialPage: Int = 0,
    val paymentStatus: PaymentUiStatus = PaymentUiStatus.IDLE,
    val purchasedPlan: SubscriptionPlan? = null,
    val isEnablePaymentService: Boolean = false,
    val isMaxTariff: Boolean = false
) : BaseViewState() {
    val selectedPlan: SubscriptionPlan?
        get() = products.getOrNull(selectedPlanIndex)
}