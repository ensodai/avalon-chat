package app.service

import app.service.chat.ReactionParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ReactionParserTest {

    @Test
    fun `parse simple reaction tag`() {
        val raw = "Привет! Как твои дела?\n<reaction>🔥</reaction>"
        val (text, reaction) = ReactionParser.parseFullText(raw)
        assertEquals("Привет! Как твои дела?", text)
        assertEquals("🔥", reaction)
    }

    @Test
    fun `parse reaction tag with character subtag`() {
        val raw = "Это отличная новость!\n<reaction>\n<character>❤️</character>\n</reaction>"
        val (text, reaction) = ReactionParser.parseFullText(raw)
        assertEquals("Это отличная новость!", text)
        assertEquals("❤️", reaction)
    }

    @Test
    fun `parse message_reactions tag`() {
        val raw = "Увидимся вечером.\n<message_reactions>✨</message_reactions>"
        val (text, reaction) = ReactionParser.parseFullText(raw)
        assertEquals("Увидимся вечером.", text)
        assertEquals("✨", reaction)
    }

    @Test
    fun `parse unclosed reaction tag at end of text`() {
        val raw = "Я очень рада тебя видеть!<reaction>🎉"
        val (text, reaction) = ReactionParser.parseFullText(raw)
        assertEquals("Я очень рада тебя видеть!", text)
        assertEquals("🎉", reaction)
    }

    @Test
    fun `parse text without reaction`() {
        val raw = "Просто обычный ответ без реакций."
        val (text, reaction) = ReactionParser.parseFullText(raw)
        assertEquals("Просто обычный ответ без реакций.", text)
        assertNull(reaction)
    }

    @Test
    fun `streaming reaction filter with character subtag`() {
        val filter = StreamingReactionFilter()
        val chunk1 = "Привет! "
        val chunk2 = "<reaction>"
        val chunk3 = "<character>🔥</character>"
        val chunk4 = "</reaction>"

        val (res1, r1) = filter.process(chunk1)
        val (res2, r2) = filter.process(chunk2)
        val (res3, r3) = filter.process(chunk3)
        val (res4, r4) = filter.process(chunk4)

        assertEquals("Привет! ", res1)
        assertEquals("", res2)
        assertEquals("", res3)
        assertEquals("", res4)
        assertEquals("🔥", r4)
        assertEquals("", filter.flush())
    }
}
