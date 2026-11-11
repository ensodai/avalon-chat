package com.ensodai.avalon.presentation.userAccountScreen.component.bottomSheet

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
import com.ensodai.avalon.generated.resources.supporter_badge_tag
import com.ensodai.avalon.generated.resources.supporter_gratitude_body
import com.ensodai.avalon.generated.resources.supporter_gratitude_btn
import com.ensodai.avalon.generated.resources.supporter_gratitude_title
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupporterGratitudeBottomSheet(
    onDismiss: () -> Unit
) {
    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val goldAccent = Color(0xFFFFD54F)
    val amberAccent = Color(0xFFFFB300)

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
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                goldAccent.copy(alpha = 0.35f),
                                amberAccent.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.5.dp, goldAccent.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconsManager.Star,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = goldAccent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Бейдж
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(goldAccent.copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.supporter_badge_tag).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = goldAccent
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Заголовок
            Text(
                text = stringResource(Res.string.supporter_gratitude_title),
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
                    .background(colors.surfaceHigh.copy(alpha = 0.4f))
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.supporter_gratitude_body),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp
                    ),
                    color = colors.textSecondary,
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка
            TechnoButton(
                text = stringResource(Res.string.supporter_gratitude_btn),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = true,
                primaryColor = goldAccent,
                onPrimaryColor = Color(0xFF1A1A1A)
            )
        }
    }
}
