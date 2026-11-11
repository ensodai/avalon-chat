package com.ensodai.avalon.presentation.userAiSettingsScreen.action

import com.ensodai.avalon.presentation.core.BaseActions
import com.ensodai.avalon.presentation.userAiSettingsScreen.viewState.ModelType

data class UserAiSettingsScreenActions(
    val onOpenHelpGuide: () -> Unit = {},
    val onOpenProviderPicker: () -> Unit = {},
    val onOpenEditApiKey: () -> Unit = {},
    val onOpenEditModel: (type: ModelType, title: String, description: String, exampleHint: String, currentValue: String) -> Unit = { _, _, _, _, _ -> },
    val onCloseBottomSheet: () -> Unit = {},
    val onProviderSelected: (String) -> Unit = {},
    val onUpdateApiKey: (String) -> Unit = {},
    val onUpdateModel: (type: ModelType, modelName: String) -> Unit = { _, _ -> },
    val onUpdateCatalogSearch: (String) -> Unit = {}
) : BaseActions()
