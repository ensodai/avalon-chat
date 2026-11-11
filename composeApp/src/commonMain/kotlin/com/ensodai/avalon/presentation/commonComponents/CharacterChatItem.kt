package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.contrastText
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.generated.resources.no_messages_yet
import com.ensodai.avalon.generated.resources.typing
import com.ensodai.avalon.presentation.chatScreen.component.topBar.TypingIndicator
import com.ensodai.avalon.presentation.mainScreen.component.characterChatsSection.ChatImage
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.utils.convertOffsetToDisplayTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant
import kotlin.uuid.Uuid


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CharacterChatItem(
    modifier: Modifier,
    chatName: String,
    chatId: Uuid,
    characterId: Uuid,
    chatImage: String?,
    lastTimeMessage: Instant?,
    lastMessage: String?,
    lastMessageType: AvalonMessageContentType? = null,
    draftMessage: String? = null,
    isTyping: Boolean,
    onSelectChat: (Uuid) -> Unit,
    onEnableSelectionChats: (Uuid) -> Unit,
    isEnableSelectionChats: Boolean,
    isSelected: Boolean,
    unreadCount: Int,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val customRipple = ripple(
        bounded = true
    )
    val colors = com.ensodai.avalon.theme.AvalonTheme.colors
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        onSelectChat(chatId)
                    },
                    onLongClick = {
                        if (!isEnableSelectionChats) {
                            onEnableSelectionChats(chatId)
                        }
                    },
                    indication = customRipple,
                    interactionSource = interactionSource
                )
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
        ) {

            ChatImage(
                isSelected = isSelected,
                chatImage = chatImage,
                avatarSeed = characterId.toString() + chatName
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = chatName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))
                Box {
                    BaseItemAlphaAnimation(isVisible = isTyping) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TypingIndicator(modifier = Modifier.offset(y = 1.dp))
                            Text(
                                modifier = Modifier.padding(start = 4.dp),
                                text = stringResource(Res.string.typing),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textSecondary,
                            )
                        }
                    }
                    BaseItemAlphaAnimation(isVisible = !isTyping) {
                        if (!draftMessage.isNullOrBlank()) {
                            Text(
                                text = stringResource(Res.string.chat_draft) + ": " + draftMessage,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            when (lastMessageType) {
                                AvalonMessageContentType.GIFT, AvalonMessageContentType.STICKER_GIFT -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = IconsManager.Gift,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = colors.textSecondary
                                        )
                                        Text(
                                            modifier = Modifier.padding(start = 4.dp),
                                            text = stringResource(Res.string.chat_item_gift),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                AvalonMessageContentType.IMAGE -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = IconsManager.Image,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = colors.textSecondary
                                        )
                                        Text(
                                            modifier = Modifier.padding(start = 4.dp),
                                            text = stringResource(Res.string.chat_item_photo),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                else -> {
                                    Text(
                                        text = lastMessage
                                            ?: stringResource(Res.string.no_messages_yet),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                if (lastTimeMessage != null && lastMessage != null) {
                    Text(
                        text = lastTimeMessage.convertOffsetToDisplayTime(),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .height(18.dp)
                            .widthIn(min = 18.dp)
                            .background(color = colors.neonAccent, shape = CircleShape)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.neonAccent.contrastText()
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 76.dp),
            color = colors.strokeSubtle
        )
    }
}
