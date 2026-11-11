package com.ensodai.avalon.presentation.characterEventsScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.CharacterEventDto
import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.avalon.domain.useCases.characterEventsScreenUseCase.GetCharacterEventsUseCase
import com.ensodai.avalon.presentation.characterEventsScreen.CharacterEventsScreenViewModel
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.launch

fun CharacterEventsScreenViewModel.createViewState() {
    val charId = characterId
    updateViewState { it.copy(isLoading = true, errorMessage = null) }
    viewModelScope.launch(IODispatcher) {
        try {
            val fetchedEvents = getCharacterEvents.execute(charId)
            updateViewState {
                it.copy(
                    isLoading = false,
                    events = fetchedEvents
                )
            }
        } catch (e: Exception) {
            val errorMsg = resourceWrapper.getString(Res.string.character_events_error_load, e.message ?: "")
            updateViewState {
                it.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }
}

fun CharacterEventsScreenViewModel.onAddEvent() {
    updateViewState { state ->
        val allPossibleLevels = listOf(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65)
        val selectedLevels = state.events.map { it.requiredLevel }
        val nextFreeLevel = allPossibleLevels.firstOrNull { it !in selectedLevels }
        
        if (nextFreeLevel != null) {
            val newEvent = CharacterEventDto(
                id = null,
                requiredLevel = nextFreeLevel,
                eventName = "",
                eventTriggerScenario = "",
                eventPermanentTrait = ""
            )
            state.copy(events = state.events + newEvent)
        } else {
            state
        }
    }
}

fun CharacterEventsScreenViewModel.onDeleteEvent(index: Int) {
    updateViewState { state ->
        if (index in state.events.indices) {
            val updatedList = state.events.toMutableList().apply { removeAt(index) }
            state.copy(events = updatedList)
        } else {
            state
        }
    }
}

fun CharacterEventsScreenViewModel.onEventNameChanged(index: Int, text: String) {
    updateViewState { state ->
        if (index in state.events.indices) {
            val updatedList = state.events.toMutableList().apply {
                this[index] = this[index].copy(eventName = text)
            }
            state.copy(events = updatedList)
        } else {
            state
        }
    }
}

fun CharacterEventsScreenViewModel.onTriggerScenarioChanged(index: Int, text: String) {
    updateViewState { state ->
        if (index in state.events.indices) {
            val updatedList = state.events.toMutableList().apply {
                this[index] = this[index].copy(eventTriggerScenario = text)
            }
            state.copy(events = updatedList)
        } else {
            state
        }
    }
}

fun CharacterEventsScreenViewModel.onPermanentTraitChanged(index: Int, text: String) {
    updateViewState { state ->
        if (index in state.events.indices) {
            val updatedList = state.events.toMutableList().apply {
                this[index] = this[index].copy(eventPermanentTrait = text)
            }
            state.copy(events = updatedList)
        } else {
            state
        }
    }
}

fun CharacterEventsScreenViewModel.onRequiredLevelChanged(index: Int, level: Int) {
    updateViewState { state ->
        if (index in state.events.indices) {
            val updatedList = state.events.toMutableList().apply {
                this[index] = this[index].copy(requiredLevel = level)
            }
            state.copy(events = updatedList)
        } else {
            state
        }
    }
}

fun CharacterEventsScreenViewModel.onSave() {
    val charId = characterId
    val currentEvents = viewState.value.events

    viewModelScope.launch(IODispatcher) {
        // Валидация уникальности уровней
        val levels = currentEvents.map { it.requiredLevel }
        if (levels.size != levels.toSet().size) {
            val errorMsg = resourceWrapper.getString(Res.string.character_events_error_duplicates)
            updateViewState { it.copy(errorMessage = errorMsg) }
            return@launch
        }

        // Валидация полей
        val hasEmptyFields = currentEvents.any { 
            it.eventName.isBlank() || it.eventTriggerScenario.isBlank() || it.eventPermanentTrait.isBlank() 
        }
        if (hasEmptyFields) {
            val errorMsg = resourceWrapper.getString(Res.string.character_events_error_empty_fields)
            updateViewState { it.copy(errorMessage = errorMsg) }
            return@launch
        }

        updateViewState { it.copy(isSaving = true, errorMessage = null, saveSuccess = false) }
        try {
            updateCharacterEvents.execute(charId, currentEvents)
            updateViewState {
                it.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            }
        } catch (e: Exception) {
            val errorMsg = resourceWrapper.getString(Res.string.character_events_error_save, e.message ?: "")
            updateViewState {
                it.copy(
                    isSaving = false,
                    errorMessage = errorMsg
                )
            }
        }
    }
}
