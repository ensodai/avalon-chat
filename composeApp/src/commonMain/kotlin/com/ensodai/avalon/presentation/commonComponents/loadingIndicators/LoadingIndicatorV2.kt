package com.ensodai.avalon.presentation.commonComponents.loadingIndicators

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LoadingIndicatorV2(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true
) {
    CircularProgressIndicator(modifier = modifier)
}
