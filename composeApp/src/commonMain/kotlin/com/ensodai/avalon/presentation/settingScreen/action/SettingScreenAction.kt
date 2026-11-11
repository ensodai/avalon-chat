package com.ensodai.avalon.presentation.settingScreen.action

data class SettingScreenAction(
    val onChangeLanguage: (String) -> Unit,
    val onToggleFlowMode: () -> Unit,
    val onCloseAlertDialog: () -> Unit,
    val createViewState: () -> Unit,
    val onChangeLocalMode: () -> Unit,
    val onDeleteAccount: () -> Unit,
    val onShowDeleteAccountDialog: () -> Unit,
    val onLogout: () -> Unit,
    val onShowLogoutAlertDialog: () -> Unit,
    val onToggleOutreachMode: () -> Unit,
    val onChangeOutreachLimit: (Int) -> Unit,
)
