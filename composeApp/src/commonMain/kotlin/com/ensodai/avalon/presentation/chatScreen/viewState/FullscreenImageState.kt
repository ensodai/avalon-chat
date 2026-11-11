package com.ensodai.avalon.presentation.chatScreen.viewState

import com.ensodai.shared.avalon.chat.models.ImagePayload

data class FullscreenImageState(
    val isShowDialog: Boolean = false,
    val imagePayloads: List<ImagePayload> = emptyList(),
    val imageIndex: Int = 0
)