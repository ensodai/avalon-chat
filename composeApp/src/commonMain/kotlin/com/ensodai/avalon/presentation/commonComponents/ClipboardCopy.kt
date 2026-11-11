package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.text_copied
import com.ensodai.avalon.presentation.commonComponents.snackbar.LocalAppSnackbarController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Composable-хук, который предоставляет готовую функцию для копирования текста
 * в буфер обмена и показа snackbar-сообщения.
 *
 * @param toastMessageResId ID строкового ресурса для сообщения.
 * @return Лямбда-функция `(String) -> Unit`, которую нужно вызывать для копирования текста.
 */
@Composable
@Suppress("DEPRECATION")
fun copyToClipboardAndShowToast(
    toastMessageResId: StringResource = Res.string.text_copied
): (text: String) -> Unit {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarController = LocalAppSnackbarController.current
    val toastMessage = stringResource(toastMessageResId)

    return remember(clipboard, scope, snackbarController, toastMessage) {
        { textToCopy ->
            scope.launch {
                clipboard.setText(AnnotatedString(textToCopy))
                snackbarController.showMessage(toastMessage)
            }
        }
    }
}
