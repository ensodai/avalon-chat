package com.ensodai.avalon.presentation.relationshipRoadScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.domain.useCases.relationshipRoadScreenUseCase.GetCharacterEventsRoadmapUseCase
import com.ensodai.avalon.domain.useCases.relationshipRoadScreenUseCase.GetLocalCharacterProfileUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.relationshipRoadScreen.action.RelationshipRoadScreenActions
import com.ensodai.avalon.presentation.relationshipRoadScreen.viewState.RelationshipRoadScreenViewState
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class RelationshipRoadScreenViewModel(
    private val getLocalCharacterProfile: GetLocalCharacterProfileUseCase,
    private val getCharacterEventsRoadmap: GetCharacterEventsRoadmapUseCase,
    @InjectedParam val characterId: Uuid,
) : BaseViewModel<RelationshipRoadScreenViewState, RelationshipRoadScreenActions>(
    initialState = RelationshipRoadScreenViewState(characterId = characterId)
) {

    override val actions = RelationshipRoadScreenActions()

    init {
        observeFriendship()
        loadRoadmap()
    }

    private fun observeFriendship() {
        viewModelScope.launch(IODispatcher) {
            getLocalCharacterProfile.execute(characterId)
                .distinctUntilChanged()
                .collectLatest { tuple ->
                    if (tuple != null) {
                        updateViewState { state ->
                            state.copy(
                                relationshipLevel = tuple.character.relationshipLevel,
                                currentExperience = tuple.character.experiencePoints
                            )
                        }
                    }
                }
        }
    }

    private fun loadRoadmap() {
        if (viewState.value.roadmapItems.isEmpty()) {
            updateViewState { it.copy(isLoading = true, errorMessage = null) }
        }
        viewModelScope.launch(IODispatcher) {
            getCharacterEventsRoadmap.execute(characterId)
                .collectLatest { roadmap ->
                    updateViewState { state ->
                        state.copy(
                            isLoading = false,
                            roadmapItems = roadmap
                        )
                    }
                }
        }
    }
}
