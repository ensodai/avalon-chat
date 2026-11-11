@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package app.apiv4.services.impl

import app.domain.auth.usecases.GoogleSignInUseCase
import app.domain.auth.usecases.WebGoogleSignInUseCase
import app.domain.auth.usecases.RefreshTokenUseCase
import app.domain.auth.usecases.LogoutUseCase
import app.domain.auth.usecases.DeleteUserAccountUseCase
import com.ensodai.shared.avalon.auth.models.*
import com.ensodai.shared.avalon.rpc.services.AuthRpcService
import app.apiv4.infrastructure.BaseRpcService
import app.apiv4.infrastructure.RpcSessionContext
import app.data.db.tables.users.AppUsers
import app.data.db.tables.AuthSessions
import app.data.repository.SessionRepository
import app.data.repository.UserRepository
import app.data.repository.UserRepositoryApp
import app.plugins.generateAccessToken
import app.plugins.verifyAccessToken
import app.utils.EnvProvider
import app.utils.PasswordHasher
import app.utils.dbQuery
import app.utils.getUTCTimeNow
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.header
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.component.KoinComponent
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
class AuthServiceImpl(
    @InjectedParam private val call: ApplicationCall,
    @InjectedParam session: RpcSessionContext,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val webGoogleSignInUseCase: WebGoogleSignInUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val deleteUserAccountUseCase: DeleteUserAccountUseCase,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val userRepositoryApp: UserRepositoryApp
) : BaseRpcService(session), AuthRpcService, KoinComponent {

    override fun observeAuthState(): Flow<AuthState> = session.state

    override suspend fun login(request: LoginRequest): AuthResponse {
        val username = request.username.trim()
        val user = userRepositoryApp.findUserByUsername(username)
            ?: throw IllegalArgumentException("Пользователь не найден")

        val passwordHash = user.passwordHash
            ?: throw IllegalArgumentException("Для этого аккаунта не задан пароль")

        if (!PasswordHasher.verify(request.password, passwordHash)) {
            throw IllegalArgumentException("Неверный пароль")
        }

        val userAgent = call.request.header("User-Agent")?.take(255)
        val ip = call.request.origin.remoteHost.take(64)
        val deviceId = request.deviceId.take(128)
        val deviceName = request.deviceName.take(128)

        val newSession = sessionRepository.createNewSession(
            userId = user.id,
            deviceId = deviceId,
            deviceName = deviceName,
            userAgent = userAgent,
            ip = ip,
            fcmToken = request.fcmToken
        )

        val accessToken = generateAccessToken(
            userId = user.id.toString(),
            sessionId = newSession.id.value.toString()
        )

        val authResponse = AuthResponse(
            accessToken = accessToken,
            refreshToken = newSession.refreshToken,
            userId = user.id
        )

        updateSessionState(authResponse)
        updateDbTimezoneOffset(authResponse.userId, request.timezoneOffset)
        return authResponse
    }

    override suspend fun register(request: RegisterRequest): AuthResponse {
        if (!EnvProvider.isPublicRegistrationAllowed) {
            throw IllegalStateException("Публичная регистрация отключена администратором сервера. Обратитесь к администратору для получения аккаунта.")
        }

        val username = request.username.trim()
        require(username.length in 3..20) { "Имя пользователя должно быть от 3 до 20 символов" }
        require(request.password.length >= 6) { "Пароль должен содержать минимум 6 символов" }

        val existingUser = userRepositoryApp.findUserByUsername(username)
        if (existingUser != null) {
            throw IllegalArgumentException("Пользователь с именем '$username' уже существует")
        }

        val passwordHash = PasswordHasher.hash(request.password)
        val user = userRepositoryApp.createUserWithPassword(username, passwordHash)

        val userAgent = call.request.header("User-Agent")?.take(255)
        val ip = call.request.origin.remoteHost.take(64)
        val deviceId = request.deviceId.take(128)
        val deviceName = request.deviceName.take(128)

        val newSession = sessionRepository.createNewSession(
            userId = user.id,
            deviceId = deviceId,
            deviceName = deviceName,
            userAgent = userAgent,
            ip = ip,
            fcmToken = request.fcmToken
        )

        val accessToken = generateAccessToken(
            userId = user.id.toString(),
            sessionId = newSession.id.value.toString()
        )

        val authResponse = AuthResponse(
            accessToken = accessToken,
            refreshToken = newSession.refreshToken,
            userId = user.id
        )

        updateSessionState(authResponse)
        updateDbTimezoneOffset(authResponse.userId, request.timezoneOffset)
        return authResponse
    }

    override suspend fun googleSignIn(request: GoogleAuthRequest): AuthResponse {
        val authResponse = googleSignInUseCase.execute(request, call.request)
        updateSessionState(authResponse)
        updateDbTimezoneOffset(authResponse.userId, request.timezoneOffset)
        return authResponse
    }

    override suspend fun webGoogleSignIn(request: WebGoogleAuthRequest): AuthResponse {
        val authResponse = webGoogleSignInUseCase.execute(request, call.request)
        updateSessionState(authResponse)
        updateDbTimezoneOffset(authResponse.userId, request.timezoneOffset)
        return authResponse
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): AuthResponse {
        val authResponse = refreshTokenUseCase.execute(request.refreshToken, request.fcmToken)
        updateSessionState(authResponse)
        updateDbTimezoneOffset(authResponse.userId, request.timezoneOffset)
        return authResponse
    }

    override suspend fun logout(request: LogoutRequest) {
        logoutUseCase.execute(request.refreshToken)
        session.updateState(AuthState.Guest)
    }

    override suspend fun deleteAccount() {
        val userId = userId()
        deleteUserAccountUseCase.execute(userId)
        session.updateState(AuthState.Guest)
    }

    override suspend fun updateTimezone(timezoneOffset: Int) {
        val uId = userId()
        dbQuery {
            AppUsers.update({ AppUsers.id eq uId }) {
                it[AppUsers.timezoneOffset] = timezoneOffset
                it[updatedAt] = getUTCTimeNow()
            }
        }
    }

    override suspend fun updatePushToken(fcmToken: String) {
        ensureAuthorized()
        val sessId = sessionId()
        dbQuery {
            AuthSessions.update({ AuthSessions.id eq sessId }) {
                it[AuthSessions.fcmToken] = fcmToken
            }
        }
    }

    private suspend fun updateDbTimezoneOffset(userId: Uuid, offset: Int?) {
        if (offset == null) return
        dbQuery {
            AppUsers.update({ AppUsers.id eq userId }) {
                it[timezoneOffset] = offset
                it[updatedAt] = getUTCTimeNow()
            }
        }
    }
    
    override suspend fun authenticate(token: String): AuthResponse? {
        val verified = verifyAccessToken(token, sessionRepository)
        if (verified == null) {
            session.updateState(AuthState.TokenExpired)
            return null
        }

        val userId = verified.userId
        val username = dbQuery {
            val user = userRepository.findById(userId)
            user?.nickName ?: user?.name ?: "Unknown"
        }

        session.updateState(
            AuthState.Authorized(
                userId = userId,
                sessionId = verified.sessionId,
                username = username
            ),
            expiresAtMs = verified.expiresAtMs
        )

        return AuthResponse(
            accessToken = token,
            refreshToken = "", 
            userId = verified.userId
        )
    }


    private suspend fun updateSessionState(response: AuthResponse, manualSessionId: Uuid? = null) {
        val userId = response.userId
        
        val username = dbQuery {
            val user = userRepository.findById(userId)
            user?.nickName ?: user?.name ?: "Unknown"
        }

        val verified = verifyAccessToken(response.accessToken, sessionRepository)
        val sessionId = manualSessionId ?: verified?.sessionId ?: Uuid.random()

        session.updateState(
            AuthState.Authorized(
                userId = userId,
                sessionId = sessionId,
                username = username
            ),
            expiresAtMs = verified?.expiresAtMs
        )
    }
}
