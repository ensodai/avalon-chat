package com.ensodai.avalon.presentation.mainScreen.component.chatSection

sealed class ChatsScreens {

    data object UserChatsScreen : ChatsScreens()

    data object AllCharactersChatsScreen : ChatsScreens()

    data object GlobalLorebooksScreen : ChatsScreens()
}