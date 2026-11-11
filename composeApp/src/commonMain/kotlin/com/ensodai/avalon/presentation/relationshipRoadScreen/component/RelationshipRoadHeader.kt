package com.ensodai.avalon.presentation.relationshipRoadScreen.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.relationship.RelationshipLevelCalculator
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun RelationshipRoadHeader(
    currentLevel: Int,
    currentXp: Long,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val maxXp = RelationshipLevelCalculator.getExperienceRequiredForNextLevel(currentLevel)
    val progress = (currentXp.toFloat() / maxXp).coerceIn(0f, 1f)

    GemCard(
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(Res.string.relationship_road_progress_title),
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.sp
                ),
                color = colors.textSecondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.relationship_road_level_pattern, currentLevel),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colors.neonAccent
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.relationship_road_current_xp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                Text(
                    text = "$currentXp / $maxXp XP",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.textMain
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = colors.neonAccent,
                trackColor = colors.progressTrack
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.relationship_road_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
