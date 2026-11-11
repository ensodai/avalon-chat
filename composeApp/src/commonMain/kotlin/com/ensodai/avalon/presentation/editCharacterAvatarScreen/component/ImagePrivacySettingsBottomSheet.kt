package com.ensodai.avalon.presentation.editCharacterAvatarScreen.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.apply_action
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.energy_cost_label
import com.ensodai.avalon.generated.resources.energy_cost_range_error
import com.ensodai.avalon.generated.resources.example_level_placeholder
import com.ensodai.avalon.generated.resources.friendship_level_reward_desc
import com.ensodai.avalon.generated.resources.friendship_level_reward_title
import com.ensodai.avalon.generated.resources.image_settings_title
import com.ensodai.avalon.generated.resources.level_range_error
import com.ensodai.avalon.generated.resources.paid_photo_settings_desc
import com.ensodai.avalon.generated.resources.private_photo_label
import com.ensodai.avalon.generated.resources.required_friendship_level_label
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePrivacySettingsBottomSheet(
    image: CharacterImageDTO,
    onDismiss: () -> Unit,
    onSave: (isPrivate: Boolean, energyCost: Int, requiredLevel: Int?) -> Unit
) {
    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isPrivate by remember { mutableStateOf(image.isPrivate) }
    var energyCostInput by remember { mutableStateOf(image.energyCost.toString()) }
    var requiredLevelInput by remember { mutableStateOf(image.requiredLevel?.toString() ?: "") }

    var energyError by remember { mutableStateOf<String?>(null) }
    var levelError by remember { mutableStateOf<String?>(null) }

    val energyCostRangeError = stringResource(Res.string.energy_cost_range_error)
    val levelRangeError = stringResource(Res.string.level_range_error)

    fun validateAndSave() {
        var isValid = true

        val cost = energyCostInput.toIntOrNull() ?: 0
        if (isPrivate) {
            if (cost < 50 || cost > 50000) {
                energyError = energyCostRangeError
                isValid = false
            } else {
                energyError = null
            }
        } else {
            energyError = null
        }

        val level = requiredLevelInput.toIntOrNull()
        if (requiredLevelInput.isNotEmpty()) {
            if (level == null || level < 1 || level > 65) {
                levelError = levelRangeError
                isValid = false
            } else {
                levelError = null
            }
        } else {
            levelError = null
        }

        if (isValid) {
            onSave(isPrivate, if (isPrivate) cost else 0, if (requiredLevelInput.isEmpty()) null else level)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.overlaySurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            Text(
                text = stringResource(Res.string.image_settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textMain,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Приватность (энергия)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.private_photo_label),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textMain,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(Res.string.paid_photo_settings_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Switch(
                    checked = isPrivate,
                    onCheckedChange = {
                        isPrivate = it
                        if (!it) {
                            energyCostInput = "0"
                            energyError = null
                        } else if (energyCostInput == "0" || energyCostInput.isEmpty()) {
                            energyCostInput = "150" // значение по умолчанию
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.neonAccent,
                        checkedTrackColor = colors.neonAccent.copy(alpha = 0.5f)
                    )
                )
            }

            if (isPrivate) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = energyCostInput,
                    onValueChange = {
                        energyCostInput = it
                        energyError = null
                    },
                    label = { Text(stringResource(Res.string.energy_cost_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = energyError != null,
                    supportingText = energyError?.let { { Text(it, color = colors.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textMain,
                        unfocusedTextColor = colors.textMain,
                        focusedBorderColor = colors.neonAccent,
                        unfocusedBorderColor = colors.strokeActive,
                        errorBorderColor = colors.error
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Награда за уровень отношений
            Text(
                text = stringResource(Res.string.friendship_level_reward_title),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textMain,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(Res.string.friendship_level_reward_desc),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = requiredLevelInput,
                onValueChange = {
                    requiredLevelInput = it
                    levelError = null
                },
                label = { Text(stringResource(Res.string.required_friendship_level_label)) },
                placeholder = { Text(stringResource(Res.string.example_level_placeholder)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = levelError != null,
                supportingText = levelError?.let { { Text(it, color = colors.error) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textMain,
                    unfocusedTextColor = colors.textMain,
                    focusedBorderColor = colors.neonAccent,
                    unfocusedBorderColor = colors.strokeActive,
                    errorBorderColor = colors.error
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопки управления
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TechnoButton(
                    text = stringResource(Res.string.cancel),
                    onClick = onDismiss,
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                TechnoButton(
                    text = stringResource(Res.string.apply_action),
                    onClick = { validateAndSave() },
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
