package com.ensodai.avalon.presentation.userAiSettingsScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.user_ai_guide_category
import com.ensodai.avalon.generated.resources.user_ai_guide_subtitle
import com.ensodai.avalon.generated.resources.user_ai_guide_title
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun HelpGuideCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors

    GemCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onClick,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = colors.neonAccent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconsManager.TipsAndUpdates,
                    contentDescription = null,
                    tint = colors.neonAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.user_ai_guide_category),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp
                    ),
                    color = colors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(Res.string.user_ai_guide_title),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = colors.textMain
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(Res.string.user_ai_guide_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = IconsManager.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.strokeSubtle,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
