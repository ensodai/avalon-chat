package com.ensodai.avalon.presentation.editTemperatureScreen.component

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.save
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomBarEditWelcomeCardScreen(
    loadingState: LoadingState,
    isSaveEnabled: Boolean,
    pendingUploadsMessage: String? = null,
    onNavigateBack: () -> Unit,
    onSaveClick: () -> Unit
) {
    val colors = AvalonTheme.colors

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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TechnoButton(
                text = stringResource(Res.string.cancel),
                onClick = { onNavigateBack() },
                isPrimary = false,
                modifier = Modifier.weight(1f),
                enabled = loadingState != LoadingState.Loading,
            )

            TechnoButton(
                text = stringResource(Res.string.save),
                onClick = { onSaveClick() },
                isPrimary = true,
                isLoading = loadingState == LoadingState.Loading,
                modifier = Modifier.weight(1f),
                enabled = loadingState != LoadingState.Loading && isSaveEnabled
            )
        }
    }
}
