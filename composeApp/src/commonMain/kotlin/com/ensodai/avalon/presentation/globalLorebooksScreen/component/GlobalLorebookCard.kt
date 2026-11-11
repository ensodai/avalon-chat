package com.ensodai.avalon.presentation.globalLorebooksScreen.component

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.character_lorebooks_badge_my
import com.ensodai.avalon.generated.resources.character_lorebooks_entries_count
import com.ensodai.avalon.generated.resources.global_lorebooks_author_community
import com.ensodai.avalon.generated.resources.global_lorebooks_author_format
import com.ensodai.avalon.generated.resources.global_lorebooks_btn_in_library
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.LorebookCoverImage
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlobalLorebookCard(
    lorebook: UserLorebookSummaryDto,
    onClick: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
    onToggleSave: ((Uuid, Boolean) -> Unit)? = null,
    onFork: ((Uuid) -> Unit)? = null
) {
    val colors = AvalonTheme.colors

    GemCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(lorebook.id) },
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LorebookCoverImage(
                coverUrl = lorebook.avatarUrl,
                lorebookId = lorebook.id.toString(),
                titleInitial = lorebook.name,
                modifier = Modifier
                    .width(54.dp)
                    .height(72.dp),
                cornerRadius = 8.dp,
                iconSize = 20.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lorebook.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val author = lorebook.creatorName ?: stringResource(Res.string.global_lorebooks_author_community)
                Text(
                    text = stringResource(Res.string.global_lorebooks_author_format, author),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!lorebook.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = lorebook.description!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (lorebook.isOwner) {
                        GlobalCardBadge(
                            text = stringResource(Res.string.character_lorebooks_badge_my).uppercase(),
                            containerColor = colors.background.copy(alpha = 0.6f),
                            contentColor = colors.textMain,
                            borderColor = colors.textMain.copy(alpha = 0.35f)
                        )
                    } else if (lorebook.isSavedToLibrary) {
                        GlobalCardBadge(
                            text = stringResource(Res.string.global_lorebooks_btn_in_library).uppercase(),
                            containerColor = colors.background.copy(alpha = 0.6f),
                            contentColor = colors.textSecondary,
                            borderColor = colors.strokeSubtle
                        )
                    }

                    GlobalCardBadge(
                        text = stringResource(Res.string.character_lorebooks_entries_count, lorebook.entriesCount),
                        containerColor = colors.background.copy(alpha = 0.5f),
                        contentColor = colors.placeholderText
                    )
                }
            }

            Icon(
                imageVector = IconsManager.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun GlobalCardBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(5.dp))
                } else Modifier
            )
            .background(containerColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
