package com.ensodai.avalon.presentation.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp

val LocalSnackbarBottomPadding = compositionLocalOf<MutableState<Dp>> {
    error("No LocalSnackbarBottomPadding provided")
}
