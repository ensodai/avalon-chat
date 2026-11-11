package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.swipeableMessageBubble

import androidx.compose.runtime.Immutable

@Immutable
data class SwipeToActionConfig(
    val maxSwipeOffset: Float = -300f,
    val midSwipeThreshold: Float = -150f,
    val resistanceAtStart: Float = 1.0f,
    val resistanceAtLimit: Float = 0.4f,
    val overswipeResistance: Float = 0.15f,
    val resistanceCurvePower: Float = 3.0f
)