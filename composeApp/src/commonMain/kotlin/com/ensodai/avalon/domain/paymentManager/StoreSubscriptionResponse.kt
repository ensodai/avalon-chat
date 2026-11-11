package com.ensodai.avalon.domain.paymentManager

data class StoreSubscriptionResponse(
    val id: String,
    val title: String,
    val description: String,
    val priceLabel: String,
    val originalSource: Any? // Ссылка на оригинальный объект (SkuDetails или Product)
)