package com.ensodai.avalon.presentation.lorebookDetailsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.character_lorebooks_badge_my
import com.ensodai.avalon.generated.resources.character_lorebooks_badge_public
import com.ensodai.avalon.generated.resources.character_lorebooks_entries_count
import com.ensodai.avalon.generated.resources.global_lorebooks_author_community
import com.ensodai.avalon.generated.resources.global_lorebooks_author_format
import com.ensodai.avalon.generated.resources.global_lorebooks_btn_add_library
import com.ensodai.avalon.generated.resources.global_lorebooks_btn_fork
import com.ensodai.avalon.generated.resources.global_lorebooks_btn_in_library
import com.ensodai.avalon.generated.resources.lorebook_details_edit_action
import com.ensodai.avalon.generated.resources.lorebook_details_entries_title
import com.ensodai.avalon.generated.resources.lorebook_details_no_entries
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.LorebookCoverImage
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogComplaint
import com.ensodai.avalon.presentation.lorebookDetailsScreen.action.LorebookDetailsScreenActions
import com.ensodai.avalon.presentation.lorebookDetailsScreen.component.LorebookDetailsTopBar
import com.ensodai.avalon.presentation.lorebookDetailsScreen.viewState.LorebookDetailsScreenViewState
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LorebookDetailsScreenContent(
    viewState: LorebookDetailsScreenViewState,
    actions: LorebookDetailsScreenActions,
    navigation: Navigation,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors

    AvalonBackHandler(false) {
        navigation.navigateBack()
    }

    LaunchedEffect(viewState.feedbackMessage) {
        if (viewState.feedbackMessage != null) {
            delay(3000)
            actions.onDismissFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        topBar = {
            LorebookDetailsTopBar(
                title = viewState.lorebook?.name ?: "",
                onBackClick = { navigation.navigateBack() },
                onReportClick = actions.onOpenComplaintDialog,
                isOwner = viewState.isOwner,
                isSavedToLibrary = viewState.isSavedToLibrary,
                onRemoveFromLibrary = actions.onToggleSaveToLibrary
            )
        },
        bottomBar = {
            val book = viewState.lorebook
            if (book != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (viewState.isOwner) {
                        // Edit button for owner (standard white primary button)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.textMain)
                                .clickable {
                                    navigation.navigateToEditLorebookScreen(
                                        lorebookId = viewState.lorebookId
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = IconsManager.Edit,
                                    contentDescription = null,
                                    tint = colors.background,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(Res.string.lorebook_details_edit_action),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.background
                                )
                            }
                        }
                    } else {
                        // Non-owner: Save to Library + Fork
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Save / Remove toggle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .then(
                                        if (viewState.isSavedToLibrary) {
                                            Modifier
                                                .background(colors.surfaceHigh.copy(alpha = 0.6f))
                                                .border(1.dp, colors.strokeSubtle, RoundedCornerShape(12.dp))
                                        } else {
                                            Modifier.background(colors.textMain)
                                        }
                                    )
                                    .clickable { actions.onToggleSaveToLibrary() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (viewState.isSavedToLibrary) IconsManager.Check else IconsManager.Add,
                                        contentDescription = null,
                                        tint = if (viewState.isSavedToLibrary) colors.textSecondary else colors.background,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (viewState.isSavedToLibrary)
                                            stringResource(Res.string.global_lorebooks_btn_in_library)
                                        else
                                            stringResource(Res.string.global_lorebooks_btn_add_library),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (viewState.isSavedToLibrary) colors.textSecondary else colors.background
                                    )
                                }
                            }

                            // Fork button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surfaceHigh.copy(alpha = 0.7f))
                                    .border(1.dp, colors.strokeSubtle, RoundedCornerShape(12.dp))
                                    .clickable { actions.onForkBook() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = IconsManager.ContentCopy,
                                        contentDescription = null,
                                        tint = colors.textMain,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(Res.string.global_lorebooks_btn_fork),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textMain
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (viewState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.neonAccent)
            }
        } else {
            val book = viewState.lorebook
            if (book == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "База знаний не найдена",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header card
                    item {
                        GemCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    LorebookCoverImage(
                                        coverUrl = book.avatarUrl,
                                        lorebookId = book.id.toString(),
                                        titleInitial = book.name,
                                        iconSize = 34.dp,
                                        modifier = Modifier
                                            .width(84.dp)
                                            .height(112.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = book.name,
                                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textMain
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        val author = book.creatorName ?: stringResource(Res.string.global_lorebooks_author_community)
                                        Text(
                                            text = stringResource(Res.string.global_lorebooks_author_format, author),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                if (!book.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = book.description!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textSecondary.copy(alpha = 0.9f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (viewState.isOwner) {
                                        BadgeItem(
                                            text = stringResource(Res.string.character_lorebooks_badge_my),
                                            containerColor = colors.neonAccent.copy(alpha = 0.18f),
                                            contentColor = colors.neonAccent
                                        )
                                    } else if (book.isPublic) {
                                        BadgeItem(
                                            text = stringResource(Res.string.character_lorebooks_badge_public),
                                            containerColor = colors.infoAccent.copy(alpha = 0.15f),
                                            contentColor = colors.infoAccent
                                        )
                                    }

                                    BadgeItem(
                                        text = stringResource(Res.string.character_lorebooks_entries_count, book.entries.size),
                                        containerColor = colors.surfaceHigh.copy(alpha = 0.6f),
                                        contentColor = colors.textSecondary
                                    )

                                    if (book.tokenBudget != null) {
                                        BadgeItem(
                                            text = "Бюджет: ${book.tokenBudget} токенов",
                                            containerColor = colors.surfaceHigh.copy(alpha = 0.6f),
                                            contentColor = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Feedback message
                    item {
                        AnimatedVisibility(
                            visible = viewState.feedbackMessage != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            viewState.feedbackMessage?.let { msg ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surfaceHigh.copy(alpha = 0.9f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = colors.neonAccent,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Section title
                    item {
                        Text(
                            text = stringResource(Res.string.lorebook_details_entries_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textMain
                        )
                    }

                    if (book.entries.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(Res.string.lorebook_details_no_entries),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    } else {
                        items(
                            items = book.entries,
                            key = { it.id.toString() }
                        ) { entry ->
                            GemCard(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Keys row
                                    val displayKeys = if (entry.constant) "Постоянная (активна всегда)" else entry.keys.joinToString(", ")
                                    Text(
                                        text = displayKeys,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (entry.constant) colors.neonAccent else colors.textMain,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (!entry.comment.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = entry.comment!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textSecondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = entry.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textSecondary.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    // Complaint Dialog
    AlertDialogComplaint(
        isShow = viewState.isShowComplaintDialog,
        text = viewState.complaintText,
        onChangeText = actions.onComplaintTextChanged,
        onDismiss = actions.onCloseComplaintDialog,
        onConfirm = actions.onSubmitComplaint
    )
}

@Composable
private fun BadgeItem(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = contentColor
        )
    }
}
