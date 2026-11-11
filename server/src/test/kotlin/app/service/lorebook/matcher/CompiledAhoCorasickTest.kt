package app.service.lorebook.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompiledAhoCorasickTest {

    @Test
    fun `test exact cyrillic whole word match`() {
        val builder = CompiledAhoCorasick.Builder()
        val zamokId = builder.addPattern("замок", wholeWord = true, caseSensitive = false)
        val automaton = builder.build()

        val text = "На высокой горе стоял древний замок, окруженный рвом."
        val matches = automaton.scan(text, caseSensitive = false)

        assertEquals(1, matches.size)
        assertEquals(zamokId, matches[0].patternId)
        assertEquals("замок", text.substring(matches[0].startCharIndex, matches[0].endCharIndex))
    }

    @Test
    fun `test case insensitivity with cyrillic`() {
        val builder = CompiledAhoCorasick.Builder()
        val dragonId = builder.addPattern("дракон", wholeWord = true, caseSensitive = false)
        val automaton = builder.build()

        val text = "Огнедышащий ДРАКОН взмыл в небо! Другой Дракон спал."
        val matches = automaton.scan(text, caseSensitive = false)

        assertEquals(2, matches.size)
        assertEquals(dragonId, matches[0].patternId)
        assertEquals(dragonId, matches[1].patternId)
        assertEquals("ДРАКОН", text.substring(matches[0].startCharIndex, matches[0].endCharIndex))
        assertEquals("Дракон", text.substring(matches[1].startCharIndex, matches[1].endCharIndex))
    }

    @Test
    fun `test prevents substring false positives on cyrillic`() {
        val builder = CompiledAhoCorasick.Builder()
        builder.addPattern("кот", wholeWord = true, caseSensitive = false)
        val automaton = builder.build()

        // "кот" не должен срабатывать на "бойкот", "котофей", "подкоты"
        val negativeText = "Объявили бойкот компании. Котофей ушел в лес. Все подкоты провалились."
        val negativeMatches = automaton.scan(negativeText, caseSensitive = false)
        assertEquals(0, negativeMatches.size, "Не должно быть ложных срабатываний на подстроки слова 'кот'")

        // Но на отдельное слово "кот" с пунктуацией должно сработать
        val positiveText = "Рыжий кот, громко мяукая, запрыгнул на стол (это был кот)."
        val positiveMatches = automaton.scan(positiveText, caseSensitive = false)
        assertEquals(2, positiveMatches.size)
    }

    @Test
    fun `test symbol prefixed patterns like hashtags and mentions`() {
        val builder = CompiledAhoCorasick.Builder()
        val tagId = builder.addPattern("#эльфы", wholeWord = true, caseSensitive = false)
        val mentionId = builder.addPattern("@артур", wholeWord = true, caseSensitive = false)
        val automaton = builder.build()

        val text = "В лесу скрываются #эльфы, а их лидер — @артур! Но не##эльфы и не user@артур."
        val matches = automaton.scan(text, caseSensitive = false)

        assertEquals(2, matches.size)
        assertEquals(tagId, matches[0].patternId)
        assertEquals("#эльфы", text.substring(matches[0].startCharIndex, matches[0].endCharIndex))

        assertEquals(mentionId, matches[1].patternId)
        assertEquals("@артур", text.substring(matches[1].startCharIndex, matches[1].endCharIndex))
    }

    @Test
    fun `test punctuation boundaries and brackets`() {
        val builder = CompiledAhoCorasick.Builder()
        val swordId = builder.addPattern("меч", wholeWord = true, caseSensitive = false)
        val automaton = builder.build()

        val text = "Его оружие: «меч», [меч], (меч)... И снова меч!"
        val matches = automaton.scan(text, caseSensitive = false)

        assertEquals(4, matches.size)
        matches.forEach { match ->
            assertEquals(swordId, match.patternId)
            assertEquals("меч", text.substring(match.startCharIndex, match.endCharIndex))
        }
    }

    @Test
    fun `test overlapping patterns`() {
        val builder = CompiledAhoCorasick.Builder()
        val arthurId = builder.addPattern("Артур", wholeWord = true, caseSensitive = false)
        val kingArthurId = builder.addPattern("король Артур", wholeWord = true, caseSensitive = false)
        val excaliburId = builder.addPattern("Экскалибур", wholeWord = true, caseSensitive = false)
        val automaton = builder.build()

        val text = "Легендарный король Артур поднял Экскалибур."
        val matches = automaton.scan(text, caseSensitive = false)

        // Должны найтись: "король Артур", "Артур" и "Экскалибур"
        val matchedIds = matches.map { it.patternId }.toSet()
        assertTrue(matchedIds.contains(kingArthurId))
        assertTrue(matchedIds.contains(arthurId))
        assertTrue(matchedIds.contains(excaliburId))
    }

    @Test
    fun `test word token index calculation`() {
        val builder = CompiledAhoCorasick.Builder()
        val firstId = builder.addPattern("первый", wholeWord = true, caseSensitive = false)
        val thirdId = builder.addPattern("третий", wholeWord = true, caseSensitive = false)
        val automaton = builder.build()

        val text = "Это первый шаг и третий этап."
        val matches = automaton.scan(text, caseSensitive = false)

        assertEquals(2, matches.size)
        // Считаем слова: "Это" (1), "первый" (2), "шаг" (3), "и" (4), "третий" (5), "этап" (6)
        val firstMatch = matches.first { it.patternId == firstId }
        val thirdMatch = matches.first { it.patternId == thirdId }

        assertEquals(2, firstMatch.wordTokenIndex)
        assertEquals(5, thirdMatch.wordTokenIndex)
    }

    @Test
    fun `test empty and blank input`() {
        val builder = CompiledAhoCorasick.Builder()
        builder.addPattern("тест", wholeWord = true, caseSensitive = false)
        val automaton = builder.build()

        assertEquals(0, automaton.scan("", caseSensitive = false).size)
        assertEquals(0, automaton.scan("   \n\t  ", caseSensitive = false).size)
    }
}
