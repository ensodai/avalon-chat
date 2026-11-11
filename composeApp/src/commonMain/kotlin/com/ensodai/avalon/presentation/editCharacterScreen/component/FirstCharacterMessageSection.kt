package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.first_message_stage
import com.ensodai.avalon.generated.resources.where_will_the_dialogue_begin
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun FirstCharacterMessageSection(
    modifier: Modifier = Modifier,
    firstMessage: String?,
    onEditFirstMessage: () -> Unit,
    isUpdating: Boolean
) {
    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.first_message_stage),
        value = firstMessage,
        placeholder = stringResource(Res.string.where_will_the_dialogue_begin),
        icon = IconsManager.ChatBubbleOutline,
        actionType = EditActionType.SHEET,
        onClick = onEditFirstMessage,
        isUpdating = isUpdating
    )
}
