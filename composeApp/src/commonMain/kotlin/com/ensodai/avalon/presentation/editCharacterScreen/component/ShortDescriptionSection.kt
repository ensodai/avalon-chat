package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.short_description_stage
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShortDescriptionSection(
    modifier: Modifier = Modifier,
    shortDescription: String,
    onEditShortDescription: () -> Unit,
    isUpdating: Boolean
) {
    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.short_description_stage),
        value = shortDescription,
        placeholder = stringResource(Res.string.short_description_stage),
        icon = IconsManager.ShortText,
        actionType = EditActionType.SHEET,
        onClick = onEditShortDescription,
        isUpdating = isUpdating
    )
}
