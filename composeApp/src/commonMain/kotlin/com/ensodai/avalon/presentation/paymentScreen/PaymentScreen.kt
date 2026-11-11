package com.ensodai.avalon.presentation.paymentScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PaymentScreen(
    navigation: Navigation,
    viewModel: PaymentScreenViewModel = koinViewModel(),
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {

    val viewState by viewModel.viewState.collectAsState()
    val actions = remember { viewModel.actions }

    PaymentScreenContent(
        viewState = viewState,
        actions = actions,
        navigation = navigation,
        onEnableSwipeable = onEnableSwipeable,
        onDisableSwipeable = onDisableSwipeable
    )
}