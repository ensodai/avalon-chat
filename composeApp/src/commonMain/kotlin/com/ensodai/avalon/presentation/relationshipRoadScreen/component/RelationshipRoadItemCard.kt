package com.ensodai.avalon.presentation.relationshipRoadScreen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.character.models.CharacterEventRoadmapDto
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.relationshipRoadScreen.model.getSystemRewardForLevel
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

@Composable
fun RelationshipRoadItemCard(
    item: CharacterEventRoadmapDto,
    currentLevel: Int,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val isLevelReached = currentLevel >= item.level
    var isExpanded by remember { mutableStateOf(false) }
    val systemReward = getSystemRewardForLevel(item.level)
    val hasAnyReward = item.hasReward || systemReward != null || item.rewardPhoto != null

    GemCard(
        onClick = if (hasAnyReward) {
            { isExpanded = !isExpanded }
        } else null,
        contentPadding = PaddingValues(14.dp),
        modifier = modifier
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Круглый индикатор уровня
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLevelReached) colors.neonAccent else colors.surfaceHigh
                        )
                        .border(
                            width = 1.dp,
                            color = if (isLevelReached) colors.neonAccent else colors.strokeSubtle,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.level.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isLevelReached) colors.background else colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Информация о статусе уровня
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(Res.string.relationship_road_level_item, item.level),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isLevelReached) colors.textMain else colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLevelReached) IconsManager.CheckCircle else IconsManager.Lock,
                            contentDescription = null,
                            tint = if (isLevelReached) colors.successAccent else colors.textSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLevelReached) stringResource(Res.string.relationship_road_reached) else stringResource(Res.string.relationship_road_locked),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isLevelReached) colors.successAccent else colors.textSecondary.copy(alpha = 0.7f)
                        )
                    }
                }

                // Награда
                if (hasAnyReward) {
                    val rewardIcon = when {
                        item.hasReward && systemReward != null -> IconsManager.Gift
                        item.hasReward -> IconsManager.Gift
                        else -> systemReward?.icon ?: IconsManager.Bolt
                    }
                    val rewardTitle = when {
                        item.hasReward && systemReward != null -> stringResource(Res.string.relationship_road_two_rewards)
                        item.hasReward -> stringResource(Res.string.relationship_road_reward_story)
                        else -> stringResource(Res.string.relationship_road_reward_bonus)
                    }
                    val rewardColor = when {
                        item.hasReward -> colors.premiumAccent
                        else -> colors.neonAccent
                    }
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = rewardIcon,
                                contentDescription = null,
                                tint = rewardColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = rewardTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = rewardColor
                            )
                        }
                        Text(
                            text = if (isExpanded) stringResource(Res.string.relationship_road_hide) else stringResource(Res.string.relationship_road_more),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.neonAccent
                        )
                    }
                } else {
                    Text(
                        text = stringResource(Res.string.relationship_road_no_rewards),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary.copy(alpha = 0.4f)
                    )
                }
            }

            // Раскрывающийся блок с информацией о награде
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(
                        color = colors.strokeSubtle,
                        thickness = 1.dp
                    )

                    // Блок 1: Системная награда
                    if (systemReward != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = systemReward.icon,
                                contentDescription = null,
                                tint = colors.neonAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(systemReward.title),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.neonAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(systemReward.description),
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = colors.textMain.copy(alpha = 0.9f)
                        )
                    }

                    // Разделитель между системной наградой и сюжетным событием
                    if (systemReward != null && item.hasReward) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(
                            color = colors.strokeSubtle.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                    }

                    // Блок 2: Сюжетное событие
                    if (item.hasReward) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(Res.string.relationship_road_curiosity_event_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.premiumAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val scenario = item.triggerScenario
                        if (item.isUnlocked && scenario != null) {
                            Text(
                                text = scenario,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 20.sp
                                ),
                                color = colors.textMain.copy(alpha = 0.9f)
                            )
                        } else {
                            Text(
                                text = stringResource(Res.string.relationship_road_curiosity_event_locked),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Блок 3: Фото-награда
                    val photo = item.rewardPhoto
                    if (photo != null) {
                        if (systemReward != null || item.hasReward) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(
                                color = colors.strokeSubtle.copy(alpha = 0.5f),
                                thickness = 0.5.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "ФОТО-НАГРАДА",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.neonAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage(
                                model = if (photo.isUnlocked) photo.imageUrl else "",
                                seed = photo.id.toString(),
                                blurhash = photo.thumbHash,
                                modifier = Modifier.fillMaxSize()
                            )

                            if (!photo.isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f))
                                )
                                Icon(
                                    imageVector = IconsManager.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
