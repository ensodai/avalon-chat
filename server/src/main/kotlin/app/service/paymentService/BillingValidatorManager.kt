package app.service.paymentService
import com.ensodai.shared.avalon.gifts.models.*

import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider
import io.github.oshai.kotlinlogging.KotlinLogging

private val paymentLog = KotlinLogging.logger("Payments")

class BillingValidatorManager {

    suspend fun validateSubscription(
        purchaseId: String,
        productId: String,
        provider: SubscriptionProvider,
        isSandbox: Boolean
    ): PaymentValidationResult {
        paymentLog.info { "Self-hosted: auto-validating subscription purchaseId=$purchaseId productId=$productId" }
        return PaymentValidationResult(isValid = true)
    }

    suspend fun validateConsumable(
        transactionId: String,
        productId: String,
        provider: SubscriptionProvider,
        isSandbox: Boolean
    ): PaymentValidationResult {
        paymentLog.info { "Self-hosted: auto-validating consumable transactionId=$transactionId productId=$productId" }
        return PaymentValidationResult(isValid = true)
    }
}