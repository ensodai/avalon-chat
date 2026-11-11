package com.ensodai.avalon.presentation.characterProfileScreen.component.fullDescriptionSection

sealed class ProfileSettings {

    data object FullDescriptionSetting : ProfileSettings()

    data object PrivacySettings : ProfileSettings()

    data object TemperatureSettings : ProfileSettings()

    data object FirstMessageSettings : ProfileSettings()

    data object WelcomeCardSettings : ProfileSettings()

    data object SafetySettings : ProfileSettings()
}
