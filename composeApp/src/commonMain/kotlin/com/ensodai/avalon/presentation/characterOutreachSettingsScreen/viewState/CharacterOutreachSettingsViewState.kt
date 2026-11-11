package com.ensodai.avalon.presentation.characterOutreachSettingsScreen.viewState

import com.ensodai.avalon.presentation.core.BaseViewState
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class CharacterOutreachSettingsViewState(
    val characterId: Uuid,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val isOutreachEnabled: Boolean = true,
    val outreachMorningEnabled: Boolean = true,
    val outreachMorningPrompt: String = "",
    val outreachAfternoonEnabled: Boolean = true,
    val outreachAfternoonPrompt: String = "",
    val outreachEveningEnabled: Boolean = true,
    val outreachEveningPrompt: String = ""
) : BaseViewState()

