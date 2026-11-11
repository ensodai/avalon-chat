package com.ensodai.avalon.domain.paymentManager

import kotlinx.coroutines.flow.SharedFlow

interface PaymentProvider {

    val billingState: SharedFlow<BillingState>

    fun initialize()
    suspend fun getProducts(productIds: List<String>): Result<List<StoreSubscriptionResponse>>
    fun launchBillingFlow(productId: String, oldProductId: String?)
    suspend fun checkActiveSubscription(targetProductId: String): Boolean

    suspend fun sync()
}