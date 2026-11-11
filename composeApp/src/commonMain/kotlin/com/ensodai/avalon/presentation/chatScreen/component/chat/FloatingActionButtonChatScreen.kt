package com.ensodai.avalon.presentation.chatScreen.component.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.chatScreen.component.bottomBar.FloatingPlusButton
import com.ensodai.avalon.presentation.commonComponents.BaseItemScaleAnimation
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager

@Composable
fun FloatingActionButtonChatScreen(
    isEnableSearchMode: Boolean,
    isEnableScrollDownButton: Boolean,
    showPlusButton: Boolean,
    onClickFloatingPlusButton: () -> Unit,
    onMoveUpSearchMessage: () -> Unit,
    onMoveDownSearchMessage: () -> Unit,
    onAutoScrollDown: () -> Unit
) {
    val colors = AvalonTheme.colors

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        BaseItemScaleAnimation(isEnableSearchMode) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    modifier = Modifier.size(48.dp),
                    onClick = { onMoveUpSearchMessage() },
                    shape = CircleShape,
                    containerColor = colors.surfaceHigh,
                    contentColor = colors.textMain,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    )
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = IconsManager.KeyboardArrowUp,
                        contentDescription = "Up"
                    )
                }

                FloatingActionButton(
                    modifier = Modifier.size(48.dp),
                    onClick = { onMoveDownSearchMessage() },
                    shape = CircleShape,
                    containerColor = colors.surfaceHigh,
                    contentColor = colors.textMain,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    )
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = IconsManager.KeyboardArrowDown,
                        contentDescription = "Down"
                    )
                }
            }
        }

        BaseItemScaleAnimation(isEnableScrollDownButton && !isEnableSearchMode) {
            FloatingActionButton(
                modifier = Modifier.size(48.dp),
                onClick = { onAutoScrollDown() },
                shape = CircleShape,
                containerColor = colors.surfaceHigh,
                contentColor = colors.textMain,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                )
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = IconsManager.KeyboardArrowDown,
                    contentDescription = "Down"
                )
            }
        }

        BaseItemScaleAnimation(isVisible = showPlusButton && !isEnableSearchMode) {
            FloatingPlusButton(onClick = onClickFloatingPlusButton)
        }
    }
}
