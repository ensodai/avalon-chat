package com.ensodai.avalon.presentation.lorebookDetailsScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun LorebookDetailsScreen(
    navigation: Navigation,
    lorebookId: Uuid,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LorebookDetailsViewModel = koinViewModel { parametersOf(lorebookId) }
) {
    val viewState by viewModel.viewState.collectAsState()

    LorebookDetailsScreenContent(
        viewState = viewState,
        actions = viewModel.actions,
        navigation = navigation,
        onEnableSwipeable = onEnableSwipeable,
        onDisableSwipeable = onDisableSwipeable,
        modifier = modifier
    )
}
