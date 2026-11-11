package com.ensodai.avalon.domain.paymentManager

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class NoOpPaymentProvider : PaymentProvider {
    private val state = MutableSharedFlow<BillingState>(extraBufferCapacity = 1)

    override val billingState: SharedFlow<BillingState> = state

    override fun initialize() = Unit

    override suspend fun getProducts(productIds: List<String>): Result<List<StoreSubscriptionResponse>> {
        return Result.success(emptyList())
    }

    override fun launchBillingFlow(productId: String, oldProductId: String?) {
        state.tryEmit(BillingState.Idle)
    }

    override suspend fun checkActiveSubscription(targetProductId: String): Boolean = true

    override suspend fun sync() = Unit
}
