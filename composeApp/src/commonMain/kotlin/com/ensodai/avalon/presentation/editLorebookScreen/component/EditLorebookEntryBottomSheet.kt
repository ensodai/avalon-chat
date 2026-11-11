package com.ensodai.avalon.presentation.editLorebookScreen.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.LorebookEntryPosition
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.lorebook_edit_entry_title
import com.ensodai.avalon.generated.resources.lorebook_entry_comment_label
import com.ensodai.avalon.generated.resources.lorebook_entry_comment_placeholder
import com.ensodai.avalon.generated.resources.lorebook_entry_constant
import com.ensodai.avalon.generated.resources.lorebook_entry_constant_desc
import com.ensodai.avalon.generated.resources.lorebook_entry_content_label
import com.ensodai.avalon.generated.resources.lorebook_entry_content_placeholder
import com.ensodai.avalon.generated.resources.lorebook_entry_keys_label
import com.ensodai.avalon.generated.resources.lorebook_entry_keys_placeholder
import com.ensodai.avalon.generated.resources.lorebook_entry_order_label
import com.ensodai.avalon.generated.resources.lorebook_entry_position_after
import com.ensodai.avalon.generated.resources.lorebook_entry_position_at_depth
import com.ensodai.avalon.generated.resources.lorebook_entry_position_before
import com.ensodai.avalon.generated.resources.lorebook_entry_position_label
import com.ensodai.avalon.generated.resources.lorebook_entry_position_personality
import com.ensodai.avalon.generated.resources.lorebook_entry_position_scenario
import com.ensodai.avalon.generated.resources.lorebook_entry_regex
import com.ensodai.avalon.generated.resources.lorebook_entry_regex_desc
import com.ensodai.avalon.generated.resources.lorebook_entry_secondary_keys_label
import com.ensodai.avalon.generated.resources.lorebook_entry_secondary_keys_placeholder
import com.ensodai.avalon.generated.resources.lorebook_entry_selective
import com.ensodai.avalon.generated.resources.lorebook_entry_selective_desc
import com.ensodai.avalon.generated.resources.lorebook_new_entry_title
import com.ensodai.avalon.generated.resources.lorebook_save_entry_button
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoTextField
import com.ensodai.avalon.presentation.editLorebookScreen.action.EditLorebookScreenActions
import com.ensodai.avalon.presentation.editLorebookScreen.viewState.EditLorebookScreenViewState
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLorebookEntryBottomSheet(
    viewState: EditLorebookScreenViewState,
    actions: EditLorebookScreenActions,
    modifier: Modifier = Modifier
) {
    if (!viewState.isEntryEditorVisible) return

    val colors = AvalonTheme.colors
    val scrollState = rememberScrollState()
    val isEditing = viewState.editingEntryId != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { actions.onCloseEntryEditor() },
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Text(
                text = stringResource(if (isEditing) Res.string.lorebook_edit_entry_title else Res.string.lorebook_new_entry_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Error notice
            if (viewState.entryEditorError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.error.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = viewState.entryEditorError,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.error
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Title / Comment
            Text(
                text = stringResource(Res.string.lorebook_entry_comment_label),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            TechnoTextField(
                value = viewState.editingComment,
                onValueChange = actions.onEditingCommentChanged,
                placeholder = stringResource(Res.string.lorebook_entry_comment_placeholder),
                maxLength = 80,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Activation Keys
            Text(
                text = stringResource(Res.string.lorebook_entry_keys_label),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            TechnoTextField(
                value = viewState.editingKeys,
                onValueChange = actions.onEditingKeysChanged,
                placeholder = stringResource(Res.string.lorebook_entry_keys_placeholder),
                maxLength = 200,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Content
            Text(
                text = stringResource(Res.string.lorebook_entry_content_label),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            TechnoTextField(
                value = viewState.editingContent,
                onValueChange = actions.onEditingContentChanged,
                placeholder = stringResource(Res.string.lorebook_entry_content_placeholder),
                maxLength = 2000,
                singleLine = false,
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Position (CCv3 spec selector)
            Text(
                text = stringResource(Res.string.lorebook_entry_position_label),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val positions = listOf(
                    LorebookEntryPosition.BEFORE_CHAR to Res.string.lorebook_entry_position_before,
                    LorebookEntryPosition.PERSONALITY to Res.string.lorebook_entry_position_personality,
                    LorebookEntryPosition.SCENARIO to Res.string.lorebook_entry_position_scenario,
                    LorebookEntryPosition.AFTER_CHAR to Res.string.lorebook_entry_position_after,
                    LorebookEntryPosition.AT_DEPTH to Res.string.lorebook_entry_position_at_depth
                )
                positions.forEach { (pos, titleRes) ->
                    LorebookPositionCard(
                        title = stringResource(titleRes),
                        isSelected = viewState.editingPosition == pos,
                        onClick = { actions.onEditingPositionChanged(pos) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switch: Constant
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.lorebook_entry_constant),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMain
                    )
                    Text(
                        text = stringResource(Res.string.lorebook_entry_constant_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = viewState.editingConstant,
                    onCheckedChange = actions.onEditingConstantChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.background,
                        checkedTrackColor = colors.neonAccent,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.cardBackground
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Switch: Use Regex
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.lorebook_entry_regex),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMain
                    )
                    Text(
                        text = stringResource(Res.string.lorebook_entry_regex_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = viewState.editingUseRegex,
                    onCheckedChange = actions.onEditingUseRegexChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.background,
                        checkedTrackColor = colors.neonAccent,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.cardBackground
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Switch: Selective
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.lorebook_entry_selective),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMain
                    )
                    Text(
                        text = stringResource(Res.string.lorebook_entry_selective_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = viewState.editingSelective,
                    onCheckedChange = actions.onEditingSelectiveChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.background,
                        checkedTrackColor = colors.neonAccent,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.cardBackground
                    )
                )
            }

            // Secondary keys input if selective
            if (viewState.editingSelective) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(Res.string.lorebook_entry_secondary_keys_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                TechnoTextField(
                    value = viewState.editingSecondaryKeys,
                    onValueChange = actions.onEditingSecondaryKeysChanged,
                    placeholder = stringResource(Res.string.lorebook_entry_secondary_keys_placeholder),
                    maxLength = 200,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Insertion Order / Priority
            Text(
                text = stringResource(Res.string.lorebook_entry_order_label),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            TechnoTextField(
                value = viewState.editingInsertionOrder.toString(),
                onValueChange = { str ->
                    val num = str.filter { it.isDigit() || it == '-' }.toIntOrNull() ?: 0
                    actions.onEditingInsertionOrderChanged(num)
                },
                placeholder = "0",
                maxLength = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            TechnoButton(
                text = stringResource(Res.string.lorebook_save_entry_button),
                onClick = actions.onSaveEditingEntry,
                isPrimary = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LorebookPositionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) colors.textMain else colors.strokeActive,
        animationSpec = tween(durationMillis = 200),
        label = "lorebook_pos_border"
    )
    val animatedTitleColor by animateColorAsState(
        targetValue = if (isSelected) colors.textMain else colors.textSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "lorebook_pos_title"
    )
    val backgroundColor = colors.overlaySurface.copy(alpha = if (isSelected) 0.14f else 0.04f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = animatedBorderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = animatedTitleColor,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

