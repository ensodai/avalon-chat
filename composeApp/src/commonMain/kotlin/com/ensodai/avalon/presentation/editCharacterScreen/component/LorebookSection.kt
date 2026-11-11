package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.lorebook_section_desc
import com.ensodai.avalon.generated.resources.lorebook_title
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun LorebookSection(
    modifier: Modifier = Modifier,
    onEditLorebook: () -> Unit
) {
    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.lorebook_title),
        value = stringResource(Res.string.lorebook_section_desc),
        icon = IconsManager.SystemInstruction,
        actionType = EditActionType.NAVIGATION,
        onClick = onEditLorebook,
        isUpdating = false
    )
}
