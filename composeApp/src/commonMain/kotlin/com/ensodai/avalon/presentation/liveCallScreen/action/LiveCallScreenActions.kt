package com.ensodai.avalon.presentation.liveCallScreen.action

import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.presentation.core.BaseActions

data class LiveCallScreenActions(
    val startCallScreen: () -> Unit,
    val stopCallScreen: () -> Unit,
    val toggleMute: () -> Unit,
    val showModelPicker: () -> Unit,
    val selectModel: (VoiceModel) -> Unit,
    val hideModelSheet: () -> Unit
) : BaseActions()