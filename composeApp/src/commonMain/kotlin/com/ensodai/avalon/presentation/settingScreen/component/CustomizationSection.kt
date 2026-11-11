package com.ensodai.avalon.presentation.settingScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.customization
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.presentation.settingScreen.component.SettingsGroupCard
import com.ensodai.avalon.presentation.settingScreen.component.SettingsRowItem
import com.ensodai.avalon.presentation.settingScreen.component.SettingIconBadge

@Composable
fun CustomizationSection(
    onClick: () -> Unit,
) {
    SettingsGroupCard {
        SettingsRowItem(
            title = stringResource(Res.string.customization),
            icon = {
                SettingIconBadge(
                    imageVector = IconsManager.DashboardCustomize,
                    backgroundColor = Color(0xFF10B981) // Изумрудно-зеленый цвет
                )
            },
            onClick = onClick
        )
    }
}
