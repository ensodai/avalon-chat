package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.interaction_format_setting_title_short
import com.ensodai.avalon.generated.resources.interaction_format_literary_rpg_title
import com.ensodai.avalon.generated.resources.interaction_format_chat_simulator_title
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun InteractionFormatSettingSection(
    modifier: Modifier = Modifier,
    interactionFormat: InteractionFormat,
    onEditInteractionFormat: () -> Unit,
    isUpdating: Boolean
) {
    val formatText = when (interactionFormat) {
        InteractionFormat.LITERARY_RPG -> stringResource(Res.string.interaction_format_literary_rpg_title)
        InteractionFormat.CHAT_SIMULATOR -> stringResource(Res.string.interaction_format_chat_simulator_title)
    }

    val formatIcon = IconsManager.Chat

    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.interaction_format_setting_title_short),
        value = formatText,
        icon = formatIcon,
        actionType = EditActionType.SHEET,
        onClick = onEditInteractionFormat,
        isUpdating = isUpdating
    )
}
