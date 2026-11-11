package com.ensodai.avalon.presentation.summaryExplanationScreen.action

import com.ensodai.avalon.presentation.summaryExplanationScreen.SummaryExplanationScreenViewModel

fun SummaryExplanationScreenViewModel.nextStep() {
    updateViewState { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3)) }
}

fun SummaryExplanationScreenViewModel.previousStep() {
    updateViewState { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
}

fun SummaryExplanationScreenViewModel.toggleAutoPlay() {
    updateViewState { it.copy(isAutoPlaying = !it.isAutoPlaying) }
}

fun SummaryExplanationScreenViewModel.setStep(step: Int) {
    updateViewState { it.copy(currentStep = step.coerceIn(0, 3)) }
}
