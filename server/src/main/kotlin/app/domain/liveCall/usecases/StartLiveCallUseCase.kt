package app.domain.liveCall.usecases

import app.data.db.tables.LlmConfig
import app.data.repository.GlobalSettingsRepository
import app.service.liveCall.ActiveLiveCall
import app.service.liveCall.GeminiLiveAudioCallService
import app.domain.liveCall.LiveCallStartContextService
import app.domain.liveCall.LiveCallPolicyService
import app.plugins.VerifiedAccessToken
import app.utils.logger.WssLogger
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

data class StartLiveCallCommand(
    val actor: VerifiedAccessToken,
    val characterId: Uuid,
    val chatSessionId: Uuid,
    val chatVariantId: Uuid,
    val voiceName: String,
    val languageCode: String,
    val model: String?
)

data class StartLiveCallResult(
    val activeCall: ActiveLiveCall,
    val resolvedModel: String,
    val llmConfig: LlmConfig,
    val chatSessionId: Uuid,
    val chatVariantId: Uuid
)

@Single
class StartLiveCallUseCase(
    private val liveCallStartContextService: LiveCallStartContextService,
    private val liveCallPolicyService: LiveCallPolicyService,
    private val liveAudioCallService: GeminiLiveAudioCallService,
    private val globalSettingsRepository: GlobalSettingsRepository
) {

    suspend operator fun invoke(
        command: StartLiveCallCommand
    ): StartLiveCallResult {
        WssLogger.info("LiveCall: StartLiveCallUseCase - loading context")
        val startContext = liveCallStartContextService.load(
            actor = command.actor,
            characterId = command.characterId,
            chatSessionId = command.chatSessionId,
            chatVariantId = command.chatVariantId,
            requestedModel = command.model
        )
        WssLogger.info("LiveCall: StartLiveCallUseCase - context loaded, model=${startContext.resolvedLiveModel}")

        WssLogger.info("LiveCall: StartLiveCallUseCase - checking energy")
        liveCallPolicyService.ensureCanStartCall(
            userId = command.actor.userId,
            requiredTurnCost = startContext.characterConfig.llmConfig.energyCost
        )
        WssLogger.info("LiveCall: StartLiveCallUseCase - energy OK")

        WssLogger.info("LiveCall: StartLiveCallUseCase - resolving vertex location")
        val location = globalSettingsRepository.getAudioVertexLocation()
        WssLogger.info("LiveCall: StartLiveCallUseCase - vertex location=$location")

        WssLogger.info("LiveCall: StartLiveCallUseCase - starting Gemini call")
        val activeCall = liveAudioCallService.startCall(
            characterConfig = startContext.characterConfig,
            voiceName = command.voiceName,
            languageCode = command.languageCode,
            model = startContext.resolvedLiveModel,
            vertexLocation = location
        )
        WssLogger.info("LiveCall: StartLiveCallUseCase - Gemini call started")

        return StartLiveCallResult(
            activeCall = activeCall,
            resolvedModel = startContext.resolvedLiveModel,
            llmConfig = startContext.characterConfig.llmConfig,
            chatSessionId = startContext.chatSessionId,
            chatVariantId = startContext.chatVariantId
        )
    }
}
