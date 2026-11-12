package app.domain.auth.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.auth.models.AuthResponse
import com.ensodai.shared.avalon.auth.models.GoogleAuthRequest
import app.data.repository.UserRepositoryApp
import app.data.repository.SessionRepository
import app.utils.EnvProvider
import app.plugins.generateAccessToken
import app.service.GiftsService
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.server.plugins.origin
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.header
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Factory
class GoogleSignInUseCase(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepositoryApp,
    private val giftsService: GiftsService
) {
    private val deviceIdMaxLength = 128
    private val deviceNameMaxLength = 128
    private val userAgentMaxLength = 255
    private val ipMaxLength = 64

    suspend fun execute(authRequest: GoogleAuthRequest, applicationRequest: ApplicationRequest): AuthResponse {
        try {
            val transport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory = GsonFactory.getDefaultInstance()

            val audienceList = listOf(
                EnvProvider.getGoogleClientId(),
                EnvProvider.getGoogleAndroidClientId()
            ).filter { it.isNotEmpty() }

            val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(audienceList)
                .build()

            val idToken: GoogleIdToken? = verifier.verify(authRequest.idToken)
            if (idToken != null) {
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
            } else {
                throw Exception("Invalid ID token")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Error verifying token: ${e.message}")
        }
    }
}
