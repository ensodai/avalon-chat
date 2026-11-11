package com.ensodai.avalon.domain.useCases.chatScreenUseCase.chat

import com.ensodai.avalon.domain.models.chat.RpgMessageBlock
import org.koin.core.annotation.Single

/**
 * Юзкейс для парсинга текстовых сообщений RPG-формата на отдельные смысловые блоки [RpgMessageBlock].
 *
 * Парсер использует посимвольный обход строки (конечный автомат) с технологией **look-ahead**
 * (сканирование вперед). Это гарантирует, что переключение состояний происходит только в том случае,
 * если в текущей строке есть закрывающий парный символ (например, для действий в звездочках `*` или мыслей `()`).
 *
 * Такой подход предотвращает каскадный сдвиг состояний при встрече одиночных звездочек
 * в пунктах списков Markdown (`* Пункт`) или скобок в Markdown-ссылках `[Текст](URL)`.
 *
 * ### Примеры парсинга:
 * - `*Я улыбаюсь.* Привет!` -> `Action("Я улыбаюсь.")`, `Plain(" Привет!")`
 * - `(Интересно...)` -> `Thought("Интересно...")`
 * - `Система: **Внимание!**` -> `Plain("Система: ")`, `Event("Внимание!")`
 * - `- Как дела?` -> `Speech("- Как дела?")`
 * - `[Google](https://google.com)` -> `Plain("[Google](https://google.com)")` (скобки в ссылках не парсятся как мысли)
 */
@Single
class ParseRpgMessageUseCase {

    /**
     * Внутренние состояния конечного автомата парсера.
     */
    private enum class State {
        /**
         * Обычный текст (без кавычек и ролевой разметки).
         * Пример: Привет, как дела?
         */
        PLAIN,

        /**
         * Прямая речь персонажа. Ограничивается стандартными кавычками ("...", «...», “...”) или тире в начале строки.
         * Пример: "Я пришел", «Я пришел» или - Привет!
         */
        SPEECH,

        /**
         * Действие персонажа или описание происходящего. Ограничивается одинарными звездочками.
         * Пример: *улыбается и машет рукой*
         */
        ACTION,

        /**
         * Важное системное или игровое событие (жирный текст в Markdown). Ограничивается двойными звездочками.
         * Пример: **Уровень повышен!**
         */
        EVENT,

        /**
         * Внутренние мысли персонажа. Ограничиваются круглыми скобками.
         * Пример: (Что он имеет в виду?)
         */
        THOUGHT,

        /**
         * Блок или строка кода. Ролевая разметка внутри игнорируется.
         * Пример: `print("Hello")` или ```kotlin ... ```
         */
        CODE
    }

    operator fun invoke(rawText: String): List<RpgMessageBlock> {
        val blocks = mutableListOf<RpgMessageBlock>()
        var currentState = State.PLAIN
        val stateStack = mutableListOf<State>()
        val buffer = StringBuilder()
        var activeClosingLine = -1
        var activeClosingChar = -1
        var activeClosingLength = 0

        fun flushBuffer() {
            if (buffer.isEmpty()) return
            val text = buffer.toString()
            if (text.isBlank()) {
                buffer.clear()
                return
            }
            when (currentState) {
                State.PLAIN -> blocks.add(RpgMessageBlock.Plain(text))
                State.SPEECH -> blocks.add(RpgMessageBlock.Speech(text))
                State.ACTION -> blocks.add(RpgMessageBlock.Action(text))
                State.EVENT -> blocks.add(RpgMessageBlock.Event(text))
                State.THOUGHT -> blocks.add(RpgMessageBlock.Thought(text))
                State.CODE -> blocks.add(RpgMessageBlock.Plain(text))
            }
            buffer.clear()
        }

        fun pushState(newState: State) {
            flushBuffer()
            stateStack.add(currentState)
            currentState = newState
        }

        fun popState() {
            flushBuffer()
            if (stateStack.isNotEmpty()) {
                currentState = stateStack.removeAt(stateStack.lastIndex)
            } else {
                currentState = State.PLAIN
            }
            activeClosingLine = -1
            activeClosingChar = -1
            activeClosingLength = 0
        }

        fun findClosingEventIndex(line: String, startIndex: Int): Int {
            var j = startIndex
            while (j + 1 < line.length) {
                if (line[j] == '*' && line[j + 1] == '*') {
                    if (j > 0 && (!line[j - 1].isWhitespace() || line.substring(startIndex, j).all { it.isWhitespace() })) {
                        return j
                    }
                }
                j++
            }
            return -1
        }

        fun findClosingActionIndex(line: String, startIndex: Int): Int {
            var j = startIndex
            while (j < line.length) {
                if (line[j] == '*') {
                    if (j + 1 < line.length && line[j + 1] == '*') {
                        j += 2
                        continue
                    }
                    if (j > 0 && (!line[j - 1].isWhitespace() || line.substring(startIndex, j).all { it.isWhitespace() })) {
                        return j
                    }
                }
                j++
            }
            return -1
        }

        fun findClosingThoughtIndex(line: String, startIndex: Int): Int {
            var balance = 1
            var j = startIndex
            while (j < line.length) {
                if (line[j] == '(') {
                    balance++
                } else if (line[j] == ')') {
                    balance--
                    if (balance == 0) {
                        if (j > 0 && (!line[j - 1].isWhitespace() || line.substring(startIndex, j).all { it.isWhitespace() })) {
                            return j
                        }
                    }
                }
                j++
            }
            return -1
        }

        fun findClosingSpeechIndex(line: String, startIndex: Int, openQuote: Char): Int {
            val closeQuotes = when (openQuote) {
                '"' -> listOf('"')
                '«' -> listOf('»', '«')
                '»' -> listOf('«', '»')
                '“' -> listOf('”', '“')
                '”' -> listOf('“', '”')
                else -> listOf(openQuote)
            }
            var j = startIndex
            while (j < line.length) {
                if (line[j] in closeQuotes) {
                    if (j > 0 && (!line[j - 1].isWhitespace() || line.substring(startIndex, j).all { it.isWhitespace() })) {
                        return j
                    }
                }
                j++
            }
            return -1
        }

        fun findClosingTripleBacktick(lines: List<String>, startLineIdx: Int, startCharIdx: Int): Pair<Int, Int>? {
            val currentLine = lines[startLineIdx]
            val firstMatch = currentLine.indexOf("```", startCharIdx + 3)
            if (firstMatch != -1) {
                return Pair(startLineIdx, firstMatch)
            }
            for (l in (startLineIdx + 1) until lines.size) {
                val match = lines[l].indexOf("```")
                if (match != -1) {
                    return Pair(l, match)
                }
            }
            return null
        }

        fun isSeparatingDash(line: String, index: Int): Boolean {
            val c = line[index]
            if (c != '-' && c != '—' && c != '~') return false

            if (index + 1 < line.length && line[index + 1] == '-') return false
            if (index - 1 >= 0 && line[index - 1] == '-') return false

            val hasSpaceBefore = index - 1 >= 0 && (line[index - 1] == ' ' || line[index - 1] == ' ')
            val hasSpaceAfter = index + 1 < line.length && (line[index + 1] == ' ' || line[index + 1] == ' ')

            val hasPunctuationAndSpaceBefore = index - 2 >= 0 &&
                (line[index - 1] == ' ' || line[index - 1] == ' ') &&
                (line[index - 2] == ',' || line[index - 2] == '.' || line[index - 2] == '!' || line[index - 2] == '?' || line[index - 2] == '…')

            return (hasSpaceBefore && hasSpaceAfter) || hasPunctuationAndSpaceBefore
        }

        val lines = rawText.split("\n")

        lines.forEachIndexed { lineIdx, line ->
            val trimmedLine = line.trimStart()
            val startsWithDash = trimmedLine.startsWith("- ") || trimmedLine.startsWith("--") ||
                    trimmedLine.startsWith("—") || trimmedLine.startsWith("~")

            if (currentState != State.PLAIN && activeClosingLine == -1) {
                if (currentState == State.CODE) {
                    val match = line.indexOf("```")
                    if (match != -1) {
                        activeClosingLine = lineIdx
                        activeClosingChar = match
                        activeClosingLength = 3
                    }
                } else {
                    activeClosingLine = lineIdx
                    activeClosingChar = when (currentState) {
                        State.EVENT -> findClosingEventIndex(line, 0)
                        State.ACTION -> findClosingActionIndex(line, 0)
                        State.THOUGHT -> findClosingThoughtIndex(line, 0)
                        State.SPEECH -> findClosingSpeechIndex(line, 0, '"')
                        else -> -1
                    }
                    activeClosingLength = if (currentState == State.EVENT) 2 else 1
                }
            }

            var forceSpeechLine = startsWithDash && !line.contains(":") && currentState == State.PLAIN
            if (forceSpeechLine) {
                pushState(State.SPEECH)
            }

            var i = 0
            while (i < line.length) {
                val c = line[i]

                if (activeClosingLine != -1) {
                    if (lineIdx == activeClosingLine && i == activeClosingChar) {
                        val length = activeClosingLength
                        if (currentState == State.CODE) {
                            if (length == 3) {
                                buffer.append("```")
                            } else {
                                buffer.append("`")
                            }
                        }
                        popState()
                        i += length
                        continue
                    } else {
                        buffer.append(c)
                        i++
                        continue
                    }
                }

                if ((c == '-' || c == '—' || c == '~') && isSeparatingDash(line, i)) {
                    if (currentState == State.SPEECH) {
                        popState()
                        forceSpeechLine = false
                        buffer.append(c)
                        i++
                        continue
                    } else if (currentState == State.PLAIN && i - 2 >= 0 &&
                               (line[i - 2] == '.' || line[i - 2] == '!' || line[i - 2] == '?' || line[i - 2] == '…')) {
                        pushState(State.SPEECH)
                        buffer.append(c)
                        i++
                        continue
                    }
                }

                if (c == '`' && i + 2 < line.length && line[i + 1] == '`' && line[i + 2] == '`') {
                    if (currentState == State.PLAIN) {
                        val closePair = findClosingTripleBacktick(lines, lineIdx, i)
                        if (closePair != null) {
                            pushState(State.CODE)
                            buffer.append("```")
                            activeClosingLine = closePair.first
                            activeClosingChar = closePair.second
                            activeClosingLength = 3
                            i += 3
                            continue
                        }
                    }
                }

                if (c == '`') {
                    if (currentState == State.PLAIN) {
                        val closeIdx = line.indexOf('`', i + 1)
                        if (closeIdx != -1) {
                            pushState(State.CODE)
                            buffer.append("`")
                            activeClosingLine = lineIdx
                            activeClosingChar = closeIdx
                            activeClosingLength = 1
                            i++
                            continue
                        }
                    }
                }

                if (c == '*' && i + 1 < line.length && line[i + 1] == '*') {
                    if (currentState == State.PLAIN) {
                        val isValidStart = i + 2 < line.length && (
                            line[i + 2].isLetterOrDigit() ||
                            (line[i + 2] == '.' && i + 3 < line.length && line[i + 3] == '.') ||
                            (line[i + 2].isWhitespace() && i + 4 < line.length && line[i + 3] == '*' && line[i + 4] == '*')
                        )
                        if (isValidStart) {
                            val closeIdx = findClosingEventIndex(line, i + 2)
                            if (closeIdx != -1) {
                                pushState(State.EVENT)
                                activeClosingLine = lineIdx
                                activeClosingChar = closeIdx
                                activeClosingLength = 2
                                i += 2
                                continue
                            }
                        }
                    }
                }

                if (c == '*') {
                    if (currentState == State.PLAIN) {
                        val isValidStart = i + 1 < line.length && (
                            line[i + 1].isLetterOrDigit() ||
                            (line[i + 1] == '.' && i + 2 < line.length && line[i + 2] == '.') ||
                            (line[i + 1].isWhitespace() && i + 2 < line.length && line[i + 2] == '*')
                        )
                        if (isValidStart) {
                            val closeIdx = findClosingActionIndex(line, i + 1)
                            if (closeIdx != -1) {
                                pushState(State.ACTION)
                                activeClosingLine = lineIdx
                                activeClosingChar = closeIdx
                                activeClosingLength = 1
                                i++
                                continue
                            }
                        }
                    }
                }

                if (c == '"' || c == '«' || c == '»' || c == '“' || c == '”') {
                    if (currentState == State.PLAIN) {
                        if (i + 1 < line.length && !line[i + 1].isWhitespace()) {
                            val closeIdx = findClosingSpeechIndex(line, i + 1, c)
                            if (closeIdx != -1) {
                                pushState(State.SPEECH)
                                activeClosingLine = lineIdx
                                activeClosingChar = closeIdx
                                activeClosingLength = 1
                                i++
                                continue
                            }
                        }
                    }
                }

                // Проверка на мысли в скобках
                if (c == '(') {
                    val isLink = i - 1 >= 0 && line[i - 1] == ']'
                    if (!isLink && currentState == State.PLAIN) {
                        val isValidStart = i + 1 < line.length && line[i + 1] != ')'
                        if (isValidStart) {
                            val closeIdx = findClosingThoughtIndex(line, i + 1)
                            if (closeIdx != -1) {
                                pushState(State.THOUGHT)
                                activeClosingLine = lineIdx
                                activeClosingChar = closeIdx
                                activeClosingLength = 1
                                i++
                                continue
                            }
                        }
                    }
                }

                buffer.append(c)
                i++
            }

            if (lineIdx < lines.size - 1) {
                buffer.append("\n")
            }

            if (forceSpeechLine && currentState == State.SPEECH) {
                popState()
            }
        }

        flushBuffer()

        return blocks.filter { it.text.trim().isNotEmpty() }
    }
}
