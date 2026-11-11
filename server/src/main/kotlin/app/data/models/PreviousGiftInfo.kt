package app.data.models

import kotlin.uuid.Uuid

// Класс для возврата данных о подарке
data class PreviousGiftInfo(
    val giftId: Uuid,
    val price: Int
)