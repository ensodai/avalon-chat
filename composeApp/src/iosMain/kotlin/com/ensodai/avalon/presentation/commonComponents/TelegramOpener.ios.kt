package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private object StubTelegramOpener : TelegramOpener {
    override fun openChannel() = Unit
}

@Composable
actual fun rememberTelegramOpener(): TelegramOpener {
    return remember { StubTelegramOpener }
}
