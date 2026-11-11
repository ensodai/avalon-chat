package com.ensodai.avalon.presentation.characterEventsScreen.action

import com.ensodai.avalon.presentation.core.BaseActions

data class CharacterEventsScreenActions(
    val onAddEvent: () -> Unit = {},
    val onDeleteEvent: (Int) -> Unit = {},
    val onTriggerScenarioChanged: (Int, String) -> Unit = { _, _ -> },
    val onEventNameChanged: (Int, String) -> Unit = { _, _ -> },
    val onPermanentTraitChanged: (Int, String) -> Unit = { _, _ -> },
    val onRequiredLevelChanged: (Int, Int) -> Unit = { _, _ -> },
    val onSave: () -> Unit = {},
    val onBack: () -> Unit = {}
) : BaseActions()