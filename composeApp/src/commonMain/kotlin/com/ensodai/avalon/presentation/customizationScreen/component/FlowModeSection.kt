package com.ensodai.avalon.presentation.customizationScreen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.customization_flow_mode_desc
import com.ensodai.avalon.generated.resources.customization_flow_mode_title
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun FlowModeSection(
    isFlowModeEnabled: Boolean,
    onFlowModeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors

    GemCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.customization_flow_mode_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textMain
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.customization_flow_mode_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = isFlowModeEnabled,
                onCheckedChange = onFlowModeChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.background,
                    checkedTrackColor = colors.neonAccent,
                    uncheckedThumbColor = colors.textSecondary,
                    uncheckedTrackColor = colors.cardBackground
                )
            )
        }
    }
}
