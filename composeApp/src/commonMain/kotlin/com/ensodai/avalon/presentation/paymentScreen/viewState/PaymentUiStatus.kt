package com.ensodai.avalon.presentation.paymentScreen.viewState

enum class PaymentUiStatus {
    IDLE,       // Просто смотрим карточки
    PROCESSING, // Крутится лоадер (по желанию, но пока опустим для простоты)
    SUCCESS     // УСПЕХ! Показываем экран триумфа
}
