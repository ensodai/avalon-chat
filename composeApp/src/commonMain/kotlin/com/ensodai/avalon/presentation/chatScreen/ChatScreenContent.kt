package com.ensodai.avalon.presentation.chatScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.GiftEffectOverlay
import com.ensodai.avalon.presentation.chatScreen.component.levelUp.LevelUpAnimationOverlay
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.core.pagination.PaginatorState
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.gifts.models.GiftPack
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.avalon.domain.models.MessageContainerModel
import com.ensodai.avalon.domain.models.MessageContentModel
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.avalon.domain.models.message.ChatItem
import com.ensodai.avalon.domain.models.message.MessageItem
import com.ensodai.avalon.domain.models.message.WelcomeCardItem
import com.ensodai.avalon.domain.models.chat.WelcomeInfo
import com.ensodai.shared.avalon.chat.models.MessagePayload
import com.ensodai.shared.avalon.chat.models.MessageStatus
import com.ensodai.shared.avalon.chat.models.TextPayload
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.chatScreen.action.ChatScreenActions
import com.ensodai.avalon.presentation.chatScreen.alertDialogs.AlertDialogsChatScreen
import com.ensodai.avalon.presentation.chatScreen.component.bottomBar.BottomBarChatScreen
import com.ensodai.avalon.presentation.chatScreen.component.chat.ChatBackground
import com.ensodai.avalon.presentation.chatScreen.component.chat.ChatLazyColumn
import com.ensodai.avalon.presentation.chatScreen.component.chat.FloatingActionButtonChatScreen
import com.ensodai.avalon.presentation.chatScreen.component.chat.actionButtonClickHandler
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.MessageBubble
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.SwipeableMessageBubbleHorizontalPager
import com.ensodai.avalon.presentation.chatScreen.component.explosionEffect.LocalReactionOverlayController
import com.ensodai.avalon.presentation.chatScreen.component.explosionEffect.ReactionOverlayController
import com.ensodai.avalon.presentation.chatScreen.component.explosionEffect.ReactionOverlayHost
import com.ensodai.avalon.presentation.chatScreen.component.explosionEffect.rememberReactionOverlayController
import com.ensodai.avalon.presentation.chatScreen.component.topBar.TopBarChatScreen
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.chatScreen.viewState.ActiveReactionMenu
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageContainerUiState
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageBodyUiItem
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageActionsConfigUiState
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import com.ensodai.avalon.domain.models.ChatMessageModel
import com.ensodai.avalon.domain.models.chat.RpgMessageBlock
import com.ensodai.avalon.presentation.chatScreen.viewState.BottomBarMode
import com.ensodai.avalon.presentation.chatScreen.viewState.ChatScreenModeState
import com.ensodai.avalon.presentation.chatScreen.viewState.ChatScreenViewState
import com.ensodai.avalon.presentation.chatScreen.viewState.FullscreenImageState
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageSlotState
import com.ensodai.avalon.presentation.chatScreen.viewState.SlotTypingStatus
import com.ensodai.avalon.presentation.chatScreen.viewState.TextFieldMode
import com.ensodai.avalon.domain.audio.TtsPlayerState
import com.ensodai.avalon.presentation.commonComponents.toastNotify
import com.ensodai.avalon.presentation.commonComponents.bottomSheet.BuyEnergyBottomSheet
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.theme.AvalonColors
import com.composables.icons.lucide.Bot
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.Lucide
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.ImageFullscreenDialog
import com.ensodai.avalon.presentation.commonComponents.welcomCard.CardColor
import com.ensodai.avalon.presentation.commonComponents.welcomCard.WelcomeCard
import com.ensodai.avalon.config.AppBuildConfig
import com.ensodai.avalon.presentation.navigation.LocalSnackbarBottomPadding
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.utils.EMPTY_STRING
import com.ensodai.avalon.utils.getUTCTimeNow
import com.ensodai.avalon.presentation.chatScreen.viewState.ChatItemUiState
import androidx.compose.foundation.border
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalFoundationApi::class, FlowPreview::class, ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    actions: ChatScreenActions,
    viewState: ChatScreenViewState,
    navigation: Navigation,
    viewModel: ChatScreenViewModel
) {
    val chatHistory by viewModel.chatItems.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showScrollButton by remember { mutableStateOf(false) }
    var showFloatingPlusButton by remember { mutableStateOf(false) }
    var isRefreshButtonVisible by remember { mutableStateOf(false) }
    var showDateBubble by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isGenerateResponseButtonEnable by remember { mutableStateOf(true) }
    var activeReactionMenu by remember { mutableStateOf<ActiveReactionMenu?>(null) }
    var showBuyEnergySheet by remember { mutableStateOf(false) }
    var showAiWarningSheet by remember { mutableStateOf(false) }
    val globalSnackbarPadding = LocalSnackbarBottomPadding.current
    val density = LocalDensity.current
    val myGiftsState by viewModel.myGiftsState.collectAsState()
    val allGiftsState by viewModel.allGiftsState.collectAsState()
    val reactionOverlayController = rememberReactionOverlayController()
    val messageSlotState = viewState.messageSlotState
    val chatScreenModeState = viewState.chatScreenModeState
    val selectedMessages = viewState.selectedMessages
    val selectedMessageIds = remember(selectedMessages) {
        selectedMessages.mapTo(mutableSetOf()) { it.id }
    }
    val currentSelectedSearchSlotId = viewState.currentSelectedSearchMessage?.slotId
    val isEditMessageModeEnabled = chatScreenModeState.isEnableEditMessageMode
    val isSelectedMessagesModeEnabled = chatScreenModeState.isEnableSelectedMessagesMode
    val inputSearchMessage = viewState.inputSearchMessage
    val selectedMessageText = viewState.selectedMessageText
    val selectedSystemInstructionText = viewState.selectedSystemInstructionText
    val userAvatarUrl = viewState.userAvatarUrl
    val characterAvatarUrl = viewState.avatarUrl

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { it.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .debounce(100L)
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= chatHistory.size - 10) {
                    viewModel.loadOlderMessages()
                }
            }
    }

    HandleBackPressChatScreen(
        actions = actions,
        viewState = viewState,
        navigation = navigation
    )

    UpdateDateBubbleVisible(
        listState = listState,
        update = { showDateBubble = it }
    )

    AlertDialogsChatScreen(
        viewState = viewState,
        actions = actions,
        navigation = navigation,
    )

    ShowKeyBoard(
        isEnableSearchMessageMode = chatScreenModeState.isEnableSearchMessageMode,
        focusRequester = focusRequester,
        keyboardController = keyboardController,
        focusManager = focusManager
    )
    DisposableEffect(Unit) {
        onDispose {
            globalSnackbarPadding.value = 0.dp
        }
    }

    val listRenderState = remember(
        selectedMessageIds,
        currentSelectedSearchSlotId,
        isEditMessageModeEnabled,
        isSelectedMessagesModeEnabled,
        inputSearchMessage,
        selectedMessageText,
        selectedSystemInstructionText,
        userAvatarUrl,
        characterAvatarUrl,
        viewState.currentPlayingContentId,
        viewState.ttsPlayerState,
        viewState.generatingTtsMessageIds,
        viewState.premiumStatus,
        viewState.isEnableFlowMode
    ) {
        ChatMessageListRenderState(
            selectedMessageIds = selectedMessageIds,
            currentSelectedSearchSlotId = currentSelectedSearchSlotId,
            isEditMessageModeEnabled = isEditMessageModeEnabled,
            isSelectedMessagesModeEnabled = isSelectedMessagesModeEnabled,
            inputSearchMessage = inputSearchMessage,
            selectedMessageText = selectedMessageText,
            selectedSystemInstructionText = selectedSystemInstructionText,
            userAvatarUrl = userAvatarUrl,
            characterAvatarUrl = characterAvatarUrl,
            currentPlayingContentId = viewState.currentPlayingContentId,
            ttsPlayerState = viewState.ttsPlayerState,
            generatingTtsMessageIds = viewState.generatingTtsMessageIds,
            premiumStatus = viewState.premiumStatus,
            isEnableFlowMode = viewState.isEnableFlowMode,
            chatRoleplayPreset = viewState.chatRoleplayPreset
        )
    }

    CompositionLocalProvider(
        LocalReactionOverlayController provides reactionOverlayController
    ) {
        ChatScreenScaffold(
            viewState = viewState,
            actions = actions,
            navigation = navigation,
            viewModel = viewModel,
            listState = listState,
            chatHistory = chatHistory,
            selectedMessages = selectedMessages,
            isEditMessageModeEnabled = isEditMessageModeEnabled,
            chatScreenModeState = chatScreenModeState,
            focusRequester = focusRequester,
            focusManager = focusManager,
            density = density,
            coroutineScope = coroutineScope,
            myGiftsState = myGiftsState,
            allGiftsState = allGiftsState,
            globalSnackbarPadding = globalSnackbarPadding,
            reactionOverlayController = reactionOverlayController,
            listRenderState = listRenderState,
            messageSlotState = messageSlotState,
            isRefreshButtonVisible = isRefreshButtonVisible,
            showScrollButton = showScrollButton,
            showFloatingPlusButton = showFloatingPlusButton,
            isGenerateResponseButtonEnable = isGenerateResponseButtonEnable,
            activeReactionMenu = activeReactionMenu,
            showBuyEnergySheet = showBuyEnergySheet,
            onRefreshButtonVisibleChange = { isRefreshButtonVisible = it },
            onScrollButtonVisibleChange = { showScrollButton = it },
            onFloatingPlusButtonVisibilityChange = { showFloatingPlusButton = it },
            onGenerateResponseButtonEnabledChange = { isGenerateResponseButtonEnable = it },
            onActiveReactionMenuChange = { activeReactionMenu = it },
            onShowBuyEnergySheetChange = { showBuyEnergySheet = it },
            onShowAiWarningClick = { showAiWarningSheet = true }
        )
    }

    if (showAiWarningSheet) {
        val colors = com.ensodai.avalon.theme.AvalonTheme.colors
        val uriHandler = LocalUriHandler.current
        ModalBottomSheet(
            onDismissRequest = { showAiWarningSheet = false },
            containerColor = colors.overlaySurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(colors.warningAccent.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.ShieldAlert,
                        contentDescription = null,
                        tint = colors.warningAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.ai_warning_sheet_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textMain,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WarningItem(
                        icon = Lucide.Bot,
                        title = stringResource(Res.string.ai_warning_bullet_1_title),
                        desc = stringResource(Res.string.ai_warning_bullet_1_desc),
                        tintColor = colors.neonAccent,
                        colors = colors
                    )
                    WarningItem(
                        icon = Lucide.CircleAlert,
                        title = stringResource(Res.string.ai_warning_bullet_2_title),
                        desc = stringResource(Res.string.ai_warning_bullet_2_desc),
                        tintColor = colors.warningAccent,
                        colors = colors
                    )
                    WarningItem(
                        icon = Lucide.Lock,
                        title = stringResource(Res.string.ai_warning_bullet_3_title),
                        desc = stringResource(Res.string.ai_warning_bullet_3_desc),
                        tintColor = colors.infoAccent,
                        colors = colors
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DocumentButton(
                        text = stringResource(Res.string.ai_warning_btn_terms),
                        onClick = { uriHandler.openUri(AppBuildConfig.TERMS_URL) },
                        modifier = Modifier.weight(1f),
                        colors = colors
                    )
                    DocumentButton(
                        text = stringResource(Res.string.ai_warning_btn_privacy),
                        onClick = { uriHandler.openUri(AppBuildConfig.PRIVACY_URL) },
                        modifier = Modifier.weight(1f),
                        colors = colors
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                TechnoButton(
                    text = stringResource(Res.string.ok),
                    onClick = { showAiWarningSheet = false },
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.ai_warning_sheet_disable_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

private data class ChatMessageListRenderState(
    val selectedMessageIds: Set<Uuid>,
    val currentSelectedSearchSlotId: Uuid?,
    val isEditMessageModeEnabled: Boolean,
    val isSelectedMessagesModeEnabled: Boolean,
    val inputSearchMessage: String,
    val selectedMessageText: String?,
    val selectedSystemInstructionText: String?,
    val userAvatarUrl: String?,
    val characterAvatarUrl: String?,
    val currentPlayingContentId: Uuid?,
    val ttsPlayerState: TtsPlayerState,
    val generatingTtsMessageIds: Set<Uuid>,
    val premiumStatus: PremiumStatus,
    val isEnableFlowMode: Boolean,
    val chatRoleplayPreset: ChatRoleplayPreset
)

@Composable
private fun ChatScreenScaffold(
    viewState: ChatScreenViewState,
    actions: ChatScreenActions,
    navigation: Navigation,
    viewModel: ChatScreenViewModel,
    listState: LazyListState,
    chatHistory: List<ChatItemUiState>,
    selectedMessages: List<MessageContainerModel>,
    isEditMessageModeEnabled: Boolean,
    chatScreenModeState: ChatScreenModeState,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    density: Density,
    coroutineScope: CoroutineScope,
    myGiftsState: PaginatorState<GiftPack>,
    allGiftsState: PaginatorState<GiftPack>,
    globalSnackbarPadding: MutableState<Dp>,
    reactionOverlayController: ReactionOverlayController,
    listRenderState: ChatMessageListRenderState,
    messageSlotState: MessageSlotState,
    isRefreshButtonVisible: Boolean,
    showScrollButton: Boolean,
    showFloatingPlusButton: Boolean,
    isGenerateResponseButtonEnable: Boolean,
    activeReactionMenu: ActiveReactionMenu?,
    showBuyEnergySheet: Boolean,
    onRefreshButtonVisibleChange: (Boolean) -> Unit,
    onScrollButtonVisibleChange: (Boolean) -> Unit,
    onFloatingPlusButtonVisibilityChange: (Boolean) -> Unit,
    onGenerateResponseButtonEnabledChange: (Boolean) -> Unit,
    onActiveReactionMenuChange: (ActiveReactionMenu?) -> Unit,
    onShowBuyEnergySheetChange: (Boolean) -> Unit,
    onShowAiWarningClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ChatBackground()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag(BenchmarkTags.ChatScreen),
            containerColor = Color.Transparent, // Make Scaffold transparent to see ChatBackground
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopBarChatScreen(
                    viewState = viewState,
                    actions = actions,
                    modifier = Modifier.fillMaxWidth(),
                    navigation = navigation,
                    isRefreshButtonVisible = isRefreshButtonVisible,
                    focusRequester = focusRequester,
                    focusManager = focusManager,
                    onOpenAllChatVariantsScreen = {
                        navigation.navigateToChatVariantScreen(
                            viewModel.characterId
                        )
                    },
                    characterId = viewModel.characterId
                )
            },
            floatingActionButton = {
                FloatingActionButtonChatScreen(
                    isEnableScrollDownButton = showScrollButton,
                    isEnableSearchMode = chatScreenModeState.isEnableSearchMessageMode,
                    showPlusButton = if (viewState.bottomBarState.textFieldMode == TextFieldMode.MessageInstruction && showFloatingPlusButton) false else showFloatingPlusButton,
                    onClickFloatingPlusButton = { actions.messageInputActions.onToggleBottomSheetMenu() },
                    onMoveUpSearchMessage = { actions.searchActions.moveUpSearchMessage() },
                    onMoveDownSearchMessage = { actions.searchActions.moveDownSearchMessage() },
                    onAutoScrollDown = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                            onScrollButtonVisibleChange(false)
                        }
                    }
                )
            },
            bottomBar = {
                ChatScreenBottomBar(
                    viewState = viewState,
                    actions = actions,
                    focusRequester = focusRequester,
                    focusManager = focusManager,
                    density = density,
                    globalSnackbarPadding = globalSnackbarPadding,
                    isGenerateResponseButtonEnable = isGenerateResponseButtonEnable,
                    myGiftsState = myGiftsState,
                    allGiftsState = allGiftsState,
                    onClickFloatingPlusButton = onFloatingPlusButtonVisibilityChange,
                    onShowAiWarningClick = onShowAiWarningClick
                )
            }
        ) { padding ->
            val showEffect = viewState.activeGiftEffect != null && viewState.activeGiftEffect != com.ensodai.shared.avalon.gifts.models.GiftEffectType.NONE
            val blurRadius by animateDpAsState(
                targetValue = if (showEffect) 4.dp else 0.dp,
                animationSpec = tween(durationMillis = 300)
            )

            Box {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
//                        .blur(blurRadius)
                ) {
                    ChatScreenContentLayer(
                        padding = padding,
                        listState = listState,
                        chatHistory = chatHistory,
                        selectedMessages = selectedMessages,
                        isEditMessageModeEnabled = isEditMessageModeEnabled,
                        currentSelectedSearchMessage = viewState.currentSelectedSearchMessage,
                        fullscreenImageState = viewState.fullscreenImageState,
                        actions = actions,
                        navigation = navigation,
                        listRenderState = listRenderState,
                        messageSlotState = messageSlotState,
                        activeReactionMenu = activeReactionMenu,
                        onScrollButtonVisibleChange = onScrollButtonVisibleChange,
                        onGenerateResponseButtonEnabledChange = onGenerateResponseButtonEnabledChange,
                        onActiveReactionMenuChange = onActiveReactionMenuChange,
                        onBuyEnergyClick = { onShowBuyEnergySheetChange(true) }
                    )
                }

//                LevelUpAnimationOverlay(
//                    state = viewState.levelUpAnimationState,
//                    onAnimationComplete = { actions.dialogActions.onLevelUpAnimationComplete() },
//                    modifier = Modifier
//                        .align(Alignment.TopCenter)
//                        .padding(top = padding.calculateTopPadding())
//                )

                if (viewState.activeGiftEffect != null && viewState.activeGiftEffect != com.ensodai.shared.avalon.gifts.models.GiftEffectType.NONE) {
                    GiftEffectOverlay(
                        effectType = viewState.activeGiftEffect,
                        onAnimationComplete = { actions.dialogActions.onGiftEffectFinished() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        ReactionOverlayHost(
            controller = reactionOverlayController,
            modifier = Modifier.fillMaxSize()
        )

        if (showBuyEnergySheet) {
            BuyEnergyBottomSheet(
                onDismiss = { onShowBuyEnergySheetChange(false) }
            )
        }
    }
}

@Composable
private fun ChatScreenBottomBar(
    viewState: ChatScreenViewState,
    actions: ChatScreenActions,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    density: Density,
    globalSnackbarPadding: MutableState<Dp>,
    isGenerateResponseButtonEnable: Boolean,
    myGiftsState: PaginatorState<GiftPack>,
    allGiftsState: PaginatorState<GiftPack>,
    onClickFloatingPlusButton: (Boolean) -> Unit,
    onShowAiWarningClick: () -> Unit
) {
    var bottomBarHeight by remember { mutableStateOf(0.dp) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                val heightInDp = with(density) { size.height.toDp() }
                if (heightInDp != bottomBarHeight) {
                    bottomBarHeight = heightInDp
                    globalSnackbarPadding.value = heightInDp
                }
            }
    ) {
        BottomBarChatScreen(
            modifier = Modifier.fillMaxWidth(),
            isGenerateResponseButtonEnable = isGenerateResponseButtonEnable,
            viewState = viewState,
            actions = actions,
            focusRequester = focusRequester,
            focusManager = focusManager,
            onClickFloatingPlusButton = onClickFloatingPlusButton,
            myGiftsState = myGiftsState,
            allGiftsState = allGiftsState,
            onShowAiWarningClick = onShowAiWarningClick
        )
    }
}

@Composable
private fun ChatScreenContentLayer(
    padding: PaddingValues,
    listState: LazyListState,
    chatHistory: List<ChatItemUiState>,
    selectedMessages: List<MessageContainerModel>,
    isEditMessageModeEnabled: Boolean,
    currentSelectedSearchMessage: MessageContainerModel?,
    fullscreenImageState: FullscreenImageState,
    actions: ChatScreenActions,
    navigation: Navigation,
    listRenderState: ChatMessageListRenderState,
    messageSlotState: MessageSlotState,
    activeReactionMenu: ActiveReactionMenu?,
    onScrollButtonVisibleChange: (Boolean) -> Unit,
    onGenerateResponseButtonEnabledChange: (Boolean) -> Unit,
    onActiveReactionMenuChange: (ActiveReactionMenu?) -> Unit,
    onBuyEnergyClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        ChatMessageList(
            listState = listState,
            padding = padding,
            chatHistory = chatHistory,
            selectedMessages = selectedMessages,
            isEditMessageModeEnabled = isEditMessageModeEnabled,
            currentSelectedSearchMessage = currentSelectedSearchMessage,
            actions = actions,
            navigation = navigation,
            listRenderState = listRenderState,
            messageSlotState = messageSlotState,
            activeReactionMenu = activeReactionMenu,
            onScrollButtonVisibleChange = onScrollButtonVisibleChange,
            onGenerateResponseButtonEnabledChange = onGenerateResponseButtonEnabledChange,
            onActiveReactionMenuChange = onActiveReactionMenuChange,
            onBuyEnergyClick = onBuyEnergyClick
        )

        ChatScreenOverlays(
            activeReactionMenu = activeReactionMenu,
            fullscreenImageState = fullscreenImageState,
            onDismissReactionMenu = { onActiveReactionMenuChange(null) },
            onDismissFullscreenImage = { actions.dialogActions.onDismissFullscreenImage() }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatMessageList(
    listState: LazyListState,
    padding: PaddingValues,
    chatHistory: List<ChatItemUiState>,
    selectedMessages: List<MessageContainerModel>,
    isEditMessageModeEnabled: Boolean,
    currentSelectedSearchMessage: MessageContainerModel?,
    actions: ChatScreenActions,
    navigation: Navigation,
    listRenderState: ChatMessageListRenderState,
    messageSlotState: MessageSlotState,
    activeReactionMenu: ActiveReactionMenu?,
    onScrollButtonVisibleChange: (Boolean) -> Unit,
    onGenerateResponseButtonEnabledChange: (Boolean) -> Unit,
    onActiveReactionMenuChange: (ActiveReactionMenu?) -> Unit,
    onBuyEnergyClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    ChatLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(BenchmarkTags.ChatMessagesList),
        listState = listState,
        contentPadding = padding,
        message = chatHistory,
        selectedMessages = selectedMessages,
        isEnableEditMessageMode = isEditMessageModeEnabled,
        currentSelectedSearchMessage = currentSelectedSearchMessage,
        onUserScrollDown = { isScrollingDown ->
            onScrollButtonVisibleChange(isScrollingDown && listState.canScrollBackward)
        },
        onMessagesVisible = actions.messageActions.onMessagesVisible
    ) {
        items(
            count = chatHistory.size,
            key = { index -> chatItemKey(chatHistory[index]) }
        ) { index ->
            val item = chatHistory[index]
            val isLastItemWelcome = chatHistory.lastOrNull() is ChatItemUiState.WelcomeCard
            val isGreetingSlot = (index == chatHistory.lastIndex) || (index == chatHistory.lastIndex - 1 && isLastItemWelcome)
            ChatHistoryItem(
                listIndex = index,
                item = item,
                isGreetingSlot = isGreetingSlot,
                actions = actions,
                navigation = navigation,
                listRenderState = listRenderState,
                messageSlotState = messageSlotState,
                activeReactionMenu = activeReactionMenu,
                haptic = haptic,
                modifier = Modifier.animateItem(),
                onGenerateResponseButtonEnabledChange = onGenerateResponseButtonEnabledChange,
                onActiveReactionMenuChange = onActiveReactionMenuChange,
                onBuyEnergyClick = onBuyEnergyClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatHistoryItem(
    listIndex: Int,
    modifier: Modifier,
    item: ChatItemUiState,
    isGreetingSlot: Boolean,
    actions: ChatScreenActions,
    navigation: Navigation,
    listRenderState: ChatMessageListRenderState,
    messageSlotState: MessageSlotState,
    activeReactionMenu: ActiveReactionMenu?,
    haptic: HapticFeedback,
    onGenerateResponseButtonEnabledChange: (Boolean) -> Unit,
    onActiveReactionMenuChange: (ActiveReactionMenu?) -> Unit,
    onBuyEnergyClick: () -> Unit
) {
    when (item) {
        is ChatItemUiState.Message -> ChatMessageSlotItem(
            listIndex = listIndex,
            modifier = modifier,
            item = item,
            isGreetingSlot = isGreetingSlot,
            actions = actions,
            navigation = navigation,
            listRenderState = listRenderState,
            messageSlotState = messageSlotState,
            activeReactionMenu = activeReactionMenu,
            haptic = haptic,
            onGenerateResponseButtonEnabledChange = onGenerateResponseButtonEnabledChange,
            onActiveReactionMenuChange = onActiveReactionMenuChange,
            onBuyEnergyClick = onBuyEnergyClick
        )

        is ChatItemUiState.WelcomeCard -> WelcomeCardListItem(
            info = item.welcomeInfo,
            modifier = modifier
        )
    }
}

@Composable
private fun ChatMessageSlotItem(
    listIndex: Int,
    modifier: Modifier,
    item: ChatItemUiState.Message,
    isGreetingSlot: Boolean = false,
    actions: ChatScreenActions,
    navigation: Navigation,
    listRenderState: ChatMessageListRenderState,
    messageSlotState: MessageSlotState,
    activeReactionMenu: ActiveReactionMenu?,
    haptic: HapticFeedback,
    onGenerateResponseButtonEnabledChange: (Boolean) -> Unit,
    onActiveReactionMenuChange: (ActiveReactionMenu?) -> Unit,
    onBuyEnergyClick: () -> Unit
) {
    val toast = toastNotify()
    val slot = item.slot
    val isThisSlotTyping = messageSlotState.currentTypingSlot == slot.slotId
    val currentTypingStatusForSlot = if (isThisSlotTyping) {
        messageSlotState.typingStatus
    } else {
        SlotTypingStatus.NotTyping
    }
    val isEnableSwipe = slot.containers.size > 1 || slot.slotId == messageSlotState.availableSlotForSwipe
    val isMenuVisible = activeReactionMenu?.messageId == slot.activeContainerId
    val currentMenuOffset = if (isMenuVisible) activeReactionMenu?.tapOffset else Offset.Zero

    val onActiveMessageChanged = remember(actions, slot.slotId) {
        { newActiveMessageId: Uuid ->
            actions.messageVariantActions.onSetActiveMessage(
                slot.slotId,
                newActiveMessageId
            )
        }
    }

    val onCreateNewVariant = remember(actions, slot.slotId) {
        {
            actions.messageVariantActions.onCreateNewMessageVariantForSlot(
                slot.slotId
            )
        }
    }

    val onMidSwipe = remember(haptic) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        if (isGreetingSlot && slot.containers.size > 1) {
            val activeIndex = remember(slot.containers, slot.activeContainerId) {
                slot.containers.indexOfFirst { it.id == slot.activeContainerId }.coerceAtLeast(0)
            }
            GreetingSelectorBar(
                currentIndex = activeIndex,
                totalCount = slot.containers.size,
                onPrevious = {
                    if (activeIndex > 0) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onActiveMessageChanged(slot.containers[activeIndex - 1].id)
                    }
                },
                onNext = {
                    if (activeIndex < slot.containers.size - 1) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onActiveMessageChanged(slot.containers[activeIndex + 1].id)
                    }
                },
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
            )
        }

        SwipeableMessageBubbleHorizontalPager(
            modifier = Modifier.fillMaxWidth(),
        messageSlot = slot,
        slotTypingStatus = messageSlotState.typingStatus,
        isEnableFlowMode = listRenderState.isEnableFlowMode,
        isEnableSwipe = isEnableSwipe,
        messageRarity = messageSlotState.messageRarity,
        typingStatus = currentTypingStatusForSlot,
        onActiveMessageChanged = onActiveMessageChanged,
        onCreateNewVariant = onCreateNewVariant,
        onMidSwipe = onMidSwipe,
        onSwipeInProgress = { onGenerateResponseButtonEnabledChange(!it) }
    ) { activeMessage ->
        val containerUiState = remember(item.containers, activeMessage.id) {
            item.containers.first { it.id == activeMessage.id }
        }

        MessageBubble(
            uiState = containerUiState,
            preset = item.chatRoleplayPreset,
            highlightQuery = listRenderState.inputSearchMessage,
            modifier = Modifier.testTag(BenchmarkTags.chatMessageItem(listIndex)),
            selectedMessagesText = listRenderState.selectedMessageText,
            selectedSystemInstructionText = listRenderState.selectedSystemInstructionText,
            shouldShowAnimatedText = false,
            isEnableSelectedMessagesMode = listRenderState.isSelectedMessagesModeEnabled,
            isSelected = activeMessage.id in listRenderState.selectedMessageIds || slot.slotId == listRenderState.currentSelectedSearchSlotId,
            isEnableEditMessageMode = listRenderState.isEditMessageModeEnabled,
            isEnableSelectMessage = listRenderState.isSelectedMessagesModeEnabled,
            userReaction = activeMessage.userReaction,
            characterReaction = activeMessage.characterReaction,
            userAvatarUrl = listRenderState.userAvatarUrl,
            characterAvatarUrl = listRenderState.characterAvatarUrl,
            onSelectMessage = { messageContainer ->
                actions.messageActions.onSelectMessage(messageContainer)
            },
            onEnableSelectedMessagesMode = { messageContainer ->
                actions.messageActions.onEnableSelectionMessageMode(messageContainer)
            },
            onEditMessageText = { actions.messageActions.onEditMessage(it) },
            onChangeEditedSystemInstruction = {
                actions.messageActions.onChangeEditedSystemInstruction(it)
            },
            onComplaintIconClick = { actions.dialogActions.onShowAlertDialogComplaint() },
            onOpenSettingsScreen = { navigation.navigateToSettingScreenWithGuide() },
            onActionButtonClick = {
                actionButtonClickHandler(
                    systemEventType = it,
                    navigation = navigation
                )
            },
            onBuyEnergyClick = {
                onBuyEnergyClick()
                actions.messageActions.onBuyEnergyClick()
            },
            onToggleReaction = { reaction ->
                actions.messageActions.onToggleReaction(
                    reaction,
                    slot.activeContainerId,
                    slot.slotId
                )
            },
            showReactionPicker = isMenuVisible,
            reactionPickerOffset = currentMenuOffset,
            onShowReactionMenu = { offset ->
                onActiveReactionMenuChange(
                    ActiveReactionMenu(
                        slot.activeContainerId,
                        offset
                    )
                )
            },
            onDismissReactionMenu = { onActiveReactionMenuChange(null) },
            onImageClick = { contentId ->
                actions.messageActions.onImageClick(contentId, slot)
            },
            onSendOutreachFeedback = { isPositive ->
                actions.messageActions.onSendOutreachFeedback(isPositive, activeMessage.id)
            },
            currentPlayingContentId = listRenderState.currentPlayingContentId,
            ttsPlayerState = listRenderState.ttsPlayerState,
            voice = null, // Будет автоматически выбран голос по умолчанию на бэкенде
            onPlaySpeech = { contentId, voiceModel ->
                // Проверка на подписку отключена до лучших времен — доступно для всех юзеров
                // if (listRenderState.premiumStatus == PremiumStatus.FREE) {
                //     navigation.navigateToPaymentScreen()
                // } else {
                actions.messageActions.onPlaySpeech(contentId, voiceModel)
                // }
            },
            onPauseSpeech = { actions.messageActions.onPauseSpeech() },
            generatingTtsMessageIds = listRenderState.generatingTtsMessageIds,
            slot = slot
        )
    }
    }
}

@Composable
private fun GreetingSelectorBar(
    currentIndex: Int,
    totalCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colors.cardBackground.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, colors.strokeSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onPrevious,
                enabled = currentIndex > 0,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = IconsManager.KeyboardArrowLeft,
                    contentDescription = "Previous scenario",
                    modifier = Modifier.size(16.dp),
                    tint = if (currentIndex > 0) colors.textMain else colors.textSecondary.copy(alpha = 0.3f)
                )
            }

            Text(
                text = "СЦЕНАРИЙ ${currentIndex + 1} / $totalCount",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = colors.textMain
            )

            IconButton(
                onClick = onNext,
                enabled = currentIndex < totalCount - 1,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = IconsManager.KeyboardArrowRight,
                    contentDescription = "Next scenario",
                    modifier = Modifier.size(16.dp),
                    tint = if (currentIndex < totalCount - 1) colors.textMain else colors.textSecondary.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WelcomeCardListItem(
    info: WelcomeInfo,
    modifier: Modifier
) {
    val description = info.description
    val images = info.imageUrls

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        if (!description.isNullOrEmpty() || images.isNotEmpty()) {
            WelcomeCard(
                modifier = Modifier.align(Alignment.TopCenter),
                text = description,
                images = images,
                startColor = CardColor.fromKey(info.colors.start),
                endColor = CardColor.fromKey(info.colors.end),
                isEnableLight = info.colors.isLightEnabled
            )
        }
    }
}

@Composable
private fun ChatScreenOverlays(
    activeReactionMenu: ActiveReactionMenu?,
    fullscreenImageState: FullscreenImageState,
    onDismissReactionMenu: () -> Unit,
    onDismissFullscreenImage: () -> Unit
) {
    if (activeReactionMenu != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDismissReactionMenu() },
                        onPress = { }
                    )
                }
        )
    }

    if (fullscreenImageState.isShowDialog && fullscreenImageState.imagePayloads.isNotEmpty()) {
        ImageFullscreenDialog(
            imagePayloads = fullscreenImageState.imagePayloads,
            initialPageIndex = fullscreenImageState.imageIndex,
            onDismissRequest = onDismissFullscreenImage
        )
    }
}

private fun chatItemKey(item: ChatItemUiState): Any =
    when (item) {
        is ChatItemUiState.Message -> item.slotId
        is ChatItemUiState.WelcomeCard -> item.id
    }

@Composable
private fun HandleBackPressChatScreen(
    actions: ChatScreenActions,
    viewState: ChatScreenViewState,
    navigation: Navigation
) {

    AvalonBackHandler(
        viewState.chatScreenModeState.isEnableEditMessageMode ||
                viewState.chatScreenModeState.isEnableSelectedMessagesMode ||
                viewState.chatScreenModeState.isEnableSearchMessageMode ||
                viewState.bottomBarState.bottomBarMode == BottomBarMode.MainBottomSheetMenuMode ||
                viewState.bottomBarState.bottomBarMode == BottomBarMode.GiftBottomSheetMode
    ) {
        if (
            !viewState.chatScreenModeState.isEnableEditMessageMode &&
            !viewState.chatScreenModeState.isEnableSelectedMessagesMode &&
            !viewState.chatScreenModeState.isEnableSearchMessageMode &&
            viewState.bottomBarState.bottomBarMode == BottomBarMode.Idle
        ) {
            navigation.navigateBack()
        }

        if (viewState.chatScreenModeState.isEnableEditMessageMode || viewState.chatScreenModeState.isEnableSelectedMessagesMode) {
            actions.messageActions.onDisableEditMessageMode()
            actions.messageActions.onDisableSelectionMessageMode()
        }

        if (viewState.chatScreenModeState.isEnableSearchMessageMode) {
            actions.searchActions.onDisableSearchMessageMode()
        }
        if (viewState.bottomBarState.bottomBarMode == BottomBarMode.MainBottomSheetMenuMode
            || viewState.bottomBarState.bottomBarMode == BottomBarMode.GiftBottomSheetMode
        ) {
            actions.messageInputActions.onToggleBottomSheetMenu()
        }
    }
}

@Composable
private fun UpdateDateBubbleVisible(
    listState: LazyListState,
    update: (Boolean) -> Unit,
) {
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            update(true)
        } else {
            delay(500)
            update(false)
        }
    }
}

@Composable
fun ShowKeyBoard(
    isEnableSearchMessageMode: Boolean,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
) {
    LaunchedEffect(isEnableSearchMessageMode) {
        if (isEnableSearchMessageMode) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            focusManager.clearFocus()
        }
    }
}

@Composable
private fun WarningItem(
    icon: ImageVector,
    title: String,
    desc: String,
    tintColor: Color,
    colors: AvalonColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(36.dp)
                .background(tintColor.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tintColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun DocumentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: AvalonColors
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.strokeSubtle),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Lucide.FileText,
                contentDescription = text,
                tint = colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
