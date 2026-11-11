package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun CharacterEventsSection(
    modifier: Modifier = Modifier,
    onEditEvents: () -> Unit
) {
    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.character_events_title),
        value = stringResource(Res.string.character_events_section_desc),
        icon = IconsManager.AutoMode,
        actionType = EditActionType.NAVIGATION,
        onClick = onEditEvents,
        isUpdating = false
    )
}
