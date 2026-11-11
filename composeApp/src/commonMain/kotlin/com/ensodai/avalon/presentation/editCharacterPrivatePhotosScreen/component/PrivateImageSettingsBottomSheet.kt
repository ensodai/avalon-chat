package com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.energy_cost_label
import com.ensodai.avalon.generated.resources.enter_number
import com.ensodai.avalon.generated.resources.max_price_error
import com.ensodai.avalon.generated.resources.min_price_error
import com.ensodai.avalon.generated.resources.paid_photo_settings_desc
import com.ensodai.avalon.generated.resources.paid_photo_settings_title
import com.ensodai.avalon.generated.resources.save
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateImageSettingsBottomSheet(
    image: CharacterImageDTO,
    onDismiss: () -> Unit,
    onSave: (energyCost: Int) -> Unit
) {
    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Если цена была 0, ставим стандартную 150
    val initialCost = if (image.energyCost <= 0) "150" else image.energyCost.toString()
    var energyCostInput by remember { mutableStateOf(initialCost) }
    var energyError by remember { mutableStateOf<String?>(null) }

    // Проверка ошибок в реальном времени
    val cost = energyCostInput.toIntOrNull()
    val isError = cost == null || cost < 50 || cost > 50000

    val enterNumberError = stringResource(Res.string.enter_number)
    val minPriceError = stringResource(Res.string.min_price_error)
    val maxPriceError = stringResource(Res.string.max_price_error)

    LaunchedEffect(energyCostInput) {
        val parsedCost = energyCostInput.toIntOrNull()
        energyError = when {
            parsedCost == null -> enterNumberError
            parsedCost < 50 -> minPriceError
            parsedCost > 50000 -> maxPriceError
            else -> null
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
                text = stringResource(Res.string.paid_photo_settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textMain,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(Res.string.paid_photo_settings_desc),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = energyCostInput,
                onValueChange = {
                    energyCostInput = it
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

            Spacer(modifier = Modifier.height(24.dp))

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
                    text = stringResource(Res.string.save),
                    onClick = {
                        val finalCost = energyCostInput.toIntOrNull()
                        if (finalCost != null && !isError) {
                            onSave(finalCost)
                            onDismiss()
                        }
                    },
                    isPrimary = true,
                    enabled = !isError, // Кнопка неактивна при ошибке
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
