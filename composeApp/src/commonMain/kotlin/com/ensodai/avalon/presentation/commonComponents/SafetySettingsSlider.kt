package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.block_few
import com.ensodai.avalon.generated.resources.block_high
import com.ensodai.avalon.generated.resources.block_low_medium_high
import com.ensodai.avalon.generated.resources.block_medium_high
import com.ensodai.avalon.generated.resources.block_most
import com.ensodai.avalon.generated.resources.block_none
import com.ensodai.avalon.generated.resources.block_some
import com.ensodai.avalon.generated.resources.show_always
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource


@Composable
fun SafetySettingsSlider(
    settingName: String,
    safetyValue: Int,
    onChangeValue: (Int) -> Unit,
    modifier: Modifier
) {
    val haptic = LocalHapticFeedback.current
    val colors = AvalonTheme.colors

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = settingName,
                modifier = Modifier.weight(1f),
                color = colors.warningAccent,
            )
            Text(
                text = when (safetyValue) {
                    1 -> stringResource(Res.string.block_none)
                    2 -> stringResource(Res.string.block_few)
                    3 -> stringResource(Res.string.block_some)
                    else -> stringResource(Res.string.block_most)
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            TooltipOnImmediateClick(
                safetyValue = safetyValue
            )
        }

        Slider(
            value = safetyValue.toFloat(),
            onValueChange = { newValue ->
                if (newValue.toInt() != safetyValue) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onChangeValue(newValue.toInt())
                }

            },
            steps = 2,
            valueRange = 1f..4f,
            colors = SliderColors(
                thumbColor = colors.textMain,
                activeTrackColor = colors.textMain,
                activeTickColor = Color.Transparent,
                inactiveTrackColor = colors.textMain,
                inactiveTickColor = Color.Transparent,
                disabledThumbColor = colors.textMain.copy(alpha = 0.5f),
                disabledActiveTrackColor = colors.textSecondary.copy(alpha = 0.5f),
                disabledActiveTickColor = Color.Transparent,
                disabledInactiveTrackColor = colors.strokeSubtle.copy(alpha = 0.5f),
                disabledInactiveTickColor = Color.Transparent
            )
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TooltipOnImmediateClick(safetyValue: Int) {

    val tooltipState = rememberTooltipState()
    val coroutineScope = rememberCoroutineScope()
    val tooltipText = when (safetyValue) {
        1 -> stringResource(Res.string.show_always)
        2 -> stringResource(Res.string.block_high)
        3 -> stringResource(Res.string.block_medium_high)
        else -> stringResource(Res.string.block_low_medium_high)
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above
        ),
        tooltip = { PlainTooltip { Text(tooltipText) } },
        state = tooltipState,
        focusable = true,
        enableUserInput = false
    ) {
        IconButton(
            onClick = {
                coroutineScope.launch {
                    tooltipState.show()
                    delay(3000)
                    tooltipState.dismiss()
                }
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = IconsManager.Info,
                contentDescription = "test info",
                tint = LocalContentColor.current.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
