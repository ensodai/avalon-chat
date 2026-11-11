package app.plugins

import app.data.repository.SessionRepository
import app.utils.ACCESS_TOKEN_LIVE
import app.utils.EnvProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject
import java.util.*
import kotlin.time.Clock
import kotlin.uuid.Uuid

const val JWT_AUTH = "auth-jwt"
private val jwtAlgorithm: Algorithm get() = Algorithm.HMAC256(EnvProvider.getJwtSecret())
private const val MY_APP = "myapp"
private const val MY_APP_AUDIENCE = "myapp_audience"
const val USER_ID_CLAIM = "userId"
const val SESSION_ID_CLAIM = "sessionId"

data class VerifiedAccessToken(
    val userId: Uuid,
    val sessionId: Uuid,
    val expiresAtMs: Long
)

fun Application.configureSecurity() {
    // Внедряем репозиторий с помощью Koin
    val sessionRepository: SessionRepository by inject()

    install(Authentication) {
        jwt(JWT_AUTH) {
            realm = "Access to 'myapp'"

            verifier(
                JWT
                    .require(jwtAlgorithm)
                    .withIssuer(MY_APP)
                    .build()
            )

            validate { credential ->
                verifyPayloadAccessToken(credential.payload, sessionRepository)?.let { JWTPrincipal(credential.payload) }
            }
        }
    }
}

suspend fun verifyAccessToken(
    token: String,
    sessionRepository: SessionRepository
): VerifiedAccessToken? {
    val decoded = try {
        JWT
            .require(jwtAlgorithm)
            .withIssuer(MY_APP)
            .build()
            .verify(token)
    } catch (_: Exception) {
        return null
    }

    return verifyDecodedAccessToken(decoded, sessionRepository)
}

private suspend fun verifyPayloadAccessToken(
    payload: Payload,
    sessionRepository: SessionRepository
): VerifiedAccessToken? {
    if (!payload.audience.contains(MY_APP_AUDIENCE)) return null

    val userId = try {
        Uuid.parse(payload.getClaim(USER_ID_CLAIM)?.asString() ?: return null)
    } catch (_: Exception) {
        return null
    }
    val sessionId = try {
        Uuid.parse(payload.getClaim(SESSION_ID_CLAIM)?.asString() ?: return null)
    } catch (_: Exception) {
        return null
    }

    val sessionIsValidInDb = sessionRepository.isSessionValid(
        sessionId = sessionId,
        userId = userId
    )
    if (!sessionIsValidInDb) return null

    return VerifiedAccessToken(
        userId = userId,
        sessionId = sessionId,
        expiresAtMs = payload.expiresAt?.time ?: 0L
    )
}

private suspend fun verifyDecodedAccessToken(
    decoded: DecodedJWT,
    sessionRepository: SessionRepository
): VerifiedAccessToken? {
    if (!decoded.audience.contains(MY_APP_AUDIENCE)) return null

    val userId = try {
        Uuid.parse(decoded.getClaim(USER_ID_CLAIM)?.asString() ?: return null)
    } catch (_: Exception) {
        return null
    }
    val sessionId = try {
        Uuid.parse(decoded.getClaim(SESSION_ID_CLAIM)?.asString() ?: return null)
    } catch (_: Exception) {
        return null
    }

    val sessionIsValidInDb = sessionRepository.isSessionValid(
        sessionId = sessionId,
        userId = userId
    )
    if (!sessionIsValidInDb) return null

    return VerifiedAccessToken(
        userId = userId,
        sessionId = sessionId,
        expiresAtMs = decoded.expiresAt?.time ?: 0L
    )
}

fun generateAccessToken(userId: String, sessionId: String): String {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    val expiresAt = Date(currentTime + ACCESS_TOKEN_LIVE)

    return JWT.create()
        .withIssuer(MY_APP)
        .withAudience(MY_APP_AUDIENCE)
        .withClaim(USER_ID_CLAIM, userId)
        .withClaim(SESSION_ID_CLAIM, sessionId)
        .withIssuedAt(Date(currentTime))
        .withExpiresAt(expiresAt)
        .sign(jwtAlgorithm)
}
fun generateRefreshToken(): String {
    val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    val length = 64
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}
