package com.ensodai.avalon.presentation.addCharacterScreen.component

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.add_character_lorebook_change
import com.ensodai.avalon.generated.resources.add_character_lorebook_not_selected
import com.ensodai.avalon.generated.resources.add_character_lorebook_stage_desc
import com.ensodai.avalon.generated.resources.add_character_lorebook_stage_title
import com.ensodai.avalon.generated.resources.character_lorebooks_create_dialog_title
import com.ensodai.avalon.generated.resources.character_lorebooks_entries_count
import com.ensodai.avalon.generated.resources.character_lorebooks_library_sheet_title
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.LorebookCoverImage
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddCharacterLorebookStageScreen(
    selectedLorebook: UserLorebookSummaryDto?,
    onOpenAttachBottomSheet: () -> Unit,
    onOpenCreateDialog: () -> Unit,
    onDetachLorebook: () -> Unit
) {
    val colors = AvalonTheme.colors

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.add_character_lorebook_stage_title),
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.add_character_lorebook_stage_desc),
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedLorebook == null) {
                GemCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.add_character_lorebook_not_selected),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        TechnoButton(
                            text = stringResource(Res.string.character_lorebooks_library_sheet_title),
                            onClick = onOpenAttachBottomSheet,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TechnoButton(
                            text = stringResource(Res.string.character_lorebooks_create_dialog_title),
                            onClick = onOpenCreateDialog,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                GemCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LorebookCoverImage(
                                coverUrl = selectedLorebook.avatarUrl,
                                lorebookId = selectedLorebook.id.toString(),
                                titleInitial = selectedLorebook.name,
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(70.dp),
                                cornerRadius = 8.dp,
                                iconSize = 22.dp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = selectedLorebook.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textMain,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (!selectedLorebook.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = selectedLorebook.description.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.background.copy(alpha = 0.5f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = stringResource(
                                            Res.string.character_lorebooks_entries_count,
                                            selectedLorebook.entriesCount
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = onDetachLorebook,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.background.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = IconsManager.Close,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TechnoButton(
                            text = stringResource(Res.string.add_character_lorebook_change),
                            onClick = onOpenAttachBottomSheet,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
