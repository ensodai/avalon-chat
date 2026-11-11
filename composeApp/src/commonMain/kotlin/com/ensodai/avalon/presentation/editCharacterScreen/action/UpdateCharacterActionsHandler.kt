package com.ensodai.avalon.presentation.editCharacterScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterDescriptionUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterNameUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterVisibilityUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateFirstMessageUseCase
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editCharacterScreen.EditCharacterScreenViewModel
import com.ensodai.avalon.presentation.editCharacterScreen.viewState.BottomSheetEditCharacterScreen
import com.ensodai.avalon.presentation.editCharacterScreen.viewState.CurrentUpdatingSection
import com.ensodai.avalon.utils.IODispatcher
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterOutreachUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterVoiceUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterInteractionFormatUseCase
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterMesExampleUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterScenarioUseCase
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun EditCharacterScreenViewModel.updateCharacterName(
    name: String,
    characterId: Uuid?,
    updateCharacterName: UpdateCharacterNameUseCase
) {
    if (characterId == null || name.length < 3) return
    updateViewState {
        it.copy(
            loadingState = LoadingState.Loading,
            currentUpdatingSection = CurrentUpdatingSection.Name
        )
    }
    viewModelScope.launch(IODispatcher) {
        updateCharacterName.update(
            characterId = characterId,
            name = name
        ).fold(
            onSuccess = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Success,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Error,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            }
        )
    }
}

fun EditCharacterScreenViewModel.updateCharacterDescription(
    description: String,
    characterId: Uuid?,
    updateCharacterDescription: UpdateCharacterDescriptionUseCase
) {
    if (characterId == null || description.length < 3) return
    updateViewState {
        it.copy(
            loadingState = LoadingState.Loading,
            currentUpdatingSection = CurrentUpdatingSection.Description
        )
    }
    viewModelScope.launch(IODispatcher) {
        updateCharacterDescription.update(
            characterId = characterId,
            description = description
        ).fold(
            onSuccess = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Success,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Error,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            }
        )
    }
}

fun EditCharacterScreenViewModel.updateCharacterFirstMessage(
    message: String,
    characterId: Uuid?,
    updateFirstMessage: UpdateFirstMessageUseCase
) {
    if (characterId == null) return
    updateViewState {
        it.copy(
            loadingState = LoadingState.Loading,
            currentUpdatingSection = CurrentUpdatingSection.FirstMessage
        )
    }
    viewModelScope.launch(IODispatcher) {
        updateFirstMessage.update(
            characterId = characterId,
            firstMessage = message
        ).fold(
            onSuccess = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Success,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Error,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            }
        )
    }
}

fun EditCharacterScreenViewModel.updateCharacterVisibility(
    visibility: CharacterVisibility,
    characterId: Uuid?,
    updateCharacterVisibility: UpdateCharacterVisibilityUseCase
) {
    if (characterId == null) return
    updateViewState {
        it.copy(
            loadingState = LoadingState.Loading,
            currentUpdatingSection = CurrentUpdatingSection.Visibility
        )
    }
    viewModelScope.launch(IODispatcher) {
        updateCharacterVisibility.update(
            characterId = characterId,
            visibility = visibility
        ).fold(
            onSuccess = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Success,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Error,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            }
        )
    }
}

fun EditCharacterScreenViewModel.updateCharacterOutreach(
    isOutreachEnabled: Boolean,
    characterId: Uuid?,
    updateCharacterOutreach: UpdateCharacterOutreachUseCase
) {
    if (characterId == null) return
    updateViewState {
        it.copy(
            loadingState = LoadingState.Loading,
            currentUpdatingSection = CurrentUpdatingSection.Outreach
        )
    }
    viewModelScope.launch(IODispatcher) {
        updateCharacterOutreach.update(
            characterId = characterId,
            isOutreachEnabled = isOutreachEnabled
        ).fold(
            onSuccess = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Success,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Error,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            }
        )
    }
}

fun EditCharacterScreenViewModel.updateCharacterVoice(
    voice: VoiceModel,
    characterId: Uuid?,
    updateCharacterVoice: UpdateCharacterVoiceUseCase
) {
    if (characterId == null) return
    updateViewState {
        it.copy(
            loadingState = LoadingState.Loading,
            currentUpdatingSection = CurrentUpdatingSection.Voice
        )
    }
    viewModelScope.launch(IODispatcher) {
        updateCharacterVoice.update(
            characterId = characterId,
            voice = voice
        ).fold(
            onSuccess = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Success,
                        voice = voice,
                        isShowVoicePicker = false,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Error,
                        isShowVoicePicker = false,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            }
        )
    }
}

fun EditCharacterScreenViewModel.updateCharacterInteractionFormat(
    interactionFormat: InteractionFormat,
    characterId: Uuid?,
    updateCharacterInteractionFormat: UpdateCharacterInteractionFormatUseCase
) {
    if (characterId == null) return
    updateViewState {
        it.copy(
            loadingState = LoadingState.Loading,
            currentUpdatingSection = CurrentUpdatingSection.InteractionFormat
        )
    }
    viewModelScope.launch(IODispatcher) {
        updateCharacterInteractionFormat.update(
            characterId = characterId,
            interactionFormat = interactionFormat
        ).fold(
            onSuccess = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Success,
                        interactionFormat = interactionFormat,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Error,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            }
        )
    }
}

fun EditCharacterScreenViewModel.updateCharacterScenario(
    scenario: String?,
    characterId: Uuid?,
    updateCharacterScenario: UpdateCharacterScenarioUseCase
) {
    if (characterId == null) return
    updateViewState {
        it.copy(
            loadingState = LoadingState.Loading,
            currentUpdatingSection = CurrentUpdatingSection.Scenario
        )
    }
    viewModelScope.launch(IODispatcher) {
        updateCharacterScenario.update(
            characterId = characterId,
            scenario = scenario
        ).fold(
            onSuccess = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Success,
                        scenario = scenario,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Error,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            }
        )
    }
}

fun EditCharacterScreenViewModel.updateCharacterMesExample(
    mesExample: String?,
    characterId: Uuid?,
    updateCharacterMesExample: UpdateCharacterMesExampleUseCase
) {
    if (characterId == null) return
    updateViewState {
        it.copy(
            loadingState = LoadingState.Loading,
            currentUpdatingSection = CurrentUpdatingSection.MesExample
        )
    }
    viewModelScope.launch(IODispatcher) {
        updateCharacterMesExample.update(
            characterId = characterId,
            mesExample = mesExample
        ).fold(
            onSuccess = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Success,
                        mesExample = mesExample,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Error,
                        bottomSheet = BottomSheetEditCharacterScreen.None,
                        currentUpdatingSection = CurrentUpdatingSection.None
                    )
                }
            }
        )
    }
}
