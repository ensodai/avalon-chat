package app.domain.liveCall

import app.plugins.VerifiedAccessToken
import app.service.LlmConfigService
import app.service.chat.CharacterLLMConfigService
import app.data.repository.ChatRepository
import app.utils.logger.WssLogger
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

data class LiveCallStartContext(
    val characterConfig: app.data.models.AICharacterConfigLLMDto,
    val resolvedLiveModel: String,
    val chatSessionId: Uuid,
    val chatVariantId: Uuid
)

@Single
class LiveCallStartContextService(
    private val characterLLMConfigService: CharacterLLMConfigService,
    private val llmConfigService: LlmConfigService,
    private val chatRepository: ChatRepository
) {

    suspend fun load(
        actor: VerifiedAccessToken,
        characterId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        requestedModel: String?
    ): LiveCallStartContext {

        val llmConfig = llmConfigService.getConfig(code = "gemini_live_2.5_flash_native_audio")

        WssLogger.info("LiveCall: load() - getting characterConfig for variantId=$chatVariantId")
        var characterConfig = characterLLMConfigService.get(chatVariantId, llmConfig)

        // Lazy creation: если вариант/сессия ещё не существуют — создаём
        if (characterConfig == null) {
            WssLogger.info("LiveCall: load() - variant not found, creating session+variant")
            chatRepository.createSessionAndVariant(
                userId = actor.userId,
                characterId = characterId,
                chatSessionId = chatSessionId,
                chatVariantId = chatVariantId
            )
            characterConfig = characterLLMConfigService.get(chatVariantId, llmConfig)
        }

        if (characterConfig == null) {
            throw LiveCallPolicyService.ChatVariantNotFoundException("Character config not found after creation")
        }

        return LiveCallStartContext(
            characterConfig = characterConfig,
            resolvedLiveModel = llmConfig.modelAPIID,
            chatSessionId = chatSessionId,
            chatVariantId = chatVariantId
        )
    }

}
