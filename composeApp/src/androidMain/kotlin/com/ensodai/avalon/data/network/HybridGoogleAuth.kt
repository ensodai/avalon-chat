package com.ensodai.avalon.data.network

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.ensodai.avalon.utils.L
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.uuid.Uuid

class HybridGoogleAuth(private val context: Context) {
    private val webClientId =
        "98220617037-7josi11b0ivukm65hpc8rr3f9qgd9215.apps.googleusercontent.com"
    private val androidClientId =
        "98220617037-e5v6ampldneau7iiev44630sit8a2lj3.apps.googleusercontent.com"
    private val redirectUri =
        "com.googleusercontent.apps.98220617037-e5v6ampldneau7iiev44630sit8a2lj3:/oauth2redirect".toUri()

    private val credentialManager = CredentialManager.create(context)
    private val authService = AuthorizationService(context)

    suspend fun signInNative(activity: android.app.Activity): String? {
        if (!isGooglePlayServicesAvailable()) {
            L.d("HybridGoogleAuth", "Google Services missing -> Fallback to AppAuth")
            return null
        }

        try {
            val signInOption = GetSignInWithGoogleOption.Builder(webClientId)
                .setNonce(Uuid.random().toString())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            return parseCredentialResponse(result)
        } catch (e: Exception) {
            when (e) {
                is GetCredentialCancellationException -> throw e
                is NoCredentialException -> {
                    L.d("HybridGoogleAuth", "No native credentials -> Fallback to AppAuth")
                    return null
                }

                else -> {
                    L.e("HybridGoogleAuth", "Native sign in failed: ${e.stackTraceToString()}")
                    return null
                }
            }
        }
    }

    fun getAppAuthIntent(): Intent {
        val serviceConfig = AuthorizationServiceConfiguration(
            "https://accounts.google.com/o/oauth2/v2/auth".toUri(),
            "https://www.googleapis.com/oauth2/v4/token".toUri()
        )

        val authRequest = AuthorizationRequest.Builder(
            serviceConfig,
            androidClientId,
            ResponseTypeValues.CODE,
            redirectUri
        )
            .setScopes("openid", "email", "profile")
            .build()

        return authService.getAuthorizationRequestIntent(authRequest)
    }

    suspend fun handleAppAuthResult(intent: Intent): String {
        return suspendCoroutine { cont ->
            val response = AuthorizationResponse.fromIntent(intent)
            val ex = AuthorizationException.fromIntent(intent)

            if (response != null) {
                authService.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse, exception ->
                    val idToken = tokenResponse?.idToken
                    if (idToken != null) {
                        cont.resume(idToken)
                    } else {
                        cont.resumeWithException(exception ?: Exception("No ID Token returned"))
                    }
                }
            } else {
                cont.resumeWithException(ex ?: Exception("Auth Failed"))
            }
        }
    }

    private fun parseCredentialResponse(response: GetCredentialResponse): String? {
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return googleIdTokenCredential.idToken
        }
        return null
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val api = GoogleApiAvailability.getInstance()
        return api.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    fun dispose() {
        authService.dispose()
    }
}
