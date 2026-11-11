package com.ensodai.avalon.presentation.userAccountScreen.actions

import androidx.compose.runtime.Immutable
import com.ensodai.avalon.domain.models.PersonaInfo
import com.ensodai.avalon.presentation.core.BaseActions

@Immutable
data class UserAccountScreenActions(
    val onAuth: () -> Unit,
    val onLogout: () -> Unit,
    val onDeleteAccount: () -> Unit,
    val onShowLogoutAlertDialog: () -> Unit,
    val onSelectPersonaAsDefault: (PersonaInfo) -> Unit,
    val onShowDeleteAccountAlertDialog: () -> Unit,
    val onDismissLogoutAlertDialog: () -> Unit,
    val onDismissDeleteAccountAlertDialog: () -> Unit,
    val bottomSheetActions: BottomSheetActions
) : BaseActions()

@Immutable
data class BottomSheetActions(
    val onOpenManageSubscription: () -> Unit,
    val onOpenManageEnergy: () -> Unit,
    val onOpenManageReservedEnergy: () -> Unit,
    val onOpenSupporterGratitude: () -> Unit,
    val onCloseBottomSheet: () -> Unit,
) : BaseActions()
