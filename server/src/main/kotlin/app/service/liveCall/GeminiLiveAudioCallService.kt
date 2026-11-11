package app.service.liveCall

import app.domain.liveCall.models.LiveCallInternalEvent
import com.ensodai.shared.avalon.liveCall.models.LiveCallServerEvent
import com.ensodai.shared.avalon.liveCall.models.LiveCallTurnCompleteReason
import app.data.models.AICharacterConfigLLMDto
import com.google.genai.AsyncSession
import com.google.genai.errors.GenAiIOException
import com.google.genai.types.Blob
import com.google.genai.types.LiveSendRealtimeInputParameters
import com.google.genai.types.LiveServerContent
import com.google.genai.types.LiveServerMessage
import com.google.genai.types.TurnCompleteReason
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.koin.core.annotation.Single

@Single
class GeminiLiveAudioCallService(
    private val vertexLiveClientProvider: VertexLiveClientProvider,
    private val liveCallConnectConfigFactory: LiveCallConnectConfigFactory
) {
    companion object {
        const val DEFAULT_OUTPUT_MIME = "audio/pcm;rate=16000"
    }

    suspend fun startCall(
        characterConfig: AICharacterConfigLLMDto,
        voiceName: String,
        languageCode: String,
        model: String,
        vertexLocation: String
    ): ActiveLiveCall {
        val client = vertexLiveClientProvider.getClient(vertexLocation)
        val connectConfig = liveCallConnectConfigFactory.build(
            characterConfig = characterConfig,
            voiceName = voiceName
        )

        val session = client.async.live.connect(model, connectConfig).await()
        return ActiveLiveCall(session)
    }
}

class ActiveLiveCall(private val session: AsyncSession) {
    
    val events: Flow<GeminiEvent> = callbackFlow {
        session.receive { message ->
            message.serverContent().orElse(null)?.let { content ->
                if (content.interrupted().orElse(false) == true) {
                    trySend(GeminiEvent(LiveCallServerEvent.Interrupted, LiveCallInternalEvent.InterruptedObserved))
                }

                if (content.waitingForInput().orElse(false) == true) {
                    trySend(GeminiEvent(LiveCallServerEvent.WaitingForInput, LiveCallInternalEvent.WaitingForInputObserved))
                }

                content.modelTurn().flatMap { it.parts() }.ifPresent { parts ->
                    if (parts.isNotEmpty()) {
                        trySend(GeminiEvent(null, LiveCallInternalEvent.ModelTurnChunkReceived))
                    }
                    parts.forEach { part ->
                        part.inlineData().ifPresent { blob ->
                            blob.data().orElse(null)?.let { audioBytes ->
                                trySend(GeminiEvent(LiveCallServerEvent.AudioChunk(audioBytes), null))
                            }
                        }
                    }
                }

                if (content.turnComplete().orElse(false) == true) {
                    val reason = content.turnCompleteReason()
                        .map { it.knownEnum().toProtocolReason() }
                        .orElse(null)
                    trySend(GeminiEvent(LiveCallServerEvent.TurnComplete(reason), LiveCallInternalEvent.TurnCompleted(reason)))
                }
            }

            message.usageMetadata().orElse(null)?.let { usage ->
                val internalUsage = LiveCallInternalEvent.UsageMetadataReceived(
                    totalTokens = usage.totalTokenCount().map { it.toLong() }.orElse(null),
                    promptTokens = usage.promptTokenCount().map { it.toLong() }.orElse(null),
                    responseTokens = usage.responseTokenCount().map { it.toLong() }.orElse(null),
                    cachedTokens = usage.cachedContentTokenCount().map { it.toLong() }.orElse(null),
                    thoughtsTokens = usage.thoughtsTokenCount().map { it.toLong() }.orElse(null)
                )
                trySend(GeminiEvent(null, internalUsage))
            }

            message.voiceActivity().orElse(null)?.let { activity ->
                val activityType = activity.voiceActivityType().map { it.toString() }.orElse("UNKNOWN")
                trySend(GeminiEvent(LiveCallServerEvent.VoiceActivity(activityType), null))
            }

            message.goAway().orElse(null)?.let { goAway ->
                val timeMsg = goAway.timeLeft().map { "Live session is closing in ${it}s" }.orElse("Live session is closing")
                trySend(GeminiEvent(LiveCallServerEvent.Error(timeMsg), null))
            }
        }

        awaitClose {
            runCatching { session.close() }
        }
    }

    suspend fun sendAudioChunk(audioBytes: ByteArray, mimeType: String = GeminiLiveAudioCallService.DEFAULT_OUTPUT_MIME) {
        try {
            session.sendRealtimeInput(
                LiveSendRealtimeInputParameters.builder()
                    .media(Blob.builder().mimeType(mimeType).data(audioBytes).build())
                    .build()
            ).await()
        } catch (e: GenAiIOException) {
            throw RuntimeException("Failed to send audio chunk: ${e.message}", e)
        }
    }

    suspend fun sendAudioStreamEnd() {
        try {
            session.sendRealtimeInput(
                LiveSendRealtimeInputParameters.builder().audioStreamEnd(true).build()
            ).await()
        } catch (e: GenAiIOException) {
            throw RuntimeException("Failed to send audio stream end: ${e.message}", e)
        }
    }

    suspend fun close() {
        runCatching { session.close().await() }
    }
}

data class GeminiEvent(
    val serverEvent: LiveCallServerEvent?,
    val internalEvent: LiveCallInternalEvent?
)

private fun TurnCompleteReason.Known?.toProtocolReason(): LiveCallTurnCompleteReason? =
    when (this) {
        TurnCompleteReason.Known.RESPONSE_REJECTED,
        TurnCompleteReason.Known.PROHIBITED_INPUT_CONTENT,
        TurnCompleteReason.Known.IMAGE_PROHIBITED_INPUT_CONTENT,
        TurnCompleteReason.Known.INPUT_TEXT_CONTAIN_PROMINENT_PERSON_PROHIBITED,
        TurnCompleteReason.Known.INPUT_IMAGE_CELEBRITY,
        TurnCompleteReason.Known.INPUT_IMAGE_PHOTO_REALISTIC_CHILD_PROHIBITED,
        TurnCompleteReason.Known.INPUT_TEXT_NCII_PROHIBITED,
        TurnCompleteReason.Known.INPUT_OTHER -> LiveCallTurnCompleteReason.RESPONSE_REJECTED

        TurnCompleteReason.Known.NEED_MORE_INPUT -> LiveCallTurnCompleteReason.NEED_MORE_INPUT
        TurnCompleteReason.Known.MALFORMED_FUNCTION_CALL -> LiveCallTurnCompleteReason.MALFORMED_FUNCTION_CALL
        TurnCompleteReason.Known.TURN_COMPLETE_REASON_UNSPECIFIED,
        null -> null
        else -> LiveCallTurnCompleteReason.RESPONSE_REJECTED
    }

private suspend fun <T> CompletableFuture<T>.await(): T =
    suspendCancellableCoroutine { continuation: CancellableContinuation<T> ->
        whenComplete { value, throwable ->
            if (throwable == null) continuation.resume(value)
            else continuation.resumeWithException(throwable.cause ?: throwable)
        }
        continuation.invokeOnCancellation { cancel(true) }
    }
