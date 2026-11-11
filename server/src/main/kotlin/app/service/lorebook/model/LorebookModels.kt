package app.service.lorebook.model

import com.ensodai.shared.avalon.character.models.SecondaryFilterLogic

enum class InjectionPosition {
    BEFORE_CHAR,
    AFTER_CHAR,
    BEFORE_DESC,
    AFTER_DESC,
    PERSONALITY,
    SCENARIO,
    AT_DEPTH
}

/**
 * Иммутабельная спецификация статьи лорбука для оценки условий срабатывания и бюджетирования.
 */
data class LorebookEntrySpec(
    val id: Int,
    val uid: String,
    val content: String,
    val primaryPatternIds: IntArray,
    val secondaryPatternIds: IntArray = IntArray(0),
    val regexPatterns: List<Regex> = emptyList(),
    val selective: Boolean = false,
    val filterLogic: SecondaryFilterLogic = SecondaryFilterLogic.AND_ANY,
    val proximityWindowWords: Int? = null,
    val constant: Boolean = false,
    val enabled: Boolean = true,
    val insertionOrder: Int = 100,
    val priority: Int = 100,
    val scanDepthOverride: Int? = null,
    val probability: Int = 100,
    // Управление рекурсией
    val excludeRecursion: Boolean = false,
    val preventRecursion: Boolean = false,
    val delayUntilRecursion: Boolean = false,
    val recursionLevel: Int = 0,
    // Временные эффекты (в количестве сообщений)
    val stickyTurns: Int = 0,
    val cooldownTurns: Int = 0,
    val delayMessages: Int = 0,
    // Бюджетирование и размещение
    val ignoreBudget: Boolean = false,
    val estimatedTokens: Int = 50,
    val position: InjectionPosition = InjectionPosition.AFTER_CHAR,
    val depth: Int = 4
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LorebookEntrySpec) return false

        if (id != other.id) return false
        if (uid != other.uid) return false
        if (content != other.content) return false
        if (!primaryPatternIds.contentEquals(other.primaryPatternIds)) return false
        if (!secondaryPatternIds.contentEquals(other.secondaryPatternIds)) return false
        if (regexPatterns != other.regexPatterns) return false
        if (selective != other.selective) return false
        if (filterLogic != other.filterLogic) return false
        if (proximityWindowWords != other.proximityWindowWords) return false
        if (constant != other.constant) return false
        if (enabled != other.enabled) return false
        if (insertionOrder != other.insertionOrder) return false
        if (priority != other.priority) return false
        if (scanDepthOverride != other.scanDepthOverride) return false
        if (probability != other.probability) return false
        if (excludeRecursion != other.excludeRecursion) return false
        if (preventRecursion != other.preventRecursion) return false
        if (delayUntilRecursion != other.delayUntilRecursion) return false
        if (recursionLevel != other.recursionLevel) return false
        if (stickyTurns != other.stickyTurns) return false
        if (cooldownTurns != other.cooldownTurns) return false
        if (delayMessages != other.delayMessages) return false
        if (ignoreBudget != other.ignoreBudget) return false
        if (estimatedTokens != other.estimatedTokens) return false
        if (position != other.position) return false
        if (depth != other.depth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + uid.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + primaryPatternIds.contentHashCode()
        result = 31 * result + secondaryPatternIds.contentHashCode()
        result = 31 * result + regexPatterns.hashCode()
        result = 31 * result + selective.hashCode()
        result = 31 * result + filterLogic.hashCode()
        result = 31 * result + (proximityWindowWords ?: 0)
        result = 31 * result + constant.hashCode()
        result = 31 * result + enabled.hashCode()
        result = 31 * result + insertionOrder
        result = 31 * result + priority
        result = 31 * result + (scanDepthOverride ?: 0)
        result = 31 * result + probability
        result = 31 * result + excludeRecursion.hashCode()
        result = 31 * result + preventRecursion.hashCode()
        result = 31 * result + delayUntilRecursion.hashCode()
        result = 31 * result + recursionLevel
        result = 31 * result + stickyTurns
        result = 31 * result + cooldownTurns
        result = 31 * result + delayMessages
        result = 31 * result + ignoreBudget.hashCode()
        result = 31 * result + estimatedTokens
        result = 31 * result + position.hashCode()
        result = 31 * result + depth
        return result
    }
}

/**
 * Иммутабельное состояние временных эффектов для одной статьи лорбука.
 * Сохраняется на узле каждого сообщения чата для поддержки свайпов и откатов.
 */
@JvmRecord
data class EntryTimedState(
    val stickyRemaining: Int = 0,
    val cooldownRemaining: Int = 0,
    val everMatchedCount: Int = 0
)

/**
 * Результат упаковки статей в заданный бюджет токенов.
 */
data class BudgetPackingResult(
    val selectedEntries: List<LorebookEntrySpec>,
    val droppedEntries: List<LorebookEntrySpec>,
    val tokensUsed: Int,
    val budgetOverflowed: Boolean
)

/**
 * Статья лорбука, внедряемая в историю чата на определенную глубину от конца (AT_DEPTH).
 */
data class LorebookDepthEntry(
    val depth: Int,
    val content: String
)

