package com.ensodai.avalon.presentation.summaryExplanationScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.summary_benefit_1_desc
import com.ensodai.avalon.generated.resources.summary_benefit_1_title
import com.ensodai.avalon.generated.resources.summary_benefit_2_desc
import com.ensodai.avalon.generated.resources.summary_benefit_2_title
import com.ensodai.avalon.generated.resources.summary_benefit_3_desc
import com.ensodai.avalon.generated.resources.summary_benefit_3_title
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun SummaryBenefitCards(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BenefitCard(
            icon = IconsManager.Bolt,
            iconTint = Color(0xFFFFD54F),
            title = stringResource(Res.string.summary_benefit_1_title),
            description = stringResource(Res.string.summary_benefit_1_desc)
        )

        BenefitCard(
            icon = IconsManager.Psychology,
            iconTint = Color(0xFFCE93D8),
            title = stringResource(Res.string.summary_benefit_2_title),
            description = stringResource(Res.string.summary_benefit_2_desc)
        )

        BenefitCard(
            icon = IconsManager.GeminiIcon,
            iconTint = Color(0xFF81D4FA),
            title = stringResource(Res.string.summary_benefit_3_title),
            description = stringResource(Res.string.summary_benefit_3_desc)
        )
    }
}

@Composable
private fun BenefitCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    val colors = AvalonTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.strokeSubtle.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .background(colors.overlaySurface.copy(alpha = 0.05f))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
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
