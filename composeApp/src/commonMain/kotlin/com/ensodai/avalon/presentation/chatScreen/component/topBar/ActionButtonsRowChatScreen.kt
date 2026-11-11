package com.ensodai.avalon.presentation.chatScreen.component.topBar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.AnimateCopyButton
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.AnimateDeleteButton
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.AnimateDoneButton
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.AnimateEditButton
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.AnimateRefreshButton
import com.ensodai.avalon.presentation.chatScreen.component.topBar.actionRowButtons.DropdownMenuButton

@Composable
fun ActionButtonsRowChatScreen(
    isEnableCopyButton: Boolean,
    isEnableSaveEditedMessageButton: Boolean,
    isEnableRefreshButton: Boolean,
    isEnableEditButton: Boolean,
    isEnableDeleteButton: Boolean,
    isEnableDropdownMenuButton: Boolean,
    onCopyMessage: () -> Unit,
    onRefreshGenerateMessage: () -> Unit,
    onEnableEditMessageMode: () -> Unit,
    onDeleteSelectedMessage: () -> Unit,
    onSaveEditMessage: () -> Unit,
    onCreateNewChatVariant: () -> Unit,
    onOpenAllChatVariantsScreen: () -> Unit,
    onOpenLiveCallScreen: () -> Unit,
    onEnableSearchMessageMode: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimateCopyButton(
            onClick = onCopyMessage,
            isVisible = isEnableCopyButton
        )
        AnimateRefreshButton(
            onClick = onRefreshGenerateMessage,
            isVisible = isEnableRefreshButton
        )
        AnimateEditButton(
            onClick = onEnableEditMessageMode,
            isVisible = isEnableEditButton
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .align(Alignment.CenterVertically),
            contentAlignment = Alignment.Center
        ) {
            AnimateDeleteButton(
                onClick = onDeleteSelectedMessage,
                isVisible = isEnableDeleteButton
            )
            AnimateDoneButton(
                onClick = onSaveEditMessage,
                isVisible = isEnableSaveEditedMessageButton
            )
            DropdownMenuButton(
                isEnableDropDownButton = isEnableDropdownMenuButton,
                onEnableSearchMessageMode = onEnableSearchMessageMode,
                modifier = Modifier,
                onCreateNewChatVariant = onCreateNewChatVariant,
                onOpenAllChatVariantsScreen = onOpenAllChatVariantsScreen,
                onOpenLiveCallScreen = onOpenLiveCallScreen
            )
        }
    }
}
