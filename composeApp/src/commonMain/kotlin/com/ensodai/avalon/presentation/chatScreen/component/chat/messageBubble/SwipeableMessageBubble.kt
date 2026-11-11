package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.avalon.domain.models.ChatMessageModel
import com.ensodai.avalon.domain.models.MessageContainerModel
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.avalon.navigation.avalonCustomSwipe
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.slotMessageCounter.SlotMessageCounter
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.swipeableMessageBubble.SwipeToAction
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.swipeableMessageBubble.rememberSwipeToActionState
import com.ensodai.avalon.presentation.chatScreen.viewState.SlotTypingStatus
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.uuid.Uuid

import com.ensodai.shared.avalon.chat.models.TextPayload
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType

@Composable
fun SwipeableMessageBubbleHorizontalPager(
    modifier: Modifier = Modifier,
    messageSlot: ChatMessageModel,
    slotTypingStatus: SlotTypingStatus,
    isEnableFlowMode: Boolean,
    isEnableSwipe: Boolean,
    messageRarity: Rarity,
    typingStatus: SlotTypingStatus,
    onActiveMessageChanged: (newActiveMessageId: Uuid) -> Unit,
    onCreateNewVariant: () -> Unit,
    onMidSwipe: () -> Unit,
    onSwipeInProgress: (isSwiping: Boolean) -> Unit,
    messageContent: @Composable (
        message: MessageContainerModel
    ) -> Unit
) {

    val activeMessageIndex = remember(messageSlot.containers, messageSlot.activeContainerId) {
        val index = messageSlot.containers.indexOfFirst { it.id == messageSlot.activeContainerId }
        if (index != -1) index else 0
    }
    val pagerState = rememberPagerState(
        initialPage = activeMessageIndex,
        pageCount = { messageSlot.containers.size }
    )
    val hasModelMessage = remember(messageSlot.containers) {
        messageSlot.containers.any { it.role == MessageRole.MODEL }
    }
    val swipeToActionState = rememberSwipeToActionState { isSwiping ->
        onSwipeInProgress(isSwiping)
    }
    val currentActiveContainerId by rememberUpdatedState(messageSlot.activeContainerId)
    val currentOnActiveMessageChanged by rememberUpdatedState(onActiveMessageChanged)
    var isSyncingPagerFromExternalState by remember { mutableStateOf(false) }
    var isWaitingForChunks by remember { mutableStateOf(false) }

    val handleCreateNewVariant = remember(onCreateNewVariant, isEnableFlowMode) {
        {
            if (isEnableFlowMode) {
                isWaitingForChunks = true
            }
            onCreateNewVariant()
        }
    }

    val isCounterVisible =
        messageSlot.containers.size > 1 || (messageSlot.containers.size == 1 && swipeToActionState.offset < -20f)
    val isEnableSwipeToAction = messageSlot.containers.size <= 19


    val activeMessageText = remember(messageSlot.containers, currentActiveContainerId) {
        val container = messageSlot.containers.find { it.id == currentActiveContainerId }
        val textContent = container?.content?.firstOrNull { it.type == AvalonMessageContentType.TEXT }
        (textContent?.payload as? TextPayload)?.text ?: ""
    }

    val targetPage = remember(activeMessageIndex, activeMessageText, isEnableFlowMode) {
        if (isEnableFlowMode && activeMessageText.isEmpty() && activeMessageIndex > pagerState.currentPage) {
            pagerState.currentPage
        } else {
            activeMessageIndex
        }
    }
    LaunchedEffect(isWaitingForChunks) {
        if (isWaitingForChunks) {
            kotlinx.coroutines.delay(8000)
            if (isWaitingForChunks) {
                isWaitingForChunks = false
                swipeToActionState.animateReset()
            }
        }
    }

    LaunchedEffect(targetPage, pagerState.pageCount) {
        if (targetPage < pagerState.pageCount && pagerState.currentPage != targetPage) {
            isSyncingPagerFromExternalState = true
            try {
                pagerState.animateScrollToPage(targetPage)
            } finally {
                isSyncingPagerFromExternalState = false
            }
        }
    }

    LaunchedEffect(pagerState, messageSlot.containers, isEnableFlowMode, activeMessageText) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (isSyncingPagerFromExternalState) return@collect

                // Do not sync pager page back to DB if we are currently generating an empty flow variant
                val isGeneratingEmptyVariant = isEnableFlowMode && 
                        activeMessageText.isEmpty() && 
                        messageSlot.containers.size > 1
                if (isGeneratingEmptyVariant) return@collect

                val targetMessage = messageSlot.containers.getOrNull(page)
                if (targetMessage != null && targetMessage.id != currentActiveContainerId) {
                    currentOnActiveMessageChanged(targetMessage.id)
                }
            }
    }

    LaunchedEffect(messageSlot.containers.size) {
        // Во Flow Mode баббл должен сбросить натяжение сразу, как только появилось новое сообщение (контейнер)
        if (isEnableFlowMode && isWaitingForChunks) {
            isWaitingForChunks = false
            swipeToActionState.animateReset()
        }
    }

    LaunchedEffect(slotTypingStatus) {
        if (slotTypingStatus == SlotTypingStatus.NotTyping) {
            // Unconditionally reset the bubble offset when typing is fully complete
            // This works for both Classic Mode and Flow Mode without race conditions on activeMessageText.
            isWaitingForChunks = false
            swipeToActionState.animateReset()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .avalonCustomSwipe { dragDeltaX ->
                    (pagerState.currentPage == 0 || !isEnableSwipe) && dragDeltaX > 0
                },
            key = { index -> messageSlot.containers[index].id },
            verticalAlignment = Alignment.Bottom,
            userScrollEnabled = isEnableSwipe,
            overscrollEffect = null
        ) { pageIndex ->
            val isLastPage = pageIndex == messageSlot.containers.size - 1
            val message = messageSlot.containers[pageIndex]

            if (isLastPage) {
                SwipeToAction(
                    state = swipeToActionState,
                    isEnableSwipe = isEnableSwipe,
                    modifier = Modifier,
                    isEnableSwipeToAction = isEnableSwipeToAction,
                    stayAtMaxOffset = isEnableFlowMode,
                    onSwiped = handleCreateNewVariant,
                    onMidSwipe = { onMidSwipe() },
                ) {
                    messageContent(message)
                }
            } else {
                messageContent(message)
            }
        }

        if (hasModelMessage && isCounterVisible) {
            val shouldHideTyping = isEnableFlowMode && activeMessageText.isNotEmpty()
            SlotMessageCounter(
                modifier = Modifier.align(Alignment.BottomEnd),
                messageRarity = messageRarity,
                typingStatus = if (shouldHideTyping) SlotTypingStatus.NotTyping else typingStatus,
                activeMessageIndex = activeMessageIndex + 1,
                allMessageCount = messageSlot.containers.size,
                swipeToActionState = swipeToActionState,
            )
        }
    }
}
