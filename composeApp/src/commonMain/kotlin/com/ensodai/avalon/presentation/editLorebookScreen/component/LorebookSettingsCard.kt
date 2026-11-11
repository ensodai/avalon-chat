package com.ensodai.avalon.presentation.editLorebookScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.lorebook_book_settings
import com.ensodai.avalon.generated.resources.lorebook_cover_change
import com.ensodai.avalon.generated.resources.lorebook_cover_delete
import com.ensodai.avalon.generated.resources.lorebook_cover_hint
import com.ensodai.avalon.generated.resources.lorebook_cover_title
import com.ensodai.avalon.generated.resources.lorebook_cover_upload
import com.ensodai.avalon.generated.resources.lorebook_create_book_button
import com.ensodai.avalon.generated.resources.lorebook_description_label
import com.ensodai.avalon.generated.resources.lorebook_description_placeholder
import com.ensodai.avalon.generated.resources.lorebook_name_label
import com.ensodai.avalon.generated.resources.lorebook_name_placeholder
import com.ensodai.avalon.generated.resources.lorebook_public_desc
import com.ensodai.avalon.generated.resources.lorebook_public_label
import com.ensodai.avalon.generated.resources.lorebook_recursive_scanning
import com.ensodai.avalon.generated.resources.lorebook_recursive_scanning_desc
import com.ensodai.avalon.generated.resources.lorebook_save_settings
import com.ensodai.avalon.generated.resources.lorebook_saved
import com.ensodai.avalon.generated.resources.lorebook_saving
import com.ensodai.avalon.generated.resources.lorebook_scan_depth_label
import com.ensodai.avalon.generated.resources.lorebook_token_budget_label
import com.ensodai.avalon.generated.resources.lorebook_token_budget_placeholder
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.LorebookCoverImage
import com.ensodai.avalon.presentation.commonComponents.buttons.CompactTechnoButton
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoTextField
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun LorebookSettingsCard(
    name: String,
    description: String,
    scanDepth: Int,
    tokenBudget: Int?,
    recursiveScanning: Boolean,
    isSettingsSaving: Boolean,
    isSettingsSavedMessageVisible: Boolean,
    avatarUrl: String? = null,
    isUploadingCover: Boolean = false,
    isCreatingBook: Boolean = false,
    isNewBook: Boolean = false,
    isPublic: Boolean = false,
    isOwner: Boolean = true,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onScanDepthChanged: (Int) -> Unit,
    onTokenBudgetChanged: (Int?) -> Unit,
    onRecursiveScanningChanged: (Boolean) -> Unit,
    onIsPublicChanged: (Boolean) -> Unit = {},
    onCoverPicked: (PlatformFile) -> Unit = {},
    onRemoveCover: () -> Unit = {},
    onCreateBook: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Single,
        onResult = { file: PlatformFile? ->
            if (file != null) {
                onCoverPicked(file)
            }
        }
    )

    GemCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.lorebook_book_settings),
                style = MaterialTheme.typography.titleMedium,
                color = colors.neonAccent
            )

            if (isSettingsSaving) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = colors.neonAccent
                    )
                    Text(
                        text = stringResource(Res.string.lorebook_saving),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            } else if (isSettingsSavedMessageVisible) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = IconsManager.Done,
                        contentDescription = null,
                        tint = colors.successAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(Res.string.lorebook_saved),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.successAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cover Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val coverShape = RoundedCornerShape(12.dp)
            val isClickable = isOwner && !isUploadingCover

            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 96.dp)
                    .clip(coverShape)
                    .then(
                        if (avatarUrl == null) {
                            Modifier
                                .background(colors.overlaySurface.copy(alpha = 0.05f))
                                .drawBehind {
                                    val strokeWidth = 1.5.dp.toPx()
                                    val dashWidth = 8.dp.toPx()
                                    val dashGap = 5.dp.toPx()
                                    val cornerRadiusPx = 12.dp.toPx()

                                    drawRoundRect(
                                        color = colors.strokeActive,
                                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                        size = Size(this.size.width - strokeWidth, this.size.height - strokeWidth),
                                        cornerRadius = CornerRadius(cornerRadiusPx - (strokeWidth / 2)),
                                        style = Stroke(
                                            width = strokeWidth,
                                            pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(dashWidth, dashGap),
                                                0f
                                            )
                                        )
                                    )
                                }
                        } else Modifier
                    )
                    .then(
                        if (isClickable) {
                            Modifier.clickable { launcher.launch() }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl != null) {
                    LorebookCoverImage(
                        coverUrl = avatarUrl,
                        lorebookId = name.ifBlank { "lorebook" },
                        titleInitial = name.take(1),
                        cornerRadius = 12.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = IconsManager.AddPhotoAlternate,
                            contentDescription = stringResource(Res.string.lorebook_cover_upload),
                            tint = colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (isUploadingCover) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.background.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = colors.neonAccent
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.lorebook_cover_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textMain
                )
                Text(
                    text = stringResource(Res.string.lorebook_cover_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )

                if (isOwner && avatarUrl != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = !isUploadingCover) { onRemoveCover() }
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = IconsManager.DeleteOutline,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = stringResource(Res.string.lorebook_cover_delete),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Name
        Text(
            text = stringResource(Res.string.lorebook_name_label),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        TechnoTextField(
            value = name,
            onValueChange = onNameChanged,
            placeholder = stringResource(Res.string.lorebook_name_placeholder),
            maxLength = 60,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Description
        Text(
            text = stringResource(Res.string.lorebook_description_label),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        TechnoTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            placeholder = stringResource(Res.string.lorebook_description_placeholder),
            maxLength = 250,
            singleLine = false,
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scan Depth
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.lorebook_scan_depth_label),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Text(
                text = "$scanDepth",
                style = MaterialTheme.typography.labelLarge,
                color = colors.neonAccent
            )
        }
        Slider(
            value = scanDepth.toFloat(),
            onValueChange = { onScanDepthChanged(it.roundToInt()) },
            valueRange = 1f..30f,
            steps = 28,
            colors = SliderDefaults.colors(
                thumbColor = colors.neonAccent,
                activeTrackColor = colors.neonAccent,
                inactiveTrackColor = colors.strokeActive
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Token budget
        Text(
            text = stringResource(Res.string.lorebook_token_budget_label),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        TechnoTextField(
            value = tokenBudget?.toString().orEmpty(),
            onValueChange = { str ->
                val num = str.filter { it.isDigit() }.toIntOrNull()
                onTokenBudgetChanged(num)
            },
            placeholder = stringResource(Res.string.lorebook_token_budget_placeholder),
            maxLength = 6,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Recursive scanning switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.lorebook_recursive_scanning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMain
                )
                Text(
                    text = stringResource(Res.string.lorebook_recursive_scanning_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = recursiveScanning,
                onCheckedChange = onRecursiveScanningChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.background,
                    checkedTrackColor = colors.neonAccent,
                    uncheckedThumbColor = colors.textSecondary,
                    uncheckedTrackColor = colors.cardBackground
                )
            )
        }

        if (isOwner) {
            Spacer(modifier = Modifier.height(16.dp))

            // Public lorebook switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.lorebook_public_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMain
                    )
                    Text(
                        text = stringResource(Res.string.lorebook_public_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isPublic,
                    onCheckedChange = onIsPublicChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.background,
                        checkedTrackColor = colors.neonAccent,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.cardBackground
                    )
                )
            }

            if (isNewBook) {
                Spacer(modifier = Modifier.height(16.dp))

                TechnoButton(
                    text = stringResource(Res.string.lorebook_create_book_button),
                    onClick = onCreateBook,
                    isPrimary = true,
                    isLoading = isCreatingBook,
                    enabled = name.isNotBlank() && !isCreatingBook,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
