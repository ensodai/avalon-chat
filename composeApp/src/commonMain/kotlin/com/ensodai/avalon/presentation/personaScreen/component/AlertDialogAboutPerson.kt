package com.ensodai.avalon.presentation.personaScreen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.ok
import com.ensodai.avalon.generated.resources.persona_explain
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun AlertDialogAboutPerson(
    isShowDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!isShowDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = IconsManager.Info,
                contentDescription = "Delete Icon",
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
//            Text(
//                text = title,
//                style = MaterialTheme.typography.titleLarge,
//                color = MaterialTheme.colorScheme.onSurface,
//                modifier = Modifier
//                    .padding(vertical = 8.dp)
//                    .fillMaxWidth(),
//                textAlign = TextAlign.Center
//            )
        },
        text = {
            Text(
                text = stringResource(Res.string.persona_explain),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            Row {
                Text(
                    text = stringResource(Res.string.ok),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable(onClick = onConfirm)
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = AvalonTheme.colors.surfaceHigh,
    )

}
