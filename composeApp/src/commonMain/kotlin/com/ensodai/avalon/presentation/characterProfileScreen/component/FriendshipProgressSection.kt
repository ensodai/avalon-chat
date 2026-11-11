package com.ensodai.avalon.presentation.characterProfileScreen.component

import androidx.compose.foundation.layout.*
import com.ensodai.shared.avalon.relationship.RelationshipLevelCalculator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.generated.resources.*

@Composable
fun FriendshipProgressSection(
    modifier: Modifier = Modifier,
    relationshipLevel: Int,
    experiencePoints: Long,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val maxProgressXp = RelationshipLevelCalculator.getExperienceRequiredForNextLevel(relationshipLevel)
    val progress = (experiencePoints.toFloat() / maxProgressXp).coerceIn(0f, 1f)

    GemCard(
        modifier = modifier.height(110.dp),
        onClick = onClick,
        contentPadding = PaddingValues(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = IconsManager.Star,
                    contentDescription = null,
                    tint = colors.premiumAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.friendship_progress_header),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            // Круговой прогресс-бар по центру
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(38.dp)
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = colors.neonAccent,
                    trackColor = colors.progressTrack,
                    strokeWidth = 3.dp
                )
                Text(
                    text = relationshipLevel.toString(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = colors.textMain
                )
            }

            // XP инфо
            Text(
                text = "$experiencePoints / $maxProgressXp XP",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                maxLines = 1
            )
        }
    }
}
