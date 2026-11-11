package com.ensodai.avalon.presentation.commonComponents.alertDialogs


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.ok
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun AlertDialogDeleteItem(
    title: String,
    message: String? = null,
    icon: ImageVector = IconsManager.Delete,
    isShowDialog: Boolean,
    reversedButtonColor: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = AvalonTheme.colors
    if (isShowDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = "Delete Icon",
                    modifier = Modifier.size(32.dp),
                    tint = colors.error
                )
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textMain,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            },
            confirmButton = {
                Row {
                    Text(
                        text = stringResource(Res.string.cancel),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (reversedButtonColor)
                                colors.error
                            else colors.mutedAccent,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable(onClick = onDismiss)
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Text(
                        text = stringResource(Res.string.ok),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (reversedButtonColor)
                                colors.mutedAccent
                            else colors.error,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable(onClick = onConfirm)
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = colors.overlaySurface,
        )
    }
}

