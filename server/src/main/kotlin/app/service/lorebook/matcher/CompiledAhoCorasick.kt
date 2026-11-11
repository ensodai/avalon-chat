package app.service.lorebook.matcher

import java.util.ArrayDeque

/**
 * Представляет собой единичное совпадение ключевого слова в сканируемом буфере текста.
 */
@JvmRecord
data class KeywordMatch(
    val patternId: Int,
    val startCharIndex: Int,
    val endCharIndex: Int,
    val wordTokenIndex: Int
)

/**
 * Высокопроизводительный конечный автомат Ахо-Корасик, скомпилированный в плоские
 * примитивные массивы для оптимальной локальности кэша L1/L2 процессора и нулевых
 * аллокаций в куче (heap allocations) во время сканирования текста.
 */
class CompiledAhoCorasick private constructor(
    private val stateKeys: Array<IntArray>,        // state -> отсортированные codePoints
    private val stateTargets: Array<IntArray>,     // state -> целевые состояния nextStates
    private val failLinks: IntArray,               // state -> failure state
    private val outputs: Array<IntArray>,          // state -> список совпавших patternIds
    private val patternLengths: IntArray,          // patternId -> длина паттерна в символах
    private val matchWholeWords: BooleanArray,     // patternId -> требуется ли граница целого слова
    private val startsWithAlnum: BooleanArray,     // patternId -> начинается ли с буквы/цифры
    private val endsWithAlnum: BooleanArray        // patternId -> оканчивается ли буквой/цифрой
) {

    /**
     * Сканирует [text] за время O(M + Z), где M — длина текста, Z — количество совпадений.
     * Корректно валидирует границы слов для Unicode (включая кириллицу) и тегов с префиксами (#хештег).
     */
    fun scan(text: CharSequence, caseSensitive: Boolean = false): List<KeywordMatch> {
        if (text.isEmpty() || stateKeys.size <= 1) return emptyList()

        val matches = ArrayList<KeywordMatch>(16)
        var currentState = 0
        var currentWordIndex = 0
        var inWord = false

        var i = 0
        val length = text.length
        while (i < length) {
            val rawCp = Character.codePointAt(text, i)
            val charCount = Character.charCount(rawCp)
            val isAlnum = Character.isLetterOrDigit(rawCp)

            if (isAlnum && !inWord) {
                currentWordIndex++
                inWord = true
            } else if (!isAlnum) {
                inWord = false
            }

            val cp = if (caseSensitive) rawCp else Character.toLowerCase(rawCp)

            // Переход по failure-ссылкам при несовпадении
            while (currentState != 0 && nextState(currentState, cp) == -1) {
                currentState = failLinks[currentState]
            }
            val next = nextState(currentState, cp)
            if (next != -1) {
                currentState = next
            }

            val matchedPatterns = outputs[currentState]
            if (matchedPatterns.isNotEmpty()) {
                val endIdx = i + charCount
                for (patternId in matchedPatterns) {
                    val startIdx = endIdx - patternLengths[patternId]
                    if (startIdx >= 0 && isValidBoundary(text, startIdx, endIdx, patternId)) {
                        matches.add(KeywordMatch(patternId, startIdx, endIdx, currentWordIndex))
                    }
                }
            }

            i += charCount
        }
        return matches
    }

    private fun nextState(state: Int, codePoint: Int): Int {
        val keys = stateKeys[state]
        if (keys.isEmpty()) return -1
        val idx = keys.binarySearch(codePoint)
        return if (idx >= 0) stateTargets[state][idx] else -1
    }

    /**
     * Проверяет границы слова в O(1) с учётом Unicode, знаков препинания и префиксов тегов.
     * Устраняет ложные срабатывания (например, «кот» в «бойкот») и поддерживает теги вроде #эльфы.
     */
    private fun isValidBoundary(text: CharSequence, startIdx: Int, endIdx: Int, patternId: Int): Boolean {
        if (!matchWholeWords[patternId]) return true

        // Левая граница
        if (startIdx > 0) {
            val prevCp = Character.codePointBefore(text, startIdx)
            if (startsWithAlnum[patternId]) {
                if (Character.isLetterOrDigit(prevCp)) return false
            } else {
                val firstPatternCp = Character.codePointAt(text, startIdx)
                if (Character.isLetterOrDigit(prevCp) || prevCp == firstPatternCp) return false
            }
        }

        // Правая граница
        if (endIdx < text.length) {
            val nextCp = Character.codePointAt(text, endIdx)
            if (endsWithAlnum[patternId]) {
                if (Character.isLetterOrDigit(nextCp)) return false
            } else {
                val lastPatternCp = Character.codePointBefore(text, endIdx)
                if (Character.isLetterOrDigit(nextCp) || nextCp == lastPatternCp) return false
            }
        }

        return true
    }

    class Builder {
        private class TrieNode {
            val children = HashMap<Int, Int>()
            val outputs = ArrayList<Int>()
        }

        private val nodes = ArrayList<TrieNode>().apply { add(TrieNode()) }
        private val patternLengths = ArrayList<Int>()
        private val matchWholeWords = ArrayList<Boolean>()
        private val startsWithAlnum = ArrayList<Boolean>()
        private val endsWithAlnum = ArrayList<Boolean>()

        fun addPattern(pattern: String, wholeWord: Boolean = true, caseSensitive: Boolean = false): Int {
            require(pattern.isNotEmpty()) { "Pattern must not be empty" }
            val patternId = patternLengths.size

            var current = 0
            var i = 0
            val len = pattern.length
            while (i < len) {
                val rawCp = Character.codePointAt(pattern, i)
                val cp = if (caseSensitive) rawCp else Character.toLowerCase(rawCp)
                val next = nodes[current].children.getOrPut(cp) {
                    nodes.add(TrieNode())
                    nodes.size - 1
                }
                current = next
                i += Character.charCount(rawCp)
            }

            nodes[current].outputs.add(patternId)
            patternLengths.add(pattern.length)
            matchWholeWords.add(wholeWord)

            val firstCp = Character.codePointAt(pattern, 0)
            val lastCp = Character.codePointBefore(pattern, pattern.length)
            startsWithAlnum.add(Character.isLetterOrDigit(firstCp))
            endsWithAlnum.add(Character.isLetterOrDigit(lastCp))

            return patternId
        }

        fun build(): CompiledAhoCorasick {
            val numStates = nodes.size
            val fail = IntArray(numStates)
            val queue = ArrayDeque<Int>()

            // Корень: глубина 1
            for ((_, targetState) in nodes[0].children) {
                fail[targetState] = 0
                queue.add(targetState)
            }

            // BFS расчёт failure links и перенос совпадений
            while (queue.isNotEmpty()) {
                val u = queue.poll()
                for ((cp, v) in nodes[u].children) {
                    var f = fail[u]
                    while (f != 0 && !nodes[f].children.containsKey(cp)) {
                        f = fail[f]
                    }
                    fail[v] = nodes[f].children[cp] ?: 0
                    nodes[v].outputs.addAll(nodes[fail[v]].outputs)
                    queue.add(v)
                }
            }

            // Сплющивание дерева в плоские примитивные массивы
            val stateKeys = Array(numStates) { s ->
                nodes[s].children.keys.sorted().toIntArray()
            }
            val stateTargets = Array(numStates) { s ->
                val sortedKeys = stateKeys[s]
                IntArray(sortedKeys.size) { idx -> nodes[s].children[sortedKeys[idx]]!! }
            }
            val compiledOutputs = Array(numStates) { s ->
                nodes[s].outputs.distinct().toIntArray()
            }

            return CompiledAhoCorasick(
                stateKeys = stateKeys,
                stateTargets = stateTargets,
                failLinks = fail,
                outputs = compiledOutputs,
                patternLengths = patternLengths.toIntArray(),
                matchWholeWords = matchWholeWords.toBooleanArray(),
                startsWithAlnum = startsWithAlnum.toBooleanArray(),
                endsWithAlnum = endsWithAlnum.toBooleanArray()
            )
        }
    }
}
