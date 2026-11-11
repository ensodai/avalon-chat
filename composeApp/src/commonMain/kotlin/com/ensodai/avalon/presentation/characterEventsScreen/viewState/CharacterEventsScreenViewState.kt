package com.ensodai.avalon.presentation.characterEventsScreen.viewState

import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.shared.avalon.character.models.CharacterEventDto
import kotlin.uuid.Uuid

data class CharacterEventsScreenViewState(
    val characterId: Uuid? = null,
    val isLoading: Boolean = false,
    val events: List<CharacterEventDto> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
) : BaseViewState()