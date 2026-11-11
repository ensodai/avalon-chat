package com.ensodai.avalon.presentation.presentationUtils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun calculateCustomImeBottomPadding(): Dp {
    val insets = WindowInsets.navigationBars.asPaddingValues()
    val imeInsets = WindowInsets.ime.asPaddingValues()

    return remember {
        derivedStateOf {
            val bottomPadding = insets.calculateBottomPadding()
            val imeBottomPadding = imeInsets.calculateBottomPadding()
            if (imeBottomPadding > bottomPadding) {
                imeBottomPadding - bottomPadding
            } else {
                0.dp
            }
        }
    }.value
}

/**
 * Вычисляет вертикальный сдвиг, необходимый для того, чтобы контент оказался
 * над клавиатурой, учитывая высоту системной навигационной панели.
 *
 * Возвращает Dp, который нужно использовать в Modifier.offset(y = -value).
 */
@Composable
fun calculateCustomImeVerticalOffset(): Dp {
    // Получаем информацию о системных вставках
    val navBarInsets = WindowInsets.navigationBars.asPaddingValues()
    val imeInsets = WindowInsets.ime.asPaddingValues()

    // Используем derivedStateOf для оптимизации:
    // вычисление будет происходить только при изменении insets.
    return remember {
        derivedStateOf {
            val navBarBottom = navBarInsets.calculateBottomPadding()
            val imeBottom = imeInsets.calculateBottomPadding()

            // Наша задача - поднять контент на высоту клавиатуры,
            // которая "перекрывает" навигационную панель.
            if (imeBottom > navBarBottom) {
                imeBottom - navBarBottom
            } else {
                // Клавиатура скрыта или не перекрывает navBar, сдвиг не нужен.
                0.dp
            }
        }
    }.value // .value разворачивает State<Dp> в Dp
}

@Composable
fun bottomSystemButtonsPadding(): Dp {
    return WindowInsets
        .navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
}

@Composable
fun statusBarPaddingDp() = WindowInsets.statusBars
    .only(WindowInsetsSides.Top)
    .asPaddingValues()
    .calculateTopPadding()
