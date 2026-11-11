package com.ensodai.avalon.presentation.editCharacterScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.GetCharacterEditDataUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterDescriptionUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterInteractionFormatUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterMesExampleUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterNameUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterOutreachUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterScenarioUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterVisibilityUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterVoiceUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateFirstMessageUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.launch
import com.ensodai.avalon.presentation.editCharacterScreen.action.BottomSheetActions
import com.ensodai.avalon.presentation.editCharacterScreen.action.BottomSheetEditActions
import com.ensodai.avalon.presentation.editCharacterScreen.action.EditCharacterActions
import com.ensodai.avalon.presentation.editCharacterScreen.action.closeBottomSheet
import com.ensodai.avalon.presentation.editCharacterScreen.action.closeVoicePicker
import com.ensodai.avalon.presentation.editCharacterScreen.action.createViewState
import com.ensodai.avalon.presentation.editCharacterScreen.action.openEditDescriptionBottomSheet
import com.ensodai.avalon.presentation.editCharacterScreen.action.openEditFirstMessageBottomSheet
import com.ensodai.avalon.presentation.editCharacterScreen.action.openEditInteractionFormatBottomSheet
import com.ensodai.avalon.presentation.editCharacterScreen.action.openEditMesExampleBottomSheet
import com.ensodai.avalon.presentation.editCharacterScreen.action.openEditNameBottomSheet
import com.ensodai.avalon.presentation.editCharacterScreen.action.openEditOutreachBottomSheet
import com.ensodai.avalon.presentation.editCharacterScreen.action.openEditScenarioBottomSheet
import com.ensodai.avalon.presentation.editCharacterScreen.action.openEditVisibilityBottomSheet
import com.ensodai.avalon.presentation.editCharacterScreen.action.openVoicePicker
import com.ensodai.avalon.presentation.editCharacterScreen.action.updateCharacterDescription
import com.ensodai.avalon.presentation.editCharacterScreen.action.updateCharacterFirstMessage
import com.ensodai.avalon.presentation.editCharacterScreen.action.updateCharacterInteractionFormat
import com.ensodai.avalon.presentation.editCharacterScreen.action.updateCharacterMesExample
import com.ensodai.avalon.presentation.editCharacterScreen.action.updateCharacterName
import com.ensodai.avalon.presentation.editCharacterScreen.action.updateCharacterOutreach
import com.ensodai.avalon.presentation.editCharacterScreen.action.updateCharacterScenario
import com.ensodai.avalon.presentation.editCharacterScreen.action.updateCharacterVisibility
import com.ensodai.avalon.presentation.editCharacterScreen.action.updateCharacterVoice
import com.ensodai.avalon.presentation.editCharacterScreen.viewState.EditCharacterViewState
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class EditCharacterScreenViewModel(
    private val getCharacter: GetCharacterEditDataUseCase,
    @InjectedParam val characterId: Uuid,
    private val updateCharacterName: UpdateCharacterNameUseCase,
    private val updateFirstMessage: UpdateFirstMessageUseCase,
    private val updateCharacterDescription: UpdateCharacterDescriptionUseCase,
    private val updateCharacterScenario: UpdateCharacterScenarioUseCase,
    private val updateCharacterMesExample: UpdateCharacterMesExampleUseCase,
    private val updateCharacterVisibility: UpdateCharacterVisibilityUseCase,
    private val updateCharacterOutreach: UpdateCharacterOutreachUseCase,
    private val updateCharacterVoice: UpdateCharacterVoiceUseCase,
    private val updateCharacterInteractionFormat: UpdateCharacterInteractionFormatUseCase,
    private val characterRpcService: CharacterRpcService
) : BaseViewModel<EditCharacterViewState, EditCharacterActions>(
    initialState = EditCharacterViewState()
) {

    override val actions = EditCharacterActions(
        bottomSheetActions = BottomSheetActions(
            onCloseBottomSheet = { closeBottomSheet() },
            onOpenEditNameBottomSheet = { openEditNameBottomSheet() },
            onOpenEditDescriptionBottomSheet = { openEditDescriptionBottomSheet() },
            onOpenEditScenarioBottomSheet = { openEditScenarioBottomSheet() },
            onOpenEditMesExampleBottomSheet = { openEditMesExampleBottomSheet() },
            onOpenEditFirstMessageBottomSheet = { openEditFirstMessageBottomSheet() },
            onOpenEditVisibilityBottomSheet = { openEditVisibilityBottomSheet() },
            onOpenEditOutreachBottomSheet = { openEditOutreachBottomSheet() },
            onOpenVoicePicker = { openVoicePicker() },
            onCloseVoicePicker = { closeVoicePicker() },
            onOpenEditInteractionFormatBottomSheet = { openEditInteractionFormatBottomSheet() }
        ),
        bottomSheetEditActions = BottomSheetEditActions(
            onUpdateCharacterName = {
                updateCharacterName(
                    name = it,
                    characterId = characterId,
                    updateCharacterName = updateCharacterName
                )
            },
            onUpdateCharacterDescription = {
                updateCharacterDescription(
                    description = it,
                    characterId = characterId,
                    updateCharacterDescription = updateCharacterDescription
                )
            },
            onUpdateCharacterScenario = {
                updateCharacterScenario(
                    scenario = it,
                    characterId = characterId,
                    updateCharacterScenario = updateCharacterScenario
                )
            },
            onUpdateCharacterMesExample = {
                updateCharacterMesExample(
                    mesExample = it,
                    characterId = characterId,
                    updateCharacterMesExample = updateCharacterMesExample
                )
            },
            onUpdateCharacterFirstMessage = {
                updateCharacterFirstMessage(
                    message = it,
                    characterId = characterId,
                    updateFirstMessage = updateFirstMessage
                )
            },
            onUpdateCharacterVisibility = {
                updateCharacterVisibility(
                    visibility = it,
                    characterId = characterId,
                    updateCharacterVisibility = updateCharacterVisibility
                )
            },
            onUpdateCharacterOutreach = {
                updateCharacterOutreach(
                    isOutreachEnabled = it,
                    characterId = characterId,
                    updateCharacterOutreach = updateCharacterOutreach
                )
            },
            onUpdateCharacterVoice = {
                updateCharacterVoice(
                    voice = it,
                    characterId = characterId,
                    updateCharacterVoice = updateCharacterVoice
                )
            },
            onUpdateCharacterInteractionFormat = {
                updateCharacterInteractionFormat(
                    interactionFormat = it,
                    characterId = characterId,
                    updateCharacterInteractionFormat = updateCharacterInteractionFormat
                )
            }
        )
    )

    init {
        createViewState(
            characterId = characterId,
            getCharacterById = getCharacter,
            characterRpcService = characterRpcService
        )
    }

    fun refreshPromptDetails() {
        viewModelScope.launch(IODispatcher) {
            runCatching {
                val promptDetails = characterRpcService.getCharacterPromptDetails(characterId)
                if (promptDetails != null) {
                    updateViewState {
                        it.copy(
                            scenario = promptDetails.scenario,
                            mesExample = promptDetails.mesExample
                        )
                    }
                } else {
                    val characterResponse = characterRpcService.getCharacter(characterId)
                    updateViewState {
                        it.copy(
                            scenario = characterResponse.scenario,
                            mesExample = characterResponse.mesExample
                        )
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}
