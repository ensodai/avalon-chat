package com.ensodai.avalon.presentation.loginScreen.viewState

import com.ensodai.avalon.config.AppBuildConfig
import com.ensodai.avalon.presentation.core.BaseViewState

data class LoginScreenViewState(
    val serverUrl: String = AppBuildConfig.BASE_URL,
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : BaseViewState()
