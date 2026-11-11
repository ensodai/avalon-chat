package com.ensodai.avalon.presentation.chatScreen.action

import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.shared.avalon.gifts.models.Gift
import com.ensodai.avalon.domain.models.ChatMessageModel
import com.ensodai.avalon.domain.models.MessageContainerModel
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import kotlin.uuid.Uuid

data class ChatScreenActions(
    val messageActions: MessageActions,
    val messageInputActions: MessageInputActions,
    val chatManagementActions: ChatManagementActions,
    val dialogActions: DialogActions,
    val searchActions: SearchActions,
    val messageVariantActions: MessageVariantActions
)

// Действия, связанные с конкретными сообщениями в списке
data class MessageActions(
    val onSelectMessage: (messageContainer: MessageContainerModel) -> Unit,
    val onEnableSelectionMessageMode: (messageContainer: MessageContainerModel) -> Unit,
    val onDisableSelectionMessageMode: () -> Unit,
    val onDeleteSelectedMessage: () -> Unit,
    val onEnableEditMessageMode: () -> Unit,
    val onDisableEditMessageMode: () -> Unit,
    val onEditMessage: (String) -> Unit,
    val onChangeEditedSystemInstruction: (String) -> Unit,
    val onSaveEditedMessage: () -> Unit,
    val onToggleReaction: (reaction: String?, messageContainerId: Uuid?, slotId: Uuid) -> Unit,
    val onImageClick: (contentId: Uuid, slot: ChatMessageModel) -> Unit,
    val onMessagesVisible: (List<Uuid>) -> Unit,
    val onSendOutreachFeedback: (isPositive: Boolean, containerId: Uuid) -> Unit,
    val onPlaySpeech: (contentId: Uuid, voice: VoiceModel?) -> Unit,
    val onPauseSpeech: () -> Unit,
    val onBuyEnergyClick: () -> Unit
)

// Действия, связанные с полем ввода внизу экрана
data class MessageInputActions(
    val onSendMessageClick: (String) -> Unit,
    val onChangeInputUserMessage: (String) -> Unit,
    val onForceGenerateResponse: () -> Unit,
    val onRefreshGeneratedMessage: () -> Unit,
    val onStopGenerateMessage: () -> Unit,
    val onAttachImages: (List<CharacterImageDTO>) -> Unit,
    val onDetachImages: (CharacterImageDTO) -> Unit,
    val onToggleBottomSheetMenu: () -> Unit,
    val onSetInstructionMode: (Boolean) -> Unit,
    val onSendGift: (Gift) -> Unit,
    val onEnableGiftBottomSheetMode: () -> Unit,
    val onSetSystemInstruction: (String) -> Unit,
    val onClearSystemInstruction: () -> Unit
)

// Действия, управляющие всем чатом
data class ChatManagementActions(
    val onDeleteAllChatMessages: () -> Unit,
    val onDeleteChatConfigWithMessages: () -> Unit
)

// Действия по показу/скрытию диалоговых окон
data class DialogActions(
    val onCloseAlertDialog: () -> Unit,
    val onShowAlertDialogDeleteMessage: () -> Unit,
    val onShowAlertDialogComplaint: () -> Unit,
    val onChangeReportMessage: (String) -> Unit,
    val onShowPermissionDialog: () -> Unit,
    val onDismissFullscreenImage: () -> Unit,
    val onLevelUpAnimationComplete: () -> Unit,
    val onGiftEffectFinished: () -> Unit
)

// Все, что связано с поиском по сообщениям
data class SearchActions(
    val onEnableSearchMessageMode: () -> Unit,
    val onDisableSearchMessageMode: () -> Unit,
    val onChangeInputSearchMessage: (String) -> Unit,
    val onSearchMessage: () -> Unit,
    val moveUpSearchMessage: () -> Unit,
    val moveDownSearchMessage: () -> Unit
)

// Управление вариантами сообщений (A/B тесты или альтернативные ответы)
data class MessageVariantActions(
    val onCreateNewChatVariant: () -> Unit,
    val onCreateNewMessageVariantForSlot: (slotId: Uuid) -> Unit,
    val onSetActiveMessage: (messageSlot: Uuid, newActiveMessageId: Uuid) -> Unit,
    val onSelectActiveLlmConfig: (config: AvailableLlmConfigs) -> Unit
)
