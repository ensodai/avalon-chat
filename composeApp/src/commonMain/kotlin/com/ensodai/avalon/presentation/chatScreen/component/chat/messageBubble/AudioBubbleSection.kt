package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.ensodai.shared.avalon.chat.models.AudioPayload
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.domain.audio.TtsPlayerState
import com.ensodai.avalon.theme.AvalonTheme
import kotlin.random.Random
import kotlin.uuid.Uuid

@Composable
fun AudioBubbleSection(
    modifier: Modifier = Modifier,
    contentId: Uuid,
    audioPayload: AudioPayload,
    currentPlayingContentId: Uuid?,
    ttsPlayerState: TtsPlayerState,
    voice: VoiceModel?,
    onPlaySpeech: (Uuid, VoiceModel?) -> Unit,
    onPauseSpeech: () -> Unit,
    showExpandButton: Boolean = false,
    isTextExpanded: Boolean = false,
    onExpandToggle: () -> Unit = {}
) {
    val colors = AvalonTheme.colors
    val isCurrentPlaying = currentPlayingContentId == contentId
    val isStreaming = audioPayload.id == Uuid.parse("00000000-0000-0000-0000-000000000000")
    val isStreamingActive = isStreaming || (isCurrentPlaying && ttsPlayerState is TtsPlayerState.Playing && ttsPlayerState.isStreaming)

    // Вычисляем состояние воспроизведения конкретно для этого сообщения
    val isPlaying = isCurrentPlaying && ttsPlayerState is TtsPlayerState.Playing
    val isLoading = isCurrentPlaying && ttsPlayerState is TtsPlayerState.Loading
    val isPaused = isCurrentPlaying && ttsPlayerState is TtsPlayerState.Paused

    val progress = when {
        !isCurrentPlaying -> 0f
        ttsPlayerState is TtsPlayerState.Playing -> ttsPlayerState.progress
        ttsPlayerState is TtsPlayerState.Paused -> ttsPlayerState.progress
        else -> 0f
    }

    // Детерминированная генерация высот звуковой волны (32 столбика)
    val heights = remember(contentId) {
        val random = Random(contentId.hashCode())
        List(32) { random.nextInt(4, 24).dp }
    }



    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Кнопка Play/Pause / Loader
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.textMain.copy(alpha = 0.08f))
                .clickable {
                    if (isPlaying) {
                        onPauseSpeech()
                    } else {
                        onPlaySpeech(contentId, voice)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                // Пульсирующий/вращающийся лоадер ожидания
                val infiniteTransition = rememberInfiniteTransition(label = "TtsLoader")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "TtsLoaderRotation"
                )
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation),
                    color = colors.neonAccent,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (isPlaying) Lucide.Pause else Lucide.Play,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = colors.neonAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Waveform (Звуковая волна)
        Row(
            modifier = Modifier
                .weight(1f)
                .height(28.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            heights.forEachIndexed { index, height ->
                val barProgress = if (heights.size > 1) {
                    index.toFloat() / (heights.size - 1).toFloat()
                } else 0f
                val isFilled = barProgress <= progress

                val barColor = if (isFilled) {
                    colors.neonAccent
                } else {
                    colors.textMain.copy(alpha = 0.2f)
                }

                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(height)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(barColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 3. Таймер длительности
        Text(
            text = if (isStreamingActive && audioPayload.durationMs == null) "0:00" else formatDuration(progress, audioPayload.durationMs ?: 0L),
            color = colors.textSecondary,
            fontSize = 11.sp,
            modifier = Modifier.widthIn(min = 32.dp)
        )

        if (showExpandButton) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { onExpandToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isTextExpanded) Lucide.ChevronUp else Lucide.ChevronDown,
                    contentDescription = if (isTextExpanded) "Свернуть текст" else "Раскрыть текст",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatDuration(progress: Float, durationMs: Long): String {
    val totalSeconds = if (durationMs > 0) durationMs / 1000 else 0
    val currentSeconds = (totalSeconds * progress).toLong()

    val displaySeconds = if (progress > 0) currentSeconds else totalSeconds
    val mins = displaySeconds / 60
    val secs = displaySeconds % 60
    return "${mins.toString().padStart(1, '0')}:${secs.toString().padStart(2, '0')}"
}
