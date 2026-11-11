package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble

import androidx.compose.ui.graphics.Color

sealed interface MessageActionsConfig {
    data object None : MessageActionsConfig

    // Одиночная кнопка действия (например, оплата, квота)
    data class SingleAction(
        val text: String,
        val actionColor: Color? = null,
        val onClick: () -> Unit
    ) : MessageActionsConfig

    // Две кнопки действия (например, покупка подписки и покупка разового пакета энергии)
    data class DoubleAction(
        val primaryText: String,
        val primaryClick: () -> Unit,
        val secondaryText: String,
        val secondaryClick: () -> Unit,
        val primaryColor: Color? = null,
        val secondaryColor: Color? = null
    ) : MessageActionsConfig

    // Две кнопки фидбека (👍 Интересно / 👎 Нет)
    data class FeedbackAction(
        val onPositiveClick: () -> Unit,
        val onNegativeClick: () -> Unit
    ) : MessageActionsConfig
}
