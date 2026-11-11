package com.ensodai.avalon.presentation.liveCallScreen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.liveCall.models.Language
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.livecall_select_language
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerSheet(
    selectedLanguage: Language,
    onSelect: (Language) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AvalonTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.cardBackground
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.livecall_select_language),
                fontSize = 18.sp,
                color = colors.textMain,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Language.values().forEach { language ->
                LanguageItem(
                    language = language,
                    isSelected = language == selectedLanguage,
                    onClick = {
                        onSelect(language)
                    }
                )
            }

            // Spacer для безопасной зоны внизу
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${language.flag}  ${language.code}  ${language.displayName}",
            fontSize = 16.sp,
            color = if (isSelected) colors.textMain else colors.textSecondary
        )
    }
}
