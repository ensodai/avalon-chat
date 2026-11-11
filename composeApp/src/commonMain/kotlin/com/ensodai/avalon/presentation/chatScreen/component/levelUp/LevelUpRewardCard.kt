package com.ensodai.avalon.presentation.chatScreen.component.levelUp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.relationshipRoadScreen.model.SystemReward
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun LevelUpRewardCard(
    reward: SystemReward,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors

    // Золотисто-премиальный градиент для обводки
    val goldGradient = Brush.linearGradient(
        colors = listOf(
            colors.premiumAccent,
            colors.premiumAccent.copy(alpha = 0.4f),
            colors.neonAccent.copy(alpha = 0.5f),
            colors.premiumAccent
        )
    )

    // Мягкое золотистое свечение внутри круга иконки
    val iconBgGradient = Brush.radialGradient(
        colors = listOf(
            colors.premiumAccent.copy(alpha = 0.25f),
            colors.premiumAccent.copy(alpha = 0.05f)
        )
    )

    GemCard(
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.2.dp, goldGradient, RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Круглый контейнер с золотистым градиентом и векторной иконкой награды
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgGradient)
                    .border(1.5.dp, colors.premiumAccent.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = reward.icon,
                    contentDescription = null,
                    tint = colors.premiumAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Премиальный золотой бэдж
                Text(
                    text = "РАЗБЛОКИРОВАНА НАГРАДА",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = colors.premiumAccent
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Название награды
                Text(
                    text = stringResource(reward.title),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = colors.textMain
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Описание бонуса
                Text(
                    text = stringResource(reward.description),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 16.sp
                    ),
                    color = colors.textSecondary
                )
            }
        }
    }
}
