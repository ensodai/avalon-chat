package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.runtime.Composable

interface TelegramOpener {
    fun openChannel()
}

@Composable
expect fun rememberTelegramOpener(): TelegramOpener
