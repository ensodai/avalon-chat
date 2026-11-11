package app.apiv4.routes

import app.apiv4.infrastructure.RpcSessionContext
import app.apiv4.services.impl.*
import app.plugins.ApplicationJson
import com.ensodai.shared.avalon.rpc.services.*
import com.ensodai.shared.avalon.chat.services.ChatRpcService
import io.ktor.server.routing.*
import io.ktor.util.AttributeKey
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.ktor.server.KrpcRoute
import kotlinx.rpc.krpc.serialization.json.json
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.getKoin

import app.plugins.verifyAccessToken
import app.data.repository.SessionRepository
import app.data.repository.UserRepository
import com.ensodai.shared.avalon.auth.models.AuthState
import app.utils.dbQuery

val RpcSessionContextKey = AttributeKey<RpcSessionContext>("RpcSessionContext")

fun KrpcRoute.getOrCreateSessionContext(): RpcSessionContext {
    return call.attributes.computeIfAbsent(RpcSessionContextKey) {
        RpcSessionContext(this, AuthState.Guest, null)
    }
}

fun Route.apiV4Route() {
    rpc("/api/v4/rpc") {
        rpcConfig {
            serialization {
                json(ApplicationJson)
            }
        }

        registerService<AuthRpcService> { getService<AuthServiceImpl>(this, call, getOrCreateSessionContext()) }
        registerService<CharacterRpcService> { getService<CharacterServiceImpl>(this, call, getOrCreateSessionContext()) }
        registerService<ChatRpcService> { getService<ChatRpcServiceImpl>(this, call, getOrCreateSessionContext()) }
        registerService<PersonaRpcService> { getService<PersonaServiceImpl>(this, call, getOrCreateSessionContext()) }
        registerService<GiftsRpcService> { getService<GiftsServiceImpl>(this, getOrCreateSessionContext()) }
        registerService<SyncRpcService> { getService<SyncServiceImpl>(this, getOrCreateSessionContext()) }
        registerService<UserRpcService> { getService<UserServiceImpl>(this, call, getOrCreateSessionContext()) }
        registerService<CategoryRpcService> { getService<CategoryServiceImpl>(this, getOrCreateSessionContext()) }
        registerService<LiveCallRpcService> { getService<LiveCallRpcServiceImpl>(this, getOrCreateSessionContext()) }
        registerService<FileUploadRpcService> { getService<FileUploadServiceImpl>(this, call, getOrCreateSessionContext()) }
        registerService<SessionSupportRpcService> { getService<SessionSupportServiceImpl>(this, call, getOrCreateSessionContext()) }
        registerService<AppVersionRpcService> { getService<AppVersionRpcServiceImpl>(this, getOrCreateSessionContext()) }
    }
}

inline fun <reified T : Any> getService(route: KrpcRoute, vararg params: Any?): T =
    route.call.application.getKoin().get<T> { parametersOf(*params) }
