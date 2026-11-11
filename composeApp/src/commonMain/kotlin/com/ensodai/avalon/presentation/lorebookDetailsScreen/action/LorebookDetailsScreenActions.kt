package com.ensodai.avalon.presentation.lorebookDetailsScreen.action

import com.ensodai.avalon.presentation.core.BaseActions

data class LorebookDetailsScreenActions(
    val onOpenComplaintDialog: () -> Unit,
    val onCloseComplaintDialog: () -> Unit,
    val onComplaintTextChanged: (String) -> Unit,
    val onSubmitComplaint: () -> Unit,
    val onToggleSaveToLibrary: () -> Unit,
    val onForkBook: () -> Unit,
    val onDismissFeedback: () -> Unit
) : BaseActions()
