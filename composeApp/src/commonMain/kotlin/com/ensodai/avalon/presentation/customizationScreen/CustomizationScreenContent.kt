package com.ensodai.avalon.presentation.customizationScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.customizationScreen.action.CustomizationScreenActions
import com.ensodai.avalon.presentation.customizationScreen.component.ThemePresetCard
import com.ensodai.avalon.presentation.customizationScreen.viewState.CustomizationScreenViewState
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.customization_themes
import com.ensodai.avalon.generated.resources.customization_title
import com.ensodai.avalon.presentation.customizationScreen.component.ChatBackgroundSection
import com.ensodai.avalon.presentation.customizationScreen.component.AiWarningSection
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.settingScreen.component.SettingIconBadge
import com.ensodai.avalon.presentation.settingScreen.component.SettingsGroupCard
import com.ensodai.avalon.presentation.settingScreen.component.SettingsRowItem
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import androidx.compose.ui.text.style.TextOverflow
import com.ensodai.avalon.generated.resources.chat_presets_title
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.customizationScreen.component.FlowModeSection
import com.ensodai.avalon.presentation.customizationScreen.component.getPresetTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationScreenContent(
    viewState: CustomizationScreenViewState,
    actions: CustomizationScreenActions,
    navigation: Navigation
) {
    val colors = AvalonTheme.colors

    Scaffold(
        topBar = {
            GemIslandTopBar(
                title = {
                    Text(
                        text = stringResource(Res.string.customization_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    NavigationBackButton(onBackButtonClick = { navigation.navigateBack() })
                }
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            )
        ) {
            item {
                // --- Секция: Theme Presets (горизонтальный скролл) ---
                Text(
                    text = stringResource(Res.string.customization_themes),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textMain,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewState.themePresets) { preset ->
                        ThemePresetCard(
                            preset = preset,
                            isSelected = preset == viewState.selectedThemePreset,
                            onClick = { actions.onThemeSelected(preset) }
                        )
                    }
                }
            }


            item {
                Spacer(modifier = Modifier.height(16.dp))

                // --- Секция: Chat Background ---
                ChatBackgroundSection(
                    backgroundImagePath = viewState.chatBackgroundImagePath,
                    backgroundBlur = viewState.chatBackgroundBlur,
                    backgroundImages = viewState.chatBackgroundImages,
                    onBackgroundImageSelected = actions.onBackgroundImageSelected,
                    onSelectBackgroundImage = actions.onSelectBackgroundImage,
                    onDeleteBackgroundImage = actions.onDeleteBackgroundImage,
                    onBackgroundBlurChanged = actions.onBackgroundBlurChanged,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsGroupCard(verticalPadding = 0.dp) {
                    SettingsRowItem(
                        title = stringResource(Res.string.chat_presets_title),
                        description = getPresetTitle(viewState.chatRoleplayPreset),
                        icon = {
                            SettingIconBadge(
                                imageVector = IconsManager.DashboardCustomize,
                                backgroundColor = colors.neonAccent
                            )
                        },
                        onClick = actions.onChatRoleplayPresetsClick
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                AiWarningSection(
                    showAiWarning = viewState.showAiWarning,
                    onShowAiWarningChanged = actions.onShowAiWarningChanged,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                FlowModeSection(
                    isFlowModeEnabled = viewState.isFlowModeEnabled,
                    onFlowModeChanged = actions.onFlowModeChanged,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
