package com.ensodai.avalon.presentation.liveCallScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.livecall_microphone_off
import com.ensodai.avalon.generated.resources.microphone_permission_denied
import com.ensodai.avalon.generated.resources.person_screen
import com.ensodai.avalon.presentation.commonComponents.drawHexagonPath
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.liveCallScreen.action.LiveCallScreenActions
import com.ensodai.avalon.presentation.liveCallScreen.component.CallButton
import com.ensodai.avalon.presentation.liveCallScreen.component.CallTimer
import com.ensodai.avalon.presentation.liveCallScreen.component.CharacterPill
import com.ensodai.avalon.presentation.liveCallScreen.component.ErrorBanner
import com.ensodai.avalon.presentation.liveCallScreen.component.HexagonOrb
import com.ensodai.avalon.presentation.liveCallScreen.component.VoiceModelPickerSheet
import com.ensodai.avalon.presentation.liveCallScreen.component.MuteButton
import com.ensodai.avalon.presentation.liveCallScreen.component.SelectorButton
import com.ensodai.avalon.presentation.liveCallScreen.component.StatusText
import com.ensodai.avalon.presentation.liveCallScreen.viewState.LiveCallPhase
import com.ensodai.avalon.presentation.liveCallScreen.viewState.LiveCallScreenViewState
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.personaScreen.viewState.PersonaLoadingState
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCallScreenContent(
    navigation: Navigation,
    state: LiveCallScreenViewState,
    actions: LiveCallScreenActions,
    userVolume: Float = 0f,
    aiVolume: Float = 0f,
    permissionDenied: Boolean = false
) {
    val updatedState = state.copy(permissionDenied = permissionDenied)
    val colors = AvalonTheme.colors

    Scaffold(
        topBar = {
            GemIslandTopBar(
                title = {
                    CharacterPill(
                        name = updatedState.characterName,
                        avatarUrl = updatedState.characterAvatarUrl
                    )
                },
                navigationIcon = {
                    NavigationBackButton(
                        onBackButtonClick = {
                            navigation.navigateBack()
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(horizontal = 24.dp)
        ) {
            // === ЦЕНТРАЛЬНАЯ ЗОНА — гексагон + статус ===
            // Выровнена по центру, не зависит от нижней панели
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HexagonOrb(
                    phase = updatedState.callPhase,
                    userVolume = userVolume,
                    aiVolume = aiVolume,
                    isRejected = updatedState.turnRejected != null,
                    rejectTriggerCount = updatedState.rejectTriggerCount
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Status + timer — фиксированная высота
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.height(56.dp)
                ) {
                    StatusText(phase = updatedState.callPhase)
                    Spacer(modifier = Modifier.height(4.dp))
                    CallTimer(
                        seconds = updatedState.callDurationSeconds,
                        visible = updatedState.callPhase == LiveCallPhase.ACTIVE ||
                                updatedState.callPhase == LiveCallPhase.INTERRUPTED ||
                                updatedState.callPhase == LiveCallPhase.RECONNECTING
                    )
                }

                // Muted indicator — fixed height
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (updatedState.isMuted) {
                        Text(
                            text = stringResource(Res.string.livecall_microphone_off),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.mutedAccent
                        )
                    }
                }
            }

            // === НИЖНЯЯ ЗОНА — ошибки + селектор + кнопки ===
            // Зафиксирована внизу, появление/исчезновение элементов не влияет на центр
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Error banner
                updatedState.error?.let { error ->
                    ErrorBanner(error = error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (updatedState.permissionDenied) {
                    Text(
                        text = stringResource(Res.string.microphone_permission_denied),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.mutedAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Model selector — hidden during active call
                if (!updatedState.isCallActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SelectorButton(
                            displayText = updatedState.selectedModel.toString(),
                            onClick = actions.showModelPicker
                        )
                    }
                }

                // Call buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MuteButton(isMuted = updatedState.isMuted, onClick = actions.toggleMute)

                    Spacer(modifier = Modifier.weight(1f))

                    CallButton(
                        isCallActive = updatedState.isCallActive,
                        isConnecting = updatedState.callPhase == LiveCallPhase.CONNECTING,
                        onStartCall = actions.startCallScreen,
                        onStopCall = actions.stopCallScreen
                    )
                }
            }
        }
    }

    // Voice model picker bottom sheet
    if (updatedState.showModelSheet) {
        VoiceModelPickerSheet(
            selectedModel = updatedState.selectedModel,
            onSelect = actions.selectModel,
            onDismiss = actions.hideModelSheet
        )
    }
}

@Composable
fun rippleTintForPhase(phase: LiveCallPhase): Color {
    val colors = AvalonTheme.colors
    return when (phase) {
        LiveCallPhase.ACTIVE -> colors.accentSecondary
        LiveCallPhase.RECONNECTING -> colors.warningAccent
        else -> colors.textSecondary
    }
}

fun getPulseDuration(phase: LiveCallPhase): Int = when (phase) {
    LiveCallPhase.IDLE -> 3000
    LiveCallPhase.CONNECTING -> 2000
    LiveCallPhase.ACTIVE -> 1200
    LiveCallPhase.INTERRUPTED -> 800
    LiveCallPhase.RECONNECTING -> 2000
    LiveCallPhase.STOPPING -> 1000
    LiveCallPhase.STOPPED -> 3000
}
