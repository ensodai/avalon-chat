package app.apiv4.infrastructure

import io.ktor.server.application.*
import kotlin.coroutines.CoroutineContext

class RpcCallContext(val call: ApplicationCall) : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<RpcCallContext>
    override val key: CoroutineContext.Key<*> = Key
}
