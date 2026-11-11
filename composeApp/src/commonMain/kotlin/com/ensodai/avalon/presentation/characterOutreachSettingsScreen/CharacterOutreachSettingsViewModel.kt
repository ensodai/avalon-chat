package com.ensodai.avalon.presentation.characterOutreachSettingsScreen

import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.avalon.domain.useCases.characterOutreachSettingsUseCase.GetWarmOutreachSettingsUseCase
import com.ensodai.avalon.domain.useCases.characterOutreachSettingsUseCase.UpdateWarmOutreachSettingsUseCase
import com.ensodai.avalon.presentation.characterOutreachSettingsScreen.action.*
import com.ensodai.avalon.presentation.characterOutreachSettingsScreen.viewState.CharacterOutreachSettingsViewState
import com.ensodai.avalon.presentation.core.BaseViewModel
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class CharacterOutreachSettingsViewModel(
    val getWarmOutreachSettingsUseCase: GetWarmOutreachSettingsUseCase,
    val updateWarmOutreachSettingsUseCase: UpdateWarmOutreachSettingsUseCase,
    val resourceWrapper: ResourceWrapper,
    @InjectedParam val characterId: Uuid
) : BaseViewModel<CharacterOutreachSettingsViewState, CharacterOutreachSettingsActions>(
    initialState = CharacterOutreachSettingsViewState(characterId = characterId)
) {

    override val actions = CharacterOutreachSettingsActions(
        onToggleOutreach = { onToggleOutreach(it) },
        onToggleMorningSlot = { onToggleMorningSlot(it) },
        onMorningPromptChange = { onMorningPromptChange(it) },
        onToggleAfternoonSlot = { onToggleAfternoonSlot(it) },
        onAfternoonPromptChange = { onAfternoonPromptChange(it) },
        onToggleEveningSlot = { onToggleEveningSlot(it) },
        onEveningPromptChange = { onEveningPromptChange(it) },
        onSave = { onSave() }
    )

    init {
        loadSettings()
    }
}
