package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.mes_example_stage
import com.ensodai.avalon.generated.resources.mes_example_stage_desc
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun MesExampleSection(
    modifier: Modifier = Modifier,
    mesExample: String?,
    onEditMesExample: () -> Unit,
    isUpdating: Boolean = false
) {
    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.mes_example_stage),
        value = mesExample,
        placeholder = stringResource(Res.string.mes_example_stage_desc),
        icon = IconsManager.Chat,
        actionType = EditActionType.NAVIGATION,
        onClick = onEditMesExample,
        isUpdating = isUpdating
    )
}
