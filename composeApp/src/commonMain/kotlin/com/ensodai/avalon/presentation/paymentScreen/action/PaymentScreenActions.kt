package com.ensodai.avalon.presentation.paymentScreen.action

import com.ensodai.avalon.presentation.core.BaseActions
import com.ensodai.avalon.presentation.paymentScreen.viewState.SubscriptionPlan

data class PaymentScreenActions(
    val onPurchase: (plan: SubscriptionPlan?) -> Unit,
    val onRestorePurchase: () -> Unit,
    val onSelectPlan: (index: Int) -> Unit
) : BaseActions()
