package com.ensodai.avalon.presentation.characterProfileScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.ensodai.shared.avalon.character.models.CharacterLorebookBindingDto
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.character_lorebooks_entries_count
import com.ensodai.avalon.generated.resources.character_profile_badge_canon
import com.ensodai.avalon.generated.resources.character_profile_badge_user
import com.ensodai.avalon.generated.resources.character_profile_lorebooks_add
import com.ensodai.avalon.generated.resources.character_profile_lorebooks_empty
import com.ensodai.avalon.generated.resources.character_profile_lorebooks_manage
import com.ensodai.avalon.generated.resources.character_profile_lorebooks_title
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.LorebookCoverImage
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
fun ProfileLorebooksSection(
    lorebooks: List<CharacterLorebookBindingDto>,
    canManage: Boolean,
    onManageClick: () -> Unit,
    onLorebookClick: (Uuid) -> Unit,
    onDetachLorebook: (Uuid) -> Unit,
    onAddLorebookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors

    GemCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = IconsManager.SystemInstruction,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = stringResource(Res.string.character_profile_lorebooks_title).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )

                if (canManage) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onManageClick)
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.character_profile_lorebooks_manage),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = IconsManager.KeyboardArrowRight,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (lorebooks.isEmpty()) {
                Text(
                    text = stringResource(Res.string.character_profile_lorebooks_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.placeholderText,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    lorebooks.forEachIndexed { index, item ->
                        val isUserAttached = !item.isCanon

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onLorebookClick(item.lorebookId) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LorebookCoverImage(
                                coverUrl = item.avatarUrl,
                                lorebookId = item.lorebookId.toString(),
                                titleInitial = item.name.take(1),
                                cornerRadius = 8.dp,
                                iconSize = 18.dp,
                                modifier = Modifier.size(width = 42.dp, height = 56.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textMain,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (item.isCanon) {
                                        BadgePill(
                                            text = stringResource(Res.string.character_profile_badge_canon).uppercase(),
                                            textColor = colors.textMain,
                                            backgroundColor = colors.background.copy(alpha = 0.6f),
                                            borderColor = colors.textMain.copy(alpha = 0.35f)
                                        )
                                    } else {
                                        BadgePill(
                                            text = stringResource(Res.string.character_profile_badge_user).uppercase(),
                                            textColor = colors.textSecondary,
                                            backgroundColor = colors.background.copy(alpha = 0.6f),
                                            borderColor = colors.strokeSubtle
                                        )
                                    }
                                }

                                if (!item.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = item.description.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = stringResource(Res.string.character_lorebooks_entries_count, item.entriesCount),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.placeholderText
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            if (isUserAttached) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(colors.background.copy(alpha = 0.6f))
                                        .clickable { onDetachLorebook(item.lorebookId) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconsManager.Close,
                                        contentDescription = "Detach lorebook",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = IconsManager.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = colors.textSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (index < lorebooks.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(colors.strokeSubtle.copy(alpha = 0.35f))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Кнопка "Подключить книгу знаний" (белый контурный стиль)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, colors.textMain, RoundedCornerShape(10.dp))
                    .background(Color.Transparent)
                    .clickable(onClick = onAddLorebookClick)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = IconsManager.Add,
                        contentDescription = null,
                        tint = colors.textMain,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(Res.string.character_profile_lorebooks_add),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textMain,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgePill(
    text: String,
    textColor: Color,
    backgroundColor: Color,
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
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
