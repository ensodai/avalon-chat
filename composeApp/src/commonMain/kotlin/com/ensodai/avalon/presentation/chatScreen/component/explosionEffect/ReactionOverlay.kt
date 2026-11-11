package com.ensodai.avalon.presentation.chatScreen.component.explosionEffect

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned

data class ActiveReactionExplosion(
    val id: Long,
    val emojis: List<String>,
    val originInWindow: Offset,
    val config: ExplosionConfig
)

@Stable
class ReactionOverlayController {
    private var nextId by mutableLongStateOf(0L)
    private val activeExplosions = mutableStateListOf<ActiveReactionExplosion>()
    private var hostPositionInWindow by mutableStateOf(Offset.Zero)

    val explosions: List<ActiveReactionExplosion> get() = activeExplosions

    fun updateHostPosition(positionInWindow: Offset) {
        hostPositionInWindow = positionInWindow
    }

    fun emit(emojis: List<String>, originInWindow: Offset, config: ExplosionConfig): Long {
        val id = nextId++
        activeExplosions += ActiveReactionExplosion(
            id = id,
            emojis = emojis,
            originInWindow = originInWindow,
            config = config
        )
        return id
    }

    fun updateExplosionOrigin(id: Long, originInWindow: Offset) {
        val index = activeExplosions.indexOfFirst { it.id == id }
        if (index == -1) return

        activeExplosions[index] = activeExplosions[index].copy(originInWindow = originInWindow)
    }

    fun toLocal(originInWindow: Offset): Offset {
        return originInWindow - hostPositionInWindow
    }

    fun remove(id: Long) {
        activeExplosions.removeAll { it.id == id }
    }
}

val LocalReactionOverlayController = compositionLocalOf<ReactionOverlayController> {
    ReactionOverlayController()
}

@Composable
fun rememberReactionOverlayController(): ReactionOverlayController {
    return remember { ReactionOverlayController() }
}

@Composable
fun ReactionOverlayHost(
    controller: ReactionOverlayController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            controller.updateHostPosition(coordinates.localToWindow(Offset.Zero))
        }
    ) {
        controller.explosions.forEach { explosion ->
            ReactionEffect(
                explosionId = explosion.id,
                emojis = explosion.emojis,
                modifier = Modifier.matchParentSize(),
                origin = controller.toLocal(explosion.originInWindow),
                config = explosion.config,
                onFinished = { controller.remove(explosion.id) }
            )
        }
    }
}
