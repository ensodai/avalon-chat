package com.ensodai.avalon.domain.models.chat

import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import kotlin.uuid.Uuid

data class ChatCharacterUi(
    val id: Uuid,
    val name: String,
    val avatarUrl: String?,
    val firstMessage: String?,
    val welcomeInfo: WelcomeInfo?,
    val availableLlmConfigs: List<AvailableLlmConfigs>,
    val selectedConfig: AvailableLlmConfigs
)