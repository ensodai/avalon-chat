package app.service.chat

import app.service.lorebook.budget.TokenBudgetOptimizer
import app.service.lorebook.engine.ChatTurnMessage
import app.service.lorebook.engine.LorebookEvaluationEngine
import app.service.lorebook.matcher.CompiledAhoCorasick
import app.service.lorebook.model.EntryTimedState
import app.service.lorebook.model.InjectionPosition
import app.service.lorebook.model.LorebookDepthEntry
import app.service.lorebook.model.LorebookEntrySpec
import com.ensodai.shared.avalon.character.models.CharacterLorebookDetailsDto
import com.ensodai.shared.avalon.character.models.CharacterLorebookEntryItemDto
import com.ensodai.shared.avalon.character.models.LorebookEntryPosition
import com.ensodai.shared.avalon.character.models.SecondaryFilterLogic
import org.koin.core.annotation.Single
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

data class MatchedLorebookResult(
    val beforeChar: List<String> = emptyList(),
    val afterChar: List<String> = emptyList(),
    val beforeDesc: List<String> = emptyList(),
    val afterDesc: List<String> = emptyList(),
    val personality: List<String> = emptyList(),
    val scenario: List<String> = emptyList(),
    val atDepth: List<LorebookDepthEntry> = emptyList(),
    val nextTimedStates: Map<Uuid, EntryTimedState> = emptyMap()
)

@Single
class LorebookMatcher {

    // Кэш скомпилированных конечных автоматов по хешу записей лорбука
    private val automatonCache = ConcurrentHashMap<Pair<Uuid, Int>, Pair<CompiledAhoCorasick, List<LorebookEntrySpec>>>()

    /**
     * Сканирует текст недавних сообщений на совпадение со статьями нескольких Lorebook
     * с помощью промышленного ядра Avalon Lorebook Engine.
     */
    fun matchMultiple(
        lorebooks: List<CharacterLorebookDetailsDto>,
        recentMessagesText: List<String>,
        previousTimedStates: Map<Uuid, EntryTimedState> = emptyMap()
    ): MatchedLorebookResult {
        if (lorebooks.isEmpty()) {
            return MatchedLorebookResult()
        }

        val chatMessages = recentMessagesText.mapIndexed { idx, text ->
            ChatTurnMessage(
                id = idx.toString(),
                speakerName = if (idx % 2 == 0) "User" else "Assistant",
                text = text,
                isAssistant = idx % 2 != 0
            )
        }

        val allBudgetedEntries = ArrayList<LorebookEntrySpec>()
        val allNextTimedStates = HashMap<Uuid, EntryTimedState>()

        for (lorebook in lorebooks) {
            if (lorebook.entries.isEmpty()) continue

            val (automaton, specs) = compileBook(lorebook)
            if (specs.isEmpty()) continue

            val specIdByUid = specs.associate { it.uid to it.id }
            val intPreviousStates = previousTimedStates.mapNotNull { (uuid, state) ->
                val intId = specIdByUid[uuid.toString()] ?: return@mapNotNull null
                intId to state
            }.toMap()

            val engine = LorebookEvaluationEngine(
                entries = specs,
                automaton = automaton,
                globalScanDepth = lorebook.scanDepth.coerceAtLeast(1),
                recursiveScanning = lorebook.recursiveScanning,
                maxRecursionSteps = 3
            )

            val evaluationOutput = engine.evaluate(chatMessages, intPreviousStates)

            val uidBySpecId = specs.associate { it.id to it.uid }
            evaluationOutput.nextTimedStates.forEach { (intId, state) ->
                val uidStr = uidBySpecId[intId]
                if (uidStr != null) {
                    runCatching { Uuid.parse(uidStr) }.getOrNull()?.let { uuid ->
                        allNextTimedStates[uuid] = state
                    }
                }
            }

            if (evaluationOutput.activatedEntries.isEmpty()) continue

            val packingResult = TokenBudgetOptimizer.packEntries(
                activated = evaluationOutput.activatedEntries,
                tokenBudget = lorebook.tokenBudget ?: Int.MAX_VALUE
            )

            allBudgetedEntries.addAll(packingResult.selectedEntries)
        }

        // Финальная сортировка по каноническому порядку для стабилизации KV-кэша LLM
        val sortedEntries = allBudgetedEntries.sortedWith(
            compareBy<LorebookEntrySpec> { it.position.ordinal }
                .thenBy { it.insertionOrder }
                .thenBy { it.id }
        )

        val beforeChar = ArrayList<String>()
        val afterChar = ArrayList<String>()
        val beforeDesc = ArrayList<String>()
        val afterDesc = ArrayList<String>()
        val personality = ArrayList<String>()
        val scenario = ArrayList<String>()
        val atDepth = ArrayList<LorebookDepthEntry>()

        val entriesByUid = lorebooks.flatMap { it.entries }.associateBy { it.id.toString() }

        for (spec in sortedEntries) {
            val content = spec.content.trim()
            if (content.isBlank()) continue

            val originalEntry = entriesByUid[spec.uid]
            val formatted = if (!originalEntry?.comment.isNullOrBlank()) {
                "### ${originalEntry.comment}\n$content"
            } else {
                content
            }

            when (spec.position) {
                InjectionPosition.BEFORE_CHAR -> beforeChar.add(formatted)
                InjectionPosition.AFTER_CHAR -> afterChar.add(formatted)
                InjectionPosition.BEFORE_DESC -> beforeDesc.add(formatted)
                InjectionPosition.AFTER_DESC -> afterDesc.add(formatted)
                InjectionPosition.PERSONALITY -> personality.add(formatted)
                InjectionPosition.SCENARIO -> scenario.add(formatted)
                InjectionPosition.AT_DEPTH -> atDepth.add(LorebookDepthEntry(depth = spec.depth, content = formatted))
            }
        }

        return MatchedLorebookResult(
            beforeChar = beforeChar,
            afterChar = afterChar,
            beforeDesc = beforeDesc,
            afterDesc = afterDesc,
            personality = personality,
            scenario = scenario,
            atDepth = atDepth,
            nextTimedStates = allNextTimedStates
        )
    }

    /**
     * Одиночный метод для совместимости.
     */
    fun match(
        lorebook: CharacterLorebookDetailsDto?,
        recentMessagesText: List<String>,
        previousTimedStates: Map<Uuid, EntryTimedState> = emptyMap()
    ): MatchedLorebookResult {
        return matchMultiple(if (lorebook != null) listOf(lorebook) else emptyList(), recentMessagesText, previousTimedStates)
    }

    private fun compileBook(lorebook: CharacterLorebookDetailsDto): Pair<CompiledAhoCorasick, List<LorebookEntrySpec>> {
        val cacheKey = Pair(lorebook.id, lorebook.entries.hashCode())
        val cached = automatonCache[cacheKey]
        if (cached != null) return cached

        val builder = CompiledAhoCorasick.Builder()
        val specs = ArrayList<LorebookEntrySpec>(lorebook.entries.size)

        for ((idx, entry) in lorebook.entries.withIndex()) {
            if (!entry.enabled) continue

            val primaryPatternIds = ArrayList<Int>()
            val regexPatterns = ArrayList<Regex>()

            for (key in entry.keys) {
                val trimmed = key.trim()
                if (trimmed.isBlank()) continue
                if (entry.useRegex) {
                    runCatching { Regex(trimmed, RegexOption.IGNORE_CASE) }.getOrNull()?.let {
                        regexPatterns.add(it)
                    }
                } else {
                    primaryPatternIds.add(builder.addPattern(trimmed, wholeWord = true, caseSensitive = false))
                }
            }

            val secondaryPatternIds = ArrayList<Int>()
            if (entry.selective && !entry.secondaryKeys.isNullOrEmpty()) {
                for (secKey in entry.secondaryKeys!!) {
                    val trimmed = secKey.trim()
                    if (trimmed.isBlank()) continue
                    if (entry.useRegex) {
                        runCatching { Regex(trimmed, RegexOption.IGNORE_CASE) }.getOrNull()?.let {
                            regexPatterns.add(it)
                        }
                    } else {
                        secondaryPatternIds.add(builder.addPattern(trimmed, wholeWord = true, caseSensitive = false))
                    }
                }
            }

            val position = when (entry.position) {
                LorebookEntryPosition.BEFORE_CHAR -> InjectionPosition.BEFORE_CHAR
                LorebookEntryPosition.AFTER_CHAR -> InjectionPosition.AFTER_CHAR
                LorebookEntryPosition.BEFORE_DESC -> InjectionPosition.BEFORE_DESC
                LorebookEntryPosition.AFTER_DESC -> InjectionPosition.AFTER_DESC
                LorebookEntryPosition.PERSONALITY -> InjectionPosition.PERSONALITY
                LorebookEntryPosition.SCENARIO -> InjectionPosition.SCENARIO
                LorebookEntryPosition.AT_DEPTH -> InjectionPosition.AT_DEPTH
            }

            val estimatedTokens = (entry.content.length / 3.5).toInt().coerceAtLeast(1)

            specs.add(
                LorebookEntrySpec(
                    id = idx,
                    uid = entry.id.toString(),
                    content = entry.content,
                    primaryPatternIds = primaryPatternIds.toIntArray(),
                    secondaryPatternIds = secondaryPatternIds.toIntArray(),
                    regexPatterns = regexPatterns,
                    selective = entry.selective,
                    filterLogic = entry.secondaryLogic,
                    constant = entry.constant,
                    enabled = entry.enabled,
                    insertionOrder = entry.insertionOrder,
                    priority = entry.priority,
                    probability = entry.probability,
                    scanDepthOverride = entry.scanDepthOverride ?: lorebook.scanDepth,
                    stickyTurns = entry.stickyTurns,
                    cooldownTurns = entry.cooldownTurns,
                    delayMessages = entry.delayMessages,
                    excludeRecursion = entry.excludeRecursion,
                    preventRecursion = entry.preventRecursion,
                    delayUntilRecursion = entry.delayUntilRecursion,
                    estimatedTokens = estimatedTokens,
                    position = position,
                    depth = entry.depth
                )
            )
        }

        val compiled = Pair(builder.build(), specs)
        automatonCache[cacheKey] = compiled
        return compiled
    }
}
