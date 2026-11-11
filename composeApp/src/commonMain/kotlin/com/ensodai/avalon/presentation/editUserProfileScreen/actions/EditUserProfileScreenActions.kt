package com.ensodai.avalon.presentation.editUserProfileScreen.actions

import com.ensodai.avalon.presentation.core.BaseActions

data class EditUserProfileScreenActions(
    val onChangeNickName: (String) -> Unit,
    val onSaveNickName: () -> Unit
) : BaseActions()