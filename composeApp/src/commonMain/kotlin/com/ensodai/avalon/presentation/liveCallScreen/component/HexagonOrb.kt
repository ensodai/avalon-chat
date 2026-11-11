package com.ensodai.avalon.presentation.liveCallScreen.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.liveCallScreen.rippleTintForPhase
import com.ensodai.avalon.presentation.liveCallScreen.viewState.LiveCallPhase

@Composable
fun HexagonOrb(
    phase: LiveCallPhase,
    userVolume: Float,
    aiVolume: Float,
    isRejected: Boolean = false,
    rejectTriggerCount: Int = 0
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        // Ripple rings — ВСЕГДА в композиции, но невидимы когда не нужны
        // Это предотвращает "подпрыгивание" layout при появлении/исчезновении
        HexRippleRings(
            color = rippleTintForPhase(phase),
            active = phase == LiveCallPhase.ACTIVE,
            visible = phase == LiveCallPhase.ACTIVE || phase == LiveCallPhase.RECONNECTING
        )

        // Жидкий неоновый гексагон — деформация + трёхслойное свечение + реакция на голос
        val normalizedUserVolume = (userVolume / 0.008f).coerceIn(0f, 1f)
        val normalizedAiVolume = (aiVolume / 0.15f).coerceIn(0f, 1f)
        LiquidNeonHexagon(
            userVolume   = normalizedUserVolume,
            aiVolume     = normalizedAiVolume,
            isAiSpeaking = phase == LiveCallPhase.ACTIVE && normalizedAiVolume >= normalizedUserVolume,
            isInterrupted = phase == LiveCallPhase.INTERRUPTED,
            isRejected   = isRejected,
            rejectTriggerCount = rejectTriggerCount
        )
    }
}
