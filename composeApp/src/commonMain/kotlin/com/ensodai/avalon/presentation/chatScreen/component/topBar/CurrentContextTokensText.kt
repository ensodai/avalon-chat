package com.ensodai.avalon.presentation.chatScreen.component.topBar

import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme

@Composable
fun CurrentContextTokensText(
    currentContextTokens: String
) {
    Text(
        text = currentContextTokens,
        style = MaterialTheme.typography.bodySmall,
        color = AvalonTheme.colors.textSecondary
    )
}
