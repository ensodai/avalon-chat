package com.ensodai.avalon.presentation.selectAiProviderScreen.action

import com.ensodai.avalon.presentation.core.BaseActions

data class SelectAiProviderScreenActions(
    val onSelectProvider: (providerId: String) -> Unit,
    val onRefresh: () -> Unit
) : BaseActions()
