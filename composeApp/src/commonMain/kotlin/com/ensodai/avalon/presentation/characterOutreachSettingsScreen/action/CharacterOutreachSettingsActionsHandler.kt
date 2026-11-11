package com.ensodai.avalon.presentation.characterOutreachSettingsScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.UpdateWarmOutreachSettingsRequest
import com.ensodai.shared.avalon.character.models.WarmOutreachSettingsDto
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.presentation.characterOutreachSettingsScreen.CharacterOutreachSettingsViewModel
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun CharacterOutreachSettingsViewModel.loadSettings() {
    updateViewState { it.copy(isLoading = true, errorMessage = null) }
    viewModelScope.launch(IODispatcher) {
        try {
            val settings = getWarmOutreachSettingsUseCase.execute(characterId)
            updateViewState {
                it.copy(
                    isLoading = false,
                    isOutreachEnabled = settings.isOutreachEnabled,
                    outreachMorningEnabled = settings.outreachMorningEnabled,
                    outreachMorningPrompt = settings.outreachMorningPrompt ?: "",
                    outreachAfternoonEnabled = settings.outreachAfternoonEnabled,
                    outreachAfternoonPrompt = settings.outreachAfternoonPrompt ?: "",
                    outreachEveningEnabled = settings.outreachEveningEnabled,
                    outreachEveningPrompt = settings.outreachEveningPrompt ?: ""
                )
            }
        } catch (e: Exception) {
            val errorMsg = resourceWrapper.getString(Res.string.outreach_settings_load_error, e.message ?: "")
            updateViewState {
                it.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }
}

fun CharacterOutreachSettingsViewModel.onToggleOutreach(enabled: Boolean) {
    updateViewState { it.copy(isOutreachEnabled = enabled) }
}

fun CharacterOutreachSettingsViewModel.onToggleMorningSlot(enabled: Boolean) {
    updateViewState { it.copy(outreachMorningEnabled = enabled) }
}

fun CharacterOutreachSettingsViewModel.onMorningPromptChange(text: String) {
    updateViewState { it.copy(outreachMorningPrompt = text) }
}

fun CharacterOutreachSettingsViewModel.onToggleAfternoonSlot(enabled: Boolean) {
    updateViewState { it.copy(outreachAfternoonEnabled = enabled) }
}

fun CharacterOutreachSettingsViewModel.onAfternoonPromptChange(text: String) {
    updateViewState { it.copy(outreachAfternoonPrompt = text) }
}

fun CharacterOutreachSettingsViewModel.onToggleEveningSlot(enabled: Boolean) {
    updateViewState { it.copy(outreachEveningEnabled = enabled) }
}

fun CharacterOutreachSettingsViewModel.onEveningPromptChange(text: String) {
    updateViewState { it.copy(outreachEveningPrompt = text) }
}

@OptIn(ExperimentalUuidApi::class)
fun CharacterOutreachSettingsViewModel.onSave() {
    val state = viewState.value

    viewModelScope.launch(IODispatcher) {
        // Валидация длины пожеланий (максимум 250 символов)
        if (state.outreachMorningPrompt.length > 250 ||
            state.outreachAfternoonPrompt.length > 250 ||
            state.outreachEveningPrompt.length > 250
        ) {
            val errorMsg = resourceWrapper.getString(Res.string.outreach_settings_prompt_max_length_error)
            updateViewState { it.copy(errorMessage = errorMsg) }
            return@launch
        }

        updateViewState { it.copy(isSaving = true, errorMessage = null, saveSuccess = false) }

        val requestDto = WarmOutreachSettingsDto(
            isOutreachEnabled = state.isOutreachEnabled,
            outreachMorningEnabled = state.outreachMorningEnabled,
            outreachMorningPrompt = state.outreachMorningPrompt.takeIf { it.isNotBlank() },
            outreachAfternoonEnabled = state.outreachAfternoonEnabled,
            outreachAfternoonPrompt = state.outreachAfternoonPrompt.takeIf { it.isNotBlank() },
            outreachEveningEnabled = state.outreachEveningEnabled,
            outreachEveningPrompt = state.outreachEveningPrompt.takeIf { it.isNotBlank() }
        )

        try {
            updateWarmOutreachSettingsUseCase.execute(
                UpdateWarmOutreachSettingsRequest(
                    characterId = characterId,
                    settings = requestDto
                )
            )
            updateViewState {
                it.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            }
        } catch (e: Exception) {
            val errorMsg = resourceWrapper.getString(Res.string.outreach_settings_save_error, e.message ?: "")
            updateViewState {
                it.copy(
                    isSaving = false,
                    errorMessage = errorMsg
                )
            }
        }
    }
}
