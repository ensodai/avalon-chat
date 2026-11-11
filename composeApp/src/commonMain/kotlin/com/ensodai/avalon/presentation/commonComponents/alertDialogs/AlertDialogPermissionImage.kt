package com.ensodai.avalon.presentation.commonComponents.alertDialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.allow_storage_access
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.open_settings
import com.ensodai.avalon.generated.resources.permission_denied
import com.ensodai.avalon.generated.resources.settings
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.utils.rememberPlatformAppActions
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun AlertDialogPermissionImage(
    onCloseButtonClick: () -> Unit,
    isShowPermissionDialog: Boolean
) {
    val colors = AvalonTheme.colors
    val platformAppActions = rememberPlatformAppActions()
    if (isShowPermissionDialog) {
        AlertDialog(
            containerColor = colors.overlaySurface,
            shape = RoundedCornerShape(16.dp),
            onDismissRequest = { onCloseButtonClick() },
            icon = {
                Icon(
                    imageVector = IconsManager.Settings,
                    contentDescription = stringResource(Res.string.settings),
                    tint = colors.infoAccent,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(text = stringResource(Res.string.permission_denied))
            },
            text = {
                Text(
                    text = stringResource(Res.string.allow_storage_access),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCloseButtonClick()
                        platformAppActions.openAppSettings()
                    }
                ) {
                    Text(text = stringResource(Res.string.open_settings))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onCloseButtonClick()
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.cancel),
                        color = colors.error,
                    )
                }
            }
        )
    }
}
