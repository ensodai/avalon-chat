package com.ensodai.avalon.presentation.mainScreen.action

import com.ensodai.avalon.data.db.dto.CategoryItemDto
import com.ensodai.avalon.presentation.core.BaseActions
import com.ensodai.avalon.presentation.mainScreen.viewState.SearchLoadingState
import kotlin.uuid.Uuid

data class MainScreenActions(
    val onClickChat: (Uuid) -> Unit,
    val onEnableSelectionChatsModel: (Uuid) -> Unit,
    val setDefaultScreenMode: () -> Unit,
    val onDeleteSelectedChats: () -> Unit,
    val onCopySelectedChat: () -> Unit,
    val onClickTestButton: () -> Unit,
    val onCloseAlertDialog: () -> Unit,
    val auth: (String?) -> Unit,
    val onOpenUserChatsScreen: () -> Unit,
    val onOpenAllCharactersChatsScreen: () -> Unit,
    val onSelectCategory: (category: CategoryItemDto) -> Unit,
    val onArchivedSelectedChats: () -> Unit,
    val onCloseTelegramReminder: () -> Unit,
    val onCloseOpenApiTransitionAnnouncement: () -> Unit,
    val onOpenOpenApiTransitionBottomSheet: () -> Unit,
    val onDismissOpenApiTransitionBottomSheet: () -> Unit,
    val refreshCategoryCounters: () -> Unit,
    val onSearchMode: () -> Unit,
    val onSelectScreenPage: (Int) -> Unit,
    val onChangeSearchText: (String) -> Unit,
    val updateSearchState: (SearchLoadingState) -> Unit,
    val testButton: () -> Unit
) : BaseActions()
