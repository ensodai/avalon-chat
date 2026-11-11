package com.ensodai.avalon.presentation.characterOutreachSettingsScreen.action

import com.ensodai.avalon.presentation.core.BaseActions

data class CharacterOutreachSettingsActions(
    val onToggleOutreach: (Boolean) -> Unit = {},
    val onToggleMorningSlot: (Boolean) -> Unit = {},
    val onMorningPromptChange: (String) -> Unit = {},
    val onToggleAfternoonSlot: (Boolean) -> Unit = {},
    val onAfternoonPromptChange: (String) -> Unit = {},
    val onToggleEveningSlot: (Boolean) -> Unit = {},
    val onEveningPromptChange: (String) -> Unit = {},
    val onSave: () -> Unit = {}
) : BaseActions()

