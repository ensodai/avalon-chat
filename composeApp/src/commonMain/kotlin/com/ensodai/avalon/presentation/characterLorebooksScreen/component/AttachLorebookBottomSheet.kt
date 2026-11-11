package com.ensodai.avalon.presentation.characterLorebooksScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.character_lorebooks_already_attached
import com.ensodai.avalon.generated.resources.character_lorebooks_attach_action
import com.ensodai.avalon.generated.resources.character_lorebooks_badge_my
import com.ensodai.avalon.generated.resources.character_lorebooks_badge_public
import com.ensodai.avalon.generated.resources.character_lorebooks_entries_count
import com.ensodai.avalon.generated.resources.character_lorebooks_library_empty
import com.ensodai.avalon.generated.resources.character_lorebooks_library_sheet_title
import com.ensodai.avalon.generated.resources.lorebook_import_btn
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.LorebookCoverImage
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.theme.AvalonTheme
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachLorebookBottomSheet(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    library: List<UserLorebookSummaryDto>,
    attachedLorebookIds: Set<Uuid>,
    isLoading: Boolean,
    onAttach: (Uuid) -> Unit,
    onImportLorebook: ((PlatformFile) -> Unit)? = null
) {
    if (!isVisible) return

    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filePickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("json")),
        mode = FileKitMode.Single
    ) { file ->
        file?.let { onImportLorebook?.invoke(it) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.character_lorebooks_library_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textMain,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (onImportLorebook != null) {
                    TechnoButton(
                        text = stringResource(Res.string.lorebook_import_btn),
                        onClick = { filePickerLauncher.launch() },
                        isPrimary = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.neonAccent)
                }
            } else if (library.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.character_lorebooks_library_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        if (onImportLorebook != null) {
                            TechnoButton(
                                text = stringResource(Res.string.lorebook_import_btn),
                                onClick = { filePickerLauncher.launch() },
                                isPrimary = true
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(library, key = { it.id.toString() }) { item ->
                        val isAttached = attachedLorebookIds.contains(item.id)

                        GemCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LorebookCoverImage(
                                    coverUrl = item.avatarUrl,
                                    lorebookId = item.id.toString(),
                                    titleInitial = item.name.take(1),
                                    cornerRadius = 8.dp,
                                    iconSize = 18.dp,
                                    modifier = Modifier.size(width = 42.dp, height = 56.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textMain,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (item.isOwner) {
                                            BadgeChip(
                                                text = stringResource(Res.string.character_lorebooks_badge_my),
                                                isAccent = false
                                            )
                                        }
                                        if (item.isPublic) {
                                            BadgeChip(
                                                text = stringResource(Res.string.character_lorebooks_badge_public),
                                                isAccent = false
                                            )
                                        }
                                        BadgeChip(
                                            text = stringResource(Res.string.character_lorebooks_entries_count, item.entriesCount),
                                            isAccent = false
                                        )
                                    }
                                }

                                if (isAttached) {
                                    Text(
                                        text = stringResource(Res.string.character_lorebooks_already_attached),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.textSecondary.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                } else {
                                    TechnoButton(
                                        text = stringResource(Res.string.character_lorebooks_attach_action),
                                        onClick = { onAttach(item.id) },
                                        isPrimary = true,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(
    text: String,
    isAccent: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isAccent) colors.neonAccent.copy(alpha = 0.15f) else colors.surfaceHigh)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isAccent) colors.neonAccent else colors.textSecondary
        )
    }
}
