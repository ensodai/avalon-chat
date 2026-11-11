package com.ensodai.avalon.presentation.globalLorebooksScreen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GlobalLorebooksScreen(
    navigation: Navigation,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(),
    searchQuery: String = "",
    isSearchMode: Boolean = false,
    viewModel: GlobalLorebooksViewModel = koinViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()

    LaunchedEffect(searchQuery, isSearchMode) {
        val query = if (isSearchMode) searchQuery else ""
        viewModel.actions.onSearchQueryChanged(query)
    }

    GlobalLorebooksScreenContent(
        viewState = viewState,
        actions = viewModel.actions,
        navigation = navigation,
        modifier = modifier,
        innerPadding = innerPadding
    )
}
