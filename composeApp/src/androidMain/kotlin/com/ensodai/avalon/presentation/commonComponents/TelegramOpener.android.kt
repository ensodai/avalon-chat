package com.ensodai.avalon.presentation.commonComponents

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.open_telegram_failed
import com.ensodai.avalon.presentation.commonComponents.snackbar.LocalAppSnackbarController
import kotlinx.coroutines.launch
import com.ensodai.avalon.config.AppBuildConfig
import org.jetbrains.compose.resources.getString

private class AndroidTelegramOpener(
    private val context: Context,
    private val onOpenFailed: () -> Unit
) : TelegramOpener {
    override fun openChannel() {
        val channelName = AppBuildConfig.TELEGRAM_CHANNEL
        try {
            val intent = Intent(Intent.ACTION_VIEW)
                .setData("tg://resolve?domain=$channelName".toUri())
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            val webIntent = Intent(Intent.ACTION_VIEW)
                .setData("https://t.me/$channelName".toUri())
            try {
                context.startActivity(webIntent)
            } catch (_: ActivityNotFoundException) {
                onOpenFailed()
            }
        }
    }
}

@Composable
actual fun rememberTelegramOpener(): TelegramOpener {
    val context = LocalContext.current
    val snackbarController = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()

    return remember(context, snackbarController, scope) {
        AndroidTelegramOpener(context) {
            scope.launch {
                snackbarController.showMessage(getString(Res.string.open_telegram_failed))
            }
        }
    }
}
