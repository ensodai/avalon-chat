package com.ensodai.avalon.presentation.settingScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.telegram_avalon_avatars
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.presentation.settingScreen.component.SettingsGroupCard
import com.ensodai.avalon.presentation.settingScreen.component.SettingsRowItem
import com.ensodai.avalon.presentation.settingScreen.component.SettingIconBadge

@Composable
fun TelegramLinkSection(
    onClick: () -> Unit,
) {
    SettingsGroupCard {
        SettingsRowItem(
            title = stringResource(Res.string.telegram_avalon_avatars),
            icon = {
                SettingIconBadge(
                    imageVector = IconsManager.Message,
                    backgroundColor = Color(0xFF0088CC) // Цвет Telegram
                )
            },
            actionContent = {
                Text(
                    text = "@avalon_avatars",
                    color = AvalonTheme.colors.infoAccent,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onClick = onClick
        )
    }
}
