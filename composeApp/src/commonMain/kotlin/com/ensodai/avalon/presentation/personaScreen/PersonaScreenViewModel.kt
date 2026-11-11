package com.ensodai.avalon.presentation.personaScreen

import com.ensodai.avalon.domain.useCases.personaScreenUseCase.CreateNewPersonaUseCase
import com.ensodai.avalon.domain.useCases.personaScreenUseCase.DeletePersonaUseCase
import com.ensodai.avalon.domain.useCases.personaScreenUseCase.GetPersonaByIdUseCase
import com.ensodai.avalon.domain.useCases.personaScreenUseCase.UpdatePersonaUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.personaScreen.action.PersonaScreenAction
import com.ensodai.avalon.presentation.personaScreen.action.changeDescription
import com.ensodai.avalon.presentation.personaScreen.action.changeName
import com.ensodai.avalon.presentation.personaScreen.action.closeAlertDialog
import com.ensodai.avalon.presentation.personaScreen.action.createNewPersona
import com.ensodai.avalon.presentation.personaScreen.action.createViewState
import com.ensodai.avalon.presentation.personaScreen.action.deletePersona
import com.ensodai.avalon.presentation.personaScreen.action.saveChanges
import com.ensodai.avalon.presentation.personaScreen.action.showAboutAlertDialog
import com.ensodai.avalon.presentation.personaScreen.action.showDeletePersonaAlertDialog
import com.ensodai.avalon.presentation.personaScreen.viewState.PersonaScreenViewState
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class PersonaScreenViewModel(
    private val getPersonaById: GetPersonaByIdUseCase,
    private val createNewPersona: CreateNewPersonaUseCase,
    private val updatePersona: UpdatePersonaUseCase,
    private val deletePersona: DeletePersonaUseCase,
    @InjectedParam private val id: Uuid?,
    @InjectedParam private val isUpdatePersona: Boolean
) : BaseViewModel<PersonaScreenViewState, PersonaScreenAction>(
    initialState = PersonaScreenViewState()
) {

    override val actions = PersonaScreenAction(
        createViewState = { _, _ -> },
        onChangeName = { changeName(name = it) },
        onChangeDescription = { changeDescription(description = it) },
        onSaveChanges = { saveChanges(updatePersona = updatePersona) },
        onCreateNewPersona = { createNewPersona(createNewPersona = createNewPersona) },
        onDeletePersona = { deletePersona(deletePersona = deletePersona) },
        onCloseAlertDialog = { closeAlertDialog() },
        onShowDeletePersonaAlertDialog = { showDeletePersonaAlertDialog() },
        onShowAboutPersonAlertDialog = { showAboutAlertDialog() }
    )

    init {
        createViewState(
            id = id,
            isUpdatePersona = isUpdatePersona,
            getPersonaById = getPersonaById
        )
    }
}
