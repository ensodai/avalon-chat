package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun SmartGoogleSignInButton(
    modifier: Modifier,
    onSignInGoogleToken: (String) -> Unit,
    onDismiss: () -> Unit,
    onError: (Exception) -> Unit
) {
    GoogleSignInButtonContent(
        modifier = modifier,
        enabled = true,
        onClick = {
            onError(UnsupportedOperationException("Google Sign-In is not implemented for iOS yet"))
        }
    )
}
