package com.ensodai.avalon.presentation.commonComponents.topBarIcons

import androidx.compose.runtime.Composable
import com.ensodai.avalon.theme.IconsManager

@Composable
fun AnimateCopyButton(
    isVisible: Boolean,
    onClick: () -> Unit
) {
    AnimatedIconButton(
        isVisible = isVisible,
        onClick = { onClick() },
        icon = IconsManager.ContentCopy,
        contentDescription = "Copy button"
    )
}

@Composable
fun AnimateDeleteButton(
    isVisible: Boolean,
    onClick: () -> Unit
) {
    AnimatedIconButton(
        isVisible = isVisible,
        onClick = { onClick() },
        icon = IconsManager.Delete,
        contentDescription = "delete button"
    )
}

@Composable
fun AnimateDoneButton(
    isVisible: Boolean,
    onClick: () -> Unit
) {
    AnimatedIconButton(
        isVisible = isVisible,
        onClick = { onClick() },
        icon = IconsManager.Done,
        contentDescription = "Done button"
    )
}

@Composable
fun AnimateEditButton(
    isVisible: Boolean,
    onClick: () -> Unit
) {
    AnimatedIconButton(
        isVisible = isVisible,
        onClick = { onClick() },
        icon = IconsManager.Edit,
        contentDescription = "Edit button"
    )
}

@Composable
fun AnimateRefreshButton(
    isVisible: Boolean,
    onClick: () -> Unit
) {
    AnimatedIconButton(
        isVisible = isVisible,
        onClick = { onClick() },
        icon = IconsManager.Refresh,
        contentDescription = "Refresh button"
    )
}

@Composable
fun AnimatedSettingsButton(
    isVisible: Boolean,
    onClick: () -> Unit
) {
    AnimatedIconButton(
        isVisible = isVisible,
        onClick = { onClick() },
        icon = IconsManager.Settings,
        contentDescription = "Settings button"
    )
}

