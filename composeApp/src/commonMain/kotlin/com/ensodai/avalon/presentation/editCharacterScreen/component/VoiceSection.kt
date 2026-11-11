package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.theme.IconsManager

@Composable
fun VoiceSection(
    modifier: Modifier = Modifier,
    voice: VoiceModel,
    onEditVoice: () -> Unit,
    isUpdating: Boolean
) {
    EditFieldCard(
        modifier = modifier,
        title = "Голос персонажа",
        value = "${voice.emoji} ${voice.modelName} — ${voice.description}",
        icon = IconsManager.Waves,
        actionType = EditActionType.SHEET,
        onClick = onEditVoice,
        isUpdating = isUpdating
    )
}
