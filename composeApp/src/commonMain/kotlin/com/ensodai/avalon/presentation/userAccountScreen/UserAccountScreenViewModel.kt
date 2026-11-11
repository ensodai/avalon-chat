package com.ensodai.avalon.presentation.userAccountScreen

import com.ensodai.avalon.domain.useCases.commonUseCase.auth.AuthenticationUserUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.DeleteAccountUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.GetAllPersonasUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.GetSelectedPersonaUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.GetUserAccountInfoUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.LogoutUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.SelectPersonaUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.userAccountScreen.actions.BottomSheetActions
import com.ensodai.avalon.presentation.userAccountScreen.actions.UserAccountScreenActions
import com.ensodai.avalon.presentation.userAccountScreen.actions.logout
import com.ensodai.avalon.presentation.userAccountScreen.actions.onCloseBottomSheet
import com.ensodai.avalon.presentation.userAccountScreen.actions.onDeleteAccount
import com.ensodai.avalon.presentation.userAccountScreen.actions.onDismissDeleteAccountAlertDialog
import com.ensodai.avalon.presentation.userAccountScreen.actions.onDismissLogoutAlertDialog
import com.ensodai.avalon.presentation.userAccountScreen.actions.onOpenManageEnergy
import com.ensodai.avalon.presentation.userAccountScreen.actions.onOpenManageReservedEnergy
import com.ensodai.avalon.presentation.userAccountScreen.actions.onOpenManageSubscription
import com.ensodai.avalon.presentation.userAccountScreen.actions.onOpenSupporterGratitude
import com.ensodai.avalon.presentation.userAccountScreen.actions.onSelectPersonaAsDefault
import com.ensodai.avalon.presentation.userAccountScreen.actions.onShowDeleteAccountAlertDialog
import com.ensodai.avalon.presentation.userAccountScreen.actions.onShowLogoutAlertDialog
import com.ensodai.avalon.presentation.userAccountScreen.actions.updateUserAccountInfo
import com.ensodai.avalon.presentation.userAccountScreen.viewState.UserAccountScreenViewState
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class UserAccountScreenViewModel(
    private val getUserAccountInfo: GetUserAccountInfoUseCase,
    private val logout: LogoutUseCase,
    private val authenticationUser: AuthenticationUserUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val getSelectedPersona: GetSelectedPersonaUseCase,
    private val getAllPersonas: GetAllPersonasUseCase,
    private val selectPersona: SelectPersonaUseCase
) : BaseViewModel<UserAccountScreenViewState, UserAccountScreenActions>(
    initialState = UserAccountScreenViewState()
) {

    override val actions = UserAccountScreenActions(
        onAuth = { },
        onLogout = { logout(logout) },
        onDeleteAccount = { onDeleteAccount(deleteAccount) },
        onDismissLogoutAlertDialog = { onDismissLogoutAlertDialog() },
        onDismissDeleteAccountAlertDialog = { onDismissDeleteAccountAlertDialog() },
        onShowLogoutAlertDialog = { onShowLogoutAlertDialog() },
        onShowDeleteAccountAlertDialog = { onShowDeleteAccountAlertDialog() },
        onSelectPersonaAsDefault = {
            onSelectPersonaAsDefault(
                selectPersona = selectPersona,
                persona = it
            )
        },
        bottomSheetActions = BottomSheetActions(
            onOpenManageSubscription = { onOpenManageSubscription() },
            onOpenManageEnergy = { onOpenManageEnergy() },
            onOpenManageReservedEnergy = { onOpenManageReservedEnergy() },
            onOpenSupporterGratitude = { onOpenSupporterGratitude() },
            onCloseBottomSheet = { onCloseBottomSheet() }
        )
    )

    init {
        updateUserAccountInfo(
            getUserAccountInfo = getUserAccountInfo,
            getSelectedPersona = getSelectedPersona,
            getAllPersonas = getAllPersonas
        )
    }
}
