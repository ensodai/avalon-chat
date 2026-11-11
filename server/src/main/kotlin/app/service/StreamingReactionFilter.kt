package app.service

import app.service.chat.ReactionParser

class StreamingReactionFilter {
    private val buffer = StringBuilder()
    private var isInsideTag = false
    private var isInsideReaction = false
    private val reactionBuffer = StringBuilder()

    /**
     * Возвращает пару: (Текст для отправки пользователю, Реакция (если тег закрылся))
     */
    fun process(chunk: String): Pair<String, String?> {
        val output = java.lang.StringBuilder()
        var completedReaction: String? = null

        for (char in chunk) {
            if (isInsideTag) {
                buffer.append(char)
                val currentStr = buffer.toString()
                
                // Проверяем, закрылся ли тег
                if (currentStr.endsWith("</reaction>", ignoreCase = true) || 
                    currentStr.endsWith("</message_reactions>", ignoreCase = true)) {
                    isInsideTag = false
                    
                    val rawReaction = reactionBuffer.toString()
                    val cleanContent = rawReaction
                        .substringBefore("</reaction")
                        .substringBefore("</REACTION")
                        .substringBefore("</message_reactions")
                        .substringBefore("</MESSAGE_REACTIONS")
                        .trim()
                    completedReaction = ReactionParser.extractReactionEmoji(cleanContent)
                    
                    buffer.clear()
                    reactionBuffer.clear()
                    isInsideReaction = false
                } else if (isInsideReaction) {
                    reactionBuffer.append(char)
                } else {
                    // Ждем пока полностью сложится открывающий тег
                    if (currentStr.endsWith("<reaction>", ignoreCase = true) || 
                        currentStr.endsWith("<message_reactions>", ignoreCase = true)) {
                        isInsideReaction = true
                    }
                }
            } else {
                if (char == '<') {
                    buffer.append(char)
                } else if (buffer.isNotEmpty()) {
                    buffer.append(char)
                    val currentStr = buffer.toString()
                    
                    // Проверяем, является ли буфер префиксом тега
                    if ("<reaction>".startsWith(currentStr, ignoreCase = true) || 
                        "<message_reactions>".startsWith(currentStr, ignoreCase = true)) {
                        
                        if (currentStr.equals("<reaction>", ignoreCase = true) || 
                            currentStr.equals("<message_reactions>", ignoreCase = true)) {
                            isInsideTag = true
                            isInsideReaction = true
                        }
                    } else {
                        // Это не наш тег, отдаем содержимое буфера и очищаем его
                        output.append(buffer.toString())
                        buffer.clear()
                    }
                } else {
                    output.append(char)
                }
            }
        }
        
        return Pair(output.toString(), completedReaction)
    }

    /**
     * Извлекает реакцию, если стрим завершился до закрытия тега.
     */
    fun getPendingReaction(): String? {
        val raw = reactionBuffer.toString().trim()
        if (raw.isNotEmpty()) {
            return ReactionParser.extractReactionEmoji(raw)
        }
        return null
    }

    /**
     * Сбросить фильтр в конце стрима (выбрасывает только обычный текст, не теги реакций)
     */
    fun flush(): String {
        val currentStr = buffer.toString()
        val isLikelyTagPrefix = "<reaction>".startsWith(currentStr, ignoreCase = true) ||
                "<message_reactions>".startsWith(currentStr, ignoreCase = true) ||
                isInsideTag ||
                isInsideReaction

        val result = if (!isLikelyTagPrefix) currentStr else ""
        buffer.clear()
        isInsideTag = false
        isInsideReaction = false
        reactionBuffer.clear()
        return result
    }
}
