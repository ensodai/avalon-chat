package com.ensodai.avalon.presentation.chatScreen.viewState

import com.ensodai.avalon.utils.EMPTY_STRING

data class SearchMessagesState(
    val inputSearchMessageQuery: String = EMPTY_STRING,
    val searchMessagesIds: List<Int> = emptyList(),
    val currentSelectedSearchMessageId: Int? = null,
) {
    val selectedMessageNumber: Int = searchMessagesIds.indexOf(currentSelectedSearchMessageId)
}