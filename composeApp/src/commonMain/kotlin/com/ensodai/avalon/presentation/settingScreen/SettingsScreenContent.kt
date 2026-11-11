package com.ensodai.avalon.presentation.settingScreen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.commonComponents.rememberTelegramOpener
import com.ensodai.avalon.theme.AvalonTheme

import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.presentation.settingScreen.component.SettingsGroupCard
import com.ensodai.avalon.presentation.settingScreen.component.SettingsRowItem
import com.ensodai.avalon.presentation.settingScreen.component.SettingIconBadge
import com.ensodai.avalon.presentation.settingScreen.component.SettingsDivider
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.logout
import com.ensodai.avalon.generated.resources.delete_account
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

import com.ensodai.avalon.presentation.settingScreen.component.CreditsSection
import com.ensodai.avalon.presentation.settingScreen.component.CustomizationSection
import com.ensodai.avalon.presentation.settingScreen.component.AiSettingsSection
import com.ensodai.avalon.presentation.settingScreen.component.TelegramLinkSection
import com.ensodai.avalon.presentation.settingScreen.component.alertDialog.AlertDialogDeleteAccount
import com.ensodai.avalon.presentation.settingScreen.component.appSettingSection.AppSettingSection
import com.ensodai.avalon.presentation.settingScreen.component.topBar.TopBarSettingsScreen
import com.ensodai.avalon.presentation.settingScreen.provider.ProvideSettingScreenConstructor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreenContent(provideConstructor: ProvideSettingScreenConstructor) {
    val action = provideConstructor.action
    val viewState = provideConstructor.viewState
    val telegramOpener = rememberTelegramOpener()
    val colors = AvalonTheme.colors

    AlertDialogDeleteAccount(
        isShowDeleteAccountAlertDialog = viewState.isShowDeleteAccountDialog,
        onDismissDeleteAccountAlertDialog = { action.onCloseAlertDialog() },
        onDeleteAccount = { action.onDeleteAccount() }
    )

    Scaffold(
        topBar = {
            TopBarSettingsScreen(
                modifier = Modifier
                    .fillMaxWidth(),
                onBackButtonClick = { provideConstructor.navigation.navigateBack() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .imePadding()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            )
        ) {
            item {
                AppSettingSection(
                    isEnableLocalMode = viewState.localMode,
                    isEnableFlowMode = viewState.flowMode,
                    language = viewState.language,
                    languageList = viewState.languageList,
                    isOutreachEnabled = viewState.isOutreachEnabled,
                    dailyOutreachLimit = viewState.dailyOutreachLimit,
                    onChangeLanguage = { action.onChangeLanguage(it) },
                    onChangeFlowMode = { action.onToggleFlowMode() },
                    onChangeLocalMode = { action.onChangeLocalMode() },
                    onChangeOutreachMode = {
                        // if (!viewState.isOutreachEnabled && !viewState.isPremium) {
                        //     provideConstructor.navigation.navigateToPaymentScreen()
                        // } else {
                        action.onToggleOutreachMode()
                        // }
                    },
                    onChangeOutreachLimit = { action.onChangeOutreachLimit(it) }
                )
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                CustomizationSection(
                    onClick = {
                        provideConstructor.navigation.navigateToCustomizationScreen()
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                AiSettingsSection(
                    onClick = {
                        provideConstructor.navigation.navigateToUserAiSettingsScreen()
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                TelegramLinkSection(
                    onClick = {
                        telegramOpener.openChannel()
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                SettingsGroupCard {
                    SettingsRowItem(
                        title = stringResource(Res.string.logout),
                        icon = {
                            SettingIconBadge(
                                imageVector = IconsManager.Logout,
                                backgroundColor = Color(0xFF6B7280) // Серый цвет
                            )
                        },
                        onClick = { action.onLogout() }
                    )

                    SettingsDivider()

                    SettingsRowItem(
                        title = stringResource(Res.string.delete_account),
                        icon = {
                            SettingIconBadge(
                                imageVector = IconsManager.DeleteOutline,
                                backgroundColor = colors.error.copy(alpha = 0.15f), // Мягкий красный фон
                                iconTint = colors.error
                            )
                        },
                        onClick = { action.onShowDeleteAccountDialog() }
                    )
                }
            }

            item {
                CreditsSection(modifier = Modifier.padding(top = 24.dp))
            }
        }
    }
}
