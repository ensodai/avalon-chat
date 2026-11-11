package app.service

import com.ensodai.shared.avalon.character.spec.CharacterCardParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CharacterCardParserTest {

    @Test
    fun testParseV3JsonWithLorebook() {
        val json = """
        {
            "spec": "chara_card_v3",
            "spec_version": "3.0",
            "data": {
                "name": "Кибер-Самурай",
                "description": "Защитник неонового сектора",
                "personality": "Сдержанный, суровый, честный воин",
                "scenario": "Улицы ночного Киото 2099",
                "first_mes": "Клинок обнажен. Говори, зачем пришел.",
                "mes_example": "<START>\n{{user}}: Помоги мне.\n{{char}}: Только если твое дело правое.",
                "system_prompt": "Отвечай строго в стиле нео-самурая.",
                "post_history_instructions": "Держи ответы краткими.",
                "creator": "MasterCraft",
                "character_version": "1.2",
                "tags": ["cyberpunk", "samurai", "action"],
                "alternate_greetings": [
                    "Тени сгущаются. Ты вовремя.",
                    "Кто стучит в ворота додзё?"
                ],
                "character_book": {
                    "name": "Хроники Нео-Киото",
                    "description": "Сведения о кланах и корпорациях",
                    "scan_depth": 7,
                    "token_budget": 1200,
                    "entries": [
                        {
                            "keys": ["катана", "клинок"],
                            "content": "Плазменный клинок типа 'Мурамаса-9', прорезает мономолекулярную броню.",
                            "enabled": true,
                            "constant": false,
                            "selective": true,
                            "secondary_keys": ["оружие"],
                            "position": "before_char"
                        },
                        {
                            "keys": ["корпорация"],
                            "content": "Мегакорпорация Арасака правит верхним городом.",
                            "enabled": true,
                            "constant": true,
                            "position": "after_char"
                        }
                    ]
                }
            }
        }
        """.trimIndent()

        val parsed = CharacterCardParser.parse(json)
        assertEquals("v3", parsed.specVersion)
        assertEquals("Кибер-Самурай", parsed.data.name)
        assertEquals("Защитник неонового сектора", parsed.data.description)
        assertEquals("Клинок обнажен. Говори, зачем пришел.", parsed.data.firstMes)
        assertEquals(2, parsed.data.alternateGreetings.size)
        assertEquals(listOf("cyberpunk", "samurai", "action"), parsed.data.tags)

        val book = parsed.data.characterBook
        assertNotNull(book)
        assertEquals("Хроники Нео-Киото", book.name)
        assertEquals(7, book.scanDepth)
        assertEquals(1200, book.tokenBudget)
        assertEquals(2, book.entries.size)

        val entry1 = book.entries[0]
        assertEquals(listOf("катана", "клинок"), entry1.keys)
        assertTrue(entry1.selective == true)
        assertEquals(listOf("оружие"), entry1.secondaryKeys)
        assertEquals("before_char", entry1.position)

        val entry2 = book.entries[1]
        assertEquals(listOf("корпорация"), entry2.keys)
        assertTrue(entry2.constant == true)
        assertEquals("after_char", entry2.position)
    }

    @Test
    fun testParseV2Json() {
        val json = """
        {
            "spec": "chara_card_v2",
            "spec_version": "2.0",
            "data": {
                "name": "Лира",
                "description": "Лесная фея",
                "personality": "Веселая, озорная",
                "first_mes": "Привет-привет!",
                "creator_notes": "Создано для уютного общения"
            }
        }
        """.trimIndent()

        val parsed = CharacterCardParser.parse(json)
        assertEquals("v2", parsed.specVersion)
        assertEquals("Лира", parsed.data.name)
        assertEquals("Лесная фея", parsed.data.description)
        assertEquals("Создано для уютного общения", parsed.data.creatorNotes)
    }

    @Test
    fun testParseV1Json() {
        val json = """
        {
            "name": "Ретро Бот",
            "description": "Старый добрый бот",
            "personality": "Ностальгический",
            "first_mes": "Добро пожаловать в 80-е!"
        }
        """.trimIndent()

        val parsed = CharacterCardParser.parse(json)
        assertEquals("v1", parsed.specVersion)
        assertEquals("Ретро Бот", parsed.data.name)
        assertEquals("Добро пожаловать в 80-е!", parsed.data.firstMes)
    }
}
