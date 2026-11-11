package com.ensodai.avalon.presentation.commonComponents.privacyCards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.interaction_format_literary_rpg_title
import com.ensodai.avalon.generated.resources.interaction_format_literary_rpg_description
import com.ensodai.avalon.generated.resources.interaction_format_chat_simulator_title
import com.ensodai.avalon.generated.resources.interaction_format_chat_simulator_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun InteractionFormatCardColumn(
    modifier: Modifier = Modifier,
    selectedFormat: InteractionFormat,
    onOptionSelected: (InteractionFormat) -> Unit
) {
    Column(modifier) {
        AvailabilityCard(
            title = stringResource(Res.string.interaction_format_literary_rpg_title),
            description = stringResource(Res.string.interaction_format_literary_rpg_description),
            isSelected = selectedFormat == InteractionFormat.LITERARY_RPG,
            onClick = { onOptionSelected(InteractionFormat.LITERARY_RPG) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        AvailabilityCard(
            title = stringResource(Res.string.interaction_format_chat_simulator_title),
            description = stringResource(Res.string.interaction_format_chat_simulator_description),
            isSelected = selectedFormat == InteractionFormat.CHAT_SIMULATOR,
            onClick = { onOptionSelected(InteractionFormat.CHAT_SIMULATOR) }
        )
    }
}
