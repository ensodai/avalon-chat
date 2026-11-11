package com.ensodai.avalon.domain.models.message

import com.ensodai.avalon.domain.models.chat.WelcomeInfo

data class ChatHeaderInfo(
    val firstMessage: String?,
    val welcomeInfo: WelcomeInfo?
)