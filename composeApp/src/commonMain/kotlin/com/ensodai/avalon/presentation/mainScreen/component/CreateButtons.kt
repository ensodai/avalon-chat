package com.ensodai.avalon.presentation.mainScreen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.presentation.commonComponents.BaseItemScaleAnimation
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager

@Composable
fun CreateButtons(
    onOpenTimeChat: () -> Unit,
    onOpenEditChatConfigScreen: () -> Unit,
    onOpenImportCharacterScreen: () -> Unit,
    onClickTestButton: () -> Unit,
    isShowFloatingActionButton: Boolean
) {
    val colors = AvalonTheme.colors
    var isExpanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        label = "fab_rotation"
    )

    BaseItemScaleAnimation(isShowFloatingActionButton) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Раскрывающиеся опции (появляются вверх)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Опция 1: Импортировать
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.cardBackground,
                            border = BorderStroke(1.dp, colors.surfaceHigh),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = "Импортировать",
                                color = colors.textMain,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        FloatingActionButton(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            onClick = {
                                isExpanded = false
                                onOpenImportCharacterScreen()
                            },
                            containerColor = colors.surfaceHigh,
                            contentColor = colors.neonAccent,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Icon(
                                imageVector = IconsManager.Download,
                                contentDescription = "Импортировать карточку",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Опция 2: Создать нового
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.cardBackground,
                            border = BorderStroke(1.dp, colors.surfaceHigh),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = "Создать",
                                color = colors.textMain,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        FloatingActionButton(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            onClick = {
                                isExpanded = false
                                onOpenEditChatConfigScreen()
                            },
                            containerColor = colors.surfaceHigh,
                            contentColor = colors.neonAccent,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Icon(
                                imageVector = IconsManager.SmartToy,
                                contentDescription = "Создать персонажа",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Главная кнопка FAB (плюс / крестик)
            FloatingActionButton(
                shape = CircleShape,
                onClick = { isExpanded = !isExpanded },
                containerColor = colors.neonAccent,
                contentColor = colors.userBubbleTextColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                )
            ) {
                Icon(
                    imageVector = IconsManager.Add,
                    contentDescription = if (isExpanded) "Закрыть меню" else "Добавить персонажа",
                    modifier = Modifier.graphicsLayer(rotationZ = rotation)
                )
            }
        }
    }
}
