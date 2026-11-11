package com.ensodai.avalon.presentation.loginScreen.action

import com.ensodai.avalon.presentation.core.BaseActions

data class LoginScreenActions(
    val onServerUrlChanged: (String) -> Unit = {},
    val onUsernameChanged: (String) -> Unit = {},
    val onPasswordChanged: (String) -> Unit = {},
    val onTogglePasswordVisibility: () -> Unit = {},
    val onToggleRegisterMode: () -> Unit = {},
    val onSubmit: () -> Unit = {},
    val onGoogleSignInSuccess: (token: String) -> Unit = {}
) : BaseActions()
