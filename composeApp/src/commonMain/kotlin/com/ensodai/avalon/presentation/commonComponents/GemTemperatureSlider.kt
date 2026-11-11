package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.creativity
import com.ensodai.avalon.generated.resources.logic
import com.ensodai.avalon.generated.resources.moderation
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun GemTemperatureSlider(
    modifier: Modifier = Modifier,
    sliderPosition: Float,
    onChangeTemperature: (Float) -> Unit
) {
    val colors = AvalonTheme.colors
    val stageConfigs = remember(colors) {
        listOf(
            StageConfig(
                stageKey = StageKey.Logic,
                textResourceId = Res.string.logic,
                color = colors.onlineStatus
            ),
            StageConfig(
                stageKey = StageKey.Moderation,
                textResourceId = Res.string.moderation,
                color = colors.successAccent
            ),
            StageConfig(
                stageKey = StageKey.Creativity,
                textResourceId = Res.string.creativity,
                color = colors.warningAccent
            )
        )
    }

    Column(modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                stageConfigs.forEach { config ->
                    StageColumn(config = config, sliderPosition = sliderPosition)
                }
            }
        }

        Slider(
            value = sliderPosition,
            onValueChange = { onChangeTemperature(it) },
            valueRange = 0f..1f,
            colors = SliderColors(
                thumbColor = colors.textMain,
                activeTrackColor = colors.textMain,
                activeTickColor = Color.Transparent,
                inactiveTrackColor = colors.textMain,
                inactiveTickColor = Color.Transparent,
                disabledThumbColor = colors.textMain.copy(alpha = 0.5f),
                disabledActiveTrackColor = colors.textSecondary.copy(alpha = 0.5f),
                disabledActiveTickColor = Color.Transparent,
                disabledInactiveTrackColor = colors.placeholderText.copy(alpha = 0.5f),
                disabledInactiveTickColor = Color.Transparent
            )
        )
    }
}


private data class StageConfig(
    val stageKey: StageKey,
    val textResourceId: StringResource,
    val color: Color,
    val minSize: Dp = 50.dp,
    val maxSize: Dp = 200.dp,
    val minFontSize: TextUnit = 11.sp,
    val maxFontSize: TextUnit = 24.sp
)

private enum class StageKey {
    Logic,
    Moderation,
    Creativity
}

@Composable
private fun StageColumn(
    config: StageConfig,
    sliderPosition: Float
) {
    val currentSize = calculateSize(
        sliderPosition = sliderPosition,
        minSize = config.minSize,
        maxSize = config.maxSize,
        stageKey = config.stageKey
    )
    val currentFontSize = calculateFontSize(
        sliderPosition = sliderPosition,
        minFontSize = config.minFontSize,
        maxFontSize = config.maxFontSize,
        stageKey = config.stageKey
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(currentSize)
                .background(config.color.copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(currentSize * 0.45f)
                    .background(config.color, CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(config.textResourceId),
            fontSize = currentFontSize
        )
    }
}

private fun calculateSize(
    sliderPosition: Float,
    minSize: Dp,
    maxSize: Dp,
    stageKey: StageKey
): Dp {
    return when (stageKey) {
        StageKey.Logic -> if (sliderPosition <= 0.5f) {
            lerp(maxSize, minSize, sliderPosition / 0.5f)
        } else {
            minSize
        }

        StageKey.Moderation -> if (sliderPosition <= 0.5f) {
            lerp(minSize, maxSize, sliderPosition / 0.5f)
        } else {
            lerp(maxSize, minSize, (sliderPosition - 0.5f) / 0.5f)
        }

        StageKey.Creativity -> if (sliderPosition <= 0.5f) {
            minSize
        } else {
            lerp(minSize, maxSize, (sliderPosition - 0.5f) / 0.5f)
        }
    }
}

private fun calculateFontSize(
    sliderPosition: Float,
    minFontSize: TextUnit,
    maxFontSize: TextUnit,
    stageKey: StageKey
): TextUnit {
    return when (stageKey) {
        StageKey.Logic -> if (sliderPosition <= 0.5f) {
            lerp(maxFontSize, minFontSize, sliderPosition / 0.5f)
        } else {
            minFontSize
        }

        StageKey.Moderation -> if (sliderPosition <= 0.5f) {
            lerp(minFontSize, maxFontSize, sliderPosition / 0.5f)
        } else {
            lerp(maxFontSize, minFontSize, (sliderPosition - 0.5f) / 0.5f)
        }

        StageKey.Creativity -> if (sliderPosition <= 0.5f) {
            minFontSize
        } else {
            lerp(minFontSize, maxFontSize, (sliderPosition - 0.5f) / 0.5f)
        }
    }
}
