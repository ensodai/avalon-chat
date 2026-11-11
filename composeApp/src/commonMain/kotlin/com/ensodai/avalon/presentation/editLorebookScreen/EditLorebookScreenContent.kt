package com.ensodai.avalon.presentation.editLorebookScreen

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.lorebook_add_entry
import com.ensodai.avalon.generated.resources.lorebook_add_entry_short
import com.ensodai.avalon.generated.resources.lorebook_add_first_entry
import com.ensodai.avalon.generated.resources.lorebook_empty_entries
import com.ensodai.avalon.generated.resources.lorebook_entries_title
import com.ensodai.avalon.generated.resources.lorebook_fork_button
import com.ensodai.avalon.generated.resources.lorebook_readonly_banner
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.editLorebookScreen.action.EditLorebookScreenActions
import com.ensodai.avalon.presentation.editLorebookScreen.component.EditLorebookEntryBottomSheet
import com.ensodai.avalon.presentation.editLorebookScreen.component.EditLorebookScreenTopBar
import com.ensodai.avalon.presentation.editLorebookScreen.component.LorebookEntryCard
import com.ensodai.avalon.presentation.editLorebookScreen.component.LorebookSettingsCard
import com.ensodai.avalon.presentation.editLorebookScreen.viewState.EditLorebookScreenViewState
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditLorebookScreenContent(
    viewState: EditLorebookScreenViewState,
    actions: EditLorebookScreenActions,
    navigation: Navigation,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    val colors = AvalonTheme.colors

    val isBusy = viewState.isSaving || viewState.isCreatingBook

    LaunchedEffect(isBusy) {
        if (isBusy) {
            onDisableSwipeable()
        } else {
            onEnableSwipeable()
        }
    }

    LaunchedEffect(viewState.exportSuccessMessage) {
        if (viewState.exportSuccessMessage != null) {
            delay(3000)
            actions.onDismissExportMessage()
        }
    }

    AvalonBackHandler(isBusy) {
        if (!isBusy) {
            navigation.navigateBack()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            EditLorebookScreenTopBar(
                modifier = Modifier.fillMaxWidth(),
                onBackButtonClick = { navigation.navigateBack() },
                onExportClick = actions.onExportLorebook,
                isExporting = viewState.isExporting
            )
        }
    ) { innerPadding ->
        if (viewState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.neonAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 16.dp + innerPadding.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp + innerPadding.calculateTopPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Global Error Message
                if (viewState.errorMessage != null) {
                    item(key = "lorebook_error_banner") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.error.copy(alpha = 0.15f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = viewState.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.error
                            )
                        }
                    }
                }

                // Export Success Message
                if (viewState.exportSuccessMessage != null) {
                    item(key = "lorebook_export_success_banner") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.neonAccent.copy(alpha = 0.15f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = viewState.exportSuccessMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.neonAccent
                            )
                        }
                    }
                }

                // Read-only banner for external lorebooks
                if (!viewState.isOwner) {
                    item(key = "lorebook_readonly_banner") {
                        GemCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.lorebook_readonly_banner),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TechnoButton(
                                text = stringResource(Res.string.lorebook_fork_button),
                                onClick = actions.onForkLorebook,
                                isPrimary = true,
                                isLoading = viewState.isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // General Book Settings Card
                item(key = "lorebook_settings_card") {
                    LorebookSettingsCard(
                        name = viewState.name,
                        description = viewState.description,
                        scanDepth = viewState.scanDepth,
                        tokenBudget = viewState.tokenBudget,
                        recursiveScanning = viewState.recursiveScanning,
                        isSettingsSaving = viewState.isSettingsSaving,
                        isSettingsSavedMessageVisible = viewState.isSettingsSavedMessageVisible,
                        avatarUrl = viewState.avatarUrl,
                        isUploadingCover = viewState.isUploadingCover,
                        isCreatingBook = viewState.isCreatingBook,
                        isNewBook = false,
                        isPublic = viewState.isPublic,
                        isOwner = viewState.isOwner,
                        onNameChanged = actions.onNameChanged,
                        onDescriptionChanged = actions.onDescriptionChanged,
                        onScanDepthChanged = actions.onScanDepthChanged,
                        onTokenBudgetChanged = actions.onTokenBudgetChanged,
                        onRecursiveScanningChanged = actions.onRecursiveScanningChanged,
                        onIsPublicChanged = actions.onIsPublicChanged,
                        onCoverPicked = actions.onCoverPicked,
                        onRemoveCover = actions.onRemoveCover,
                        onCreateBook = actions.onSaveSettings
                    )
                }

                // Entries Section Header
                item(key = "lorebook_entries_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stringResource(Res.string.lorebook_entries_title)} (${viewState.entries.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textMain
                        )
                        if (viewState.isOwner && viewState.entries.isNotEmpty()) {
                            HeaderActionButton(
                                text = stringResource(Res.string.lorebook_add_entry_short),
                                icon = IconsManager.Add,
                                isPrimary = true,
                                onClick = { actions.onOpenCreateEntry() },
                                modifier = Modifier.height(34.dp)
                            )
                        }
                    }
                }

                // Empty state
                if (viewState.entries.isEmpty()) {
                    item(key = "lorebook_empty_entries_state") {
                        GemCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentPadding = PaddingValues(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = IconsManager.SystemInstruction,
                                    contentDescription = null,
                                    tint = colors.neonAccent.copy(alpha = 0.5f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(Res.string.lorebook_empty_entries),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                if (viewState.isOwner) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TechnoButton(
                                        text = "+ ${stringResource(Res.string.lorebook_add_first_entry)}",
                                        onClick = { actions.onOpenCreateEntry() },
                                        isPrimary = true,
                                        modifier = Modifier.fillMaxWidth(0.85f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Entries List
                itemsIndexed(
                    items = viewState.entries,
                    key = { _, entry -> entry.id.toString() }
                ) { index, entry ->
                    LorebookEntryCard(
                        entry = entry,
                        index = index,
                        onToggleEnabled = { actions.onToggleEntryEnabled(index) },
                        onEditClick = { actions.onOpenEditEntry(index) },
                        onDeleteClick = { actions.onDeleteEntry(index) },
                        isEditable = viewState.isOwner
                    )
                }

                item(key = "lorebook_bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Modal Editor Bottom Sheet
    EditLorebookEntryBottomSheet(
        viewState = viewState,
        actions = actions
    )
}

@Composable
private fun HeaderActionButton(
    text: String,
    icon: ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "btn_scale",
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val bgColor = Color.Transparent
    val borderColor = if (isPrimary) colors.textMain else colors.strokeActive
    val contentColor = if (isPrimary) colors.textMain else colors.textSecondary

    Box(
        modifier = modifier
            .height(34.dp)
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
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
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
