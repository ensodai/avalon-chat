package com.ensodai.avalon.presentation.commonComponents.alertDialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.complaint_content
import com.ensodai.avalon.generated.resources.ok
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoInput

@Composable
fun AlertDialogComplaint(
    isShow: Boolean,
    text: String,
    onChangeText: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = AvalonTheme.colors
    if (isShow) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            icon = {
                Icon(
                    imageVector = IconsManager.Report,
                    contentDescription = "Exception",
                    modifier = Modifier.size(32.dp),
                    tint = colors.error
                )
            },
            text = {
                TechnoInput(
                    value = text,
                    onValueChange = { onChangeText(it) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    maxLines = 6,
                )
            },
            title = {
                Text(text = stringResource(Res.string.complaint_content))
            },
            confirmButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.ok),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colors.mutedAccent,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    onConfirm()
                                }
                            )
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = colors.overlaySurface
        )
    }
}

