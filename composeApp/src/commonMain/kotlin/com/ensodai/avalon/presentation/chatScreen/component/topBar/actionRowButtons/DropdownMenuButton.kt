package com.ensodai.avalon.presentation.chatScreen.component.topBar.actionRowButtons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.all_chat_variants
import com.ensodai.avalon.generated.resources.new_chat_variant
import com.ensodai.avalon.generated.resources.livecall_start_call
import com.ensodai.avalon.presentation.commonComponents.BaseItemScaleAnimation
import com.ensodai.avalon.presentation.commonComponents.ExpressiveMenuItem
import com.ensodai.avalon.presentation.commonComponents.ExpressivePopupMenu
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun DropdownMenuButton(
    isEnableDropDownButton: Boolean,
    onEnableSearchMessageMode: () -> Unit,
    modifier: Modifier,
    onCreateNewChatVariant: () -> Unit,
    onOpenAllChatVariantsScreen: () -> Unit,
    onOpenLiveCallScreen: () -> Unit
) {
    BaseItemScaleAnimation(isVisible = isEnableDropDownButton) {
        ExpressivePopupMenu(
            modifier = modifier,
            triggerContent = { onClick ->
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = IconsManager.MoreVert,
                        contentDescription = "Expand menu",
                        tint = AvalonTheme.colors.textMain
                    )
                }
            },
            menuContent = { onDismiss ->

                ExpressiveMenuItem(
                    text = stringResource(Res.string.new_chat_variant),
                    icon = IconsManager.AddComment,
                    onClick = {
                        onCreateNewChatVariant()
                        onDismiss()
                    }
                )

                ExpressiveMenuItem(
                    text = stringResource(Res.string.all_chat_variants),
                    icon = IconsManager.DensitySmall,
                    onClick = {
                        onOpenAllChatVariantsScreen()
                        onDismiss()
                    }
                )

//                ExpressiveMenuItem(
//                    text = stringResource(Res.string.livecall_start_call),
//                    icon = IconsManager.Phone,
//                    onClick = {
//                        onOpenLiveCallScreen()
//                        onDismiss()
//                    }
//                )

//                ExpressiveMenuItem(
//                    text = stringResource(Res.string.search),
//                    icon = Icons.Default.Search,
//                    onClick = {
//                        onEnableSearchMessageMode()
//                        onDismiss()
//                    }
//                )
            }
        )
    }
}
