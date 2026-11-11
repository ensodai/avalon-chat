package com.ensodai.avalon.presentation.chatScreen.viewState

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import kotlin.uuid.Uuid

@Immutable
data class ActiveReactionMenu(
    val messageId: Uuid?,
    val tapOffset: Offset
)