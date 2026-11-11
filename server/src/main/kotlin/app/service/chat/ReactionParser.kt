package app.service.chat

import app.service.chat.EmojiExtractor
import app.service.chat.EmojiPalette

/**
 * Единый парсер реакций для всех LLM провайдеров (Gemini, OpenRouter, OpenAI, DeepSeek, Groq).
 * Извлекает эмодзи-реакцию из тегов <reaction>...</reaction> или <message_reactions>...</message_reactions>
 * и гарантирует полное удаление XML-тегов из результирующего текста.
 */
object ReactionParser {

    /**
     * Разбирает полный текст ответа модели.
     * Возвращает пару: (Чистый текст без тегов реакции, Валидированная эмодзи-реакция или null)
     */
    fun parseFullText(rawText: String): Pair<String, String?> {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return "" to null

        // 1. Ищем закрытые теги <reaction>...</reaction> или <message_reactions>...</message_reactions>
        val combinedRegex = Regex(
            """<(reaction|message_reactions)>(.*?)</\1>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val match = combinedRegex.findAll(trimmed).lastOrNull()

        if (match != null) {
            val innerContent = match.groupValues[2].trim()
            val finalEmoji = extractReactionEmoji(innerContent)
            val cleanText = trimmed.substring(0, match.range.first).trim()
            return cleanText to finalEmoji
        }

        // 2. Проверяем незакрытый тег в конце текста (на случай обрыва стрима или токенов)
        val unclosedRegex = Regex(
            """<(reaction|message_reactions)>(.*)$""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val unclosedMatch = unclosedRegex.find(trimmed)
        if (unclosedMatch != null) {
            val innerContent = unclosedMatch.groupValues[2].trim()
            val finalEmoji = extractReactionEmoji(innerContent)
            val cleanText = trimmed.substring(0, unclosedMatch.range.first).trim()
            return cleanText to finalEmoji
        }

        return trimmed to null
    }

    /**
     * Извлекает и валидирует эмодзи или текстовую реакцию из содержимого тега реакции.
     * Поддерживает прямой эмодзи, вложенные теги <character>...</character>, <user>...</user> и текстовые описания.
     */
    fun extractReactionEmoji(innerContent: String): String? {
        if (innerContent.isBlank()) return null

        val characterReactionRegex = Regex("""<character>\s*([^<]+)\s*</character>""", RegexOption.IGNORE_CASE)
        val userReactionRegex = Regex("""<user>\s*([^<]+)\s*</user>""", RegexOption.IGNORE_CASE)

        val rawReaction = characterReactionRegex.find(innerContent)?.groupValues?.get(1)?.trim()
            ?: userReactionRegex.find(innerContent)?.groupValues?.get(1)?.trim()
            ?: innerContent

        if (rawReaction.isBlank() || rawReaction.equals("null", true) || rawReaction.equals("none", true)) {
            return null
        }

        val emoji = EmojiExtractor.extractFirstEmoji(rawReaction)
        return if (!emoji.isNullOrBlank() && !emoji.equals("null", true)) {
            EmojiPalette.validateReaction(emoji)
        } else {
            rawReaction
        }
    }
}
