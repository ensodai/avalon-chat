@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package com.ensodai.shared.avalon.gifts.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
enum class GiftActionType {
    NONE,
    CAMERA_SELFIE,
    CAMERA_PHOTO,
    TEXT_EVENT
}

@Serializable
enum class GiftEffectType {
    NONE,
    HEARTS,
    SPARKLES,
    CONFETTI,
    FIREWORKS
}

@Serializable
data class Gift(
    val id: Uuid,
    val packId: Uuid,
    val fileUrl: String,
    val price: Int,
    val actionType: GiftActionType,
    val giftName: String,
    val giftDescription: String,
    val effectType: GiftEffectType = GiftEffectType.NONE
)

@Serializable
data class GiftPack(
    val id: Uuid,
    val authorId: Uuid?,
    val name: String,
    val description: String?,
    val coverUrl: String,
    val isPublic: Boolean,
    val price: Int,
    val gifts: List<Gift> = emptyList()
)

@Serializable
data class GiftPackResponse(val pack: GiftPack)

@Serializable
data class GiftPacksResponse(val packs: List<GiftPack>)

@Serializable
data class GiftItemResponse(val item: Gift)

@Serializable
data class CreateGiftPackRequest(
    val name: String,
    val description: String?,
    val coverUrl: String,
    val isPublic: Boolean,
    val price: Int
)

@Serializable
data class CreateGiftItemRequest(
    val packId: Uuid,
    val fileUrl: String,
    val price: Int,
    val actionType: GiftActionType,
    val giftName: String,
    val giftDescription: String
)

@Serializable
data class GetGiftPacksRequest(
    val limit: Int = 20,
    val offset: Int = 0
)

@Serializable
enum class PremiumStatus {
    FREE,
    SUPPORTER,
    PREMIUM,
    CRYSTAL,
    ELITE,
    SPECIAL
}

@Serializable
enum class SubscriptionProvider {
    GOOGLE,
    RUSTORE,
    TEST,
    SYSTEM_GRANT
}

@Serializable
data class SubscriptionStatusResponse(
    val userId: Uuid,
    val isActive: Boolean,
    val expirationDate: Instant? = null,
    val isAutoRenewing: Boolean = false,
    val premiumStatus: PremiumStatus,
    val maxDailyEnergy: Int,
    val currentDailyEnergy: Int,
    val maxReserveBalance: Int,
    val paymentProvider: SubscriptionProvider,
    val nextBillingDate: Instant?,
)

@Serializable
data class ConsumableResultResponse(
    val success: Boolean,
    val message: String? = null,
    val newBalance: Int? = null
)

@Serializable
data class SubscriptionDto(
    val displayName: String,
    val codeName: String,
    val dailyEnergy: String,
    val maxCapacity: String
)

@Serializable
data class ProductDto(
    val displayName: String,
    val codeName: String
)

@Serializable
data class ProductsListResponse(
    val subscriptions: List<SubscriptionDto>,
    val consumables: List<ProductDto>,
    val isEnablePaymentService: Boolean
)

@Serializable
data class LocationResultResponse(
    val countryCode: String,
    val isRuStoreRecommended: Boolean
)

@Serializable
data class ValidateSubscriptionRequest(
    val purchaseId: String,
    val productId: String,
    val provider: SubscriptionProvider,
    val isSandbox: Boolean = false
)

@Serializable
data class ValidateConsumableRequest(
    val transactionId: String,
    val productId: String,
    val provider: SubscriptionProvider,
    val isSandbox: Boolean = false
)

object BillingProductSpecs {
    val consumables = listOf(
        ProductDto(displayName = "500", codeName = "starter500"),
        ProductDto(displayName = "2000", codeName = "medium2000"),
        ProductDto(displayName = "5000", codeName = "large5000")
    )

    fun mapProductIdToEnergy(productId: String): Int {
        return when (productId) {
            "starter500" -> 500
            "medium2000" -> 2000
            "large5000" -> 5000
            else -> 0
        }
    }
}
