package com.ensodai.avalon.presentation.userAccountScreen.actions

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.network.Resource
import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.shared.avalon.gifts.models.PremiumStatus.CRYSTAL
import com.ensodai.shared.avalon.gifts.models.PremiumStatus.ELITE
import com.ensodai.shared.avalon.gifts.models.PremiumStatus.FREE
import com.ensodai.shared.avalon.gifts.models.PremiumStatus.PREMIUM
import com.ensodai.shared.avalon.gifts.models.PremiumStatus.SPECIAL
import com.ensodai.avalon.domain.models.PersonaInfo
import com.ensodai.avalon.domain.models.UserAccountInfo
import com.ensodai.avalon.domain.useCases.commonUseCase.auth.AuthenticationUserUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.DeleteAccountUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.GetAllPersonasUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.GetSelectedPersonaUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.GetUserAccountInfoUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.LogoutUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.SelectPersonaUseCase
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.presentation.userAccountScreen.UserAccountScreenViewModel
import com.ensodai.avalon.presentation.userAccountScreen.viewState.UserAccountScreenBottomSheetState
import com.ensodai.avalon.utils.EMPTY_STRING
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
fun UserAccountScreenViewModel.updateUserAccountInfo(
    getUserAccountInfo: GetUserAccountInfoUseCase,
    getSelectedPersona: GetSelectedPersonaUseCase,
    getAllPersonas: GetAllPersonasUseCase,
) {
    viewModelScope.launch {
        viewState
            .map { it.isAuthorized }
            .distinctUntilChanged()
            .flatMapLatest { isAuthorized ->
                if (isAuthorized) {
                    combineAuthorizedUserAccountState(
                        accountInfoFlow = getUserAccountInfo(),
                        selectedPersonaFlow = getSelectedPersona.getFlow()
                            .onStart { emit(null) },
                        personasFlow = getAllPersonas.get()
                            .onStart { emit(emptyList()) }
                    )
                } else {
                    flowOf(
                        UserAccountUiState(
                            resource = Resource.Success(
                                UserAccountInfo(
                                    name = EMPTY_STRING,
                                    nickName = EMPTY_STRING,
                                    id = null,
                                    avatarUrl = EMPTY_STRING,
                                    currentQuota = 0,
                                    maxQuota = 0,
                                    reserveBalance = 0,
                                    maxReserveBalance = 0,
                                    premiumStatus = FREE,
                                    nextBillingDate = null,
                                    paymentProvider = SubscriptionProvider.TEST,
                                    charactersCount = 10,
                                    totalUnclaimedCreatorEnergy = 0.0,
                                    isOutreachEnabled = true,
                                    dailyOutreachLimit = 1
                                )
                            ),
                            selectedPersona = null,
                            personas = emptyList()
                        )
                    )
                }
            }
            .collect { state ->
                when (val resource = state.resource) {
                    is Resource.Loading -> {
                        updateViewState {
                            it.copy(
                                loadingState = LoadingState.Loading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val userAccountInfo = resource.data

                        val premiumStatus = when (userAccountInfo.premiumStatus) {
                            FREE -> {
                                UserTariff.Free
                            }
                            
                            PremiumStatus.SUPPORTER -> {
                                UserTariff.Supporter
                            }

                            PREMIUM -> {
                                UserTariff.Premium
                            }

                            CRYSTAL -> {
                                UserTariff.Crystal
                            }

                            ELITE -> {
                                UserTariff.Elite
                            }

                            SPECIAL -> {
                                UserTariff.Special
                            }
                        }
                        updateViewState {
                            it.copy(
                                loadingState = LoadingState.Success,
                                userName = userAccountInfo.name,
                                userNickName = userAccountInfo.nickName,
                                userId = userAccountInfo.id,
                                userAvatar = userAccountInfo.avatarUrl,
                                currentQuota = userAccountInfo.currentQuota,
                                maxQuota = userAccountInfo.maxQuota,
                                reserveBalance = userAccountInfo.reserveBalance,
                                maxReserveBalance = userAccountInfo.maxReserveBalance,
                                premiumStatus = premiumStatus,
                                purchasedBatteries = userAccountInfo.reserveBalance,
                                nextBillingDate = userAccountInfo.nextBillingDate,
                                paymentProvider = userAccountInfo.paymentProvider,
                                charactersCount = userAccountInfo.charactersCount,
                                totalUnclaimedCreatorEnergy = userAccountInfo.totalUnclaimedCreatorEnergy,
                                isEarlySupporter = userAccountInfo.isEarlySupporter,
                                selectedPersona = state.selectedPersona,
                                personas = state.personas
                            )
                        }
                    }

                    is Resource.Error -> {
                        updateViewState {
                            it.copy(
                                loadingState = LoadingState.Error,
                                userId = null,
                                selectedPersona = state.selectedPersona,
                                personas = state.personas
                            )
                        }
                    }
                }
            }
    }
}

private fun combineAuthorizedUserAccountState(
    accountInfoFlow: Flow<Resource<UserAccountInfo>>,
    selectedPersonaFlow: Flow<PersonaInfo?>,
    personasFlow: Flow<List<PersonaInfo>>
): Flow<UserAccountUiState> {
    return combine(
        accountInfoFlow,
        selectedPersonaFlow,
        personasFlow
    ) { resource, selectedPersona, personas ->
        UserAccountUiState(
            resource = resource,
            selectedPersona = selectedPersona,
            personas = personas
        )
    }.distinctUntilChanged()
}

private data class UserAccountUiState(
    val resource: Resource<UserAccountInfo>,
    val selectedPersona: PersonaInfo?,
    val personas: List<PersonaInfo>
)

fun UserAccountScreenViewModel.logout(logout: LogoutUseCase) {
    viewModelScope.launch(IODispatcher) {
        logout.logout()
        updateViewState {
            it.copy(isShowLogoutAlertDialog = false)
        }
    }
}

fun UserAccountScreenViewModel.auth(authenticationUser: AuthenticationUserUseCase) {

}

fun UserAccountScreenViewModel.onDeleteAccount(deleteAccount: DeleteAccountUseCase) {
    viewModelScope.launch(IODispatcher) {
        deleteAccount.delete()
        updateViewState {
            it.copy(isShowDeleteAccountAlertDialog = false)
        }
    }
}

fun UserAccountScreenViewModel.onDismissLogoutAlertDialog() {
    updateViewState {
        it.copy(isShowLogoutAlertDialog = false)
    }
}

fun UserAccountScreenViewModel.onDismissDeleteAccountAlertDialog() {
    updateViewState {
        it.copy(isShowDeleteAccountAlertDialog = false)
    }

}

fun UserAccountScreenViewModel.onShowLogoutAlertDialog() {
    updateViewState {
        it.copy(isShowLogoutAlertDialog = true)
    }
}

fun UserAccountScreenViewModel.onShowDeleteAccountAlertDialog() {
    updateViewState {
        it.copy(isShowDeleteAccountAlertDialog = true)
    }
}

fun UserAccountScreenViewModel.onSelectPersonaAsDefault(
    selectPersona: SelectPersonaUseCase,
    persona: PersonaInfo
) {
    viewModelScope.launch(IODispatcher) {
        selectPersona.selectLocal(persona.id)
    }
}

fun UserAccountScreenViewModel.onCloseBottomSheet() {
    updateViewState {
        it.copy(bottomSheetState = UserAccountScreenBottomSheetState.Idle)
    }
}

fun UserAccountScreenViewModel.onOpenManageSubscription() {
    val bottomSheetState = UserAccountScreenBottomSheetState.ManageSubscription(
        tariff = viewState.value.premiumStatus,
        nextBillingDate = viewState.value.nextBillingDate,
        provider = viewState.value.paymentProvider
    )
    updateViewState {
        it.copy(bottomSheetState = bottomSheetState)
    }
}

fun UserAccountScreenViewModel.onOpenManageEnergy() {
    val bottomSheetState = UserAccountScreenBottomSheetState.ManageEnergy(
        currentQuota = viewState.value.currentQuota,
        maxQuota = viewState.value.maxQuota,
        isShowUpgradeButton = viewState.value.premiumStatus == UserTariff.Free
    )
    updateViewState {
        it.copy(bottomSheetState = bottomSheetState)
    }
}

fun UserAccountScreenViewModel.onOpenManageReservedEnergy() {
    val bottomSheetState = UserAccountScreenBottomSheetState.ManageReservedEnergy(
        reserveBalance = viewState.value.reserveBalance,
        maxReserveBalance = viewState.value.maxReserveBalance
    )
    updateViewState {
        it.copy(bottomSheetState = bottomSheetState)
    }
}

fun UserAccountScreenViewModel.onOpenSupporterGratitude() {
    updateViewState {
        it.copy(bottomSheetState = UserAccountScreenBottomSheetState.SupporterGratitude)
    }
}
