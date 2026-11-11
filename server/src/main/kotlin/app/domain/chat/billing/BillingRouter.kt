package app.domain.chat.billing

import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class BillingRouter(
    private val aiStrategyResolver: AiStrategyResolver
) {

    suspend fun checkRoute(
        userId: Uuid, 
        action: GenerationActionType
    ): RouteResult {
        return when (val strategy = aiStrategyResolver.resolveStrategy(userId)) {
            is AiExecutionStrategy.ServerVertex -> {
                RouteResult.ServerEnergy(cost = 1)
            }
            is AiExecutionStrategy.CustomProvider -> {
                if (strategy.apiKey.isBlank()) {
                    return RouteResult.Error.CustomKeyMissing
                }

                if (action == GenerationActionType.IMAGE_GIFT) {
                    if (!strategy.providerId.equals("google", ignoreCase = true)) {
                        if (strategy.imageModel.isNullOrBlank() || strategy.imagePromptModel.isNullOrBlank()) {
                            return RouteResult.Error.NoImageModelConfigured
                        }
                    }
                }

                RouteResult.FreeCustomKey(
                    apiKey = strategy.apiKey,
                    baseUrl = strategy.baseUrl ?: "https://openrouter.ai/api/v1"
                )
            }
        }
    }
}
