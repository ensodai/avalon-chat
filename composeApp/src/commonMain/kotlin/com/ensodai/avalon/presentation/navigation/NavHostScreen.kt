package com.ensodai.avalon.presentation.navigation

import androidx.compose.runtime.Composable
import com.ensodai.avalon.App

/**
 * Legacy entry points kept as thin wrappers during the migration from Fragula/Navigation2
 * to the new Avalon/navigation3 stack.
 */
@Composable
fun NavHostScreenComposeNavigation() {
    App()
}

@Composable
fun NavHostScreenFragulaNavigation() {
    App()
}
