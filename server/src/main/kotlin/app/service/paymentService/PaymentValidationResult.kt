package app.service.paymentService

import kotlin.time.Instant

data class PaymentValidationResult(
    val isValid: Boolean,
    val linkedOrderId: String? = null,
    val expirationDate: Instant? = null,
    val isAutoRenewing: Boolean = false,
    val priceAmountMicros: Long? = null,
    val priceCurrencyCode: String? = null,
    // Сюда мы положим токен старой подписки, если это апгрейд в гугл провайдере
    val linkedPurchaseToken: String? = null,
    val isTestPurchase: Boolean = false,
    val countryCode: String? = null,
    val isNetworkError: Boolean = false
)