package app.apiv4.infrastructure

import app.utils.UserActivityTracker
import kotlin.uuid.Uuid

abstract class BaseRpcService(protected val session: RpcSessionContext) {
    /** Гарантирует наличие активной сессии, приостанавливая выполнение при необходимости. */
    suspend fun ensureAuthorized() {
        session.userId()
    }

    /** Выполняет блок кода только при наличии активной сессии, передавая в него ID пользователя. */
    suspend fun <T> authorized(block: suspend (Uuid) -> T): T {
        return block(session.userId())
    }

    suspend fun userId(): Uuid {
        val uId = session.userId()
        UserActivityTracker.updateLastSeen(uId)
        return uId
    }
    
    suspend fun sessionId(): Uuid = session.sessionId()
}
