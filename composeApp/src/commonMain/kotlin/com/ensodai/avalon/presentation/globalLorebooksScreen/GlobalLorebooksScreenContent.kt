package com.ensodai.avalon.presentation.globalLorebooksScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.global_lorebooks_empty_catalog
import com.ensodai.avalon.generated.resources.global_lorebooks_empty_search
import com.ensodai.avalon.presentation.globalLorebooksScreen.action.GlobalLorebooksScreenActions
import com.ensodai.avalon.presentation.globalLorebooksScreen.component.GlobalLorebookCard
import com.ensodai.avalon.presentation.globalLorebooksScreen.viewState.GlobalLorebooksScreenViewState
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.presentationUtils.bottomSystemButtonsPadding
import com.ensodai.avalon.theme.AvalonTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalLorebooksScreenContent(
    viewState: GlobalLorebooksScreenViewState,
    actions: GlobalLorebooksScreenActions,
    navigation: Navigation,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val colors = AvalonTheme.colors
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    // Pagination detection
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            actions.onLoadMore()
        }
    }

    // Auto-dismiss toast/snackbar
    LaunchedEffect(viewState.actionFeedbackMessage) {
        if (viewState.actionFeedbackMessage != null) {
            delay(3000)
            actions.onDismissFeedbackMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = viewState.isRefreshing,
            onRefresh = { actions.onRefresh() },
            state = pullToRefreshState,
            indicator = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    PullToRefreshDefaults.Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = viewState.isRefreshing,
                        state = pullToRefreshState,
                        maxDistance = 88.dp
                    )
                }
            }
        ) {
            if (viewState.isCatalogLoading && viewState.catalogBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.neonAccent)
                }
            } else if (viewState.catalogBooks.isEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = pullToRefreshState.distanceFraction.dp * 40),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding() + 8.dp,
                        bottom = bottomSystemButtonsPadding()
                    )
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 96.dp, start = 32.dp, end = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val emptyText = if (viewState.searchQuery.isNotBlank()) {
                                stringResource(Res.string.global_lorebooks_empty_search)
                            } else {
                                stringResource(Res.string.global_lorebooks_empty_catalog)
                            }
                            Text(
                                text = emptyText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding() + 8.dp + pullToRefreshState.distanceFraction.dp * 40,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = bottomSystemButtonsPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = viewState.catalogBooks,
                        key = { it.id.toString() }
                    ) { book ->
                        GlobalLorebookCard(
                            lorebook = book,
                            onClick = { navigation.navigateToLorebookDetailsScreen(it) },
                            onToggleSave = { id, save -> actions.onToggleSaveToLibrary(id, save) },
                            onFork = { actions.onForkLorebook(it) }
                        )
                    }

                    if (viewState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = colors.neonAccent,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Feedback toast notification overlay
        AnimatedVisibility(
            visible = viewState.actionFeedbackMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = innerPadding.calculateTopPadding() + 8.dp)
                .padding(horizontal = 24.dp)
        ) {
            viewState.actionFeedbackMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceHigh.copy(alpha = 0.95f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = colors.neonAccent,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
