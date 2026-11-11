package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.dangerous_content
import com.ensodai.avalon.generated.resources.harassment
import com.ensodai.avalon.generated.resources.hate
import com.ensodai.avalon.generated.resources.in_development
import com.ensodai.avalon.generated.resources.sexually_explicit
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.utils.currentTimeMillis
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun SafetySettingsColumnSliders(
    modifier: Modifier = Modifier,
    harassment: Int,
    hate: Int,
    sexuallyExplicit: Int,
    dangerousContent: Int,
    onChangeHarassment: (Int) -> Unit,
    onChangeHate: (Int) -> Unit,
    onChangeSexuallyExplicit: (Int) -> Unit,
    onChangeDangerousContent: (Int) -> Unit
) {

    var isShowSafetySettings by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.wrapContentSize()
    ) {
        Column(
            modifier = Modifier
                .then(if (!isShowSafetySettings) Modifier.alpha(0.35f) else Modifier)
        ) {
            SafetySettingsSlider(
                modifier = Modifier,
                settingName = stringResource(Res.string.harassment),
                safetyValue = harassment,
                onChangeValue = onChangeHarassment
            )
            DividerLine()
            SafetySettingsSlider(
                modifier = Modifier,
                settingName = stringResource(Res.string.hate),
                safetyValue = hate,
                onChangeValue = onChangeHate,
            )
            DividerLine()
            SafetySettingsSlider(
                modifier = Modifier,
                settingName = stringResource(Res.string.sexually_explicit),
                safetyValue = sexuallyExplicit,
                onChangeValue = onChangeSexuallyExplicit,
            )
            DividerLine()
            SafetySettingsSlider(
                modifier = Modifier,
                settingName = stringResource(Res.string.dangerous_content),
                safetyValue = dangerousContent,
                onChangeValue = onChangeDangerousContent,
            )
        }

        BluredScreen(
            modifier = Modifier.matchParentSize(),
            isShowSafetySettings = isShowSafetySettings,
            onExpandSafetySettings = { isShowSafetySettings = true }
        )
    }
}

@Composable
fun BluredScreen(
    modifier: Modifier = Modifier,
    isShowSafetySettings: Boolean,
    onExpandSafetySettings: () -> Unit
) {
    val colors = AvalonTheme.colors
    var tapCount by remember { mutableStateOf(0) }
    var firstTapTime by remember { mutableStateOf(0L) }

    if (!isShowSafetySettings) {

        Box(
            modifier = modifier
                .fillMaxSize()
                .clickable {
                    val currentTime = currentTimeMillis()
                    if (firstTapTime == 0L || (currentTime - firstTapTime) > 4000L) {
                        firstTapTime = currentTime
                        tapCount = 1
                    } else {
                        tapCount++
                    }
                    if (tapCount >= 10) {
                        onExpandSafetySettings()
                        tapCount = 0
                        firstTapTime = 0L
                    }
                }
        ) {

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row {
                    Icon(
                        imageVector = IconsManager.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = stringResource(Res.string.in_development),
                        style = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMain
                        )
                    )
                }

            }
        }
    }
}
