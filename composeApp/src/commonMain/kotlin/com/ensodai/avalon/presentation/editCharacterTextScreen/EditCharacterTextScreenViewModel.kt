package com.ensodai.avalon.presentation.editCharacterTextScreen

import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.useCases.editCharacterFullDescriptionScreenUseCase.GetCharacterFullDescriptionUseCase
import com.ensodai.avalon.domain.useCases.editCharacterFullDescriptionScreenUseCase.UpdateCharacterFullDescriptionUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterMesExampleUseCase
import com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase.UpdateCharacterScenarioUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.editCharacterTextScreen.action.EditCharacterTextScreenActions
import com.ensodai.avalon.presentation.editCharacterTextScreen.action.createViewState
import com.ensodai.avalon.presentation.editCharacterTextScreen.action.saveText
import com.ensodai.avalon.presentation.editCharacterTextScreen.action.updateVisibility
import com.ensodai.avalon.presentation.editCharacterTextScreen.model.CharacterTextType
import com.ensodai.avalon.presentation.editCharacterTextScreen.viewState.EditCharacterTextScreenViewState
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class EditCharacterTextScreenViewModel(
    @InjectedParam val characterId: Uuid,
    @InjectedParam val textType: CharacterTextType,
    private val getCharacterFullDescription: GetCharacterFullDescriptionUseCase,
    private val updateCharacterFullDescription: UpdateCharacterFullDescriptionUseCase,
    private val updateCharacterScenario: UpdateCharacterScenarioUseCase,
    private val updateCharacterMesExample: UpdateCharacterMesExampleUseCase,
    private val characterRpcService: CharacterRpcService
) : BaseViewModel<EditCharacterTextScreenViewState, EditCharacterTextScreenActions>(
    initialState = EditCharacterTextScreenViewState(textType = textType)
) {

    override val actions = EditCharacterTextScreenActions(
        onSaveText = {
            saveText(
                characterId = characterId,
                text = it,
                textType = textType,
                updateCharacterFullDescription = updateCharacterFullDescription,
                updateCharacterScenario = updateCharacterScenario,
                updateCharacterMesExample = updateCharacterMesExample
            )
        },
        onChangeVisibility = {
            updateVisibility(it)
        }
    )

    init {
        createViewState(
            characterId = characterId,
            textType = textType,
            getCharacterFullDescription = getCharacterFullDescription,
            characterRpcService = characterRpcService
        )
    }
}
