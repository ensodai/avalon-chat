package com.ensodai.avalon.presentation.settingScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.presentation.settingScreen.component.SettingsGroupCard
import com.ensodai.avalon.presentation.settingScreen.component.SettingsRowItem
import com.ensodai.avalon.presentation.settingScreen.component.SettingIconBadge

@Composable
fun AiSettingsSection(
    onClick: () -> Unit,
) {
    SettingsGroupCard {
        SettingsRowItem(
            title = "User AI Settings",
            icon = {
                SettingIconBadge(
                    imageVector = IconsManager.SmartToy,
                    backgroundColor = Color(0xFF8B5CF6) // Purple color
                )
            },
            onClick = onClick
        )
    }
}
