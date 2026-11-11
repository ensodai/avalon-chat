package com.ensodai.avalon.presentation.editLorebookScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.CharacterLorebookEntryItemDto
import com.ensodai.shared.avalon.character.models.LorebookEntryPosition
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.lorebook_badge_constant
import com.ensodai.avalon.generated.resources.lorebook_badge_pos_after
import com.ensodai.avalon.generated.resources.lorebook_badge_priority
import com.ensodai.avalon.generated.resources.lorebook_badge_regex
import com.ensodai.avalon.generated.resources.lorebook_badge_selective
import com.ensodai.avalon.generated.resources.lorebook_entry_position_after
import com.ensodai.avalon.generated.resources.lorebook_entry_position_at_depth
import com.ensodai.avalon.generated.resources.lorebook_entry_position_personality
import com.ensodai.avalon.generated.resources.lorebook_entry_position_scenario
import com.ensodai.avalon.generated.resources.lorebook_delete_entry
import com.ensodai.avalon.generated.resources.lorebook_edit_entry_action
import com.ensodai.avalon.generated.resources.lorebook_entry_constant_no_keys
import com.ensodai.avalon.generated.resources.lorebook_entry_fallback_key
import com.ensodai.avalon.generated.resources.lorebook_entry_fallback_title
import com.ensodai.avalon.generated.resources.lorebook_entry_keys_section_title
import com.ensodai.avalon.generated.resources.lorebook_entry_secondary_keys_section_title
import com.ensodai.avalon.generated.resources.lorebook_entry_symbols_count
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LorebookEntryCard(
    entry: CharacterLorebookEntryItemDto,
    index: Int,
    onToggleEnabled: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isEditable: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val contentAlpha = if (entry.enabled) 1f else 0.5f

    val title = entry.comment?.ifBlank { null }
        ?: entry.keys.firstOrNull()?.let { stringResource(Res.string.lorebook_entry_fallback_key, it) }
        ?: stringResource(Res.string.lorebook_entry_fallback_title, index + 1)

    val hasBadges = entry.constant ||
        entry.position != LorebookEntryPosition.BEFORE_CHAR ||
        entry.selective ||
        entry.useRegex ||
        entry.insertionOrder != 0

    GemCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isEditable) { onEditClick() },
        contentPadding = PaddingValues(16.dp)
    ) {
        // Top row: Index badge + Title + Enabled Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Index pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.surfaceHigh)
                        .border(1.dp, colors.strokeActive.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.neonAccent
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = entry.enabled,
                onCheckedChange = { if (isEditable) onToggleEnabled() },
                enabled = isEditable,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.background,
                    checkedTrackColor = colors.neonAccent,
                    uncheckedThumbColor = colors.textSecondary,
                    uncheckedTrackColor = colors.cardBackground
                )
            )
        }

        // Meta badges row (FlowRow)
        if (hasBadges) {
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Constant flag (Emerald green)
                if (entry.constant) {
                    EntryBadgeChip(
                        text = stringResource(Res.string.lorebook_badge_constant),
                        textColor = colors.successAccent,
                        backgroundColor = colors.successAccent.copy(alpha = 0.12f),
                        borderColor = colors.successAccent.copy(alpha = 0.35f),
                        icon = IconsManager.Bolt
                    )
                }

                // Insertion Position: show badge if different from default BEFORE_CHAR
                if (entry.position != LorebookEntryPosition.BEFORE_CHAR) {
                    val posText = when (entry.position) {
                        LorebookEntryPosition.AFTER_CHAR -> stringResource(Res.string.lorebook_entry_position_after)
                        LorebookEntryPosition.PERSONALITY -> stringResource(Res.string.lorebook_entry_position_personality)
                        LorebookEntryPosition.SCENARIO -> stringResource(Res.string.lorebook_entry_position_scenario)
                        LorebookEntryPosition.AT_DEPTH -> stringResource(Res.string.lorebook_entry_position_at_depth)
                        else -> null
                    }
                    if (posText != null) {
                        EntryBadgeChip(
                            text = posText,
                            textColor = colors.neonAccent,
                            backgroundColor = colors.neonAccent.copy(alpha = 0.12f),
                            borderColor = colors.neonAccent.copy(alpha = 0.35f),
                            icon = IconsManager.KeyboardArrowDown
                        )
                    }
                }

                // Selective flag
                if (entry.selective) {
                    EntryBadgeChip(
                        text = stringResource(Res.string.lorebook_badge_selective),
                        textColor = colors.textSecondary,
                        backgroundColor = colors.surfaceHigh,
                        borderColor = colors.strokeActive.copy(alpha = 0.5f),
                        icon = IconsManager.Tune
                    )
                }

                // Regex flag
                if (entry.useRegex) {
                    EntryBadgeChip(
                        text = stringResource(Res.string.lorebook_badge_regex),
                        textColor = colors.textSecondary,
                        backgroundColor = colors.surfaceHigh,
                        borderColor = colors.strokeActive.copy(alpha = 0.5f),
                        icon = IconsManager.Code
                    )
                }

                // Custom priority
                if (entry.insertionOrder != 0) {
                    EntryBadgeChip(
                        text = stringResource(Res.string.lorebook_badge_priority, entry.insertionOrder),
                        textColor = colors.textSecondary,
                        backgroundColor = colors.surfaceHigh,
                        borderColor = colors.strokeActive.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Activation Keys section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(contentAlpha)
        ) {
            if (entry.keys.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = IconsManager.Search,
                        contentDescription = null,
                        tint = colors.neonAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = stringResource(Res.string.lorebook_entry_keys_section_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.neonAccent
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entry.keys.forEach { key ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surfaceHigh)
                                .border(1.dp, colors.strokeActive.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMain
                            )
                        }
                    }
                }
            } else if (entry.constant) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = IconsManager.Done,
                        contentDescription = null,
                        tint = colors.successAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = stringResource(Res.string.lorebook_entry_constant_no_keys),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }

            // Secondary Keys (if selective and present)
            if (!entry.secondaryKeys.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = IconsManager.Tune,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = stringResource(Res.string.lorebook_entry_secondary_keys_section_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entry.secondaryKeys.orEmpty().forEach { key ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.cardBackground)
                                .border(1.dp, colors.strokeActive.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Snippet Box (Styled lore block)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(contentAlpha)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceHigh.copy(alpha = 0.45f))
                .border(1.dp, colors.strokeActive.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom row: Character count info + Action buttons (Edit & Delete)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Char count label
            Row(
                modifier = Modifier.alpha(contentAlpha),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = IconsManager.Description,
                    contentDescription = null,
                    tint = colors.textSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = stringResource(Res.string.lorebook_entry_symbols_count, entry.content.length),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary.copy(alpha = 0.7f)
                )
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Edit action button
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceHigh)
                        .border(1.dp, colors.strokeActive, RoundedCornerShape(8.dp))
                        .clickable(onClick = onEditClick)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = IconsManager.Edit,
                            contentDescription = null,
                            tint = colors.textMain,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = stringResource(Res.string.lorebook_edit_entry_action),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = colors.textMain
                        )
                    }
                }

                // Delete action button
                if (isEditable) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.error.copy(alpha = 0.12f))
                            .border(1.dp, colors.error.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .clickable(onClick = onDeleteClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconsManager.Delete,
                            contentDescription = stringResource(Res.string.lorebook_delete_entry),
                            tint = colors.error,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryBadgeChip(
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
