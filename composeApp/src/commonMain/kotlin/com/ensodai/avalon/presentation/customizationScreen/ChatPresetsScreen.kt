package com.ensodai.avalon.presentation.customizationScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatPresetsScreen(
    navigation: Navigation,
    viewModel: ChatPresetsViewModel = koinViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()

    ChatPresetsScreenContent(
        viewState = viewState,
        navigation = navigation,
        onPresetSelected = { preset -> viewModel.selectPreset(preset) },
        onFontSizeChanged = { size -> viewModel.updateBubbleFontSize(size) },
        onShapeTypeChanged = { shape -> viewModel.updateBubbleShapeType(shape) },
        onCornerRadiusChanged = { radius -> viewModel.updateBubbleCornerRadius(radius) },
        onBubbleColorSchemeSelected = { scheme -> viewModel.selectBubbleColorScheme(scheme) }
    )
}
