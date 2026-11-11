package com.ensodai.avalon.domain.paymentManager

data class DisplaySubscriptionDto(
    val id: String,
    val title: String,
    val description: String,
    val priceLabel: String,
    val dailyEnergy: String,
    val maxCapacity: String,
    val originalSource: Any? // Ссылка на оригинальный объект (SkuDetails или Product)
)

data class DisplayConsumableDto(
    val id: String,
    val title: String,
    val description: String,
    val priceLabel: String,
    val energyAmount: Int,
    val originalSource: Any?
)

data class SubscriptionItemResult(
    val isEnablePaymentService: Boolean,
    val displaySubscriptionDto: List<DisplaySubscriptionDto>
)