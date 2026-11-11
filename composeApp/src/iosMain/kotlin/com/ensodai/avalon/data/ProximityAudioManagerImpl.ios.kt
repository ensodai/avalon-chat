package com.ensodai.avalon.data

import com.ensodai.avalon.domain.proximity.ProximityAudioManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual fun createProximityAudioManager(): ProximityAudioManager {
    return ProximityAudioManagerStub()
}

internal class ProximityAudioManagerStub : ProximityAudioManager {
    private val _isNear = MutableStateFlow(false)
    override val isNear: StateFlow<Boolean> = _isNear.asStateFlow()
    override val isAvailable: Boolean = false

    override fun start() { /* no-op on iOS */ }
    override fun stop() { /* no-op on iOS */ }
}
