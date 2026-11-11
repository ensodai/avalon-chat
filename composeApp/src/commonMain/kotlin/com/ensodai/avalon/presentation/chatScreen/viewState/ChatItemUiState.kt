package com.ensodai.avalon.presentation.chatScreen.viewState

import androidx.compose.runtime.Immutable
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.MessageStatus
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.models.ImagePayload
import com.ensodai.shared.avalon.chat.models.AudioPayload
import com.ensodai.shared.avalon.chat.models.SystemEventPayload
import com.ensodai.shared.avalon.chat.models.SystemEventType
import com.ensodai.avalon.domain.models.chat.WelcomeInfo
import com.ensodai.avalon.domain.models.chat.RpgMessageBlock
import com.ensodai.avalon.domain.models.ChatMessageModel
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import kotlin.uuid.Uuid

@Immutable
sealed interface MessageActionsConfigUiState {
    @Immutable
    data object None : MessageActionsConfigUiState

    @Immutable
    data object Feedback : MessageActionsConfigUiState

    @Immutable
    data class Single(val systemEventType: SystemEventType) : MessageActionsConfigUiState

    @Immutable
    data class Double(val systemEventType: SystemEventType) : MessageActionsConfigUiState
}

@Immutable
data class MessageContainerUiState(
    val id: Uuid,
    val role: MessageRole,
    val rarity: Rarity,
    val status: MessageStatus,
    val isRead: Boolean,
    val bodyItems: List<MessageBodyUiItem>,
    val userReaction: String?,
    val characterReaction: String?,
    val showStatus: Boolean,
    val actionsConfig: MessageActionsConfigUiState,
    val showExternalPlayButton: Boolean,
    val isTtsLoading: Boolean,
    val rawText: String,
    val systemInstructionText: String?,
    val isStreaming: Boolean
)

@Immutable
sealed interface ChatItemUiState {
    val id: String

    @Immutable
    data class WelcomeCard(
        override val id: String,
        val welcomeInfo: WelcomeInfo
    ) : ChatItemUiState

    @Immutable
    data class Message(
        override val id: String,
        val slotId: Uuid,
        val activeContainerId: Uuid,
        val slot: ChatMessageModel,
        val containers: List<MessageContainerUiState>,
        val chatRoleplayPreset: ChatRoleplayPreset
    ) : ChatItemUiState
}

@Immutable
sealed interface MessageBodyUiItem {
    @Immutable
    data class Text(
        val textId: Uuid,
        val rawText: String,
        val parsedBlocks: List<RpgMessageBlock>,
        val isStreaming: Boolean
    ) : MessageBodyUiItem

    @Immutable
    data class Image(
        val imageId: Uuid,
        val payload: ImagePayload
    ) : MessageBodyUiItem

    @Immutable
    data class Audio(
        val audioId: Uuid,
        val payload: AudioPayload,
        val isPlaying: Boolean
    ) : MessageBodyUiItem

    @Immutable
    data class SystemEvent(
        val systemEventId: Uuid,
        val text: String,
        val systemEventType: SystemEventType
    ) : MessageBodyUiItem

    @Immutable
    data class StickerGift(
        val url: String?
    ) : MessageBodyUiItem
}
