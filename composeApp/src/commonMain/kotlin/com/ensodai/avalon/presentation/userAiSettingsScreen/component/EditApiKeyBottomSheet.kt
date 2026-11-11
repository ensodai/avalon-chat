package com.ensodai.avalon.presentation.userAiSettingsScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.user_ai_api_key
import com.ensodai.avalon.generated.resources.user_ai_api_key_input_hint
import com.ensodai.avalon.generated.resources.user_ai_api_key_sheet_desc
import com.ensodai.avalon.generated.resources.user_ai_api_key_verifying
import com.ensodai.avalon.generated.resources.user_ai_api_key_verify_and_save
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoInput
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditApiKeyBottomSheet(
    currentApiKey: String,
    isTesting: Boolean,
    sheetTestError: String?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AvalonTheme.colors
    var text by remember { mutableStateOf(currentApiKey) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            if (!isTesting) onDismiss()
        },
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(Res.string.user_ai_api_key),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = stringResource(Res.string.user_ai_api_key_sheet_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TechnoInput(
                value = text,
                onValueChange = { text = it },
                placeholder = stringResource(Res.string.user_ai_api_key_input_hint),
                modifier = Modifier.fillMaxWidth()
            )

            if (sheetTestError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFEF5350).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFEF5350).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = IconsManager.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = sheetTestError,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFEF5350)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TechnoButton(
                text = if (isTesting) stringResource(Res.string.user_ai_api_key_verifying) else stringResource(Res.string.user_ai_api_key_verify_and_save),
                onClick = { onSave(text.trim()) },
                isPrimary = true,
                isLoading = isTesting,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
