package com.ensodai.avalon.presentation.characterLorebooksScreen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.character_lorebooks_create_and_attach
import com.ensodai.avalon.generated.resources.character_lorebooks_create_dialog_desc_placeholder
import com.ensodai.avalon.generated.resources.character_lorebooks_create_dialog_name_placeholder
import com.ensodai.avalon.generated.resources.character_lorebooks_create_dialog_public_checkbox
import com.ensodai.avalon.generated.resources.character_lorebooks_create_dialog_title
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoTextField
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateLorebookDialog(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    name: String,
    description: String,
    isPublic: Boolean,
    isLoading: Boolean,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onIsPublicChanged: (Boolean) -> Unit,
    onCreateAndAttach: () -> Unit
) {
    if (!isVisible) return

    val colors = AvalonTheme.colors

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(Res.string.character_lorebooks_create_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textMain
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TechnoTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    placeholder = stringResource(Res.string.character_lorebooks_create_dialog_name_placeholder),
                    maxLength = 60,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                TechnoTextField(
                    value = description,
                    onValueChange = onDescriptionChanged,
                    placeholder = stringResource(Res.string.character_lorebooks_create_dialog_desc_placeholder),
                    maxLength = 250,
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.character_lorebooks_create_dialog_public_checkbox),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMain,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isPublic,
                        onCheckedChange = onIsPublicChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.background,
                            checkedTrackColor = colors.neonAccent,
                            uncheckedThumbColor = colors.textSecondary,
                            uncheckedTrackColor = colors.cardBackground
                        )
                    )
                }
            }
        },
        confirmButton = {
            TechnoButton(
                text = stringResource(Res.string.character_lorebooks_create_and_attach),
                onClick = onCreateAndAttach,
                isPrimary = true,
                isLoading = isLoading,
                enabled = name.isNotBlank() && !isLoading
            )
        },
        dismissButton = {
            Text(
                text = stringResource(Res.string.cancel),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textSecondary,
                modifier = Modifier
                    .clickable(onClick = onDismissRequest)
                    .padding(8.dp)
            )
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = colors.overlaySurface
    )
}
