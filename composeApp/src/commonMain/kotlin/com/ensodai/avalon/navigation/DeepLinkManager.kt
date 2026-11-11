package com.ensodai.avalon.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.uuid.Uuid

class DeepLinkManager {
    private val _pendingRoute = MutableStateFlow<Route?>(null)
    val pendingRoute: StateFlow<Route?> = _pendingRoute.asStateFlow()

    fun dispatchChatDeepLink(characterId: Uuid) {
        _pendingRoute.value = Route.ChatScreen(characterId, showAiWarning = true)
    }

    fun clearPendingRoute() {
        _pendingRoute.value = null
    }
}
