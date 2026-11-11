package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DateBubble(
    date: String?,
    modifier: Modifier = Modifier
) {
    if (date != null)
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp
        ) {
            Text(
                text = date,
                modifier = Modifier
                    .padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = com.ensodai.avalon.theme.AvalonTheme.colors.textSecondary
            )
        }
}

//@Composable
//fun UpdateDateBubbleValue(
//    listState: LazyListState,
//    message: LazyPagingItems<SlotWithMessages>,
//    updateDate: (String?) -> Unit
//) {
//    val topVisibleMessageTimestamp by remember(listState, message.loadState) {
//        derivedStateOf {
//            if (message.itemCount == 0 || message.loadState.refresh is LoadState.Loading) {
//                null
//            } else {
//                val displayedItems = listState.layoutInfo.visibleItemsInfo
//                if (displayedItems.isEmpty()) {
//                    null
//                } else {
//                    val viewportEndOffset = listState.layoutInfo.viewportEndOffset
//                    val fraction = 0.87f
//                    val scrollThreshold = viewportEndOffset * fraction
//
//                    val topVisibleItem = displayedItems.firstOrNull {
//                        it.offset + it.size >= scrollThreshold
//                    } ?: displayedItems.lastOrNull()
//
//                    if (topVisibleItem?.index == null ||
//                        topVisibleItem.index < 0 ||
//                        topVisibleItem.index >= message.itemCount
//                    ) {
//                        null
//                    } else {
//                        message.peek(topVisibleItem.index)?.messageSlot?.timestamp
//                    }
//                }
//            }
//        }
//    }
//
//    val dayMonthDateFormatter = remember {
//        SimpleDateFormat("dd MMMM", Locale.getDefault())
//    }
//
//    val displayedDate by remember(topVisibleMessageTimestamp) {
//        derivedStateOf {
//            topVisibleMessageTimestamp?.let { dayMonthDateFormatter.format(Date(it.toInstant().toEpochMilli())) }
//        }
//    }
//
//    LaunchedEffect(displayedDate) {
//        updateDate(displayedDate)
//    }
//}