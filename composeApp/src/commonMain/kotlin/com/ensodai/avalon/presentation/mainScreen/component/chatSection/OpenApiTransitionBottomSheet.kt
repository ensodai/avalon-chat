package com.ensodai.avalon.presentation.mainScreen.component.chatSection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.announcement_openapi_sheet_body
import com.ensodai.avalon.generated.resources.announcement_openapi_sheet_btn_close
import com.ensodai.avalon.generated.resources.announcement_openapi_sheet_btn_settings
import com.ensodai.avalon.generated.resources.announcement_openapi_sheet_title
import com.ensodai.avalon.generated.resources.announcement_openapi_tag
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenApiTransitionBottomSheet(
    onDismiss: () -> Unit,
    onOpenAiSettings: () -> Unit
) {
    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val accentColor = colors.infoAccent

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = colors.overlaySurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Иконка
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.35f),
                                colors.premiumAccent.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.5.dp, accentColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconsManager.GeminiIcon,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Тэг
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.announcement_openapi_tag).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Заголовок
            Text(
                text = stringResource(Res.string.announcement_openapi_sheet_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colors.textMain,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Текст обращения
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceHigh.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.announcement_openapi_sheet_body),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp
                    ),
                    color = colors.textSecondary,
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Кнопка перехода в настройки ИИ
            TechnoButton(
                text = stringResource(Res.string.announcement_openapi_sheet_btn_settings),
                onClick = onOpenAiSettings,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка "Понятно" / закрыть
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.announcement_openapi_sheet_btn_close),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary
                )
            }
        }
    }
}
