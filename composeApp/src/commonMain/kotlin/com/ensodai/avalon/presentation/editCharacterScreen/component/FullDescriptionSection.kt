package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.full_description_stage
import com.ensodai.avalon.generated.resources.history_character_habits
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun FullDescriptionSection(
    modifier: Modifier = Modifier,
    fullDescription: String?,
    onEditFullDescription: () -> Unit
) {

    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.full_description_stage),
        value = fullDescription,
        placeholder = stringResource(Res.string.history_character_habits),
        icon = IconsManager.Description,
        actionType = EditActionType.NAVIGATION,
        onClick = onEditFullDescription,
        isUpdating = false
    )
}
