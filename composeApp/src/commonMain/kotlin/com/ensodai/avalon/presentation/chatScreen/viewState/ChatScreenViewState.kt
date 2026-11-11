package com.ensodai.avalon.presentation.chatScreen.viewState

import com.ensodai.avalon.domain.audio.TtsPlayerState
import androidx.compose.runtime.Immutable
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.data.db.dto.CharacterConfigDto
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.avalon.domain.models.MessageContainerModel
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.shared.avalon.gifts.models.GiftEffectType
import com.ensodai.avalon.domain.models.chat.WelcomeInfo
import com.ensodai.avalon.presentation.chatScreen.component.chat.ChatActionButton
import com.ensodai.avalon.presentation.core.ChatStatus
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import com.ensodai.avalon.utils.EMPTY_STRING
import kotlin.uuid.Uuid

const val ONE_SELECTED_MESSAGE = 1

@Immutable
data class ChatScreenViewState(

    // 🔹 Основная информация о чате
    val characterName: String = EMPTY_STRING,
    val characterId: Uuid? = null,
    val avatarUrl: String? = null,
    val userAvatarUrl: String? = null,
    val onlineStatus: Boolean = true,
    val welcomeCard: WelcomeInfo? = null,
    val availableLlmConfigs: List<AvailableLlmConfigs> = emptyList(),
    val selectedConfig: AvailableLlmConfigs = AvailableLlmConfigs(
        codeName = "default",
        displayName = "default",
        price = 1
    ),
    // 🔹 Контекст и токены
    val currentContextTokens: Int = 0,

    // 🔹 Вводимые пользователем данные
    val inputSearchMessage: String = EMPTY_STRING,
    val selectedMessageText: String? = EMPTY_STRING,
    val selectedSystemInstructionText: String? = null,

    // 🔹 Поиск и выделенные сообщения
    val searchMessages: List<MessageContainerModel> = emptyList(),
    val currentSelectedSearchMessage: MessageContainerModel? = null,
    val selectedMessages: List<MessageContainerModel> = mutableListOf(),

    // 🔹 Файлы и вложения
    val attachedImages: List<CharacterImageDTO> = mutableListOf(),
    val isFileAttached: Boolean = false,
    val isLoadingFiles: Boolean = false,

    // 🔹 Чат
    val isLastUserMessage: Boolean = false,
    val actionButtons: List<ChatActionButton> = emptyList<ChatActionButton>(),

    // 🔹 Исключения и ошибки

    // 🔹 Флаги состояния UI
    val chatStatus: ChatStatus = ChatStatus.Online,
    val messageSlotState: MessageSlotState = MessageSlotState(),
    val isEnableFlowMode: Boolean = false,
    val isRefreshButtonVisible: Boolean = false,
    val chatScreenModeState: ChatScreenModeState = ChatScreenModeState(),
    val actionButtonState: ActionButtonState = ActionButtonState(),
//    val loadingState: LoadingState = LoadingState.Idle,

    // 🔹 Диалоги и модальные окна
    val isShowPermissionDialog: Boolean = false,
    val isShowAlertDialogClearHistory: Boolean = false,
    val isShowAlertDialogDeleteSelectedMessage: Boolean = false,
    val isShowAlertDialogDeleteChatConfigWithMessages: Boolean = false,
    val fullscreenImageState: FullscreenImageState = FullscreenImageState(),

    //🔹Конфиг для сохранения в локальную базу
    val characterRemoteConfig: CharacterConfigDto? = null,
    val loadingState: ChatScreenLoadingState = ChatScreenLoadingState.Loading,

    val reportCharacterState: ReportCharacterStateChatScreen = ReportCharacterStateChatScreen(),

    val bottomBarState: BottomBarState = BottomBarState(),

    val chatHasHistory: Boolean = false,

    // 🔹 Озвучивание сообщений (TTS)
    val currentPlayingContentId: Uuid? = null,
    val ttsPlayerState: TtsPlayerState = TtsPlayerState.Idle,
    val generatingTtsMessageIds: Set<Uuid> = emptySet(),
    val premiumStatus: PremiumStatus = PremiumStatus.FREE,
    val levelUpAnimationState: LevelUpAnimationState = LevelUpAnimationState(),
    val activeGiftEffect: GiftEffectType? = null,
    val showAiWarning: Boolean = true,
    val chatRoleplayPreset: ChatRoleplayPreset = ChatRoleplayPreset.STANDARD_BUBBLE
) {
    // 🔹 Кнопки UI (видимость и доступность)

    val isSelectedMessageTextOnly = selectedMessages.firstOrNull()?.content?.all {
        it.type == com.ensodai.shared.avalon.chat.models.AvalonMessageContentType.TEXT ||
                it.type == com.ensodai.shared.avalon.chat.models.AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION
    } ?: false

    val isEnableCopyButton =
        selectedMessages.size == ONE_SELECTED_MESSAGE &&
                !chatScreenModeState.isEnableEditMessageMode &&
                !chatScreenModeState.isEnableSearchMessageMode &&
                isSelectedMessageTextOnly

    val isEnableEditButton =
        selectedMessages.size == ONE_SELECTED_MESSAGE &&
                !chatScreenModeState.isEnableEditMessageMode &&
                !chatScreenModeState.isEnableSearchMessageMode &&
                selectedMessages.firstOrNull()?.role != MessageRole.SYSTEM &&
                isSelectedMessageTextOnly

    val isEnableDeleteButton =
        chatScreenModeState.isEnableSelectedMessagesMode && !chatScreenModeState.isEnableEditMessageMode && !chatScreenModeState.isEnableSearchMessageMode
    val isEnableDropdownMenuButton =
        !chatScreenModeState.isEnableSelectedMessagesMode && !chatScreenModeState.isEnableEditMessageMode && !chatScreenModeState.isEnableSearchMessageMode

    val isEnableCounter =
        !chatScreenModeState.isEnableEditMessageMode && !chatScreenModeState.isEnableSearchMessageMode
    val isEnableChatInfoRow = !chatScreenModeState.isEnableSearchMessageMode
    val isEnableBackButton = !chatScreenModeState.isEnableSelectedMessagesMode

    // 🔹 Прочее
    val shouldShowBottomBar = !chatScreenModeState.isEnableEditMessageMode
    val selectedMessageNumber: Int = searchMessages.indexOf(currentSelectedSearchMessage) + 1
}

@Immutable
data class ActionButtonState(
    val type: ActionButtonType = ActionButtonType.FORCE_START,
    val isEnabled: Boolean = true
)

@Immutable
enum class ActionButtonType {
    SEND,           // Отправка (Icon: Send)
    FORCE_START,    // Генерация/Продолжить (Icon: DoubleArrowRight)
    STOP            // Стоп (Icon: Stop)
}

@Immutable
data class BottomBarState(
    val messageText: String = EMPTY_STRING,
    val messageSystemInstructionText: String? = null,
    val attachedImages: List<CharacterImageDTO> = mutableListOf(),
    val bottomBarMode: BottomBarMode = BottomBarMode.Idle,
    val textFieldMode: TextFieldMode = TextFieldMode.Message
)

@Immutable
enum class BottomBarMode {
    Idle,
    MainBottomSheetMenuMode,
    GiftBottomSheetMode
}

@Immutable
enum class TextFieldMode {
    Message,
    MessageInstruction
}

@Immutable
data class ChatScreenModeState(
    val isEnableEditMessageMode: Boolean = false,
    val isEnableSelectedMessagesMode: Boolean = false,
    val isEnableSearchMessageMode: Boolean = false
)

@Immutable
data class MessageSlotState(
    val currentTypingSlot: Uuid = Uuid.random(),
    val availableSlotForSwipe: Uuid? = null,
    val typingStatus: SlotTypingStatus = SlotTypingStatus.NotTyping,
    val messageRarity: Rarity = Rarity.COMMON
)

@Immutable
sealed class SlotTypingStatus {
    object Typing : SlotTypingStatus()
    object Ready : SlotTypingStatus()
    object NotTyping : SlotTypingStatus()
}

@Immutable
data class ReportCharacterStateChatScreen(
    val isShowReportDialog: Boolean = false,
    val reportMessage: String = EMPTY_STRING
)

@Immutable
sealed class ChatScreenLoadingState {
    object Loading : ChatScreenLoadingState()
    object Success : ChatScreenLoadingState()
    object Error : ChatScreenLoadingState()
    object Idle : ChatScreenLoadingState()
}

val ChatScreenViewState.characterHasAvatars: Boolean
    get() = avatarUrl != null && avatarUrl.isNotEmpty()

@Immutable
data class LevelUpAnimationState(
    val isVisible: Boolean = false,
    val fromLevel: Int = 1,
    val toLevel: Int = 1
)