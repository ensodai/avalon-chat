@file:OptIn(ExperimentalUuidApi::class)

package com.ensodai.avalon.presentation.userCharactersScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.avalon.domain.sessionWorker.DeletionType
import com.ensodai.avalon.domain.sessionWorker.DeletionUndoManager
import com.ensodai.avalon.domain.useCases.userCharacterScreenUseCase.ClaimCharacterEnergyUseCase
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.deleted_character
import com.ensodai.avalon.presentation.userCharactersScreen.UserCharacterScreenViewModel
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun UserCharacterScreenViewModel.deleteCharacter(
    id: Uuid,
    characterName: String,
    deletionUndoManager: DeletionUndoManager,
    resourceWrapper: ResourceWrapper
) {
    viewModelScope.launch(IODispatcher) {
        deletionUndoManager.startDeletion(
            ids = listOf(id),
            type = DeletionType.CHARACTER,
            displayText = resourceWrapper.getString(Res.string.deleted_character, characterName),
        )
    }
}

fun UserCharacterScreenViewModel.claimEnergy(
    characterId: Uuid,
    claimCharacterEnergy: ClaimCharacterEnergyUseCase
) {
    viewModelScope.launch {
        updateViewState { it.copy(claimingCharacterId = characterId) }
        val result = claimCharacterEnergy(characterId)
        result.fold(
            onSuccess = { response ->
                updateViewState {
                    it.copy(
                        claimingCharacterId = null,
                        claimSuccessEvent = Pair(characterId, response.claimedAmount)
                    )
                }
            },
            onFailure = { error ->
                updateViewState { it.copy(claimingCharacterId = null) }
            }
        )
    }
}

fun UserCharacterScreenViewModel.clearClaimSuccess() {
    updateViewState { it.copy(claimSuccessEvent = null) }
}
