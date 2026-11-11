package app.service.image

/**
 * Статистика использования токенов для LLM запросов.
 */
data class TokenUsage(
    val totalTokens: Long,
    val promptTokens: Long,
    val outputTokens: Long,
    val cachedTokens: Long = 0,
    val thoughtsTokens: Long = 0
) {
    companion object {
        val ZERO = TokenUsage(0, 0, 0, 0, 0)
    }
}
