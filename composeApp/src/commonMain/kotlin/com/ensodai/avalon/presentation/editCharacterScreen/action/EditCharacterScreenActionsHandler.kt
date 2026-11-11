package com.ensodai.avalon.presentation.editCharacterScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.GetCharacterEditDataUseCase
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.CharacterAvatarWithSeed
import com.ensodai.avalon.presentation.editCharacterScreen.EditCharacterScreenViewModel
import com.ensodai.avalon.presentation.editCharacterScreen.viewState.BottomSheetEditCharacterScreen
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun EditCharacterScreenViewModel.createViewState(
    characterId: Uuid,
    getCharacterById: GetCharacterEditDataUseCase,
    characterRpcService: CharacterRpcService
) {
    viewModelScope.launch(IODispatcher) {
        getCharacterById
            .get(characterId)
            .collect { characterData ->
                if (characterData != null) {
                    val avatar = if (characterData.avatar != null) {
                        listOf(
                            CharacterAvatarWithSeed(
                                imageUrl = characterData.avatar.imageUrl,
                                seed = characterId.toString() + characterData.core.name
                            )
                        )
                    } else emptyList()

                    val images = characterData.images.map { image ->
                        CharacterAvatarWithSeed(
                            id = image.id,
                            imageUrl = image.imageUrl ?: "",
                            seed = characterId.toString() + characterData.core.name + (1..10).random()
                                .toString(),
                            thumbHash = image.thumbHash,
                            isPrivate = image.isPrivate,
                            energyCost = image.energyCost,
                            requiredLevel = image.requiredLevel,
                            isUnlocked = image.isUnlocked
                        )
                    }
                    updateViewState {
                        it.copy(
                            characterId = characterId,
                            characterName = characterData.core.name,
                            shortDescription = characterData.core.shortDescription,
                            fullDescription = characterData.core.fullDescription,
                            visibility = characterData.core.visibility,
                            images = avatar.plus(images),
                            firstMessage = characterData.core.firstMessage,
                            temperature = characterData.core.temperature,
                            isOutreachEnabled = characterData.core.isOutreachEnabled,
                            voice = characterData.core.voice,
                            interactionFormat = characterData.core.interactionFormat
                        )
                    }
                }
            }
    }
    refreshPromptDetails()
}

fun EditCharacterScreenViewModel.closeBottomSheet() {
    updateViewState {
        it.copy(bottomSheet = BottomSheetEditCharacterScreen.None)
    }
}

fun EditCharacterScreenViewModel.openEditNameBottomSheet() {
    updateViewState {
        it.copy(bottomSheet = BottomSheetEditCharacterScreen.EditNameBottomSheet)
    }
}

fun EditCharacterScreenViewModel.openEditDescriptionBottomSheet() {
    updateViewState {
        it.copy(bottomSheet = BottomSheetEditCharacterScreen.EditDescriptionBottomSheet)
    }
}

fun EditCharacterScreenViewModel.openEditFirstMessageBottomSheet() {
    updateViewState {
        it.copy(bottomSheet = BottomSheetEditCharacterScreen.EditFirstMessageBottomSheet)
    }
}

fun EditCharacterScreenViewModel.openEditVisibilityBottomSheet() {
    updateViewState {
        it.copy(bottomSheet = BottomSheetEditCharacterScreen.EditVisibilityBottomSheet)
    }
}

fun EditCharacterScreenViewModel.openEditOutreachBottomSheet() {
    updateViewState {
        it.copy(bottomSheet = BottomSheetEditCharacterScreen.EditOutreachBottomSheet)
    }
}

fun EditCharacterScreenViewModel.openVoicePicker() {
    updateViewState {
        it.copy(isShowVoicePicker = true)
    }
}

fun EditCharacterScreenViewModel.closeVoicePicker() {
    updateViewState {
        it.copy(isShowVoicePicker = false)
    }
}

fun EditCharacterScreenViewModel.openEditInteractionFormatBottomSheet() {
    updateViewState {
        it.copy(bottomSheet = BottomSheetEditCharacterScreen.EditInteractionFormatBottomSheet)
    }
}

fun EditCharacterScreenViewModel.openEditScenarioBottomSheet() {
    updateViewState {
        it.copy(bottomSheet = BottomSheetEditCharacterScreen.EditScenarioBottomSheet)
    }
}

fun EditCharacterScreenViewModel.openEditMesExampleBottomSheet() {
    updateViewState {
        it.copy(bottomSheet = BottomSheetEditCharacterScreen.EditMesExampleBottomSheet)
    }
}