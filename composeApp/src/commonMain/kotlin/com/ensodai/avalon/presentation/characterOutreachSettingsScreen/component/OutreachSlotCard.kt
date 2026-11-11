package com.ensodai.avalon.presentation.characterOutreachSettingsScreen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.outreach_settings_prompt_label
import com.ensodai.avalon.generated.resources.outreach_settings_prompt_placeholder
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoTextField
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun OutreachSlotCard(
    title: String,
    isEnabled: Boolean,
    onToggleSlot: (Boolean) -> Unit,
    promptValue: String,
    onPromptChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    parentEnabled: Boolean = true
) {
    val colors = AvalonTheme.colors

    GemCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        enabled = parentEnabled
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (parentEnabled) colors.textMain else colors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Switch(
                    checked = isEnabled && parentEnabled,
                    onCheckedChange = { onToggleSlot(it) },
                    enabled = parentEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.background,
                        checkedTrackColor = colors.neonAccent,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.cardBackground
                    )
                )
            }

            AnimatedVisibility(
                visible = isEnabled && parentEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(Res.string.outreach_settings_prompt_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TechnoTextField(
                        value = promptValue,
                        onValueChange = onPromptChange,
                        placeholder = stringResource(Res.string.outreach_settings_prompt_placeholder),
                        modifier = Modifier.fillMaxWidth(),
                        isError = isError,
                        maxLength = 250,
                        enabled = parentEnabled,
                        singleLine = false,
                        minLines = 3
                    )
                }
            }
        }
    }
}
