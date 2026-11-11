package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.layout.Layout
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun GemIslandTopBar(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    actionsCircular: Boolean = false,
    titleHeight: Dp = 40.dp,
    titleContentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    isTitleVisible: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
    ) {
        Layout(
            content = {
                // Элемент 0: Navigation Icon
                Box(propagateMinConstraints = true) {
                    if (navigationIcon != null) {
                        GemPill(
                            modifier = Modifier.size(44.dp),
                            horizontalArrangement = Arrangement.Center,
                            fillContentSize = true
                        ) {
                            navigationIcon()
                        }
                    }
                }

                // Элемент 1: Title
                Box(propagateMinConstraints = true) {
                    if (title != null) {
                        val titleAlpha by animateFloatAsState(
                            targetValue = if (isTitleVisible) 1f else 0f,
                            animationSpec = tween(durationMillis = 150)
                        )
                        val titleScale by animateFloatAsState(
                            targetValue = if (isTitleVisible) 1f else 0.85f,
                            animationSpec = tween(durationMillis = 150)
                        )

                        if (titleAlpha > 0.01f) {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = titleAlpha
                                        scaleX = titleScale
                                        scaleY = titleScale
                                    }
                            ) {
                                GemPill(
                                    modifier = Modifier
                                        .height(titleHeight),
                                    contentPadding = titleContentPadding,
                                    horizontalArrangement = Arrangement.Center,
                                    fillContentSize = false,
                                    fillMaxHeight = true
                                ) {
                                    ProvideTextStyle(MaterialTheme.typography.titleMedium.copy(color = AvalonTheme.colors.textMain)) {
                                        title()
                                    }
                                }
                            }
                        }
                    }
                }

                // Элемент 2: Actions
                Box(propagateMinConstraints = true) {
                    if (actions != null) {
                        val actionsPaddingHorizontal by animateDpAsState(
                            targetValue = if (actionsCircular) 0.dp else 4.dp,
                            animationSpec = spring(
                                dampingRatio = 0.75f,
                                stiffness = 600f
                            ),
                            label = "ActionsPadding"
                        )

                        val actionsArrangement = if (actionsCircular) Arrangement.Center else Arrangement.End
                        val actionsAlignment = if (actionsCircular) Alignment.CenterHorizontally else Alignment.End
                        val actionsCenterAlignment = if (actionsCircular) Alignment.Center else Alignment.CenterEnd

                        GemPill(
                            modifier = Modifier
                                .height(44.dp)
                                .widthIn(min = 44.dp),
                            contentPadding = PaddingValues(horizontal = actionsPaddingHorizontal.coerceAtLeast(0.dp)),
                            horizontalArrangement = actionsArrangement,
                            fillContentSize = false,
                            fillMaxHeight = true
                        ) {
                            CompositionLocalProvider(
                                LocalMinimumInteractiveComponentSize provides Dp.Unspecified
                            ) {
                                Row(
                                    modifier = Modifier
                                        .wrapContentWidth(align = actionsAlignment, unbounded = true)
                                        .animateContentSize(
                                            animationSpec = spring(
                                                dampingRatio = 0.75f,
                                                stiffness = 600f
                                            ),
                                            alignment = actionsCenterAlignment
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = actionsArrangement
                                ) {
                                    actions()
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .align(Alignment.Center)
        ) { measurables, constraints ->
            val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

            // Измеряем левую и правую кнопки
            val navPlaceable = measurables[0].measure(looseConstraints)
            val actionsPlaceable = measurables[2].measure(looseConstraints)

            val navWidth = navPlaceable.width
            val actionsWidth = actionsPlaceable.width

            // Максимальная доступная ширина заголовка с учетом отступов (8.dp с каждой стороны)
            val paddingPx = 8.dp.roundToPx()
            val maxTitleWidth = constraints.maxWidth - navWidth - actionsWidth - (paddingPx * 2)

            val titleConstraints = constraints.copy(
                minWidth = 0,
                maxWidth = maxTitleWidth.coerceAtLeast(0)
            )
            val titlePlaceable = measurables[1].measure(titleConstraints)
            val titleWidth = titlePlaceable.width

            val layoutWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else navWidth + actionsWidth + (paddingPx * 2) + titleWidth
            val layoutHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else maxOf(navPlaceable.height, titlePlaceable.height, actionsPlaceable.height)

            layout(layoutWidth, layoutHeight) {
                // Размещаем левую иконку (по левому краю)
                val navY = (constraints.maxHeight - navPlaceable.height) / 2
                navPlaceable.placeRelative(0, navY)

                // Размещаем правую иконку/кнопки (по правому краю)
                val actionsX = constraints.maxWidth - actionsWidth
                val actionsY = (constraints.maxHeight - actionsPlaceable.height) / 2
                actionsPlaceable.placeRelative(actionsX, actionsY)

                // Размещаем заголовок
                // Идеально по центру экрана:
                val preferredTitleX = (constraints.maxWidth - titleWidth) / 2

                // Но заголовок не должен перекрывать левую и правую кнопки:
                val minTitleX = navWidth + paddingPx
                val maxTitleX = constraints.maxWidth - actionsWidth - titleWidth - paddingPx

                val titleX = preferredTitleX.coerceIn(minTitleX, maxTitleX.coerceAtLeast(minTitleX))
                val titleY = (constraints.maxHeight - titlePlaceable.height) / 2
                titlePlaceable.placeRelative(titleX, titleY)
            }
        }
    }
}
