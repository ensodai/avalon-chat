package com.ensodai.avalon.presentation.characterLorebooksScreen.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.CharacterLorebookBindingDto
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.character_lorebooks_badge_my
import com.ensodai.avalon.generated.resources.character_lorebooks_badge_primary
import com.ensodai.avalon.generated.resources.character_lorebooks_badge_public
import com.ensodai.avalon.generated.resources.character_lorebooks_detach
import com.ensodai.avalon.generated.resources.character_lorebooks_edit
import com.ensodai.avalon.generated.resources.character_lorebooks_entries_count
import com.ensodai.avalon.generated.resources.character_lorebooks_make_primary
import com.ensodai.avalon.generated.resources.character_lorebooks_view_readonly
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.LorebookCoverImage
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterLorebookItemCard(
    binding: CharacterLorebookBindingDto,
    onToggleEnabled: (Uuid, Boolean) -> Unit,
    onSetPrimary: (Uuid) -> Unit,
    onDetach: (Uuid) -> Unit,
    onOpenDetails: (Uuid) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors

    GemCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenDetails(binding.lorebookId) },
        contentPadding = PaddingValues(14.dp)
    ) {
        // Top row: Cover + Title + Author + Enable switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LorebookCoverImage(
                    coverUrl = binding.avatarUrl,
                    lorebookId = binding.lorebookId.toString(),
                    titleInitial = binding.name,
                    modifier = Modifier
                        .width(42.dp)
                        .height(56.dp),
                    cornerRadius = 8.dp,
                    iconSize = 18.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = binding.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (binding.isEnabled) colors.textMain else colors.textSecondary.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!binding.isOwner && binding.creatorName != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "by @${binding.creatorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = binding.isEnabled,
                onCheckedChange = { onToggleEnabled(binding.lorebookId, it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.background,
                    checkedTrackColor = colors.neonAccent,
                    uncheckedThumbColor = colors.textSecondary,
                    uncheckedTrackColor = colors.cardBackground
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Badges row: distinct semantic styling
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (binding.isPrimary) {
                BadgeChip(
                    text = stringResource(Res.string.character_lorebooks_badge_primary),
                    textColor = colors.neonAccent,
                    backgroundColor = colors.neonAccent.copy(alpha = 0.15f),
                    borderColor = colors.neonAccent.copy(alpha = 0.4f),
                    icon = IconsManager.Star
                )
            }
            if (binding.isOwner) {
                BadgeChip(
                    text = stringResource(Res.string.character_lorebooks_badge_my),
                    textColor = colors.textMain,
                    backgroundColor = colors.cardBackground,
                    borderColor = colors.strokeActive,
                    icon = IconsManager.Person
                )
            }
            if (binding.isPublic) {
                BadgeChip(
                    text = stringResource(Res.string.character_lorebooks_badge_public),
                    textColor = colors.successAccent,
                    backgroundColor = colors.successAccent.copy(alpha = 0.12f),
                    borderColor = colors.successAccent.copy(alpha = 0.35f),
                    icon = IconsManager.Public
                )
            }
            BadgeChip(
                text = stringResource(Res.string.character_lorebooks_entries_count, binding.entriesCount),
                textColor = colors.textSecondary,
                backgroundColor = colors.surfaceHigh,
                borderColor = null,
                icon = IconsManager.Description
            )
        }

        if (!binding.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = binding.description.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom actions row: Make Primary / Edit / Detach
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!binding.isPrimary) {
                    CardActionButton(
                        text = stringResource(Res.string.character_lorebooks_make_primary),
                        icon = IconsManager.Star,
                        onClick = { onSetPrimary(binding.lorebookId) },
                        isAccent = false
                    )
                }

                CardActionButton(
                    text = if (binding.isOwner) {
                        stringResource(Res.string.character_lorebooks_edit)
                    } else {
                        stringResource(Res.string.character_lorebooks_view_readonly)
                    },
                    icon = if (binding.isOwner) IconsManager.Edit else IconsManager.Visibility,
                    onClick = { onOpenDetails(binding.lorebookId) },
                    isAccent = false
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.error.copy(alpha = 0.12f))
                    .border(1.dp, colors.error.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .clickable(onClick = { onDetach(binding.lorebookId) }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconsManager.Delete,
                    contentDescription = stringResource(Res.string.character_lorebooks_detach),
                    tint = colors.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun BadgeChip(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    borderColor: Color?,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(6.dp))
                } else Modifier
            )
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CardActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false
) {
    val colors = AvalonTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "card_btn_scale",
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val contentColor = if (isAccent) colors.neonAccent else colors.textMain
    val bgColor = if (isAccent) colors.neonAccent.copy(alpha = 0.12f) else colors.surfaceHigh
    val borderColor = if (isAccent) colors.neonAccent.copy(alpha = 0.4f) else colors.strokeActive

    Box(
        modifier = modifier
            .height(36.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = contentColor),
                onClick = onClick
            )
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
