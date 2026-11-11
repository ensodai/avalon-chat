package com.ensodai.avalon.presentation.settingScreen.component.appSettingSection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.language
import com.ensodai.avalon.generated.resources.user_outreach_setting_title
import com.ensodai.avalon.generated.resources.user_outreach_setting_description
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.presentation.settingScreen.component.SettingsGroupCard
import com.ensodai.avalon.presentation.settingScreen.component.SettingsRowItem
import com.ensodai.avalon.presentation.settingScreen.component.SettingIconBadge
import com.ensodai.avalon.presentation.settingScreen.component.SettingsDivider
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppSettingSection(
    isEnableFlowMode: Boolean,
    isEnableLocalMode: Boolean,
    language: String?,
    languageList: List<String>,
    isOutreachEnabled: Boolean,
    dailyOutreachLimit: Int,
    onChangeLanguage: (String) -> Unit,
    onChangeFlowMode: () -> Unit,
    onChangeLocalMode: () -> Unit,
    onChangeOutreachMode: () -> Unit,
    onChangeOutreachLimit: (Int) -> Unit
) {
    val colors = AvalonTheme.colors
    var isShowLanguageSheet by remember { mutableStateOf(false) }

    SettingsGroupCard {
        SettingsRowItem(
            title = stringResource(Res.string.language),
            icon = {
                SettingIconBadge(
                    imageVector = IconsManager.Language,
                    backgroundColor = Color(0xFF3B82F6) // Синий цвет
                )
            },
            actionContent = {
                Text(
                    text = language.orEmpty(),
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onClick = { isShowLanguageSheet = true }
        )

//        SettingsDivider()
//
//        SettingsRowItem(
//            title = stringResource(Res.string.user_outreach_setting_title),
//            description = stringResource(Res.string.user_outreach_setting_description),
//            icon = {
//                SettingIconBadge(
//                    imageVector = IconsManager.Bolt,
//                    backgroundColor = Color(0xFFA855F7) // Фиолетовый цвет
//                )
//            },
//            actionContent = {
//                Switch(
//                    checked = isOutreachEnabled,
//                    onCheckedChange = {
//                        onChangeOutreachMode()
//                    },
//                    colors = SwitchDefaults.colors(
//                        checkedThumbColor = colors.background,
//                        checkedTrackColor = colors.neonAccent,
//                        uncheckedThumbColor = colors.textSecondary,
//                        uncheckedTrackColor = colors.cardBackground
//                    )
//                )
//            }
//        )
//
//        if (isOutreachEnabled) {
//            val isRussian = language?.startsWith("ru", ignoreCase = true) == true
//            val limitText = if (isRussian) {
//                when (dailyOutreachLimit) {
//                    1 -> "Максимум 1 персонаж в день"
//                    2, 3, 4 -> "Максимум $dailyOutreachLimit персонажа в день"
//                    else -> "Максимум $dailyOutreachLimit персонажей в день"
//                }
//            } else {
//                when (dailyOutreachLimit) {
//                    1 -> "Max 1 character per day"
//                    else -> "Max $dailyOutreachLimit characters per day"
//                }
//            }
//
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 60.dp, end = 16.dp, bottom = 12.dp)
//            ) {
//                Text(
//                    text = limitText,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = colors.neonAccent
//                )
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Slider(
//                    value = dailyOutreachLimit.coerceIn(1, 10).toFloat(),
//                    onValueChange = { newValue ->
//                        onChangeOutreachLimit(newValue.toInt())
//                    },
//                    valueRange = 1f..10f,
//                    steps = 8,
//                    colors = SliderDefaults.colors(
//                        thumbColor = colors.neonAccent,
//                        activeTrackColor = colors.neonAccent,
//                        inactiveTrackColor = colors.cardBackground,
//                        activeTickColor = colors.background,
//                        inactiveTickColor = colors.textSecondary
//                    ),
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//        }
    }

    if (isShowLanguageSheet) {
        AppSettingsLanguagePickerSheet(
            selectedLanguage = language,
            languageList = languageList,
            onSelect = onChangeLanguage,
            onDismiss = { isShowLanguageSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsLanguagePickerSheet(
    selectedLanguage: String?,
    languageList: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AvalonTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.overlaySurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = colors.dragHandle
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(Res.string.language),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textMain,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            languageList.forEachIndexed { index, lang ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(lang)
                            onDismiss()
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lang,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (lang == selectedLanguage) colors.neonAccent else colors.textMain,
                        modifier = Modifier.weight(1f)
                    )
                    if (lang == selectedLanguage) {
                        Icon(
                            imageVector = IconsManager.Check,
                            contentDescription = null,
                            tint = colors.neonAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (index < languageList.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = colors.strokeSubtle.copy(alpha = 0.3f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
