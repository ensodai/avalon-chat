package com.ensodai.avalon.presentation.summaryExplanationScreen.action

import com.ensodai.avalon.presentation.core.BaseActions

data class SummaryExplanationScreenActions(
    val onNextStep: () -> Unit = {},
    val onPreviousStep: () -> Unit = {},
    val onToggleAutoPlay: () -> Unit = {},
    val onStepSelected: (Int) -> Unit = {}
) : BaseActions()
