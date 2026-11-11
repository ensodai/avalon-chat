package app.apiv4.services.impl

import app.apiv4.infrastructure.BaseRpcService
import app.apiv4.infrastructure.RpcSessionContext
import app.apiv4.infrastructure.RpcSessionStatus
import app.domain.liveCall.usecases.*
import app.domain.liveCall.*
import app.domain.liveCall.models.*
import app.plugins.VerifiedAccessToken
import com.ensodai.shared.avalon.liveCall.models.*
import com.ensodai.shared.avalon.rpc.services.LiveCallRpcService
import app.utils.logger.WssLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

@Factory
class LiveCallRpcServiceImpl(
    @InjectedParam sessionContext: RpcSessionContext,
    private val startLiveCallUseCase: StartLiveCallUseCase,
    private val processCompletedLiveTurnUseCase: ProcessCompletedLiveTurnUseCase
) : BaseRpcService(sessionContext), LiveCallRpcService {

    override fun startLiveCall(
        characterId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        config: LiveCallConfig,
        audioStream: Flow<ByteArray>
    ): Flow<LiveCallServerEvent> = channelFlow {
        ensureAuthorized()
        val userId = userId()
        
        // Звонки временно отключены (отрубаем сразу при запросе)
        WssLogger.info("Live audio call requested by user $userId, but live calls are currently disabled.")
        send(LiveCallServerEvent.CallRejected(
            reason = LiveCallRejectReason.INSUFFICIENT_ENERGY,
            message = "Live calls are temporarily disabled"
        ))
        return@channelFlow

        val sessionId = sessionId()
        
        val actor = VerifiedAccessToken(userId = userId, sessionId = sessionId, expiresAtMs = 0L)

        // 1. Стартуем звонок
        val startResult = try {
            startLiveCallUseCase(
                command = StartLiveCallCommand(
                    actor = actor,
                    characterId = characterId,
                    chatSessionId = chatSessionId,
                    chatVariantId = chatVariantId,
                    voiceName = config.voiceName,
                    languageCode = config.languageCode,
                    model = config.model
                )
            )
        } catch (e: LiveCallPolicyService.InsufficientStartEnergyException) {
            send(LiveCallServerEvent.CallRejected(
                reason = LiveCallRejectReason.INSUFFICIENT_ENERGY,
                requiredCost = e.requiredCost,
                dailyEnergy = e.currentBalance.dailyEnergy,
                reserveEnergy = e.currentBalance.reserveEnergy
            ))
            return@channelFlow
        } catch (e: Exception) {
            send(LiveCallServerEvent.Error("Failed to start live call: ${e.message}"))
            return@channelFlow
        }

        val activeCall = startResult.activeCall
        var currentTurnUsage: LiveTurnUsageSnapshot? = null
        var hadModelOutput = false
        var interrupted = false
        var waitingForInput = false

        send(LiveCallServerEvent.CallStarted(
            chatVariantId = startResult.chatVariantId,
            model = startResult.resolvedModel
        ))

        // 2. Слушаем отмену сессии по таймауту токена
        val sessionStatusJob = launch {
            session.status.collectLatest { status ->
                when (status) {
                    RpcSessionStatus.EXPIRED_TIMEOUT,
                    RpcSessionStatus.REVOKED -> {
                        send(LiveCallServerEvent.CallStopped(
                            reason = LiveCallStopReason.SESSION_EXPIRED,
                            message = "Session expired or revoked"
                        ))
                        activeCall.close()
                    }
                    else -> {}
                }
            }
        }

        // 3. Отправляем байты от клиента в Gemini
        val audioSendJob = launch {
            try {
                audioStream.collect { chunk ->
                    activeCall.sendAudioChunk(chunk)
                }
                activeCall.sendAudioStreamEnd()
            } catch (e: CancellationException) {
                // Нормальное завершение при закрытии channelFlow
            } catch (e: Exception) {
                send(LiveCallServerEvent.Error("Audio send failed: ${e.message}"))
            }
        }

        // 4. Получаем ответы от Gemini, биллим и отдаем клиенту
        try {
            activeCall.events.collect { geminiEvent ->
                // Передаем клиенту внешний ивент (аудио-чанки, статусы)
                if (geminiEvent.serverEvent != null) {
                    send(geminiEvent.serverEvent)
                }

                // Обрабатываем внутренние события для биллинга
                when (val internal = geminiEvent.internalEvent) {
                    LiveCallInternalEvent.ModelTurnChunkReceived -> hadModelOutput = true
                    LiveCallInternalEvent.InterruptedObserved -> interrupted = true
                    LiveCallInternalEvent.WaitingForInputObserved -> waitingForInput = true
                    is LiveCallInternalEvent.UsageMetadataReceived -> {
                        currentTurnUsage = LiveTurnUsageSnapshot(
                            totalTokens = internal.totalTokens,
                            promptTokens = internal.promptTokens,
                            responseTokens = internal.responseTokens,
                            cachedTokens = internal.cachedTokens,
                            thoughtsTokens = internal.thoughtsTokens
                        )
                    }
                    is LiveCallInternalEvent.TurnCompleted -> {
                        val billingResult = processCompletedLiveTurnUseCase(
                            ProcessCompletedLiveTurnCommand(
                                userId = userId,
                                chatVariantId = startResult.chatVariantId,
                                llmConfig = startResult.llmConfig,
                                resolvedModel = startResult.resolvedModel,
                                hadModelOutput = hadModelOutput,
                                interrupted = interrupted,
                                waitingForInput = waitingForInput,
                                turnCompleteReason = internal.reason,
                                usage = currentTurnUsage
                            )
                        )

                        // Сбрасываем стейт хода
                        hadModelOutput = false
                        interrupted = false
                        waitingForInput = false
                        currentTurnUsage = null

                        when (billingResult) {
                            ProcessCompletedLiveTurnResult.Rejected -> {
                                send(LiveCallServerEvent.TurnRejected(LiveCallTurnRejectReason.RESPONSE_REJECTED))
                            }
                            is ProcessCompletedLiveTurnResult.StopInsufficientEnergy -> {
                                send(LiveCallServerEvent.CallStopped(
                                    reason = LiveCallStopReason.INSUFFICIENT_ENERGY,
                                    message = "Insufficient energy",
                                    requiredCost = billingResult.requiredCost,
                                    dailyEnergy = billingResult.dailyEnergy,
                                    reserveEnergy = billingResult.reserveEnergy
                                ))
                                activeCall.close()
                                return@collect // Выходим из collect, звонок закончен
                            }
                            else -> {} // Billed, NonBillable, AmbiguousLogged — продолжаем звонок
                        }
                    }
                    null -> {}
                }
            }
        } catch (e: CancellationException) {
            // Нормальное закрытие канала
        } catch (e: Exception) {
            send(LiveCallServerEvent.Error("Live call error: ${e.message}"))
        } finally {
            sessionStatusJob.cancel()
            audioSendJob.cancel()
            activeCall.close()
        }
    }
}
