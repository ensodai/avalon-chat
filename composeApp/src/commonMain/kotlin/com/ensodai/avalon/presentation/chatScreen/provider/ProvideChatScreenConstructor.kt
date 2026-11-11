package com.ensodai.avalon.presentation.chatScreen.provider

import com.ensodai.avalon.presentation.chatScreen.action.ChatScreenActions
import com.ensodai.avalon.presentation.chatScreen.viewState.ChatScreenViewState
import com.ensodai.avalon.presentation.navigation.Navigation

data class ProvideChatScreenConstructor(
    val navigation: Navigation,
    val actions: ChatScreenActions,
    val viewState: ChatScreenViewState
)