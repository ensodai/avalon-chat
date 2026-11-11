package com.ensodai.avalon.presentation.mainScreen.component.topBar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.theme.IconsManager

@Composable
fun ActionsRowMainScreen(
    modifier: Modifier = Modifier,
    isShowAccountButton: Boolean,
    isShowDeleteButton: Boolean,
    isShowEditButton: Boolean,
    isShowCopyButton: Boolean,
    isShowSearchButton: Boolean,
    onCopyClick: () -> Unit,
    onSearchCharacterClick: () -> Unit,
    onEditSelectedChat: () -> Unit,
    onDeleteSelectedChats: () -> Unit,
    onOpenAccountClick: () -> Unit,
    onArchivedSelectedChats: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
//        // СѓРґР°Р»РёС‚СЊ
//        BaseItemScaleAnimation(isVisible =false) {
//            IconButton(
//                onClick = { onCopyClick() },
//            ) {
//                Icon(
//                    imageVector = Icons.Outlined.ContentCopy,
//                    contentDescription = "ContentCopy"
//                )
//            }
//        }
//        // СѓРґР°Р»РёС‚СЊ
//        BaseItemScaleAnimation(isVisible =false) {
//            IconButton(
//                onClick = { onEditSelectedChat() },
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Edit,
//                    contentDescription = "Edit"
//                )
//            }
//        }
        BaseItemAlphaAnimation(isVisible = isShowDeleteButton) {
            IconButton(
                onClick = { onArchivedSelectedChats() },
            ) {
                Icon(
                    imageVector = IconsManager.Archive,
                    contentDescription = "Archive selected chats"
                )
            }
        }
        Box {
            BaseItemAlphaAnimation(isVisible = isShowDeleteButton) {
                IconButton(
                    onClick = { onDeleteSelectedChats() },
                ) {
                    Icon(
                        imageVector = IconsManager.Delete,
                        contentDescription = "Delete selected chats"
                    )
                }
            }
            BaseItemAlphaAnimation(isVisible = isShowAccountButton) {
                IconButton(
                    onClick = { onOpenAccountClick() },
                    modifier = Modifier.testTag(BenchmarkTags.MainAccountButton)
                ) {
                    Icon(
                        imageVector = IconsManager.AccountCircle,
                        contentDescription = "Open account"
                    )
                }
            }
            BaseItemAlphaAnimation(isVisible = isShowSearchButton) {
                IconButton(
                    onClick = { onSearchCharacterClick() },
                ) {
                    Icon(
                        imageVector = IconsManager.Search,
                        contentDescription = "Search"
                    )
                }
            }
        }
    }
}
