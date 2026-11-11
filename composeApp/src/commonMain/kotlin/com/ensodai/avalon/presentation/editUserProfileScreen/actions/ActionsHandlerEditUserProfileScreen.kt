package com.ensodai.avalon.presentation.editUserProfileScreen.actions

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.avalon.domain.useCases.editUserProfileScreenUseCase.CheckNicknameAvailabilityUseCase
import com.ensodai.avalon.domain.useCases.editUserProfileScreenUseCase.NicknameValidator
import com.ensodai.avalon.domain.useCases.editUserProfileScreenUseCase.UpdateUserNicknameUseCase
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.checking_username
import com.ensodai.avalon.generated.resources.connection_error
import com.ensodai.avalon.generated.resources.error_consecutive_special
import com.ensodai.avalon.generated.resources.error_empty
import com.ensodai.avalon.generated.resources.error_inappropriate
import com.ensodai.avalon.generated.resources.error_invalid_chars
import com.ensodai.avalon.generated.resources.error_invalid_start_end
import com.ensodai.avalon.generated.resources.error_system_name
import com.ensodai.avalon.generated.resources.error_too_long
import com.ensodai.avalon.generated.resources.error_too_short
import com.ensodai.avalon.generated.resources.this_username_is_already_taken
import com.ensodai.avalon.generated.resources.user_name_is_free
import com.ensodai.avalon.presentation.editUserProfileScreen.EditUserProfileScreenViewModel
import com.ensodai.avalon.presentation.editUserProfileScreen.viewState.LoadingStateChangeNickName
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

fun EditUserProfileScreenViewModel.changeNickName(
    nickName: String,
) {
    updateViewState {
        it.copy(
            nickName = nickName
        )
    }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
fun EditUserProfileScreenViewModel.checkNickName(
    nicknameValidator: NicknameValidator,
    resourceWrapper: ResourceWrapper,
    checkNicknameAvailability: CheckNicknameAvailabilityUseCase
) {
    viewModelScope.launch(IODispatcher) {
        viewState
            .map { it.nickName }
            .distinctUntilChanged()
            .transformLatest { nickName ->

                val localResult = nicknameValidator.validate(nickName)

                if (localResult != NicknameValidator.ValidationResult.Available) {
                    emit(localResult)
                    return@transformLatest
                }

                emit(NicknameValidator.ValidationResult.CheckingAvailability)
                delay(1000)

                checkNicknameAvailability.check(nickName).fold(
                    onSuccess = { response ->
                        if (response.available) {
                            emit(NicknameValidator.ValidationResult.Available)
                        } else {
                            emit(NicknameValidator.ValidationResult.Taken)
                        }
                    },
                    onFailure = {
                        emit(NicknameValidator.ValidationResult.NetworkError)
                    }
                )
            }
            .collect { result ->
                val errorMessage = getErrorMessage(
                    result = result,
                    resourceWrapper = resourceWrapper
                )
                updateViewState {
                    it.copy(
                        validationResult = result,
                        validationMessage = errorMessage,
                    )
                }
            }
    }
}

suspend fun EditUserProfileScreenViewModel.getErrorMessage(
    result: NicknameValidator.ValidationResult,
    resourceWrapper: ResourceWrapper
): String {
    return when (result) {
        NicknameValidator.ValidationResult.Available -> resourceWrapper.getString(Res.string.user_name_is_free)
        NicknameValidator.ValidationResult.Empty -> resourceWrapper.getString(Res.string.error_empty)
        NicknameValidator.ValidationResult.TooShort -> resourceWrapper.getString(
            Res.string.error_too_short,
            NicknameValidator.MIN_LENGTH
        )

        NicknameValidator.ValidationResult.TooLong -> resourceWrapper.getString(
            Res.string.error_too_long,
            NicknameValidator.MAX_LENGTH
        )

        NicknameValidator.ValidationResult.InvalidCharacters -> resourceWrapper.getString(Res.string.error_invalid_chars)
        NicknameValidator.ValidationResult.InvalidStartOrEnd -> resourceWrapper.getString(Res.string.error_invalid_start_end)
        NicknameValidator.ValidationResult.ConsecutiveSpecialChars -> resourceWrapper.getString(Res.string.error_consecutive_special)
        NicknameValidator.ValidationResult.Inappropriate -> resourceWrapper.getString(Res.string.error_inappropriate)
        NicknameValidator.ValidationResult.SystemName -> resourceWrapper.getString(Res.string.error_system_name)
        NicknameValidator.ValidationResult.CheckingAvailability -> resourceWrapper.getString(Res.string.checking_username)
        NicknameValidator.ValidationResult.Taken -> resourceWrapper.getString(Res.string.this_username_is_already_taken)
        NicknameValidator.ValidationResult.NetworkError -> resourceWrapper.getString(Res.string.connection_error)
    }
}

fun EditUserProfileScreenViewModel.updateUserNickname(updateUserNickname: UpdateUserNicknameUseCase) {
    val nickName = viewState.value.nickName
    val isAvailableUpdated =
        viewState.value.validationResult == NicknameValidator.ValidationResult.Available
    if (isAvailableUpdated) {
        viewModelScope.launch(IODispatcher) {
            updateViewState {
                it.copy(
                    loadingState = LoadingStateChangeNickName.Loading
                )
            }
            updateUserNickname.update(nickName).fold(
                onSuccess = {
                    updateViewState {
                        it.copy(
                            loadingState = LoadingStateChangeNickName.Success
                        )
                    }
                },
                onFailure = {
                    updateViewState {
                        it.copy(
                            loadingState = LoadingStateChangeNickName.Error
                        )
                    }
                }
            )
        }
    }
}
