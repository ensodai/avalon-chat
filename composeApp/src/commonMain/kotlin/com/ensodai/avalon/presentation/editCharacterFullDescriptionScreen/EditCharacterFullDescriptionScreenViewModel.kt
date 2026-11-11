package com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen

import com.ensodai.avalon.domain.useCases.editCharacterFullDescriptionScreenUseCase.GetCharacterFullDescriptionUseCase
import com.ensodai.avalon.domain.useCases.editCharacterFullDescriptionScreenUseCase.UpdateCharacterFullDescriptionUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.action.EditCharacterFullDescriptionScreenActions
import com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.action.createViewState
import com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.action.updateCharacterFullDescription
import com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.action.updateFullDescriptionVisibility
import com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.viewState.EditCharacterFullDescriptionScreenViewState
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class EditCharacterFullDescriptionScreenViewModel(
    @InjectedParam val characterId: Uuid,
    private val getCharacterFullDescription: GetCharacterFullDescriptionUseCase,
    private val updateCharacterFullDescription: UpdateCharacterFullDescriptionUseCase
) : BaseViewModel<EditCharacterFullDescriptionScreenViewState, EditCharacterFullDescriptionScreenActions>(
    initialState = EditCharacterFullDescriptionScreenViewState()
) {

    override val actions = EditCharacterFullDescriptionScreenActions(
        onUpdateCharacterFullDescription = {
            updateCharacterFullDescription(
                characterId = characterId,
                fullDescription = it,
                updateCharacterFullDescription = updateCharacterFullDescription
            )
        },
        onUpdateFullDescriptionVisibility = {
            updateFullDescriptionVisibility(it)
        }
    )

    init {
        createViewState(
            getCharacterFullDescription = getCharacterFullDescription,
            characterId = characterId
        )
    }
}
