package com.ensodai.avalon.presentation.personaScreen.action

import com.ensodai.avalon.presentation.core.BaseActions
import kotlin.uuid.Uuid

data class PersonaScreenAction(
    val createViewState: (Uuid?, Boolean) -> Unit,
    val onChangeName: (String) -> Unit,
    val onChangeDescription: (String) -> Unit,
    val onCreateNewPersona: () -> Unit,
    val onSaveChanges: () -> Unit,
    val onDeletePersona: () -> Unit,
    val onCloseAlertDialog: () -> Unit,
    val onShowDeletePersonaAlertDialog: () -> Unit,
    val onShowAboutPersonAlertDialog: () -> Unit
) : BaseActions()