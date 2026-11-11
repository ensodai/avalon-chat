package com.ensodai.avalon.presentation.chatVariantScreen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.data.db.dto.ChatVariant
import com.ensodai.avalon.navigation.avalonCustomSwipe
import com.ensodai.avalon.presentation.chatVariantScreen.action.ChatVariantScreenActions
import com.ensodai.avalon.presentation.chatVariantScreen.component.AlertDialog
import com.ensodai.avalon.presentation.chatVariantScreen.component.BottomSheetMenu
import com.ensodai.avalon.presentation.chatVariantScreen.component.StoryCardItem
import com.ensodai.avalon.presentation.chatVariantScreen.component.TopBarChatVariantScreen
import com.ensodai.avalon.presentation.chatVariantScreen.viewState.ChatVariantScreenViewState
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.presentationUtils.bottomSystemButtonsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatVariantScreenContent(
    viewState: ChatVariantScreenViewState,
    actions: ChatVariantScreenActions,
    navigation: Navigation,
    chatVariants: List<ChatVariant>
) {
    AlertDialog(
        state = viewState.alertDialog,
        actions = actions.alertDialogActions
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBarChatVariantScreen(
                modifier = Modifier
                    .fillMaxWidth(),
                onBackIconClick = { navigation.navigateBack() },
            )
        }
    ) { innerPadding ->

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .avalonCustomSwipe { dragDeltaX ->
                    dragDeltaX > 0
                },
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = bottomSystemButtonsPadding() + 12.dp,
                start = 12.dp,
                end = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                count = chatVariants.size,
                key = { index -> chatVariants[index].id }
            ) { currentChatIndex ->
                val story = chatVariants[currentChatIndex]
                val loadingState = if (story.id == viewState.selectedStory?.id) {
                    viewState.loadingState
                } else {
                    LoadingState.Idle
                }
                StoryCardItem(
                    modifier = Modifier.animateItem(
                        fadeInSpec = null,
                        placementSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntOffset.VisibilityThreshold
                        ),
                        fadeOutSpec = null
                    ),
                    story = story,
                    isActiveVariant = story.id == viewState.activeChatVariantId,
                    loadingState = loadingState,
                    onClickStory = { actions.activateChatVariant(it) },
                    onLongClickStory = {
                        actions.onShowBottomSheet(it)
                    }
                )
            }
        }
    }

    BottomSheetMenu(
        loadingState = viewState.loadingState,
        state = viewState.bottomSheetState,
        actions = actions.bottomSheetActions,
    )
}
