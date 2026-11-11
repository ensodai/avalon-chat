package com.ensodai.avalon.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.ensodai.avalon.theme.presets.TechNoirColors

@Composable
fun GemTheme(
    customColors: AvalonColors = TechNoirColors,
    content: @Composable () -> Unit
) {
    val colorScheme = customColors.toMaterialColorScheme()
    val typography = appTypography()
    ApplySystemBarsStyle(isLightTheme = customColors.isLight)

    CompositionLocalProvider(
        LocalAvalonColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

@Composable
expect fun ApplySystemBarsStyle(isLightTheme: Boolean)
