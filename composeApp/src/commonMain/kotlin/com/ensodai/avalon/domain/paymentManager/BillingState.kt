package com.ensodai.avalon.domain.paymentManager

sealed interface BillingState {
    data object Idle : BillingState
    data object Loading : BillingState // Когда открылась шторка или идет валидация
    data class Success(val productId: String) : BillingState
    data class Error(val message: String, val cause: Throwable? = null) : BillingState
}