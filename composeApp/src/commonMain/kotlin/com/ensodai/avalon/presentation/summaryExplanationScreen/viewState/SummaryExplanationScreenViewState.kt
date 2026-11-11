package com.ensodai.avalon.presentation.summaryExplanationScreen.viewState

import com.ensodai.avalon.presentation.core.BaseViewState

data class SummaryExplanationScreenViewState(
    val currentStep: Int = 0,
    val isAutoPlaying: Boolean = true
) : BaseViewState()
