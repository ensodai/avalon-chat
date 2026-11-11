package com.ensodai.avalon.presentation.splashScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.appUpgradeScreen.PulsingHexagonLogo
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun SplashScreenContent() {
    val colors = AvalonTheme.colors
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PulsingHexagonLogo()
        }
    }
}
