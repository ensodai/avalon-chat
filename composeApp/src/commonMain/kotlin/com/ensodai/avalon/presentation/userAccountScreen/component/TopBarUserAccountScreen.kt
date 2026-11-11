package com.ensodai.avalon.presentation.userAccountScreen.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.profile
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.presentation.paymentScreen.viewState.icon
import com.ensodai.avalon.presentation.paymentScreen.viewState.primaryTint
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun TopBarUserAccountScreen(
    modifier: Modifier = Modifier,
    userNickName: String,
    premiumStatus: UserTariff,
    onBackButtonClick: () -> Unit,
    onOpenSettingsScreen: () -> Unit,
    onEditButtonClick: () -> Unit,
    isShowEditButton: Boolean
) {
    val colors = AvalonTheme.colors

    GemIslandTopBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (userNickName.isNotBlank()) "@$userNickName" else stringResource(Res.string.profile),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                if (premiumStatus != UserTariff.Free) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = premiumStatus.icon,
                        contentDescription = premiumStatus.title,
                        tint = premiumStatus.primaryTint(),
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        },
        navigationIcon = {
            NavigationBackButton(
                onBackButtonClick = onBackButtonClick
            )
        },
        actions = {
            if (isShowEditButton) {
                IconButton(onClick = onEditButtonClick) {
                    Icon(
                        imageVector = IconsManager.Edit,
                        contentDescription = "Edit profile",
                        tint = colors.textMain
                    )
                }
            }
            IconButton(onClick = onOpenSettingsScreen) {
                Icon(
                    imageVector = IconsManager.Settings,
                    contentDescription = "Settings",
                    tint = colors.textMain
                )
            }
        }
    )
}


