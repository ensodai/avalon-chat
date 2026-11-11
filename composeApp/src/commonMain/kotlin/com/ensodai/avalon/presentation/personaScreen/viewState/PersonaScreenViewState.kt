package com.ensodai.avalon.presentation.personaScreen.viewState

import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.utils.EMPTY_STRING
import kotlin.uuid.Uuid

data class PersonaScreenViewState(
    val name: String = EMPTY_STRING,
    val description: String = EMPTY_STRING,
    val isErrorName: Boolean = false,
    val loadingState: PersonaLoadingState = PersonaLoadingState.Idle,
    val id: Uuid? = null,
    val isShowDeleteAlertDialog: Boolean = false,
    val isShowAboutAlertDialog: Boolean = false

) : BaseViewState()

sealed class PersonaLoadingState {

    object Idle : PersonaLoadingState()

    object Loading : PersonaLoadingState()

    object Success : PersonaLoadingState()

    object Error : PersonaLoadingState()

}
