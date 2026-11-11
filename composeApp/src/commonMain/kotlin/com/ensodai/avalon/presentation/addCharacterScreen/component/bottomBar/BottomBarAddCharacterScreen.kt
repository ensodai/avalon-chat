package com.ensodai.avalon.presentation.addCharacterScreen.component.bottomBar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.back_button
import com.ensodai.avalon.generated.resources.continue_button
import com.ensodai.avalon.generated.resources.publish
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomBarAddCharacterScreen(
    isShowContinueButton: Boolean,
    isShowBackButton: Boolean,
    isShowPublishButton: Boolean,
    isMainActionEnabled: Boolean,
    pendingUploadsMessage: String? = null,
    goToNextStage: () -> Unit,
    goToPreviousStage: () -> Unit,
    publishCharacter: () -> Unit
) {
    val colors = AvalonTheme.colors

    val mainActionColor by animateColorAsState(
        targetValue = if (isShowPublishButton) colors.warningAccent else colors.textMain,
        label = "ButtonColor"
    )

    val mainActionContentColor = colors.background

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (pendingUploadsMessage != null) {
            Text(
                text = pendingUploadsMessage,
                color = colors.textSecondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isShowBackButton) {
                TechnoButton(
                    text = stringResource(Res.string.back_button),
                    onClick = goToPreviousStage,
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    enabled = isMainActionEnabled
                )
            }

            if (isShowContinueButton || isShowPublishButton) {
                TechnoButton(
                    text = if (isShowPublishButton) stringResource(Res.string.publish)
                    else stringResource(Res.string.continue_button),
                    onClick = {
                        if (isShowPublishButton) publishCharacter() else goToNextStage()
                    },
                    isPrimary = true,
                    primaryColor = mainActionColor,
                    onPrimaryColor = mainActionContentColor,
                    modifier = Modifier.weight(1f),
                    enabled = isMainActionEnabled
                )
            }
        }
    }
}
