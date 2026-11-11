package com.ensodai.avalon.presentation.commonComponents

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.ensodai.avalon.data.network.HybridGoogleAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun SmartGoogleSignInButton(
    modifier: Modifier,
    onSignInGoogleToken: (String) -> Unit,
    onDismiss: () -> Unit,
    onError: (Exception) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isGoogleAuthInProgress by remember { mutableStateOf(false) }

    val authManager = remember { HybridGoogleAuth(context) }
    val activity = remember(context) { context.findActivity() }

    val browserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            coroutineScope.launch {
                isGoogleAuthInProgress = true
                try {
                    val token = authManager.handleAppAuthResult(result.data!!)
                    withContext(Dispatchers.Main) {
                        onSignInGoogleToken(token)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { onError(e) }
                    isGoogleAuthInProgress = false
                }
            }
        } else {
            onDismiss()
            isGoogleAuthInProgress = false
        }
    }

    val clickEnabled = !isGoogleAuthInProgress && activity != null

    GoogleSignInButtonContent(
        modifier = modifier,
        enabled = clickEnabled,
        onClick = {
            val currentActivity = activity
            if (currentActivity == null) return@GoogleSignInButtonContent

            isGoogleAuthInProgress = true

            coroutineScope.launch {
                try {
                    val nativeToken = authManager.signInNative(currentActivity)

                    if (nativeToken != null) {
                        onSignInGoogleToken(nativeToken)
                    } else {
                        val intent = authManager.getAppAuthIntent()
                        browserLauncher.launch(intent)
                    }
                } catch (e: GetCredentialCancellationException) {
                    onDismiss()
                    isGoogleAuthInProgress = false
                } catch (e: Exception) {
                    onError(e)
                    isGoogleAuthInProgress = false
                }
            }
        }
    )

    DisposableEffect(Unit) { onDispose { authManager.dispose() } }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
