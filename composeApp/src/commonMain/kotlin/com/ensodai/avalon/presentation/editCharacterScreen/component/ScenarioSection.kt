package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.scenario_stage
import com.ensodai.avalon.generated.resources.scenario_stage_desc
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScenarioSection(
    modifier: Modifier = Modifier,
    scenario: String?,
    onEditScenario: () -> Unit,
    isUpdating: Boolean = false
) {
    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.scenario_stage),
        value = scenario,
        placeholder = stringResource(Res.string.scenario_stage_desc),
        icon = IconsManager.Explore,
        actionType = EditActionType.NAVIGATION,
        onClick = onEditScenario,
        isUpdating = isUpdating
    )
}
