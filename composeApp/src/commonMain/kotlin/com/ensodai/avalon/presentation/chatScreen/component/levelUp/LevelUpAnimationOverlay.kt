package com.ensodai.avalon.presentation.chatScreen.component.levelUp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ensodai.avalon.presentation.relationshipRoadScreen.model.SystemReward
import com.ensodai.avalon.presentation.relationshipRoadScreen.model.getSystemRewardForLevel
import com.ensodai.avalon.presentation.chatScreen.viewState.LevelUpAnimationState
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LevelUpAnimationOverlay(
    state: LevelUpAnimationState,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val haptic = LocalHapticFeedback.current

    var currentPlayingLevel by remember { mutableIntStateOf(state.fromLevel) }
    var targetPlayingLevel by remember { mutableIntStateOf(state.fromLevel + 1) }

    val progress = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    var activeReward by remember { mutableStateOf<SystemReward?>(null) }
    var showRewardCard by remember { mutableStateOf(false) }
    var hasRewardShowed by remember { mutableStateOf(false) }

    // Для жеста смахивания (Swipe to dismiss)
    var offsetX by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    val dismissOverlay: () -> Unit = {
        onAnimationComplete()
    }

    LaunchedEffect(state) {
        if (!state.isVisible) return@LaunchedEffect

        currentPlayingLevel = state.fromLevel
        targetPlayingLevel = state.fromLevel + 1
        showRewardCard = false
        activeReward = null
        hasRewardShowed = false
        offsetX = 0f

        for (level in state.fromLevel until state.toLevel) {
            val isLastStep = (level + 1 == state.toLevel)

            currentPlayingLevel = level
            targetPlayingLevel = level + 1

            val totalLevelsToPlay = state.toLevel - state.fromLevel
            val animDuration = if (totalLevelsToPlay > 3) 150 else 250

            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing)
            )

            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            scale.animateTo(1.3f, tween(durationMillis = 80))
            
            // Логика "было/стало": на последнем шаге левое число НЕ меняется на целевое.
            // Оно меняется только на промежуточных шагах, чтобы подготовиться к следующему забегу.
            if (!isLastStep) {
                currentPlayingLevel = level + 1
            }
            
            scale.animateTo(1.0f, tween(durationMillis = 80))

            val reward = getSystemRewardForLevel(level + 1)
            if (reward != null) {
                activeReward = reward
                showRewardCard = true
                hasRewardShowed = true

                // Если это промежуточный шаг — показываем награду на 1.5 секунды,
                // затем скрываем ее и продолжаем бежать дальше.
                if (!isLastStep) {
                    delay(1500.milliseconds)
                    showRewardCard = false
                    delay(200.milliseconds) // пауза на анимацию свертывания
                    activeReward = null
                }
            }
        }

        // Если за весь левелап не было показано наград, ИЛИ на последнем шаге награды нет — 
        // автоматически закрываем оверлей через 3 секунды.
        // Если на последнем шаге была награда (showRewardCard == true), плашка НЕ закрывается сама,
        // чтобы пользователь успел изучить награду и нажать на крестик или смахнуть баннер.
        val finalReward = getSystemRewardForLevel(state.toLevel)
        if (!hasRewardShowed || finalReward == null) {
            delay(3000.milliseconds)
            dismissOverlay()
        }
    }

    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .fillMaxSize()
            .zIndex(99f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(), // Выезжает ровно из-под топ-бара
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    // Свайп влево/вправо для закрытия плашки
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (abs(offsetX) > 100f) {
                                    dismissOverlay()
                                } else {
                                    scope.launch {
                                        offsetX = 0f
                                    }
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount
                        }
                    }
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .alpha(1f - (abs(offsetX) / 300f).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(32.dp))
                    .background(colors.surfaceHigh.copy(alpha = 0.95f))
                    .border(1.dp, colors.neonAccent.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Верхний блок: Бегущий прогресс-бар и уровни
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Левая цифра (предыдущий/исходный уровень)
                    Text(
                        text = currentPlayingLevel.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        ),
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Заполняющийся прогресс-бар
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(colors.progressTrack)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(progress.value)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(colors.accentSecondary, colors.neonAccent)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Правая цифра (новый целевой уровень)
                    Text(
                        text = targetPlayingLevel.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        ),
                        color = colors.neonAccent,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                    )

                    // Крестик закрытия (только если показана награда и оверлей ждет ручного закрытия)
                    AnimatedVisibility(
                        visible = showRewardCard,
                        enter = fadeIn(tween(300)) + scaleIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = dismissOverlay,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.textSecondary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = IconsManager.Close,
                                contentDescription = "Закрыть",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Премиальный блок наград
                if (showRewardCard && activeReward != null) {
                    activeReward?.let { reward ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.textSecondary.copy(alpha = 0.1f))
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LevelUpRewardCard(reward = reward)
                    }
                }
            }
        }
    }
}