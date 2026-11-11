package com.ensodai.avalon.domain.chatEvents.liveCall

import com.ensodai.shared.avalon.liveCall.models.*
import com.ensodai.shared.avalon.rpc.services.LiveCallRpcService
import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.di.RpcConnectionManager
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class LiveCallSocketClient(
    private val liveCallRpcService: LiveCallRpcService,
    private val rpcConnectionManager: RpcConnectionManager,
    @ApplicationScope private val scope: CoroutineScope,
    private val logger: AppLogger
) {
    data class LiveCallState(
        val phase: Phase = Phase.IDLE,
        val activeChatVariantId: Uuid? = null,
        val lastError: String? = null,
        val lastStopReason: LiveCallStopReason? = null,
        val lastTurnRejectReason: LiveCallTurnRejectReason? = null,
        val turnRejectEventCount: Int = 0
    ) {
        enum class Phase {
            IDLE,
            CONNECTING,
            ACTIVE,
            INTERRUPTED,
            RECONNECTING,
            STOPPING,
            STOPPED
        }
    }

    private data class CallParams(
        val characterId: Uuid,
        val chatSessionId: Uuid,
        val chatVariantId: Uuid,
        val config: LiveCallConfig
    )

    private val _state = MutableStateFlow(LiveCallState())
    val state: StateFlow<LiveCallState> = _state.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<LiveCallServerEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    val incomingMessages: SharedFlow<LiveCallServerEvent> = _incomingMessages.asSharedFlow()

    private val audioChunks = MutableSharedFlow<ByteArray>(
        extraBufferCapacity = 128,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var callParams: CallParams? = null
    private var callJob: Job? = null
    private var reconnectAttemptStartAt: Long = 0

    private companion object {
        const val RECONNECT_TIMEOUT_MS = 30_000L
        const val RECONNECT_DELAY_MS = 3_000L
    }

    fun startCall(
        characterId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        model: String? = null,
        voiceName: String = "Aoede",
        languageCode: String = "ru-RU"
    ) {
        val config = LiveCallConfig(
            model = model,
            voiceName = voiceName,
            languageCode = languageCode
        )
        callParams = CallParams(characterId, chatSessionId, chatVariantId, config)
        
        _state.value = _state.value.copy(
            phase = LiveCallState.Phase.CONNECTING,
            activeChatVariantId = chatVariantId,
            lastError = null,
            lastStopReason = null,
            lastTurnRejectReason = null
        )

        initiateRpcCall()
    }

    private fun initiateRpcCall() {
        val params = callParams ?: return
        callJob?.cancel()
        reconnectAttemptStartAt = 0L

        callJob = scope.launch {
            liveCallRpcService.startLiveCall(
                characterId = params.characterId,
                chatSessionId = params.chatSessionId,
                chatVariantId = params.chatVariantId,
                config = params.config,
                audioStream = audioChunks
            )
            .retryWhen { cause, attempt ->
                val currentPhase = _state.value.phase
                
                // Если мы сами остановили звонок, ретрай не нужен
                if (currentPhase == LiveCallState.Phase.STOPPED || currentPhase == LiveCallState.Phase.STOPPING || currentPhase == LiveCallState.Phase.IDLE) {
                    return@retryWhen false
                }

                if (reconnectAttemptStartAt == 0L) {
                    reconnectAttemptStartAt = com.ensodai.avalon.utils.currentTimeMillis()
                    _state.value = _state.value.copy(phase = LiveCallState.Phase.RECONNECTING)
                    logger.w("LiveCallSocketClient", "Connection lost. Starting reconnect window...")
                }

                val elapsed = com.ensodai.avalon.utils.currentTimeMillis() - reconnectAttemptStartAt
                if (elapsed > RECONNECT_TIMEOUT_MS) {
                    logger.e("LiveCallSocketClient", "Reconnect timeout (30s) reached.")
                    _state.value = _state.value.copy(
                        phase = LiveCallState.Phase.STOPPED,
                        lastError = "Connection lost and failed to reconnect"
                    )
                    return@retryWhen false
                }

                logger.w("LiveCallSocketClient", "RPC observation interrupted: ${cause.message}. Retrying in ${RECONNECT_DELAY_MS}ms (attempt $attempt)...")
                rpcConnectionManager.invalidate()
                delay(RECONNECT_DELAY_MS)
                true
            }
            .catch { e ->
                logger.e("LiveCallSocketClient", "❌ Fatal error in live call observation: ${e.message}")
                if (_state.value.phase != LiveCallState.Phase.STOPPING && _state.value.phase != LiveCallState.Phase.IDLE) {
                    _state.value = _state.value.copy(
                        phase = LiveCallState.Phase.STOPPED,
                        lastError = e.message ?: "Fatal connection error"
                    )
                }
            }
            .collect { event ->
                _incomingMessages.emit(event)
                handleServerEvent(event)
            }
        }
    }

    fun disconnect() {
        logger.i("LiveCallSocketClient", "Manual disconnect")
        callParams = null
        callJob?.cancel()
        callJob = null
        reconnectAttemptStartAt = 0L
        _state.value = _state.value.copy(
            phase = LiveCallState.Phase.IDLE,
            activeChatVariantId = null,
            lastStopReason = null,
            lastTurnRejectReason = null
        )
    }

    suspend fun sendAudioChunk(audioBytes: ByteArray) {
        val phase = _state.value.phase
        if (phase != LiveCallState.Phase.ACTIVE && phase != LiveCallState.Phase.INTERRUPTED) {
            return
        }
        audioChunks.emit(audioBytes)
    }

    suspend fun stopCall() {
        logger.i("LiveCallSocketClient", "User requested stopCall")
        _state.value = _state.value.copy(phase = LiveCallState.Phase.STOPPING)
        disconnect()
    }

    private fun handleServerEvent(event: LiveCallServerEvent) {
        logger.d("LiveCallSocketClient", "handleServerEvent — type=${event::class.simpleName}")
        _state.value = when (event) {
            is LiveCallServerEvent.CallStarted -> {
                reconnectAttemptStartAt = 0L // Reset reconnect window
                _state.value.copy(
                    phase = LiveCallState.Phase.ACTIVE,
                    activeChatVariantId = event.chatVariantId,
                    lastError = null,
                    lastStopReason = null,
                    lastTurnRejectReason = null
                )
            }

            is LiveCallServerEvent.CallRejected -> {
                callParams = null
                callJob?.cancel()
                _state.value.copy(
                    phase = LiveCallState.Phase.STOPPED,
                    activeChatVariantId = null,
                    lastError = "Call rejected: ${event.reason}",
                    lastStopReason = if (event.reason == LiveCallRejectReason.INSUFFICIENT_ENERGY) {
                        LiveCallStopReason.INSUFFICIENT_ENERGY
                    } else {
                        null
                    }
                )
            }

            is LiveCallServerEvent.CallStopped -> {
                callParams = null
                callJob?.cancel()
                _state.value.copy(
                    phase = LiveCallState.Phase.STOPPED,
                    activeChatVariantId = null,
                    lastError = event.message,
                    lastStopReason = event.reason
                )
            }

            is LiveCallServerEvent.Interrupted -> {
                _state.value.copy(phase = LiveCallState.Phase.INTERRUPTED)
            }

            is LiveCallServerEvent.WaitingForInput,
            is LiveCallServerEvent.TurnComplete,
            is LiveCallServerEvent.AudioChunk -> {
                val currentPhase = _state.value.phase
                if (currentPhase == LiveCallState.Phase.INTERRUPTED) {
                    _state.value.copy(
                        phase = LiveCallState.Phase.ACTIVE,
                        lastTurnRejectReason = null
                    )
                } else {
                    _state.value.copy(lastTurnRejectReason = null)
                }
            }

            is LiveCallServerEvent.Error -> {
                val currentPhase = _state.value.phase
                if (currentPhase == LiveCallState.Phase.STOPPING || currentPhase == LiveCallState.Phase.IDLE) {
                    _state.value
                } else {
                    // Ошибки сервера мы просто логируем и сохраняем в стейт, 
                    // реконнект обрабатывается на уровне RPC (retryWhen) если сокет падает.
                    _state.value.copy(lastError = event.message)
                }
            }

            is LiveCallServerEvent.VoiceActivity -> _state.value

            is LiveCallServerEvent.TurnRejected -> {
                _state.value.copy(
                    lastTurnRejectReason = event.reason,
                    turnRejectEventCount = _state.value.turnRejectEventCount + 1
                )
            }
        }
    }
}
