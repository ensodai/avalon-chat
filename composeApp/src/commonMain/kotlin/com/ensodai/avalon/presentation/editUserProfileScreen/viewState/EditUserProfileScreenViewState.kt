package com.ensodai.avalon.presentation.editUserProfileScreen.viewState

import com.ensodai.avalon.domain.useCases.editUserProfileScreenUseCase.NicknameValidator
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.utils.EMPTY_STRING

data class EditUserProfileScreenViewState(
    val nickName: String = EMPTY_STRING,
    val validationResult: NicknameValidator.ValidationResult = NicknameValidator.ValidationResult.Available,
    val loadingState: LoadingStateChangeNickName = LoadingStateChangeNickName.Idle,
    val validationMessage: String = EMPTY_STRING,


    ) : BaseViewState()

sealed class LoadingStateChangeNickName {
    object Idle : LoadingStateChangeNickName()
    object Loading : LoadingStateChangeNickName()
    object Success : LoadingStateChangeNickName()
    object Error : LoadingStateChangeNickName()
}

val EditUserProfileScreenViewState.isErrorNickName: Boolean
    get() = this.validationResult !in listOf(
        NicknameValidator.ValidationResult.Available,
        NicknameValidator.ValidationResult.CheckingAvailability
    )
