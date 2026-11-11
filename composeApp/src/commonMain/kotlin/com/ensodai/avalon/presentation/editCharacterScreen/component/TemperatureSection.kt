package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.neural_entropy
import com.ensodai.avalon.presentation.addCharacterScreen.component.getTemperatureTitle
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun TemperatureSection(
    modifier: Modifier = Modifier,
    temperature: Float,
    onEditTemperature: () -> Unit
) {
    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.neural_entropy),
        value = getTemperatureTitle(temperature),
        icon = IconsManager.Waves,
        actionType = EditActionType.NAVIGATION,
        onClick = onEditTemperature,
        isUpdating = false
    )
}
