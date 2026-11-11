package com.ensodai.avalon.presentation.chatScreen.component.chat

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.domain.models.MessageContainerModel
import com.ensodai.avalon.presentation.chatScreen.viewState.ChatItemUiState
import com.ensodai.shared.avalon.chat.models.MessageRole
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.uuid.Uuid

@Composable
fun ChatLazyColumn(
    modifier: Modifier,
    listState: LazyListState,
    contentPadding: PaddingValues,
    message: List<ChatItemUiState>,
    currentSelectedSearchMessage: MessageContainerModel?,
    isEnableEditMessageMode: Boolean,
    selectedMessages: List<MessageContainerModel>,
    onUserScrollDown: (Boolean) -> Unit,
    onMessagesVisible: (List<Uuid>) -> Unit = {},
    content: LazyListScope.() -> Unit
) {
    LaunchedEffect(listState, message) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .map { visibleItems ->
                visibleItems.mapNotNull { visibleItemInfo ->
                    val index = visibleItemInfo.index
                    if (index in message.indices) {
                        val item = message[index]
                        if (item is ChatItemUiState.Message) {
                            val activeContainer = item.containers.firstOrNull { it.id == item.activeContainerId }
                            if (activeContainer != null && activeContainer.role == MessageRole.MODEL && !activeContainer.isRead) {
                                item.activeContainerId
                            } else null
                        } else null
                    } else null
                }
            }
            .distinctUntilChanged()
            .collect { unreadIds ->
                if (unreadIds.isNotEmpty()) {
                    onMessagesVisible(unreadIds)
                }
            }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -1f) {
                    onUserScrollDown(true)
                }
                if (available.y > 1f) {
                    onUserScrollDown(false)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (available.y < -1f) {
                    onUserScrollDown(true)
                }
                if (available.y > 1f) {
                    onUserScrollDown(false)
                }
                return super.onPreFling(available)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y < -1f) {
                    onUserScrollDown(true)
                }
                if (available.y > 1f) {
                    onUserScrollDown(false)
                }
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    AutoscrollToSelectedSearchMessage(
        currentSelectedSearchMessage = currentSelectedSearchMessage,
        message = message,
        listState = listState,
    )

    AutoscrollToEditedMessage(
        isEnableEditMessageMode = isEnableEditMessageMode,
        selectedMessages = selectedMessages,
        message = message,
        listState = listState
    )
    AutoScrollForNewMessages(
        messages = message,
        listState = listState
    )

    LazyColumn(
        state = listState,
        reverseLayout = true,
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp
        ),
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.Bottom
        ),
        content = content
    )
}

@Composable
fun AutoScrollForNewMessages(
    messages: List<ChatItemUiState>,
    listState: LazyListState
) {
    val latestMessageId = remember(messages) {
        messages.firstOrNull()?.id
    }

    var wasAtBottom by remember(listState) { mutableStateOf(true) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isNearBottomForChat() }
            .distinctUntilChanged()
            .collect { isNearBottom ->
                wasAtBottom = isNearBottom
            }
    }

    LaunchedEffect(latestMessageId) {
        if (latestMessageId != null && wasAtBottom) {
            listState.scrollToBottomTelegramStyle()
        }
    }
}

private fun LazyListState.isNearBottomForChat(): Boolean {
    val result = firstVisibleItemIndex <= BOTTOM_INDEX_TOLERANCE &&
            firstVisibleItemScrollOffset <= BOTTOM_TOLERANCE_PX
    return result
}

suspend fun LazyListState.scrollToBottomTelegramStyle() {

    if (firstVisibleItemIndex > 2) {
        scrollToItem(2)
    }

    val telegramCurve = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)

    val animState = AnimationState(initialValue = 0f)

    try {
        scroll {
            var previousValue = 0f

            val target = -1500f

            animState.animateTo(
                targetValue = target,
                animationSpec = TweenSpec(
                    durationMillis = 400,
                    easing = telegramCurve
                )
            ) {

                val delta = value - previousValue

                val consumed = scrollBy(delta)

                if (abs(delta - consumed) > 0.5f) {
                    cancelAnimation()
                }

                previousValue = value
            }
        }
    } catch (e: CancellationException) {
        // Игнорируем штатную отмену анимации при достижении края
    }
}

private const val BOTTOM_TOLERANCE_PX = 120
private const val BOTTOM_INDEX_TOLERANCE = 2


@Composable
fun AutoscrollToFirstItemWhenScreenStarted(itemCount: Int, listState: LazyListState) {
    LaunchedEffect(itemCount) {
        if (itemCount > 0) {
            listState.animateScrollToItem(0)
        }
    }
}

@Composable
private fun AutoscrollToSelectedSearchMessage(
    currentSelectedSearchMessage: MessageContainerModel?,
    message: List<ChatItemUiState>,
    listState: LazyListState,
    desiredOffset: Int = 900
) {
    LaunchedEffect(currentSelectedSearchMessage) {
        if (currentSelectedSearchMessage == null) return@LaunchedEffect

        // Находим индекс нужного элемента по его id.
        val targetIndex = (0 until message.size).firstOrNull { index ->
            message[index].id == currentSelectedSearchMessage.id.toString()
        } ?: return@LaunchedEffect

        // Ждём, пока элемент загрузится (даже если он ещё не виден).
//        while (message.peek(targetIndex) == null) {
//            delay(16)
//        }

        // Запускаем анимированную прокрутку в отдельной корутине.
        // Эта анимация служит для предварительного перемещения элемента в область экрана.
        val scrollJob = launch {
            try {
                listState.animateScrollToItem(targetIndex, scrollOffset = 0)
            } catch (e: CancellationException) {
                // Если анимация отменена, ловим CancellationException.
            }
        }

        // Следим за layoutInfo и ждём, пока целевой элемент станет измеренным и появится среди видимых.
        val itemInfo = snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetIndex }
        }
            .filterNotNull() // ждём, пока элемент появится
            .first()

        // Как только получили информацию о размере, отменяем первоначальную анимацию (если она ещё активна).
        if (scrollJob.isActive) {
            scrollJob.cancelAndJoin()
        }

        // Вычисляем разницу между «визуальным верхом» элемента и нужной позицией.
        val delta = (itemInfo.offset + itemInfo.size - desiredOffset).toFloat()
        if (delta != 0f) {
            // Анимированно корректируем позицию списка.
            listState.animateScrollBy(delta)
        }
    }
}

//стабильная
//@Composable
//private fun AutoscrollToSelectedSearchMessage(
//    currentSelectedSearchMessage: Int?,
//    chatHistory: LazyPagingItems<ChatHistory>,
//    listState: LazyListState
//) {
//    LaunchedEffect(currentSelectedSearchMessage) {
//        if (currentSelectedSearchMessage == null) return@LaunchedEffect
//
//        snapshotFlow {
//            (0 until chatHistory.itemCount).firstOrNull { index ->
//                chatHistory.peek(index)?.id == currentSelectedSearchMessage
//            }
//        }
//            .collect { selectedMessageIndex ->
//                if (selectedMessageIndex != null) {
//                    val centerOffset = (listState.layoutInfo.viewportEndOffset / 3).toInt()
//                    listState.animateScrollToItem(
//                        index = selectedMessageIndex,
//                        scrollOffset = -centerOffset
//                    )
//                }
//            }
//    }
//}

@Composable
private fun AutoscrollToEditedMessage(
    isEnableEditMessageMode: Boolean,
    selectedMessages: List<MessageContainerModel>,
    message: List<ChatItemUiState>,
    listState: LazyListState
) {
    LaunchedEffect(isEnableEditMessageMode, selectedMessages.firstOrNull()) {
        if (isEnableEditMessageMode && selectedMessages.isNotEmpty()) {

            val editedMessageId = selectedMessages.firstOrNull()?.slotId

            if (editedMessageId != null) {

                val editedMessageIndex = (0 until message.size).firstOrNull { index ->
                    message[index].id == editedMessageId.toString()
                }

                if (editedMessageIndex != null) {
                    listState.animateScrollToItem(editedMessageIndex)
                }
            }
        }
    }
}
