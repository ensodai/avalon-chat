package com.ensodai.avalon.presentation.settingScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.domain.useCases.settingScreenUseCase.ChangeAppLanguageUseCase
import com.ensodai.avalon.domain.useCases.settingScreenUseCase.GetFlowModeUseCase
import com.ensodai.avalon.domain.useCases.settingScreenUseCase.GetLanguageSettingsUseCase
import com.ensodai.avalon.domain.useCases.settingScreenUseCase.GetSettingLocalModeFlowUseCase
import com.ensodai.avalon.domain.useCases.settingScreenUseCase.OnChangeLocalModeUseCase
import com.ensodai.avalon.domain.useCases.settingScreenUseCase.ToggleFlowModeUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.DeleteAccountUseCase
import com.ensodai.avalon.domain.useCases.userAccountScreenUseCase.LogoutUseCase
import com.ensodai.avalon.domain.useCases.settingScreenUseCase.UpdateUserOutreachUseCase
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.avalon.presentation.settingScreen.action.SettingScreenAction
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.avalon.presentation.settingScreen.viewState.SettingScreenViewState
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingScreenViewModel(
    private val getLanguageSetting: GetLanguageSettingsUseCase,
    private val changeAppLanguage: ChangeAppLanguageUseCase,
    private val toggleFlowMode: ToggleFlowModeUseCase,
    private val getFlowMode: GetFlowModeUseCase,
    private val getSettingLocalModeFlow: GetSettingLocalModeFlowUseCase,
    private val onChangeLocalMode: OnChangeLocalModeUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val logout: LogoutUseCase,
    private val updateUserOutreach: UpdateUserOutreachUseCase,
    private val localUserRepository: LocalUserRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow(SettingScreenViewState())
    val viewState: StateFlow<SettingScreenViewState> = _viewState

    val action = SettingScreenAction(
        onChangeLanguage = { changeLanguage(it) },
        onToggleFlowMode = { toggleFlowMode() },
        onCloseAlertDialog = { closeAlertDialog() },
        createViewState = { createViewState() },
        onChangeLocalMode = { changeLocalMode() },
        onDeleteAccount = { deleteAccount() },
        onShowDeleteAccountDialog = { showDeleteAccountDialog() },
        onLogout = { logout() },
        onShowLogoutAlertDialog = {},
        onToggleOutreachMode = { toggleOutreachMode() },
        onChangeOutreachLimit = { changeOutreachLimit(it) }
    )

    init {
        createViewState()
    }

    private fun createViewState() {
        updateFlowModeState()
        updateLanguage()
        updateLocalMode()
        updateAuthState()
        updateOutreachState()
    }

    private fun changeLocalMode() {
        viewModelScope.launch(context = IODispatcher) {
            onChangeLocalMode.change()
        }
    }

    private fun updateLocalMode() {
        viewModelScope.launch(context = IODispatcher) {
            getSettingLocalModeFlow
                .get()
                .distinctUntilChanged()
                .collect { value ->
                    _viewState.getAndUpdate {
                        it.copy(localMode = value)
                    }
                }
        }
    }

    private fun changeLanguage(language: String) {
        viewModelScope.launch(context = IODispatcher) {
            changeAppLanguage.changeLanguage(language)
        }
    }

    private fun toggleFlowMode() {
        viewModelScope.launch(context = IODispatcher) {
            toggleFlowMode.toggle()
        }
    }

    private fun closeAlertDialog() {
        _viewState.getAndUpdate {
            it.copy(
                isShowDeleteAccountDialog = false
            )
        }
    }

    private fun updateFlowModeState() {
        viewModelScope.launch(context = IODispatcher) {
            getFlowMode
                .get()
                .distinctUntilChanged()
                .collect { toggleMode ->
                    _viewState.getAndUpdate {
                        it.copy(flowMode = toggleMode)
                    }
                }
        }
    }

    private fun updateLanguage() {
        viewModelScope.launch(context = IODispatcher) {
            getLanguageSetting
                .get()
                .distinctUntilChanged()
                .collect { value ->
                    _viewState.getAndUpdate {
                        it.copy(language = value)
                    }
                }
        }
    }

    private fun deleteAccount() {
        closeAlertDialog()
        viewModelScope.launch(context = IODispatcher) {
            logout.delete()
        }
    }

    private fun showDeleteAccountDialog() {
        _viewState.getAndUpdate {
            it.copy(
                isShowDeleteAccountDialog = true
            )
        }
    }

    private fun updateAuthState() {

    }

    private fun logout() {
        viewModelScope.launch(IODispatcher) {
            logout.logout()
        }
    }

    private fun toggleOutreachMode() {
        val currentVal = _viewState.value.isOutreachEnabled
        val limit = _viewState.value.dailyOutreachLimit
        viewModelScope.launch(context = IODispatcher) {
            updateUserOutreach.execute(!currentVal, limit)
        }
    }

    private fun changeOutreachLimit(limit: Int) {
        val isEnabled = limit > 0
        viewModelScope.launch(context = IODispatcher) {
            updateUserOutreach.execute(isEnabled, limit)
        }
    }

    private fun updateOutreachState() {
        viewModelScope.launch(context = IODispatcher) {
            localUserRepository
                .getCurrentUserFlow()
                .distinctUntilChanged()
                .collect { user ->
                    if (user != null) {
                        _viewState.getAndUpdate {
                            it.copy(
                                isOutreachEnabled = user.isOutreachEnabled,
                                dailyOutreachLimit = user.dailyOutreachLimit,
                                isPremium = user.premiumStatus != PremiumStatus.FREE
                            )
                        }
                    }
                }
        }
    }
}
