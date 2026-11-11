package com.ensodai.avalon.presentation.editCharacterTextScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.useCases.editCharacterFullDescriptionScreenUseCase.GetCharacterFullDescriptionUseCase
import com.ensodai.avalon.domain.useCases.editCharacterFullDescriptionScreenUseCase.UpdateCharacterFullDescriptionUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterMesExampleUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterScenarioUseCase
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editCharacterTextScreen.EditCharacterTextScreenViewModel
import com.ensodai.avalon.presentation.editCharacterTextScreen.model.CharacterTextType
import com.ensodai.avalon.utils.EMPTY_STRING
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun EditCharacterTextScreenViewModel.createViewState(
    characterId: Uuid,
    textType: CharacterTextType,
    getCharacterFullDescription: GetCharacterFullDescriptionUseCase,
    characterRpcService: CharacterRpcService
) {
    updateViewState { it.copy(textType = textType) }
    viewModelScope.launch(IODispatcher) {
        when (textType) {
            CharacterTextType.PERSONALITY -> {
                getCharacterFullDescription
                    .get(characterId)
                    .collect { fullDescription ->
                        val fullDescriptionPrivacy = if (fullDescription?.isPublicFullDescription == true) {
                            CharacterVisibility.PUBLIC
                        } else {
                            CharacterVisibility.PRIVATE
                        }
                        updateViewState {
                            it.copy(
                                text = fullDescription?.fullDescription ?: EMPTY_STRING,
                                characterVisibility = fullDescriptionPrivacy,
                                textType = textType
                            )
                        }
                    }
            }

            CharacterTextType.SCENARIO -> {
                runCatching {
                    val details = characterRpcService.getCharacterPromptDetails(characterId)
                    details?.scenario ?: characterRpcService.getCharacter(characterId).scenario
                }.fold(
                    onSuccess = { scenarioText ->
                        updateViewState {
                            it.copy(
                                text = scenarioText ?: EMPTY_STRING,
                                textType = textType
                            )
                        }
                    },
                    onFailure = {
                        it.printStackTrace()
                    }
                )
            }

            CharacterTextType.MES_EXAMPLE -> {
                runCatching {
                    val details = characterRpcService.getCharacterPromptDetails(characterId)
                    details?.mesExample ?: characterRpcService.getCharacter(characterId).mesExample
                }.fold(
                    onSuccess = { mesExampleText ->
                        updateViewState {
                            it.copy(
                                text = mesExampleText ?: EMPTY_STRING,
                                textType = textType
                            )
                        }
                    },
                    onFailure = {
                        it.printStackTrace()
                    }
                )
            }
        }
    }
}

fun EditCharacterTextScreenViewModel.updateVisibility(visibility: CharacterVisibility) {
    updateViewState {
        it.copy(characterVisibility = visibility)
    }
}

fun EditCharacterTextScreenViewModel.saveText(
    characterId: Uuid?,
    text: String,
    textType: CharacterTextType,
    updateCharacterFullDescription: UpdateCharacterFullDescriptionUseCase,
    updateCharacterScenario: UpdateCharacterScenarioUseCase,
    updateCharacterMesExample: UpdateCharacterMesExampleUseCase
) {
    if (characterId == null) return

    updateViewState {
        it.copy(loadingState = LoadingState.Loading)
    }
    viewModelScope.launch(IODispatcher) {
        val result: Result<Unit> = when (textType) {
            CharacterTextType.PERSONALITY -> {
                updateCharacterFullDescription.update(
                    characterId = characterId,
                    fullDescription = text,
                    isPublicFullDescription = viewState.value.characterVisibility == CharacterVisibility.PUBLIC
                )
            }

            CharacterTextType.SCENARIO -> {
                updateCharacterScenario.update(
                    characterId = characterId,
                    scenario = text.ifBlank { null }
                )
            }

            CharacterTextType.MES_EXAMPLE -> {
                updateCharacterMesExample.update(
                    characterId = characterId,
                    mesExample = text.ifBlank { null }
                )
            }
        }

        result.fold(
            onSuccess = {
                updateViewState {
                    it.copy(loadingState = LoadingState.Success)
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(loadingState = LoadingState.Error)
                }
            }
        )
    }
}
