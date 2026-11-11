package com.ensodai.avalon.presentation.characterProfileScreen.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.generated.resources.*

@Composable
fun FriendshipOutreachSection(
    modifier: Modifier = Modifier,
    relationshipLevel: Int,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val isUnlocked = relationshipLevel >= 5

    GemCard(
        modifier = modifier
            .height(110.dp)
            .alpha(if (isUnlocked) 1f else 0.6f),
        onClick = { if (isUnlocked) onClick() },
        contentPadding = PaddingValues(10.dp),
        enabled = isUnlocked
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
                    imageVector = if (isUnlocked) IconsManager.Schedule else IconsManager.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) colors.neonAccent else colors.textSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.outreach_settings_section_title),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isUnlocked) colors.textMain else colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            // Центральная иконка
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(38.dp)
            ) {
                if (isUnlocked) {
                    Icon(
                        imageVector = IconsManager.Schedule,
                        contentDescription = null,
                        tint = colors.neonAccent,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = IconsManager.Lock,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Статус / Подпись снизу
            Text(
                text = if (isUnlocked) {
                    stringResource(Res.string.outreach_tap_to_configure)
                } else {
                    stringResource(Res.string.available_from_level_short, 5)
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isUnlocked) colors.neonAccent else colors.textSecondary,
                fontWeight = if (isUnlocked) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}
