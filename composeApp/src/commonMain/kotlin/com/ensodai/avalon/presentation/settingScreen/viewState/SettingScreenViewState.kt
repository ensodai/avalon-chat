package com.ensodai.avalon.presentation.settingScreen.viewState

import com.ensodai.avalon.data.db.LocaleHelper
import com.ensodai.avalon.utils.EMPTY_STRING

data class SettingScreenViewState(
    val isShowDeleteAccountDialog: Boolean = false,
    val isShowLogoutAlertDialog: Boolean = false,
    val language: String? = EMPTY_STRING,
    val languageList: List<String> = LocaleHelper.getLocales(),
    val flowMode: Boolean = false,
    val shouldStartGuide: Boolean = false,
    val localMode: Boolean = false,
    val isOutreachEnabled: Boolean = false,
    val dailyOutreachLimit: Int = 3,
    val isPremium: Boolean = false,
)
