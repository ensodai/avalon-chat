package com.ensodai.avalon.presentation.chatScreen.component.bottomBar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.presentation.chatScreen.viewState.ActionButtonState
import com.ensodai.avalon.presentation.chatScreen.viewState.ActionButtonType
import com.ensodai.avalon.theme.IconsManager


@Composable
fun SendMessageButtons(
    actionButtonState: ActionButtonState,
    isGenerateResponseButtonEnable: Boolean,
    attachedImagesSize: Int,
    onSendMessageClick: () -> Unit,
    onForceGenerateResponseClick: () -> Unit,
    onShowPermissionDialog: () -> Unit,
    onClickAddIconButton: () -> Unit,
    onStopGenerateResponse: () -> Unit,
    onAttachImages: (List<CharacterImageDTO>) -> Unit,
    showInlinePlusButton: Boolean,
    isInstructionMode: Boolean,
    onConfirmInstruction: () -> Unit,
    onCancelInstruction: () -> Unit
) {

    AnimatedContent(
        targetState = isInstructionMode,
        transitionSpec = {
            (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                .togetherWith(
                    fadeOut(animationSpec = tween(90))
                )
        },
        label = "ButtonSwitchAnimation",
        contentAlignment = Alignment.CenterEnd
    ) { targetIsInstructionMode ->

        if (targetIsInstructionMode) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = onConfirmInstruction,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = IconsManager.Done,
                        contentDescription = "Confirm",
                        tint = com.ensodai.avalon.theme.AvalonTheme.colors.neonAccent
                    )
                }
                IconButton(
                    onClick = onCancelInstruction,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = IconsManager.Close,
                        contentDescription = "Cancel",
                        tint = com.ensodai.avalon.theme.AvalonTheme.colors.error
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = showInlinePlusButton,
                    enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                    exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)
                ) {
                    IconButton(
                        onClick = { onClickAddIconButton() },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Icon(
                            imageVector = IconsManager.Add,
                            contentDescription = "Attach",
                            tint = com.ensodai.avalon.theme.AvalonTheme.colors.textSecondary
                        )
                    }
                }

                AnimatedContent(
                    targetState = actionButtonState.type,
                    transitionSpec = {

                        val duration = 150

                        (fadeIn(animationSpec = tween(duration)) +
                                scaleIn(initialScale = 0.8f, animationSpec = tween(duration)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(duration)) +
                                        scaleOut(
                                            targetScale = 0.8f,
                                            animationSpec = tween(duration)
                                        )
                            )
                    },
                    label = "ActionButtonAnimation"
                ) { type ->
                    when (type) {
                        ActionButtonType.SEND -> {
                            IconButton(
                                onClick = { onSendMessageClick() },
                                enabled = actionButtonState.isEnabled,
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Icon(
                                    imageVector = IconsManager.Send,
                                    contentDescription = "Send",
                                    tint = com.ensodai.avalon.theme.AvalonTheme.colors.neonAccent
                                )
                            }
                        }

                        ActionButtonType.FORCE_START -> {
                            IconButton(
                                onClick = { onForceGenerateResponseClick() },
                                enabled = actionButtonState.isEnabled,
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Icon(
                                    modifier = Modifier.size(28.dp),
                                    imageVector = IconsManager.KeyboardDoubleArrowRight,
                                    contentDescription = "Force generate"
                                )
                            }
                        }

                        ActionButtonType.STOP -> {
                            IconButton(
                                onClick = { onStopGenerateResponse() },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Icon(
                                    modifier = Modifier.size(28.dp),
                                    imageVector = IconsManager.Stop,
                                    contentDescription = "Stop",
                                    tint = com.ensodai.avalon.theme.AvalonTheme.colors.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingPlusButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(com.ensodai.avalon.theme.AvalonTheme.colors.surfaceHigh)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = IconsManager.Add,
            contentDescription = "Attach",
            tint = com.ensodai.avalon.theme.AvalonTheme.colors.textMain
        )
    }
}