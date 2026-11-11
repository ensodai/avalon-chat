package com.ensodai.avalon.presentation.chatScreen.component.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupPositionProvider
import com.ensodai.avalon.presentation.chatScreen.component.explosionEffect.EmojiPalette
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager

class SmartReactionPositionProvider(
    private val touchOffset: Offset,
    private val density: Density,
    private val paddingPx: Int = 20
) : PopupPositionProvider {

    private val maxExpectedHeightPx = with(density) { 360.dp.roundToPx() }

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {

        val globalTouchX = anchorBounds.left + touchOffset.x.toInt()
        val globalTouchY = anchorBounds.top + touchOffset.y.toInt()

        var x = globalTouchX - (popupContentSize.width / 2)

        if (x < paddingPx) x = paddingPx
        if (x + popupContentSize.width > windowSize.width - paddingPx) {
            x = windowSize.width - popupContentSize.width - paddingPx
        }

        val spaceBelow = windowSize.height - globalTouchY - paddingPx

        val spaceAbove = globalTouchY - paddingPx

        val shouldOpenUpwards = (spaceBelow < maxExpectedHeightPx) && (spaceAbove > spaceBelow)

        val y = if (shouldOpenUpwards) {
            globalTouchY - popupContentSize.height - paddingPx
        } else {

            globalTouchY + paddingPx
        }

        return IntOffset(x, y)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReactionPickerContent(
    onEmojiSelected: (String) -> Unit
) {
    val colors = AvalonTheme.colors

    val allEmojis = remember { EmojiPalette.getAvailableReactions() }

    val collapsedCount = 5

    val showExpandButton = allEmojis.size > (collapsedCount + 1)

    var isExpanded by remember { mutableStateOf(false) }

    val visibleItems = remember(isExpanded, allEmojis) {
        if (!showExpandButton) {
            allEmojis.map { ReactionItem.Emoji(it) }
        } else if (isExpanded) {
            allEmojis.map { ReactionItem.Emoji(it) }
        } else {
            allEmojis.take(collapsedCount)
                .map { ReactionItem.Emoji(it) } + ReactionItem.ExpandButton
        }
    }

    val commonSpring = spring<Dp>(
        dampingRatio = 0.75f,
        stiffness = 350f
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 24.dp else 28.dp,
        animationSpec = commonSpring,
        label = "corner"
    )


    Surface(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = 350f
                )
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = colors.overlaySurface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, colors.overlayBorder),
        shadowElevation = 8.dp
    ) {
        FlowRow(
            modifier = Modifier
                .padding(6.dp)
                .verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.Center,
            maxItemsInEachRow = 6
        ) {
            visibleItems.forEach { item ->
                when (item) {
                    is ReactionItem.Emoji -> {
                        EmojiItem(
                            emoji = item.emoji,
                            onClick = { onEmojiSelected(item.emoji) }
                        )
                    }

                    is ReactionItem.ExpandButton -> {
                        ExpandButtonItem(
                            onClick = { isExpanded = true }
                        )
                    }
                }
            }
        }
    }
}

sealed interface ReactionItem {
    data class Emoji(val emoji: String) : ReactionItem
    object ExpandButton : ReactionItem
}

@Composable
fun EmojiItem(emoji: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 24.sp)
    }
}

@Composable
fun ExpandButtonItem(onClick: () -> Unit) {
    val colors = AvalonTheme.colors

    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.textMain.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = IconsManager.KeyboardArrowDown,
            contentDescription = "More",
            tint = colors.textMain.copy(alpha = 0.8f)
        )
    }
}
