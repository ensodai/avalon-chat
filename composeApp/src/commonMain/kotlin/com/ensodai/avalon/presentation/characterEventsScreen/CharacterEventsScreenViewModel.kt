package com.ensodai.avalon.presentation.characterEventsScreen

import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.avalon.domain.useCases.characterEventsScreenUseCase.GetCharacterEventsUseCase
import com.ensodai.avalon.domain.useCases.characterEventsScreenUseCase.UpdateCharacterEventsUseCase
import com.ensodai.avalon.presentation.characterEventsScreen.action.CharacterEventsScreenActions
import com.ensodai.avalon.presentation.characterEventsScreen.action.createViewState
import com.ensodai.avalon.presentation.characterEventsScreen.action.onAddEvent
import com.ensodai.avalon.presentation.characterEventsScreen.action.onDeleteEvent
import com.ensodai.avalon.presentation.characterEventsScreen.action.onTriggerScenarioChanged
import com.ensodai.avalon.presentation.characterEventsScreen.action.onEventNameChanged
import com.ensodai.avalon.presentation.characterEventsScreen.action.onPermanentTraitChanged
import com.ensodai.avalon.presentation.characterEventsScreen.action.onRequiredLevelChanged
import com.ensodai.avalon.presentation.characterEventsScreen.action.onSave
import com.ensodai.avalon.presentation.characterEventsScreen.viewState.CharacterEventsScreenViewState
import com.ensodai.avalon.presentation.core.BaseViewModel
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class CharacterEventsScreenViewModel(
    @InjectedParam val characterId: Uuid,
    val getCharacterEvents: GetCharacterEventsUseCase,
    val updateCharacterEvents: UpdateCharacterEventsUseCase,
    val resourceWrapper: ResourceWrapper,
) : BaseViewModel<CharacterEventsScreenViewState, CharacterEventsScreenActions>(
    initialState = CharacterEventsScreenViewState(characterId = characterId)
) {

    override val actions = CharacterEventsScreenActions(
        onAddEvent = { onAddEvent() },
        onDeleteEvent = { onDeleteEvent(it) },
        onTriggerScenarioChanged = { index, text -> onTriggerScenarioChanged(index, text) },
        onEventNameChanged = { index, text -> onEventNameChanged(index, text) },
        onPermanentTraitChanged = { index, text -> onPermanentTraitChanged(index, text) },
        onRequiredLevelChanged = { index, level -> onRequiredLevelChanged(index, level) },
        onSave = { onSave() }
    )

    init {
        createViewState()
    }
}