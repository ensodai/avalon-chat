package app.service

import com.ensodai.shared.avalon.character.spec.CharacterCardEnvelope
import com.ensodai.shared.avalon.character.spec.PngCharacterCardCodec
import com.ensodai.shared.avalon.character.spec.characterCardJson
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PngCharacterCardCodecTest {

    // Минимальный 1x1 прозрачный RGBA PNG
    private val samplePng = byteArrayOf(
        0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), 0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte(),
        0x00, 0x00, 0x00, 0x0D,
        0x49, 0x48, 0x44, 0x52,
        0x00, 0x00, 0x00, 0x01,
        0x00, 0x00, 0x00, 0x01,
        0x08, 0x06, 0x00, 0x00, 0x00,
        0x1F, 0x15, 0xC4.toByte(), 0x89.toByte(),
        0x00, 0x00, 0x00, 0x0B,
        0x49, 0x44, 0x41, 0x54,
        0x78, 0x9C.toByte(), 0x63, 0x60, 0x00, 0x00, 0x00, 0x02, 0x00, 0x01, 0x73, 0x75, 0x01, 0x18,
        0x00, 0x00, 0x00, 0x00,
        0x49, 0x45, 0x4E, 0x44,
        0xAE.toByte(), 0x42, 0x60, 0x82.toByte()
    )

    @Test
    fun testEmbedAndExtractCardV3() {
        val testJson = """
            {
                "spec": "chara_card_v3",
                "spec_version": "3.0",
                "data": {
                    "name": "Алиса",
                    "description": "Кибернетическая спутница",
                    "personality": "Любопытная, преданная",
                    "scenario": "Лаборатория Нео-Токио",
                    "first_mes": "Привет, исследователь!",
                    "mes_example": "<START>\n{{user}}: Кто ты?\n{{char}}: Твой верный ИИ.",
                    "tags": ["sci-fi", "cyberpunk"]
                }
            }
        """.trimIndent()

        // Внедряем чанк ccv3
        val pngWithCard = PngCharacterCardCodec.embedCardJson(samplePng, testJson, "ccv3")
        assertTrue(pngWithCard.size > samplePng.size)
        assertTrue(PngCharacterCardCodec.isPng(pngWithCard))

        // Извлекаем карточку
        val parsed = PngCharacterCardCodec.extractCard(pngWithCard)
        assertNotNull(parsed)
        assertEquals("v3", parsed.specVersion)
        assertEquals("Алиса", parsed.data.name)
        assertEquals("Кибернетическая спутница", parsed.data.description)
        assertEquals("Лаборатория Нео-Токио", parsed.data.scenario)
        assertEquals("Привет, исследователь!", parsed.data.firstMes)
        assertEquals(listOf("sci-fi", "cyberpunk"), parsed.data.tags)
    }

    @Test
    fun testEmbedAndExtractCardV2() {
        val testJson = """
            {
                "spec": "chara_card_v2",
                "spec_version": "2.0",
                "data": {
                    "name": "Маг Элрион",
                    "description": "Древний архимаг",
                    "first_mes": "Что привело тебя в мою башню?"
                }
            }
        """.trimIndent()

        // Внедряем чанк chara (V2)
        val pngWithCard = PngCharacterCardCodec.embedCardJson(samplePng, testJson, "chara")
        assertTrue(PngCharacterCardCodec.isPng(pngWithCard))

        val parsed = PngCharacterCardCodec.extractCard(pngWithCard)
        assertNotNull(parsed)
        assertEquals("v2", parsed.specVersion)
        assertEquals("Маг Элрион", parsed.data.name)
        assertEquals("Что привело тебя в мою башню?", parsed.data.firstMes)
    }
}
