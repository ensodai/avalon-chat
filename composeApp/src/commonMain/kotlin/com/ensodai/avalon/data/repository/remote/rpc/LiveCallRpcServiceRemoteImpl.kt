@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.avalon.data.repository.remote.rpc

import com.ensodai.shared.avalon.liveCall.models.LiveCallConfig
import com.ensodai.shared.avalon.liveCall.models.LiveCallServerEvent
import com.ensodai.shared.avalon.rpc.services.LiveCallRpcService
import com.ensodai.avalon.di.RpcConnectionManager
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.flow.*
import kotlinx.rpc.withService
import kotlin.uuid.Uuid

class LiveCallRpcServiceRemoteImpl(
    private val manager: RpcConnectionManager
) : LiveCallRpcService {

    private suspend fun getService() = manager.getActiveClient().withService<LiveCallRpcService>()

    override fun startLiveCall(
        characterId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        config: LiveCallConfig,
        audioStream: Flow<ByteArray>
    ): Flow<LiveCallServerEvent> = flow {
        L.d(TAG, "--> startLiveCall (bidirectional stream) charId=$characterId")
        emitAll(
            getService().startLiveCall(
                characterId,
                chatSessionId,
                chatVariantId,
                config,
                audioStream.onStart { L.d(TAG, "  ~ audioStream started") }
            )
                .onEach { L.d(TAG, "  ~ liveCall event=$it") }
                .onCompletion { e ->
                    if (e == null) L.d(TAG, "<-- liveCall completed")
                    else {
                        L.e(TAG, "<-- liveCall error: ${e.message}")
                        manager.notifyStreamFailure(e)
                    }
                }
        )
    }

    companion object {
        private const val TAG = "LiveCallRpc"
    }
}
