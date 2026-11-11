package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.character_name
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun CharacterNameSection(
    modifier: Modifier = Modifier,
    characterName: String,
    isUpdating: Boolean,
    onEditName: () -> Unit,
) {
    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.character_name),
        value = characterName,
        icon = IconsManager.Badge,
        actionType = EditActionType.SHEET,
        onClick = onEditName,
        isUpdating = isUpdating
    )
}
