package com.ensodai.avalon.presentation.chatScreen.component.explosionEffect

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.delay


val telegramLikeConfig = ExplosionConfig(
    durationMs = 700,             // Ускоряем! 0.7 сек — идеальный тайминг для глаза
    particleCount = 24,           // Чуть больше плотности

    // Резкий разлет вверх и в стороны
    verticalRange = 300f..600f,
    horizontalRange = -400f..400f,

    // ВАЖНО: Импульс. 2.5 - 3.0 дает эффект "выстрела"
    initialImpulse = 1.8f,

    // Убираем задержку старта. В ТГ все вылетают почти разом
    startDelayRange = 0f..0.05f,

    // Ускоряем исчезновение, чтобы экран не был "замусорен"
    fadeOutStartPct = 0.3f,       // Начинают исчезать уже после 30% пути
    fadeInPct = 0.05f             // Почти мгновенное появление
)

@Composable
fun ReactionSlot(
    model: ReactionUiModel,
    isNewlyAdded: Boolean,
    onClick: () -> Unit
) {
    var hasTriggeredExplosion by remember { mutableStateOf(false) }
    var activeExplosionId by remember { mutableStateOf<Long?>(null) }
    var emojiCenterInWindow by remember { mutableStateOf<Offset?>(null) }
    val overlayController = LocalReactionOverlayController.current

    LaunchedEffect(isNewlyAdded, emojiCenterInWindow) {
        val origin = emojiCenterInWindow
        if (isNewlyAdded && origin != null && !hasTriggeredExplosion) {
            hasTriggeredExplosion = true
            activeExplosionId = overlayController.emit(
                emojis = EmojiPalette.getCascade(model.emoji),
                originInWindow = origin,
                config = telegramLikeConfig
            )
            delay(telegramLikeConfig.durationMs.toLong())
            activeExplosionId = null
        }
    }

    Box(contentAlignment = Alignment.Center) {
        ReactionPill(
            reaction = model,
            onClick = onClick,
            onEmojiAnchorChanged = { center ->
                emojiCenterInWindow = center
                activeExplosionId?.let { explosionId ->
                    overlayController.updateExplosionOrigin(explosionId, center)
                }
            }
        )
    }
}
