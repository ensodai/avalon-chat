package com.ensodai.avalon.presentation.settingScreen.component.alertDialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.delete_account
import com.ensodai.avalon.generated.resources.delete_account_explain
import com.ensodai.avalon.generated.resources.ok
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun AlertDialogDeleteAccount(
    isShowDeleteAccountAlertDialog: Boolean,
    onDismissDeleteAccountAlertDialog: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val colors = AvalonTheme.colors

    if (isShowDeleteAccountAlertDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteAccountAlertDialog,
            icon = {
                Icon(
                    imageVector = IconsManager.Delete,
                    contentDescription = "Delete Icon",
                    modifier = Modifier.size(32.dp),
                    tint = colors.error
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.delete_account),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textMain,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.delete_account_explain),
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
                            color = colors.mutedAccent,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable(onClick = onDismissDeleteAccountAlertDialog)
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Text(
                        text = stringResource(Res.string.ok),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colors.error,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable(onClick = onDeleteAccount)
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = colors.overlaySurface,
        )
    }

}
