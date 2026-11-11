package app.service.image.pipeline

import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import app.data.models.AICharacterConfigLLMDto
import app.domain.models.DomainMessage
import app.service.image.TokenUsage
import com.ensodai.shared.avalon.gifts.models.GiftActionType

data class GeneratedImageData(
    val imageBytes: ByteArray,
    val visualDescription: String,
    val tokenUsage: TokenUsage
)

interface ImagePipelineProvider {
    suspend fun generate(
        configDto: AICharacterConfigLLMDto,
        giftName: String,
        history: List<DomainMessage>,
        actionType: GiftActionType,
        reqId: String,
        aiSettings: UserAiSettingsResponse?
    ): GeneratedImageData
}
