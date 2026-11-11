package com.ensodai.avalon.presentation.chatScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.domain.global.GlobalEventBus
import com.ensodai.avalon.domain.global.GlobalEvent
import com.ensodai.avalon.domain.chatEvents.MessageStreamManager
import com.ensodai.avalon.presentation.chatScreen.viewState.LevelUpAnimationState
import com.ensodai.avalon.core.pagination.PaginatorState
import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.services.ChatRpcService
import com.ensodai.shared.avalon.gifts.models.Gift
import com.ensodai.shared.avalon.gifts.models.GiftPack
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.avalon.domain.models.ChatMessageModel
import com.ensodai.avalon.domain.models.MessageContainerModel
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.avalon.domain.models.message.ChatItem
import com.ensodai.avalon.domain.sessionWorker.DeletionType
import com.ensodai.avalon.domain.sessionWorker.DeletionUndoManager
import com.ensodai.avalon.domain.sessionWorker.MessageReadManager
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.CreateNewChatVariantUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.CreateNewMessageVariantForSlotUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.DeleteMessagesUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.EditMessageUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.GetLastMessageSlotFlowUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.SelectActiveLlmConfigForChatUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.SetActiveMessageUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.ToggleMessageReactionUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.UpdateChatDraftUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.actions.ForceGenerateResponseUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.actions.SendMessageUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.actions.StopGenerateResponseUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.actions.RequestSpeechUseCase
import com.ensodai.avalon.domain.audio.TtsPlayer
import com.ensodai.avalon.domain.audio.TtsPlayerState
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.utils.L
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.chat.ChatSyncManager
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.chat.GetChatStreamUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.ChatSyncMonitor
import com.ensodai.avalon.domain.useCases.commonUseCase.GetCharacterByIdUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.GetChatStatusUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.GetCurrentUserUseCase
import com.ensodai.avalon.domain.useCases.gift.GetGiftPacksUseCase
import com.ensodai.avalon.domain.useCases.gift.GetMyGiftsUseCase
import com.ensodai.avalon.domain.useCases.gift.SyncMyGiftsUseCase
import com.ensodai.shared.avalon.chat.models.ImagePayload
import com.ensodai.shared.avalon.chat.models.TextPayload
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.chat.models.StickerGiftPayload
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.shared.avalon.gifts.models.GiftEffectType
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.deleted_messages_count
import com.ensodai.avalon.presentation.chatScreen.action.ChatManagementActions
import com.ensodai.avalon.presentation.chatScreen.action.ChatScreenActions
import com.ensodai.avalon.presentation.chatScreen.action.DialogActions
import com.ensodai.avalon.presentation.chatScreen.action.MessageActions
import com.ensodai.avalon.presentation.chatScreen.action.MessageInputActions
import com.ensodai.avalon.presentation.chatScreen.action.MessageVariantActions
import com.ensodai.avalon.presentation.chatScreen.action.SearchActions
import com.ensodai.avalon.presentation.chatScreen.viewState.ActionButtonState
import com.ensodai.avalon.presentation.chatScreen.viewState.ActionButtonType
import com.ensodai.avalon.presentation.chatScreen.viewState.BottomBarMode
import com.ensodai.avalon.presentation.chatScreen.viewState.ChatScreenViewState
import com.ensodai.avalon.presentation.chatScreen.viewState.SlotTypingStatus
import com.ensodai.avalon.presentation.chatScreen.viewState.TextFieldMode
import com.ensodai.avalon.presentation.core.ChatStatus
import com.ensodai.avalon.utils.EMPTY_STRING
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.ensodai.avalon.presentation.chatScreen.viewState.ChatItemUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class ChatScreenViewModel(
    @InjectedParam val characterId: Uuid?,
    @InjectedParam val showAiWarningParam: Boolean,
    @InjectedParam val chatRoleplayPresetParam: ChatRoleplayPreset,
    private val chatMessageUiMapper: ChatMessageUiMapper,
    private val resourceWrapper: ResourceWrapper,
    private val editMessage: EditMessageUseCase,
    private val deleteMessages: DeleteMessagesUseCase,
    private val getCharacterById: GetCharacterByIdUseCase,
    private val createNewChatVariant: CreateNewChatVariantUseCase,
    private val createNewMessageVariantForSlot: CreateNewMessageVariantForSlotUseCase,
    private val setActiveMessage: SetActiveMessageUseCase,
    private val getLastMessageSlotFlow: GetLastMessageSlotFlowUseCase,
    private val sendMessage: SendMessageUseCase,
    private val getChatStream: GetChatStreamUseCase,
    private val getChatStatus: GetChatStatusUseCase,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val forceGenerateResponse: ForceGenerateResponseUseCase,
    private val stopGenerateResponse: StopGenerateResponseUseCase,
    private val selectActiveLlmConfigForChat: SelectActiveLlmConfigForChatUseCase,
    private val deletionUndoManager: DeletionUndoManager,
    private val toggleMessageReaction: ToggleMessageReactionUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getMyGiftsUseCase: GetMyGiftsUseCase,
    private val syncMyGiftsUseCase: SyncMyGiftsUseCase,
    private val getGiftPacksUseCase: GetGiftPacksUseCase,
    private val chatSyncMonitor: ChatSyncMonitor,
    private val chatSyncManager: ChatSyncManager,
    private val updateChatDraft: UpdateChatDraftUseCase,
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val chatRpcService: ChatRpcService,
    private val messageReadManager: MessageReadManager,
    private val requestSpeechUseCase: RequestSpeechUseCase,
    private val ttsPlayer: TtsPlayer,
    private val globalEventBus: GlobalEventBus,
    private val appSettingsRepository: AppSettingsRepository,
    private val messageStreamManager: MessageStreamManager
) : ViewModel() {

    private val _viewState = MutableStateFlow(
        ChatScreenViewState(
            showAiWarning = showAiWarningParam,
            chatRoleplayPreset = chatRoleplayPresetParam
        )
    )
    val viewState: StateFlow<ChatScreenViewState> = _viewState.asStateFlow()

    private val _myGiftsState = MutableStateFlow(PaginatorState<GiftPack>())
    val myGiftsState: StateFlow<PaginatorState<GiftPack>> = _myGiftsState.asStateFlow()

    private val _allGiftsState = MutableStateFlow(PaginatorState<GiftPack>())
    val allGiftsState: StateFlow<PaginatorState<GiftPack>> = _allGiftsState.asStateFlow()

    private val myGiftsPaginator = getMyGiftsUseCase()
    private val allGiftsPaginator = getGiftPacksUseCase()

    // Стейт с сообщениями (сырой доменный список)
    private val _chatItems = MutableStateFlow<List<ChatItem>>(emptyList())
    
    // UI стейт сообщений (отпарсенный и отмапленный)
    @OptIn(ExperimentalCoroutinesApi::class)
    val chatItems: StateFlow<List<ChatItemUiState>> = combine(
        _chatItems,
        _viewState.map { it.chatRoleplayPreset }.distinctUntilChanged(),
        _viewState.map { it.currentPlayingContentId }.distinctUntilChanged(),
        _viewState.map { it.ttsPlayerState }.distinctUntilChanged(),
        _viewState.map { it.generatingTtsMessageIds }.distinctUntilChanged()
    ) { items, preset, playingId, playerState, generatingIds ->
        chatMessageUiMapper.map(
            items = items,
            preset = preset,
            currentPlayingContentId = playingId,
            ttsPlayerState = playerState,
            generatingTtsMessageIds = generatingIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Временно отключено отображение наградных уведомлений повышения уровня в чате
//        viewModelScope.launch {
//            globalEventBus.events.collect { event ->
//                if (event is GlobalEvent.RelationshipLevelUp && event.characterId == characterId) {
//                    _viewState.update { currentState ->
//                        currentState.copy(
//                            levelUpAnimationState = LevelUpAnimationState(
//                                isVisible = true,
//                                fromLevel = event.oldLevel,
//                                toLevel = event.newLevel
//                            )
//                        )
//                    }
//                }
//            }
//        }

        if (characterId != null) {
            observeCharacter(characterId)
            observeUserPersona()
            updateIsEnableSwipe()

            viewModelScope.launch {
                getChatStream.get(characterId).collectLatest { streamResult ->
                    streamResult.uiItemsFlow.collect { _chatItems.value = it }
                }
            }

            viewModelScope.launch(IODispatcher) {
                localChatSessionsRepository.getActiveChatVariantFlow(characterId)
                    .collectLatest { variantId ->
                        if (variantId != null) {
                            messageReadManager.markAllMessagesAsRead(variantId)
                        }
                    }
            }

            loadDraft(characterId)
        }

        // Подписываемся на состояние TtsPlayer
        viewModelScope.launch {
            ttsPlayer.state.collect { playerState ->
                _viewState.update { it.copy(ttsPlayerState = playerState) }
            }
        }
        viewModelScope.launch {
            ttsPlayer.currentContentId.collect { contentId ->
                _viewState.update { it.copy(currentPlayingContentId = contentId) }
            }
        }

        // Подписываемся на глобальные события эффектов подарков
        viewModelScope.launch {
            globalEventBus.events.collect { event ->
                if (event is GlobalEvent.GiftEffectTriggered && event.characterId == characterId) {
                    _viewState.update { it.copy(activeGiftEffect = event.effectType) }
                }
            }
        }

        // Подписываемся на свитчер предупреждения об ИИ
        viewModelScope.launch {
            appSettingsRepository.getShowAiWarningFlow().collectLatest { show ->
                _viewState.update { it.copy(showAiWarning = show) }
            }
        }
    }

    val actions = ChatScreenActions(
        // Действия с сообщениями
        messageActions = MessageActions(
            onSelectMessage = { selectMessage(it) },
            onEnableSelectionMessageMode = { enableSelectionMessagesMode(it) },
            onDisableSelectionMessageMode = { disableSelectionMessagesMode() },
            onDeleteSelectedMessage = { deleteSelectedMessages() },
            onEnableEditMessageMode = { enableEditMessageMode() },
            onDisableEditMessageMode = { disableEditMessageMode() },
            onEditMessage = { editMessage(it) },
            onChangeEditedSystemInstruction = { changeEditedSystemInstruction(it) },
            onSaveEditedMessage = { saveEditedMessage() },
            onToggleReaction = { reaction, messageContainerId, slotId ->
                onToggleReaction(reaction, messageContainerId, slotId)
            },
            onImageClick = { contentId, slot -> onImageClick(contentId, slot) },
            onMessagesVisible = { markMessagesAsRead(it) },
            onSendOutreachFeedback = { isPositive, containerId ->
                sendOutreachFeedback(isPositive, containerId)
            },
            onPlaySpeech = { contentId, voice -> playSpeech(contentId, voice) },
            onPauseSpeech = { pauseSpeech() },
            onBuyEnergyClick = { onBuyEnergyRequested() }
        ),

        // Действия с полем ввода
        messageInputActions = MessageInputActions(
            onSendMessageClick = { sendMessage(it) },
            onChangeInputUserMessage = { changeInputMessage(it) },
            onForceGenerateResponse = { forceGenerateResponse() },
            onRefreshGeneratedMessage = { },
            onStopGenerateMessage = { stopGenerateMessage() },
            onAttachImages = { attachImages(it) },
            onDetachImages = { detachImages(it) },
            onToggleBottomSheetMenu = { toggleBottomSheetMenu() },
            onSetInstructionMode = { setInstructionMode(it) },
            onSetSystemInstruction = { setSystemInstruction(it) },
            onClearSystemInstruction = { clearSystemInstruction() },
            onEnableGiftBottomSheetMode = { enableGiftBottomSheetMode() },
            onSendGift = { sendGift(it) }
        ),

        // Действия с чатом
        chatManagementActions = ChatManagementActions(
            onDeleteAllChatMessages = { },
            onDeleteChatConfigWithMessages = { }
        ),

        // Действия с диалогами
        dialogActions = DialogActions(
            onCloseAlertDialog = { closeAlertDialog() },
            onShowAlertDialogDeleteMessage = { showAlertDialogDeleteMessage() },
            onShowAlertDialogComplaint = { showComplaintDialog() },
            onChangeReportMessage = { changeReportMessage(it) },
            onShowPermissionDialog = { showPermissionDialog() },
            onDismissFullscreenImage = { onDismissFullscreenImage() },
            onLevelUpAnimationComplete = { onLevelUpAnimationComplete() },
            onGiftEffectFinished = { onGiftEffectFinished() }
        ),

        // Действия с поиском
        searchActions = SearchActions(
            onEnableSearchMessageMode = { enableSearchMessageMode() },
            onDisableSearchMessageMode = { disableSearchMessageMode() },
            onChangeInputSearchMessage = { changeSearchMessage(it) },
            onSearchMessage = { searchMessage() },
            moveUpSearchMessage = { moveUpSearchMessage() },
            moveDownSearchMessage = { moveDownSearchMessage() }
        ),

        // Действия с вариантами сообщений
        messageVariantActions = MessageVariantActions(
            onCreateNewChatVariant = { createNewChatVariant() },
            onCreateNewMessageVariantForSlot = { createNewMessageVariantForSlot(messageSlotId = it) },
            onSetActiveMessage = { messageSlot, newActiveMessageId ->
                setActiveMessage(messageSlot, newActiveMessageId)
            },
            onSelectActiveLlmConfig = { selectActiveLlmConfig(config = it) }
        )
    )

    fun loadOlderMessages() {
        viewModelScope.launch {
            getChatStream.loadMore()
        }
    }

    fun markMessagesAsRead(messageContainerIds: List<Uuid>) {
        messageReadManager.markMessagesAsRead(messageContainerIds)
    }

    fun sendOutreachFeedback(isPositive: Boolean, containerId: Uuid) {
        val charId = characterId ?: return
        viewModelScope.launch(IODispatcher) {
            runCatching {
                localChatHistoryRepository.hideOutreachFeedbackForContainer(containerId)
                chatRpcService.sendOutreachFeedback(charId, isPositive)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        getChatStream.cancel()
        myGiftsPaginator.cancel()
        allGiftsPaginator.cancel()
        ttsPlayer.stop()
        messageStreamManager.clearAll()
    }

    fun playSpeech(contentId: Uuid, voice: VoiceModel?) {
        if (ttsPlayer.currentContentId.value == contentId && ttsPlayer.state.value is TtsPlayerState.Paused) {
            ttsPlayer.resume()
            return
        }
        viewModelScope.launch {
            _viewState.update { it.copy(generatingTtsMessageIds = it.generatingTtsMessageIds + contentId) }
            runCatching {
                requestSpeechUseCase.execute(contentId, voice)
            }.onFailure { e ->
                L.e("ChatScreenViewModel", "❌ Failed to play speech: ${e.message}")
                ttsPlayer.stop()
                _viewState.update {
                    it.copy(
                        currentPlayingContentId = null,
                        ttsPlayerState = TtsPlayerState.Idle
                    )
                }
            }.also {
                _viewState.update { it.copy(generatingTtsMessageIds = it.generatingTtsMessageIds - contentId) }
            }
        }
    }

    fun pauseSpeech() {
        ttsPlayer.pause()
    }

    private var isGiftsLoaded = false

    private fun loadGifts() {
        if (isGiftsLoaded) return
        isGiftsLoaded = true
        viewModelScope.launch {
            myGiftsPaginator.initialize()
            myGiftsPaginator.state.collect { _myGiftsState.value = it }
        }
        viewModelScope.launch {
            allGiftsPaginator.initialize()
            allGiftsPaginator.state.collect { _allGiftsState.value = it }
        }
    }

    fun loadMoreMyGifts() {
        viewModelScope.launch { myGiftsPaginator.loadMore() }
    }

    fun loadMoreAllGifts() {
        viewModelScope.launch { allGiftsPaginator.loadMore() }
    }

    fun refreshMyGifts() {
        myGiftsPaginator.refresh()
    }

    fun refreshAllGifts() {
        allGiftsPaginator.refresh()
    }

    private fun observeUserPersona() {
        viewModelScope.launch {
            getCurrentUser().collect { user ->
                _viewState.update { 
                    it.copy(
                        userAvatarUrl = user?.avatarUrl,
                        premiumStatus = user?.premiumStatus ?: PremiumStatus.FREE,
                        isEnableFlowMode = user?.isFlowModeEnabled ?: false
                    )
                }
            }
        }
    }

    private fun observeCharacter(id: Uuid) {
        updateTypingStatus()
        viewModelScope.launch {
            getCharacterById(id)
                .collect { characterUi ->
                    if (characterUi != null) {
                        _viewState.update { currentState ->
                            currentState.copy(
                                characterId = id,
                                characterName = characterUi.name,
                                avatarUrl = characterUi.avatarUrl,
                                welcomeCard = characterUi.welcomeInfo,
                                availableLlmConfigs = characterUi.availableLlmConfigs,
                                selectedConfig = characterUi.selectedConfig
                            )
                        }
                    }
                }
        }
    }

    private fun updateTypingStatus() {
        if (characterId != null) {
            viewModelScope.launch {
                getChatStatus(characterId)
                    .collect { chatStatus ->

                        val currentRarity: Rarity
                        val currentTypingStatus: SlotTypingStatus

                        when (chatStatus) {
                            is ChatStatus.Typing -> {
                                currentTypingStatus = SlotTypingStatus.Typing
                                currentRarity = chatStatus.rarity
                            }

                            else -> {
                                currentTypingStatus = SlotTypingStatus.NotTyping
                                currentRarity = Rarity.COMMON
                            }
                        }

                        // 2. Обновляем UI стейт
                        _viewState.update { currentState ->
                            currentState.copy(
                                chatStatus = chatStatus,
                                actionButtonState = calculateActionButtonState(
                                    status = chatStatus,
                                    inputText = currentState.bottomBarState.messageText,
                                    hasImages = currentState.attachedImages.isNotEmpty()
                                ),
                                messageSlotState = currentState.messageSlotState.copy(
                                    typingStatus = currentTypingStatus,
                                    messageRarity = currentRarity
                                )
                            )
                        }
                    }
            }
        }
    }

    private fun calculateActionButtonState(
        status: ChatStatus,
        inputText: String,
        hasImages: Boolean
    ): ActionButtonState {

        val hasContent = inputText.isNotBlank() || hasImages
        val isTyping = status is ChatStatus.Typing

        val isInteractionAllowed = status != ChatStatus.Updating

        if (hasContent) {
            return ActionButtonState(
                type = ActionButtonType.SEND,
                isEnabled = isInteractionAllowed
            )
        }

        if (isTyping) {
            return ActionButtonState(
                type = ActionButtonType.STOP,
                isEnabled = true
            )
        }

        return ActionButtonState(
            type = ActionButtonType.FORCE_START,
            isEnabled = isInteractionAllowed
        )
    }


    private fun moveUpSearchMessage() {
        viewModelScope.launch(context = IODispatcher) {
            val list = _viewState.value.searchMessages
            if (list.isEmpty()) return@launch
            val currentId = _viewState.value.currentSelectedSearchMessage
            if (currentId == null) {
                _viewState.getAndUpdate {
                    it.copy(
                        currentSelectedSearchMessage = list[0]
                    )
                }
                return@launch
            }

            if (list.isNotEmpty()) {
                val currentIndex = list.indexOf(currentId)
                _viewState.getAndUpdate {
                    it.copy(
                        currentSelectedSearchMessage = if (currentIndex in 0 until list.lastIndex) {
                            list[currentIndex + 1]
                        } else {
                            list.last()
                        }
                    )
                }
            }
        }
    }

    private fun moveDownSearchMessage() {
        viewModelScope.launch(context = IODispatcher) {
            val list = _viewState.value.searchMessages
            if (list.isEmpty()) return@launch
            val currentId = _viewState.value.currentSelectedSearchMessage
            if (currentId == null) {
                _viewState.getAndUpdate {
                    it.copy(
                        currentSelectedSearchMessage = list[0]
                    )
                }
                return@launch
            }

            if (list.isNotEmpty()) {
                val currentIndex = list.indexOf(currentId)
                _viewState.getAndUpdate {
                    it.copy(
                        currentSelectedSearchMessage = if (currentIndex > 0) {
                            list[currentIndex - 1]
                        } else {
                            list.first()
                        }
                    )
                }
            } else {
                _viewState.getAndUpdate {
                    it.copy(currentSelectedSearchMessage = null)
                }
            }
        }
    }

    private fun enableSearchMessageMode() {

    }

    private fun disableSearchMessageMode() {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(
                    chatScreenModeState = it.chatScreenModeState.copy(isEnableSearchMessageMode = false),
                    inputSearchMessage = EMPTY_STRING,
                    currentSelectedSearchMessage = null
                )
            }
        }
    }

    private fun changeSearchMessage(searchText: String) {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(inputSearchMessage = searchText)
            }
        }
    }

    private fun searchMessage() {

    }

    private fun startAttachingFiles() {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(isLoadingFiles = true)
            }
        }
    }

    private fun detachImages(file: CharacterImageDTO) {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(attachedImages = it.attachedImages - file)
            }
        }
    }

    private fun attachImages(images: List<CharacterImageDTO>) {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate { currentState ->
                val newFiles = images.filterNot { it in currentState.attachedImages }
                currentState.copy(
                    attachedImages = currentState.attachedImages + newFiles,
                    isLoadingFiles = false
                )
            }
        }
    }

    private fun changeInputMessage(message: String) {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(
                    bottomBarState = it.bottomBarState.copy(messageText = message),
                    actionButtonState = calculateActionButtonState(
                        status = it.chatStatus,
                        inputText = message,
                        hasImages = it.attachedImages.isNotEmpty()
                    )
                )
            }
            if (characterId != null) {
                updateChatDraft.execute(characterId, message)
            }
        }
    }

    private fun sendMessage(inputUserMessage: String) {
        val trimMessage = inputUserMessage.trim().take(3000)
        val instruction = _viewState.value.bottomBarState.messageSystemInstructionText

        if (trimMessage.isNotEmpty() && characterId != null) {
            viewModelScope.launch(IODispatcher) {
                updateChatDraft.execute(characterId, null)
                sendMessage.send(
                    characterId = characterId,
                    text = trimMessage,
                    llmModelCode = _viewState.value.selectedConfig.codeName,
                    systemInstruction = instruction,
                    contentType = AvalonMessageContentType.TEXT
                )
                _viewState.getAndUpdate {
                    it.copy(
                        bottomBarState = it.bottomBarState.copy(
                            messageSystemInstructionText = null,
                            messageText = EMPTY_STRING
                        )
                    )
                }
            }
        }
    }
    private fun selectMessage(message: MessageContainerModel) {
        viewModelScope.launch(context = IODispatcher) {
            if (_viewState.value.chatScreenModeState.isEnableSelectedMessagesMode) {
                _viewState.getAndUpdate {
                    val selectedMessage = if (message in it.selectedMessages) {
                        it.selectedMessages - message
                    } else {
                        it.selectedMessages + message
                    }
                    it.copy(
                        selectedMessages = selectedMessage
                    )
                }
                if (_viewState.value.selectedMessages.size == 1) {
                    val selectedMsg = _viewState.value.selectedMessages.first()
                    val payloadMessageText =
                        selectedMsg.content.find { content -> content.type == AvalonMessageContentType.TEXT }?.payload
                    val payloadSystemInstruction =
                        selectedMsg.content.find { content -> content.type == AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION }?.payload

                    val selectedMessageText =
                        if (payloadMessageText is TextPayload) payloadMessageText.text else EMPTY_STRING
                    val selectedSystemInstructionText =
                        if (payloadSystemInstruction is TextPayload) payloadSystemInstruction.text else null

                    _viewState.getAndUpdate {
                        it.copy(
                            selectedMessageText = selectedMessageText,
                            selectedSystemInstructionText = selectedSystemInstructionText
                        )
                    }
                } else {
                    _viewState.getAndUpdate {
                        it.copy(
                            selectedMessageText = EMPTY_STRING,
                            selectedSystemInstructionText = null
                        )
                    }
                }
                if (_viewState.value.selectedMessages.isEmpty()) {
                    _viewState.getAndUpdate {
                        it.copy(
                            chatScreenModeState = it.chatScreenModeState.copy(
                                isEnableSelectedMessagesMode = false
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun enableSelectionMessagesMode(messageContainer: MessageContainerModel) {
        viewModelScope.launch(context = IODispatcher) {
            if (!_viewState.value.chatScreenModeState.isEnableSearchMessageMode) {

                val payloadMessageText = messageContainer
                    .content
                    .find { content -> content.type == AvalonMessageContentType.TEXT }?.payload
                val payloadSystemInstruction = messageContainer
                    .content
                    .find { content -> content.type == AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION }?.payload
                val selectedMessageText =
                    if (payloadMessageText is TextPayload) payloadMessageText.text else EMPTY_STRING
                val selectedSystemInstructionText =
                    if (payloadSystemInstruction is TextPayload) payloadSystemInstruction.text else null

                _viewState.getAndUpdate {
                    it.copy(
                        chatScreenModeState = it.chatScreenModeState.copy(
                            isEnableSelectedMessagesMode = true
                        ),
                        selectedMessages = it.selectedMessages + messageContainer,
                        selectedMessageText = selectedMessageText,
                        selectedSystemInstructionText = selectedSystemInstructionText
                    )
                }
            }
        }
    }

    private fun disableSelectionMessagesMode() {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(
                    chatScreenModeState = it.chatScreenModeState.copy(isEnableSelectedMessagesMode = false),
                    selectedMessages = emptyList(),
                    selectedMessageText = EMPTY_STRING,
                    selectedSystemInstructionText = null
                )
            }
        }
    }

    private fun deleteSelectedMessages() {
        if (characterId == null) return

        viewModelScope.launch(context = IODispatcher) {
            val slotIds = _viewState.value.selectedMessages.map { it.slotId }

            deletionUndoManager.startDeletion(
                ids = slotIds,
                type = DeletionType.MESSAGE,
                displayText = resourceWrapper.getQuantityString(
                    Res.plurals.deleted_messages_count,
                    slotIds.size,
                    slotIds.size
                ),
                characterId = characterId
            )
            _viewState.getAndUpdate {
                it.copy(
                    chatScreenModeState = it.chatScreenModeState.copy(
                        isEnableSelectedMessagesMode = false
                    ),
                    selectedMessages = emptyList()
                )
            }
            closeAlertDialog()
        }
    }

    private fun editMessage(message: String) {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(selectedMessageText = message)
            }
        }
    }

    private fun enableEditMessageMode() {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(
                    chatScreenModeState = it.chatScreenModeState.copy(isEnableEditMessageMode = true)
                )
            }
        }
    }

    private fun disableEditMessageMode() {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(
                    chatScreenModeState = it.chatScreenModeState.copy(isEnableEditMessageMode = false)
                )
            }
        }
    }

    private fun changeEditedSystemInstruction(text: String) {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(selectedSystemInstructionText = text)
            }
        }
    }

    private fun forceGenerateResponse() {
        if (characterId != null) {
            viewModelScope.launch(context = IODispatcher) {
                forceGenerateResponse.force(
                    characterId = characterId,
                    llmModelCode = _viewState.value.selectedConfig.codeName
                )
            }
        }
    }

    private fun saveEditedMessage() {
        val selectedMessages = _viewState.value.selectedMessages
        if (selectedMessages.isNotEmpty() && characterId != null) {
            viewModelScope.launch(context = IODispatcher) {
                editMessage.update(
                    messageContainerId = selectedMessages.last().id,
                    messageText = _viewState.value.selectedMessageText.toString().take(3000),
                    characterId = characterId,
                    messageSlotId = selectedMessages.last().slotId,
                    systemInstruction = _viewState.value.selectedSystemInstructionText?.take(1500)
                )
                disableSelectionMessagesMode()
                disableEditMessageMode()
                closeAlertDialog()
            }
        }
    }

    private fun stopGenerateMessage() {
        viewModelScope.launch(context = IODispatcher) {
            if (characterId != null) {
                stopGenerateResponse.stop(characterId)
                messageStreamManager.clearAll()
            }
        }
    }

    private fun closeAlertDialog() {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(
                    isShowAlertDialogDeleteSelectedMessage = false,
                    isShowAlertDialogClearHistory = false,
                    isShowAlertDialogDeleteChatConfigWithMessages = false,
                    isShowPermissionDialog = false,
                    reportCharacterState = it.reportCharacterState.copy(isShowReportDialog = false)
                )
            }
        }
    }

    private fun onImageClick(contentId: Uuid, slot: ChatMessageModel) {
        viewModelScope.launch(context = IODispatcher) {
            val allImages = slot.containers
                .flatMap { it.content }
                .filter { it.type == AvalonMessageContentType.IMAGE }

            val clickedIndex = allImages.indexOfFirst { it.id == contentId }
            val safeImageIndex = clickedIndex.coerceAtLeast(0)
            val imagePayloads = allImages
                .map { it.payload }
                .filterIsInstance<ImagePayload>()

            _viewState.update { state ->
                state.copy(
                    fullscreenImageState = state.fullscreenImageState.copy(
                        isShowDialog = true,
                        imagePayloads = imagePayloads,
                        imageIndex = safeImageIndex
                    )
                )
            }
        }
    }

    private fun onDismissFullscreenImage() {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.update { state ->
                state.copy(
                    fullscreenImageState = state.fullscreenImageState.copy(
                        isShowDialog = false
                    )
                )
            }
        }
    }

    private fun showAlertDialogDeleteMessage() {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(isShowAlertDialogDeleteSelectedMessage = true)
            }
        }
    }

    private fun showPermissionDialog() {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(isShowPermissionDialog = true)
            }
        }
    }

    private fun showComplaintDialog() {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(
                    reportCharacterState = it.reportCharacterState.copy(isShowReportDialog = true)
                )
            }
        }
    }

    private fun changeReportMessage(text: String) {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(
                    reportCharacterState = it.reportCharacterState.copy(reportMessage = text)
                )
            }
        }
    }

    private fun createNewChatVariant() {
        viewModelScope.launch(context = IODispatcher) {
            createNewChatVariant.create(characterId)
        }
    }

    private fun createNewMessageVariantForSlot(messageSlotId: Uuid) {
        viewModelScope.launch(context = IODispatcher) {
            _viewState.getAndUpdate {
                it.copy(
                    messageSlotState = it.messageSlotState.copy(
                        currentTypingSlot = messageSlotId,
                        typingStatus = SlotTypingStatus.Ready,
                        messageRarity = Rarity.COMMON
                    )
                )
            }
            createNewMessageVariantForSlot.create(
                characterId = characterId,
                slotId = messageSlotId,
                llmModelCode = _viewState.value.selectedConfig.codeName
            )
        }
    }

    private fun setActiveMessage(messageSlot: Uuid, newActiveMessageId: Uuid) {
        viewModelScope.launch(IODispatcher) {
            setActiveMessage.set(
                characterId = characterId,
                messageSlot = messageSlot,
                newActiveMessageId = newActiveMessageId
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun updateIsEnableSwipe() {
        if (characterId != null) {
            viewModelScope.launch(context = IODispatcher) {

                val lastMessageFlow = getLastMessageSlotFlow.get(characterId)
                val messageSlotStateFlow = _viewState
                    .map { it.messageSlotState }
                    .distinctUntilChanged()
                val chatScreenModeStateFlow = _viewState
                    .map { it.chatScreenModeState }
                    .distinctUntilChanged()

                combine(
                    flow = lastMessageFlow,
                    flow2 = messageSlotStateFlow,
                    flow3 = chatScreenModeStateFlow
                ) { lastMessage, slotState, chatScreenModeStateFlow ->
                    Triple(lastMessage, slotState, chatScreenModeStateFlow)
                }
                    .collectLatest { (lastMessage, messageSlotState, chatScreenModeStateFlow) ->
                        if (lastMessage == null) return@collectLatest
                        val isModelRole = lastMessage.role == MessageRole.MODEL

                        val hasDefaultChatMode =
                            !chatScreenModeStateFlow.isEnableSelectedMessagesMode &&
                                    !chatScreenModeStateFlow.isEnableEditMessageMode &&
                                    !chatScreenModeStateFlow.isEnableSearchMessageMode

                        if (isModelRole && (messageSlotState.typingStatus is SlotTypingStatus.NotTyping) && hasDefaultChatMode) {
                            _viewState.getAndUpdate {
                                it.copy(
                                    messageSlotState = it.messageSlotState.copy(
                                        availableSlotForSwipe = lastMessage.slotId
                                    )
                                )
                            }
                        } else {
                            _viewState.getAndUpdate {
                                it.copy(
                                    messageSlotState = it.messageSlotState.copy(
                                        availableSlotForSwipe = null
                                    )
                                )
                            }
                        }
                    }
            }
        }
    }

    private fun onToggleReaction(
        reaction: String?,
        messageContainerId: Uuid?,
        slotId: Uuid
    ) {
        if (messageContainerId == null || characterId == null) return
        viewModelScope.launch(context = IODispatcher) {
            toggleMessageReaction.toggle(
                messageContainerId = messageContainerId,
                reaction = reaction,
                slotId = slotId,
                characterId = characterId,

                )
        }
    }

    private fun selectActiveLlmConfig(config: AvailableLlmConfigs) {
        viewModelScope.launch {
            if (characterId != null) {
                selectActiveLlmConfigForChat.select(
                    characterId = characterId,
                    config = config
                )
            }
            _viewState.getAndUpdate {
                it.copy(selectedConfig = config)
            }
        }
    }

    private fun toggleBottomSheetMenu() {
        _viewState.update {
            val currentState = it.bottomBarState.bottomBarMode
            val newState = if (currentState == BottomBarMode.Idle) {
                BottomBarMode.MainBottomSheetMenuMode
            } else {
                BottomBarMode.Idle
            }
            it.copy(
                bottomBarState = it.bottomBarState.copy(
                    bottomBarMode = newState
                )
            )
        }
    }

    private fun setInstructionMode(isInstructionMode: Boolean) {
        _viewState.update {
            it.copy(
                bottomBarState = it.bottomBarState.copy(
                    textFieldMode = if (isInstructionMode) TextFieldMode.MessageInstruction else TextFieldMode.Message,
                    bottomBarMode = BottomBarMode.Idle
                )
            )
        }
    }

    private fun setSystemInstruction(instruction: String) {
        _viewState.update {
            it.copy(
                bottomBarState = it.bottomBarState.copy(
                    messageSystemInstructionText = instruction.take(1500).ifBlank { null },
                    textFieldMode = TextFieldMode.Message,
                )
            )
        }
    }

    private fun clearSystemInstruction() {
        _viewState.update {
            it.copy(
                bottomBarState = it.bottomBarState.copy(
                    messageSystemInstructionText = null
                )
            )
        }
    }

    private fun enableGiftBottomSheetMode() {
        loadGifts()
        _viewState.getAndUpdate {
            it.copy(
                bottomBarState = it.bottomBarState.copy(
                    bottomBarMode = BottomBarMode.GiftBottomSheetMode
                )
            )
        }
    }

    private fun sendGift(gift: Gift) {
        if (characterId != null) {
            viewModelScope.launch(IODispatcher) {
                sendMessage.send(
                    characterId = characterId,
                    text = EMPTY_STRING,
                    llmModelCode = _viewState.value.selectedConfig.codeName,
                    systemInstruction = null,
                    contentType = AvalonMessageContentType.GIFT,
                    gift = gift
                )
            }
        }
    }

    fun onBuyEnergyRequested() {
        L.d("ChatScreenViewModel", "User requested energy purchase from chat screen")
    }

    fun onLevelUpAnimationComplete() {
        _viewState.update {
            it.copy(
                levelUpAnimationState = LevelUpAnimationState()
            )
        }
    }

    private fun onGiftEffectFinished() {
        _viewState.update {
            it.copy(
                activeGiftEffect = null
            )
        }
    }

    private fun loadDraft(id: Uuid) {
        viewModelScope.launch(IODispatcher) {
            val draft = localChatSessionsRepository.getSessionByCharacterId(id)?.draftMessage
            if (!draft.isNullOrBlank()) {
                _viewState.update { state ->
                    state.copy(
                        bottomBarState = state.bottomBarState.copy(messageText = draft),
                        actionButtonState = calculateActionButtonState(
                            status = state.chatStatus,
                            inputText = draft,
                            hasImages = state.attachedImages.isNotEmpty()
                        )
                    )
                }
            }
        }
    }
}
