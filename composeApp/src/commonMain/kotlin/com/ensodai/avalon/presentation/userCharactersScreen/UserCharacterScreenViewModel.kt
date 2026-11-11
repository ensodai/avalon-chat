@file:OptIn(ExperimentalUuidApi::class)

package com.ensodai.avalon.presentation.userCharactersScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.avalon.domain.sessionWorker.DeletionUndoManager
import com.ensodai.avalon.domain.useCases.userCharacterScreenUseCase.GetUserCharactersPagingDataUseCase
import com.ensodai.avalon.domain.useCases.userCharacterScreenUseCase.SyncUserCharactersEnergyUseCase
import com.ensodai.avalon.domain.useCases.userCharacterScreenUseCase.ClaimCharacterEnergyUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.userCharactersScreen.action.UserCharacterScreenActions
import com.ensodai.avalon.presentation.userCharactersScreen.action.deleteCharacter
import com.ensodai.avalon.presentation.userCharactersScreen.action.claimEnergy
import com.ensodai.avalon.presentation.userCharactersScreen.action.clearClaimSuccess
import com.ensodai.avalon.presentation.userCharactersScreen.viewState.UserCharacterScreenViewState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi

@KoinViewModel
class UserCharacterScreenViewModel(
    private val getUserCharactersPagingData: GetUserCharactersPagingDataUseCase,
    private val syncUserCharactersEnergy: SyncUserCharactersEnergyUseCase,
    private val claimCharacterEnergy: ClaimCharacterEnergyUseCase,
    private val deletionUndoManager: DeletionUndoManager,
    private val resourceWrapper: ResourceWrapper
) : BaseViewModel<UserCharacterScreenViewState, UserCharacterScreenActions>(
    initialState = UserCharacterScreenViewState()
) {

    init {
        // При заходе на экран сразу принудительно синхронизируем данные (включая энергию)
        viewModelScope.launch {
            syncUserCharactersEnergy()
        }
    }

    override val actions = UserCharacterScreenActions(
        onDeleteCharacter = {
            deleteCharacter(
                id = it.id,
                characterName = it.name,
                resourceWrapper = resourceWrapper,
                deletionUndoManager = deletionUndoManager
            )
        },
        onClaimEnergy = { characterId ->
            claimEnergy(characterId, claimCharacterEnergy)
        },
        onClearClaimSuccess = {
            clearClaimSuccess()
        }
    )

    val characters = getUserCharactersPagingData().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}