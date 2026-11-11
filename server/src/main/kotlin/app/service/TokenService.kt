package app.service

import app.plugins.USER_ID_CLAIM
import com.auth0.jwt.JWT
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class TokenService {
    fun getUserIdFromToken(token: String): Uuid? {
        return try {
            val decodedJWT = JWT.decode(token)
            val userIdString = decodedJWT.getClaim(USER_ID_CLAIM)?.asString()
            userIdString?.let { Uuid.parse(it) }
        } catch (e: Exception) {
            // JWTDecodeException, IllegalArgumentException и др.
            null
        }
    }
}