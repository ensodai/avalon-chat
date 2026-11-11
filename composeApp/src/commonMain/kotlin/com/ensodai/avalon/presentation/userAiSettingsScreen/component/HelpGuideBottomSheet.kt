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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.user_ai_guide_got_it
import com.ensodai.avalon.generated.resources.user_ai_guide_sheet_desc
import com.ensodai.avalon.generated.resources.user_ai_guide_sheet_title
import com.ensodai.avalon.generated.resources.user_ai_guide_step1_desc
import com.ensodai.avalon.generated.resources.user_ai_guide_step1_title
import com.ensodai.avalon.generated.resources.user_ai_guide_step2_desc
import com.ensodai.avalon.generated.resources.user_ai_guide_step2_title
import com.ensodai.avalon.generated.resources.user_ai_guide_step3_desc
import com.ensodai.avalon.generated.resources.user_ai_guide_step3_title
import com.ensodai.avalon.generated.resources.user_ai_guide_step4_desc
import com.ensodai.avalon.generated.resources.user_ai_guide_step4_title
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpGuideBottomSheet(
    onDismiss: () -> Unit
) {
    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                .padding(bottom = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(Res.string.user_ai_guide_sheet_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = stringResource(Res.string.user_ai_guide_sheet_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            GuideStepCard(
                icon = IconsManager.Hub,
                title = stringResource(Res.string.user_ai_guide_step1_title),
                description = stringResource(Res.string.user_ai_guide_step1_desc)
            )

            Spacer(modifier = Modifier.height(12.dp))

            GuideStepCard(
                icon = IconsManager.Fingerprint,
                title = stringResource(Res.string.user_ai_guide_step2_title),
                description = stringResource(Res.string.user_ai_guide_step2_desc)
            )

            Spacer(modifier = Modifier.height(12.dp))

            GuideStepCard(
                icon = IconsManager.Settings,
                title = stringResource(Res.string.user_ai_guide_step3_title),
                description = stringResource(Res.string.user_ai_guide_step3_desc)
            )

            Spacer(modifier = Modifier.height(12.dp))

            GuideStepCard(
                icon = IconsManager.GeminiIcon,
                title = stringResource(Res.string.user_ai_guide_step4_title),
                description = stringResource(Res.string.user_ai_guide_step4_desc)
            )

            Spacer(modifier = Modifier.height(24.dp))

            TechnoButton(
                text = stringResource(Res.string.user_ai_guide_got_it),
                onClick = onDismiss,
                isPrimary = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GuideStepCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    val colors = AvalonTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colors.cardBackground,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = colors.strokeSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = colors.neonAccent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.neonAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textMain
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = colors.textSecondary
                )
            }
        }
    }
}
