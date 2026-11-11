package com.ensodai.avalon.presentation.liveCallScreen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.livecall_gender_female
import com.ensodai.avalon.generated.resources.livecall_gender_male
import com.ensodai.avalon.generated.resources.livecall_select_model
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceModelPickerSheet(
    selectedModel: VoiceModel,
    onSelect: (VoiceModel) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AvalonTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.cardBackground
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text(
                        text = stringResource(Res.string.livecall_select_model),
                        fontSize = 18.sp,
                        color = colors.textMain,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                items(VoiceModel.values().size) { index ->
                    val model = VoiceModel.values()[index]
                    VoiceModelItem(
                        model = model,
                        isSelected = model == selectedModel,
                        onClick = {
                            onSelect(model)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.padding(bottom = 32.dp))
                }
            }
        }
    }
}

@Composable
private fun VoiceModelItem(
    model: VoiceModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val genderText = if (model.gender == VoiceModel.Gender.MALE) {
        stringResource(Res.string.livecall_gender_male)
    } else {
        stringResource(Res.string.livecall_gender_female)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${model.emoji}  ${model.modelName}  — ${model.description}  ($genderText)",
            fontSize = 16.sp,
            color = if (isSelected) colors.textMain else colors.textSecondary
        )
    }
}
