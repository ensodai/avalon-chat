package com.ensodai.avalon.presentation.loginScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.repository.local.DatastoreManager
import com.ensodai.avalon.domain.useCases.commonUseCase.auth.AuthenticationUserUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.loginScreen.action.LoginScreenActions
import com.ensodai.avalon.presentation.loginScreen.viewState.LoginScreenViewState
import com.ensodai.avalon.utils.L
import com.ensodai.avalon.utils.messageOrDefault
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LoginScreenViewModel(
    private val authenticationUser: AuthenticationUserUseCase,
    private val datastoreManager: DatastoreManager
) : BaseViewModel<LoginScreenViewState, LoginScreenActions>(
    initialState = LoginScreenViewState()
) {

    init {
        val savedServerUrl = datastoreManager.getServerUrl()
        if (!savedServerUrl.isNullOrBlank()) {
            updateViewState { it.copy(serverUrl = savedServerUrl) }
        }
    }

    override val actions = LoginScreenActions(
        onServerUrlChanged = { url ->
            updateViewState { it.copy(serverUrl = url, errorMessage = null) }
        },
        onUsernameChanged = { username ->
            updateViewState { it.copy(username = username, errorMessage = null) }
        },
        onPasswordChanged = { password ->
            updateViewState { it.copy(password = password, errorMessage = null) }
        },
        onTogglePasswordVisibility = {
            updateViewState { it.copy(isPasswordVisible = !it.isPasswordVisible) }
        },
        onToggleRegisterMode = {
            updateViewState { it.copy(isRegisterMode = !it.isRegisterMode, errorMessage = null) }
        },
        onSubmit = {
            handleSubmit()
        },
        onGoogleSignInSuccess = { token ->
            handleGoogleSignInSuccess(token)
        }
    )

    private fun handleSubmit() {
        val state = viewState.value
        val url = state.serverUrl.trim()
        val username = state.username.trim()
        val password = state.password

        if (url.isBlank()) {
            updateViewState { it.copy(errorMessage = "Введите URL сервера") }
            return
        }
        if (username.isBlank()) {
            updateViewState { it.copy(errorMessage = "Введите имя пользователя") }
            return
        }
        if (password.isBlank()) {
            updateViewState { it.copy(errorMessage = "Введите пароль") }
            return
        }

        viewModelScope.launch {
            updateViewState { it.copy(isLoading = true, errorMessage = null) }

            val result = if (state.isRegisterMode) {
                authenticationUser.register(url, username, password)
            } else {
                authenticationUser.login(url, username, password)
            }

            result.onSuccess {
                L.d("LoginScreenViewModel", "Auth success, waiting for navigation...")
            }.onFailure { exception ->
                L.e("LoginScreenViewModel", "Auth failed: ${exception.stackTraceToString()}")
                updateViewState {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.messageOrDefault()
                    )
                }
            }
        }
    }

    private fun handleGoogleSignInSuccess(token: String) {
        viewModelScope.launch {
            updateViewState { it.copy(isLoading = true, errorMessage = null) }

            authenticationUser.auth(token)
                .onSuccess {
                    L.d("LoginScreenViewModel", "Login success, waiting for navigation...")
                }
                .onFailure { exception ->
                    L.e(
                        "LoginScreenViewModel",
                        "Authentication failed: ${exception.stackTraceToString()}"
                    )
                    updateViewState {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error: ${exception.messageOrDefault()}"
                        )
                    }
                }
        }
    }
}
