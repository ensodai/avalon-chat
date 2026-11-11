package com.ensodai.avalon.presentation.chatScreen.component.chat

sealed class ChatActionButton {
    abstract val label: String
    abstract val onClick: () -> Unit

    data class AuthButton(
        override val label: String = "Авторизация",
        override val onClick: () -> Unit
    ) : ChatActionButton()

    data class UrlButton(
        override val label: String,
        val url: String,
        override val onClick: () -> Unit = {}
    ) : ChatActionButton()

}
