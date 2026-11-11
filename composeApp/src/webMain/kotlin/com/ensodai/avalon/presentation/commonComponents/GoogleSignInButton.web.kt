package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.browser.window

@JsFun("(value) => encodeURIComponent(value)")
private external fun encodeURIComponent(value: String): String

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
            val clientId = "98220617037-7josi11b0ivukm65hpc8rr3f9qgd9215.apps.googleusercontent.com"
            val redirectUri = "${window.location.origin}/"
            val authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=$clientId" +
                    "&redirect_uri=${encodeURIComponent(redirectUri)}" +
                    "&response_type=code" +
                    "&scope=openid%20email%20profile" +
                    "&access_type=offline"

            window.location.href = authUrl
        }
    )
}
