package app.domain.auth.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.auth.models.AuthResponse
import app.data.repository.UserRepositoryApp
import app.data.repository.SessionRepository
import app.plugins.generateAccessToken
import app.service.GiftsService
import app.utils.EnvProvider
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.server.plugins.origin
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Factory
class WebGoogleSignInUseCase(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepositoryApp,
    private val giftsService: GiftsService,
    private val httpClient: HttpClient
) {
    private val webClientId = "98220617037-7josi11b0ivukm65hpc8rr3f9qgd9215.apps.googleusercontent.com"
    private val webClientSecret = EnvProvider.getGoogleWebClientSecret()

    private val deviceIdMaxLength = 128
    private val deviceNameMaxLength = 128
    private val userAgentMaxLength = 255
    private val ipMaxLength = 64

    suspend fun execute(
        authRequest: com.ensodai.shared.avalon.auth.models.WebGoogleAuthRequest,
        applicationRequest: ApplicationRequest
    ): AuthResponse {
        try {
            val tokenResponse = exchangeCodeForTokens(
                code = authRequest.code,
                redirectUri = authRequest.redirectUri
            )

            val idTokenString = tokenResponse.idToken
                ?: throw Exception("Google did not return id_token")

            val idToken = verifyIdToken(idTokenString)
                ?: throw Exception("Invalid ID token")

            val payload = idToken.payload
            val googleSub = payload.subject
            val name = payload["name"] as? String ?: "User"
            val email = payload["email"] as? String
            val avatarUrl = payload["picture"] as? String

            val user = userRepository.findOrCreateUser(
                googleSub = googleSub,
                name = name,
                email = email,
                avatarUrl = avatarUrl
            )

            try {
                giftsService.savePackForUser(
                    userId = user.id,
                    packId = Uuid.parse("d1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d")
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val userAgent = applicationRequest.header("User-Agent")?.take(userAgentMaxLength)
            val ip = applicationRequest.origin.remoteHost.take(ipMaxLength)
            val deviceId = authRequest.deviceId.take(deviceIdMaxLength)
            val deviceName = authRequest.deviceName.take(deviceNameMaxLength)

            val newSession = sessionRepository.createNewSession(
                userId = user.id,
                deviceId = deviceId,
                deviceName = deviceName,
                userAgent = userAgent,
                ip = ip,
                fcmToken = authRequest.fcmToken
            )

            val accessToken = generateAccessToken(
                userId = user.id.toString(),
                sessionId = newSession.id.value.toString()
            )

            return AuthResponse(
                accessToken = accessToken,
                refreshToken = newSession.refreshToken,
                userId = user.id
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Error during web sign in: ${e.message}")
        }
    }

    private suspend fun exchangeCodeForTokens(
        code: String,
        redirectUri: String
    ): GoogleTokenResponse {
        return httpClient.post("https://oauth2.googleapis.com/token") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "code" to code,
                    "client_id" to webClientId,
                    "client_secret" to webClientSecret,
                    "redirect_uri" to redirectUri,
                    "grant_type" to "authorization_code"
                ).formUrlEncode()
            )
        }.body<GoogleTokenResponse>()
    }

    private suspend fun verifyIdToken(idTokenString: String): GoogleIdToken? {
        return withContext(Dispatchers.IO) {
            val transport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory = GsonFactory.getDefaultInstance()
            GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(
                    listOf(
                        webClientId,
                        "98220617037-e5v6ampldneau7iiev44630sit8a2lj3.apps.googleusercontent.com"
                    )
                )
                .build()
                .verify(idTokenString)
        }
    }
}

@Serializable
data class GoogleTokenResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("id_token") val idToken: String? = null,
    @SerialName("expires_in") val expiresIn: Int? = null,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("scope") val scope: String? = null,
)
