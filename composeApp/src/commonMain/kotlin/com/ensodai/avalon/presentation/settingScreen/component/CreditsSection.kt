package com.ensodai.avalon.presentation.settingScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.config.AppBuildConfig
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.credits
import com.ensodai.avalon.generated.resources.enso_baza
import com.ensodai.avalon.presentation.commonComponents.HighlightClickableText
import com.ensodai.avalon.utils.rememberUrlOpener
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun CreditsSection(modifier: Modifier) {
    val colors = AvalonTheme.colors
    val urlOpener = rememberUrlOpener()
    Column {
        HighlightClickableText(
            modifier = modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .background(colors.cardBackground.copy(alpha = 0.2f)),
            text = stringResource(Res.string.credits),
            highlightSegment = stringResource(Res.string.enso_baza),
            color = colors.textSecondary,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Start,
            onClick = {
                val botUsername = AppBuildConfig.TELEGRAM_BOT
                urlOpener.open("https://t.me/$botUsername?start=start")
            }
        )
        VersionNameText()
    }

}

@Composable
fun VersionNameText() {
    val colors = AvalonTheme.colors
    Text(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = colors.textSecondary,
        style = MaterialTheme.typography.bodySmall,
        text = "Version: ${AppBuildConfig.VERSION_NAME}"
    )
}
