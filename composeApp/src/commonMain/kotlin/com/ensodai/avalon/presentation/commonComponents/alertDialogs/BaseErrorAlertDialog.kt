package com.ensodai.avalon.presentation.commonComponents.alertDialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.error
import com.ensodai.avalon.generated.resources.ok
import com.ensodai.avalon.generated.resources.text_copied
import com.ensodai.avalon.presentation.commonComponents.snackbar.LocalAppSnackbarController
import com.ensodai.avalon.theme.IconsManager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Suppress("DEPRECATION")
@Composable
fun BaseErrorAlertDialog(
    text: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {

    val colors = com.ensodai.avalon.theme.AvalonTheme.colors
    val clipboardManager = LocalClipboardManager.current
    val snackbarController = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()
    val toastText = stringResource(Res.string.text_copied)
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = IconsManager.Warning,
                contentDescription = "Exception",
                modifier = Modifier.size(32.dp),
                tint = colors.error
            )
        },
        text = {
            Text(
                text = text.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMain,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        },
        title = {
            Text(text = stringResource(Res.string.error))
        },
        confirmButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (text != null) {
                            clipboardManager.setText(AnnotatedString(text))
                            scope.launch {
                                snackbarController.showMessage(toastText)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = IconsManager.ContentCopy,
                        contentDescription = "Copy to clipboard",
                    )
                }
                Text(
                    text = stringResource(Res.string.ok),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.mutedAccent,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                onDismiss()
                                onConfirm()
                            }
                        )
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = colors.overlaySurface,
    )
}
