package app.service.lorebook.engine

import app.service.lorebook.matcher.CompiledAhoCorasick
import app.service.lorebook.matcher.KeywordMatch
import app.service.lorebook.model.EntryTimedState
import app.service.lorebook.model.LorebookEntrySpec
import com.ensodai.shared.avalon.character.models.SecondaryFilterLogic
import kotlin.math.abs
import kotlin.random.Random

data class ChatTurnMessage(
    val id: String,
    val speakerName: String,
    val text: String,
    val isAssistant: Boolean
)

data class EvaluationOutput(
    val activatedEntries: List<LorebookEntrySpec>,
    val nextTimedStates: Map<Int, EntryTimedState>,
    val recursionPassesExecuted: Int
)

class LorebookEvaluationEngine(
    private val entries: List<LorebookEntrySpec>,
    private val automaton: CompiledAhoCorasick,
    private val globalScanDepth: Int = 2,
    private val recursiveScanning: Boolean = true,
    private val maxRecursionSteps: Int = 3
) {

    fun evaluate(
        chatHistory: List<ChatTurnMessage>,
        previousTimedStates: Map<Int, EntryTimedState>,
        random: Random = Random.Default
    ): EvaluationOutput {
        val chatLength = chatHistory.size
        val activatedIds = HashSet<Int>()
        val activatedEntries = ArrayList<LorebookEntrySpec>()
        val nextTimedStates = HashMap<Int, EntryTimedState>(previousTimedStates.size)
        var newlyActivatedFrontier = ArrayList<LorebookEntrySpec>()

        // Форматирование сообщений с префиксом SillyTavern \u0001Speaker: для селективных регулярок
        val formattedMessages = chatHistory.map { "\u0001${it.speakerName}:${it.text}" }

        // Кэш результатов сканирования для уникальных глубин (исключает повторный скан для одинакового scanDepth)
        val scanCache = HashMap<Int, Pair<String, Map<Int, List<KeywordMatch>>>>()

        // ПРОХОД 0: Первичный скан контекста + Переходы машины временных состояний
        for (entry in entries) {
            if (!entry.enabled) continue

            val prevState = previousTimedStates[entry.id] ?: EntryTimedState()

            // 1. Проверка задержки Delay (блокирует даже Constant статьи, пока chatLength < delayMessages)
            if (entry.delayMessages > 0 && chatLength < entry.delayMessages) {
                nextTimedStates[entry.id] = prevState
                continue
            }

            // 2. Проверка активного состояния Sticky (принудительная активация в обход ключей и вероятности)
            if (prevState.stickyRemaining > 0) {
                activatedIds.add(entry.id)
                activatedEntries.add(entry)
                newlyActivatedFrontier.add(entry)
                val nextSticky = prevState.stickyRemaining - 1
                val nextCooldown = if (nextSticky == 0 && entry.cooldownTurns > 0) entry.cooldownTurns else 0
                nextTimedStates[entry.id] = prevState.copy(
                    stickyRemaining = nextSticky,
                    cooldownRemaining = nextCooldown
                )
                continue
            }

            // 3. Проверка блокировки по кулдауну Cooldown (блокирует как Constant, так и обычные статьи)
            if (prevState.cooldownRemaining > 0) {
                nextTimedStates[entry.id] = prevState.copy(
                    cooldownRemaining = prevState.cooldownRemaining - 1
                )
                continue
            }

            // 4. Оценка постоянных (Constant) или совпадающих по ключам статей (если не отложены до рекурсии)
            val matchedInPass0 = when {
                entry.constant -> true
                entry.delayUntilRecursion -> false
                else -> {
                    val depth = (entry.scanDepthOverride ?: globalScanDepth).coerceAtLeast(0)
                    if (depth == 0) {
                        false
                    } else {
                        val (windowText, hitMap) = scanCache.getOrPut(depth) {
                            val text = formattedMessages.takeLast(depth).joinToString("\n")
                            val hits = automaton.scan(text, caseSensitive = false).groupBy { it.patternId }
                            text to hits
                        }
                        matchesFromPrecomputedHits(entry, windowText, hitMap)
                    }
                }
            }

            if (matchedInPass0 && passesProbability(entry.probability, random)) {
                activatedIds.add(entry.id)
                activatedEntries.add(entry)
                newlyActivatedFrontier.add(entry)
                nextTimedStates[entry.id] = computePostTriggerState(entry, prevState)
            } else {
                nextTimedStates[entry.id] = prevState
            }
        }

        // ПРОХОДЫ 1..maxRecursionSteps: Монотонный рекурсивный каскадный цикл
        var passCount = 0
        if (recursiveScanning) {
            while (passCount < maxRecursionSteps && newlyActivatedFrontier.isNotEmpty()) {
                val recursiveBuffer = newlyActivatedFrontier
                    .asSequence()
                    .filter { !it.preventRecursion }
                    .joinToString("\n") { it.content }

                if (recursiveBuffer.isBlank()) break

                passCount++
                val nextFrontier = ArrayList<LorebookEntrySpec>()
                val patternHits = automaton.scan(recursiveBuffer, caseSensitive = false)
                val hitMap = patternHits.groupBy { it.patternId }

                for (entry in entries) {
                    if (!entry.enabled || entry.id in activatedIds || entry.excludeRecursion) continue
                    if (entry.delayMessages > 0 && chatLength < entry.delayMessages) continue
                    val state = nextTimedStates[entry.id] ?: EntryTimedState()
                    if (state.cooldownRemaining > 0) continue

                    if (matchesFromPrecomputedHits(entry, recursiveBuffer, hitMap) &&
                        passesProbability(entry.probability, random)
                    ) {
                        activatedIds.add(entry.id)
                        activatedEntries.add(entry)
                        nextFrontier.add(entry)
                        nextTimedStates[entry.id] = computePostTriggerState(entry, state)
                    }
                }
                newlyActivatedFrontier = nextFrontier
            }
        }

        return EvaluationOutput(
            activatedEntries = activatedEntries,
            nextTimedStates = nextTimedStates,
            recursionPassesExecuted = passCount
        )
    }

    private fun computePostTriggerState(entry: LorebookEntrySpec, prev: EntryTimedState): EntryTimedState {
        val nextSticky = entry.stickyTurns
        val nextCooldown = if (nextSticky == 0 && entry.cooldownTurns > 0) entry.cooldownTurns else 0
        return EntryTimedState(
            stickyRemaining = nextSticky,
            cooldownRemaining = nextCooldown,
            everMatchedCount = prev.everMatchedCount + 1
        )
    }

    private fun matchesFromPrecomputedHits(
        entry: LorebookEntrySpec,
        haystack: String,
        hitMap: Map<Int, List<KeywordMatch>>
    ): Boolean {
        val primaryMatches = entry.primaryPatternIds.flatMap { hitMap[it].orEmpty() }
        val regexMatched = entry.regexPatterns.any { it.containsMatchIn(haystack) }

        if (primaryMatches.isEmpty() && !regexMatched) return false
        if (!entry.selective || entry.secondaryPatternIds.isEmpty()) return true

        val secondaryMatches = entry.secondaryPatternIds.flatMap { hitMap[it].orEmpty() }
        val secondaryPresentCount = entry.secondaryPatternIds.count { hitMap.containsKey(it) }
        val totalSecondary = entry.secondaryPatternIds.size

        val booleanPassed = when (entry.filterLogic) {
            SecondaryFilterLogic.AND_ANY -> secondaryPresentCount > 0
            SecondaryFilterLogic.AND_ALL -> secondaryPresentCount == totalSecondary
            SecondaryFilterLogic.NOT_ANY -> secondaryPresentCount == 0
            SecondaryFilterLogic.NOT_ALL -> secondaryPresentCount < totalSecondary
        }
        if (!booleanPassed) return false

        // Проверка окна близости слов между первичными и вторичными ключами
        val maxWordDistance = entry.proximityWindowWords
        if (maxWordDistance != null && primaryMatches.isNotEmpty() &&
            (entry.filterLogic == SecondaryFilterLogic.AND_ANY || entry.filterLogic == SecondaryFilterLogic.AND_ALL)
        ) {
            return hasMatchWithinWordWindow(primaryMatches, secondaryMatches, maxWordDistance)
        }

        return true
    }

    private fun hasMatchWithinWordWindow(
        primary: List<KeywordMatch>,
        secondary: List<KeywordMatch>,
        maxDistance: Int
    ): Boolean {
        for (p in primary) {
            for (s in secondary) {
                if (abs(p.wordTokenIndex - s.wordTokenIndex) <= maxDistance) return true
            }
        }
        return false
    }

    private fun passesProbability(probability: Int, random: Random): Boolean =
        when {
            probability >= 100 -> true
            probability <= 0 -> false
            else -> random.nextInt(100) < probability
        }
}
