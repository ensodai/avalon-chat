package com.ensodai.avalon.presentation.commonComponents.welcomCard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import kotlin.math.abs

data class PagerImage(
    val url: String? = null,
    val painter: Painter? = null,
    val contentDescription: String? = null
)

data class WelcomeCardState(
    val welcomeCardDescription: String? = null,
    val welcomeCardImages: List<WelcomeCardImageDto> = emptyList(),
    val startColor: CardColor = CardColor.DEFAULT,
    val endColor: CardColor = CardColor.DEFAULT,
    val colorLight: CardColor = CardColor.DEFAULT,
    val allowedColors: List<CardColor> = CardColor.getSelectableColors(),
    val isEnableLight: Boolean = true
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeCard(
    modifier: Modifier = Modifier,
    text: String?,
    images: List<WelcomeCardImageDto>?,
    startColor: CardColor,
    endColor: CardColor,
    isEnableLight: Boolean
) {
    val colors = AvalonTheme.colors
    if (text.isNullOrEmpty() && images.isNullOrEmpty()) return

    var isExpanded by remember { mutableStateOf(false) }

    var showMoreButton by remember { mutableStateOf(false) }

    val colorStart = startColor.value
    val colorEnd = endColor.value

    TechnoGlowCard(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        glowColorStart = colorStart,
        glowColorEnd = colorEnd,
        isEnableLight = isEnableLight,
        borderColorStart = colorStart,
        borderColorEnd = colorEnd
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(400, easing = LinearOutSlowInEasing))
        ) {

            if (!images.isNullOrEmpty()) {
                Box(
                    modifier = Modifier.clip(
                        RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp
                        )
                    )
                ) {
                    ImagePagerWithIndicator(images = images)
                }
            }

            if (!text.isNullOrEmpty()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TextContent(
                            title = text,
                            // Если развернуто - показываем всё, если нет - 5 строк
                            maxLines = if (isExpanded) Int.MAX_VALUE else 5,
                            modifier = Modifier.fillMaxWidth(),
                            onTextLayout = { hasOverflow ->
                                // Логика кнопки.
                                // Мы обновляем состояние кнопки только если НЕ развернуты.
                                // Если развернуты, кнопка "Свернуть" управляется просто флагом isExpanded.
                                if (!isExpanded) {
                                    showMoreButton = hasOverflow
                                }
                            }
                        )

                        // Градиент затемнения (только если свернуто и кнопка нужна)
                        if (!isExpanded && showMoreButton) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, colors.overlaySurface)
                                        )
                                    )
                            )
                        }
                    }

                    // Кнопка "Показать еще"
                    if (showMoreButton) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { isExpanded = !isExpanded },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isExpanded) IconsManager.KeyboardArrowUp else IconsManager.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = colors.textSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TechnoGlowCard(
    modifier: Modifier = Modifier,
    glowColorStart: Color,
    glowColorEnd: Color,
    borderColorStart: Color,
    borderColorEnd: Color,
    isEnableLight: Boolean,
    backgroundColor: Color = AvalonTheme.colors.overlaySurface,
    content: @Composable () -> Unit
) {
    val glowAlpha = if (isEnableLight) 0.4f else 0f
    val cornerRadius = 24.dp

    Box(
        modifier = modifier
            .drawBehind {
                if (isEnableLight && glowAlpha > 0f) {
                    val inset = 4.dp.toPx()
                    val cornerRadiusPx = cornerRadius.toPx()
                    val glowLayers = listOf(
                        28.dp.toPx() to glowAlpha * 0.16f,
                        18.dp.toPx() to glowAlpha * 0.22f,
                        10.dp.toPx() to glowAlpha * 0.32f
                    )

                    glowLayers.forEach { (strokeWidth, alpha) ->
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    glowColorStart.copy(alpha = alpha),
                                    glowColorEnd.copy(alpha = alpha)
                                )
                            ),
                            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                            size = androidx.compose.ui.geometry.Size(
                                width = size.width - inset * 2,
                                height = size.height - inset * 2
                            ),
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                            style = Stroke(width = strokeWidth)
                        )
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(borderColorStart, borderColorEnd)
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
        ) {
            content()
        }
    }
}

@Composable
fun TextContent(
    modifier: Modifier = Modifier,
    title: String,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (Boolean) -> Unit
) {
    val colors = AvalonTheme.colors
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0f)
                .layout { measurable, constraints ->
                    measurable.measure(constraints)
                    layout(0, 0) {}
                },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textSecondary,
                textAlign = TextAlign.Start
            ),
            maxLines = Int.MAX_VALUE,
            onTextLayout = { textLayoutResult ->
                onTextLayout(textLayoutResult.lineCount > maxLines)
            }
        )

        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textSecondary,
                textAlign = TextAlign.Start
            ),
            maxLines = maxLines
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePagerWithIndicator(
    modifier: Modifier = Modifier,
    images: List<WelcomeCardImageDto>
) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = images[page]
            ShimmerAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = item.url,
                seed = item.imagePublicId.toString(),
                contentDescription = "image",
                blurhash = item.thumbHash
            )
        }

        if (images.size > 1) {
            PageIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }
    }
}


@Composable
fun PageIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = AvalonTheme.colors.textMain,
    inactiveColor: Color = AvalonTheme.colors.textMain.copy(alpha = 0.5f),
    dotSize: Dp = 8.dp,
    spacing: Dp = 8.dp,
) {
    val colors = AvalonTheme.colors
    val (indicatorOffset, indicatorWidth) = remember(pagerState) {
        derivedStateOf {
            val currentPage = pagerState.currentPage
            val offsetFraction = pagerState.currentPageOffsetFraction
            val scrollDistance = dotSize + spacing

            if (offsetFraction == 0f) {
                return@derivedStateOf Pair(scrollDistance * currentPage, dotSize)
            }

            val targetPage = if (offsetFraction > 0) currentPage + 1 else currentPage - 1
            val easing = FastOutSlowInEasing.transform(abs(offsetFraction))

            val currentDotOffset = scrollDistance * currentPage
            val targetDotOffset = scrollDistance * targetPage

            val endOffset = lerp(currentDotOffset, targetDotOffset, easing)

            val startOffsetProgress = (abs(offsetFraction) - 0.5f).coerceAtLeast(0f) * 2f
            val startOffset =
                lerp(
                    currentDotOffset,
                    targetDotOffset,
                    FastOutSlowInEasing.transform(startOffsetProgress)
                )

            if (offsetFraction > 0) {
                Pair(startOffset, (endOffset + dotSize) - startOffset)
            } else {
                Pair(endOffset, (startOffset + dotSize) - endOffset)
            }
        }
    }.value


    Box(
        modifier = modifier
            .background(
                color = colors.overlaySurface.copy(alpha = 0.8f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pagerState.pageCount) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(inactiveColor)
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(indicatorWidth)
                .height(dotSize)
                .clip(CircleShape)
                .background(activeColor)
        )
    }
}
