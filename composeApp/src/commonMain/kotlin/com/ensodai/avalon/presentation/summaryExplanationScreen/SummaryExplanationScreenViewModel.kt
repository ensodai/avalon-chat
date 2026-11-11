package com.ensodai.avalon.presentation.summaryExplanationScreen

import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.summaryExplanationScreen.action.SummaryExplanationScreenActions
import com.ensodai.avalon.presentation.summaryExplanationScreen.action.nextStep
import com.ensodai.avalon.presentation.summaryExplanationScreen.action.previousStep
import com.ensodai.avalon.presentation.summaryExplanationScreen.action.setStep
import com.ensodai.avalon.presentation.summaryExplanationScreen.action.toggleAutoPlay
import com.ensodai.avalon.presentation.summaryExplanationScreen.viewState.SummaryExplanationScreenViewState
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SummaryExplanationScreenViewModel : BaseViewModel<SummaryExplanationScreenViewState, SummaryExplanationScreenActions>(
    initialState = SummaryExplanationScreenViewState()
) {
    override val actions = SummaryExplanationScreenActions(
        onNextStep = { nextStep() },
        onPreviousStep = { previousStep() },
        onToggleAutoPlay = { toggleAutoPlay() },
        onStepSelected = { setStep(it) }
    )
}
