package com.ensodai.avalon.presentation.editUserProfileScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.avalon.domain.useCases.editUserProfileScreenUseCase.CheckNicknameAvailabilityUseCase
import com.ensodai.avalon.domain.useCases.editUserProfileScreenUseCase.NicknameValidator
import com.ensodai.avalon.domain.useCases.editUserProfileScreenUseCase.UpdateUserNicknameUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.editUserProfileScreen.actions.EditUserProfileScreenActions
import com.ensodai.avalon.presentation.editUserProfileScreen.actions.changeNickName
import com.ensodai.avalon.presentation.editUserProfileScreen.actions.checkNickName
import com.ensodai.avalon.presentation.editUserProfileScreen.actions.updateUserNickname
import com.ensodai.avalon.presentation.editUserProfileScreen.viewState.EditUserProfileScreenViewState
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class EditUserProfileScreenViewModel(
    private val checkNicknameAvailability: CheckNicknameAvailabilityUseCase,
    private val nicknameValidator: NicknameValidator,
    private val resourceWrapper: ResourceWrapper,
    private val updateUserNickname: UpdateUserNicknameUseCase,
    @InjectedParam private val nickname: String,
) : BaseViewModel<EditUserProfileScreenViewState, EditUserProfileScreenActions>(
    initialState = EditUserProfileScreenViewState()
) {

    override val actions = EditUserProfileScreenActions(
        onChangeNickName = { nickName ->
            changeNickName(nickName = nickName)
        },
        onSaveNickName = { updateUserNickname(updateUserNickname = updateUserNickname) }
    )

    init {
        viewModelScope.launch(IODispatcher) {
            updateViewState {
                it.copy(nickName = nickname)
            }
        }
        checkNickName(
            nicknameValidator = nicknameValidator,
            resourceWrapper = resourceWrapper,
            checkNicknameAvailability = checkNicknameAvailability
        )
    }
}
