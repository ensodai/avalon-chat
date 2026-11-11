package app.service

import app.service.chat.LorebookMatcher
import com.ensodai.shared.avalon.character.models.CharacterLorebookDetailsDto
import com.ensodai.shared.avalon.character.models.CharacterLorebookEntryItemDto
import com.ensodai.shared.avalon.character.models.LorebookEntryPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class LorebookMatcherTest {

    private val matcher = LorebookMatcher()

    @Test
    fun testLorebookTriggerByKeyword() {
        val entryExcalibur = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("Экскалибур", "меч Артура"),
            content = "Экскалибур — легендарный священный клинок.",
            comment = "Оружие",
            enabled = true,
            constant = false,
            position = LorebookEntryPosition.BEFORE_CHAR
        )

        val entryCastle = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("Камелот", "замок"),
            content = "Камелот — столица королевства Логрес.",
            comment = "Локация",
            enabled = true,
            constant = false,
            position = LorebookEntryPosition.AFTER_CHAR
        )

        val lorebook = CharacterLorebookDetailsDto(
            id = Uuid.random(),
            characterId = Uuid.random(),
            name = "Лор Короля Артура",
            scanDepth = 3,
            entries = listOf(entryExcalibur, entryCastle)
        )

        // Сообщения, в которых упоминается только меч
        val messages = listOf(
            "Привет, как твои дела?",
            "Я нашел в руинах странный клинок...",
            "Это не Экскалибур случайно?"
        )

        val result = matcher.match(lorebook, messages)

        assertEquals(1, result.beforeChar.size)
        assertTrue(result.beforeChar.first().contains("Экскалибур"))
        assertTrue(result.afterChar.isEmpty())
    }

    @Test
    fun testLorebookConstantEntryAlwaysActive() {
        val constantEntry = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("магия"),
            content = "В этом мире магия запрещена инквизицией.",
            enabled = true,
            constant = true,
            position = LorebookEntryPosition.BEFORE_CHAR
        )

        val lorebook = CharacterLorebookDetailsDto(
            id = Uuid.random(),
            characterId = Uuid.random(),
            name = "Законы мира",
            entries = listOf(constantEntry)
        )

        // Никаких ключевых слов в тексте нет
        val messages = listOf("Привет", "Какая хорошая погода")

        val result = matcher.match(lorebook, messages)

        assertEquals(1, result.beforeChar.size)
        assertTrue(result.beforeChar.first().contains("магия запрещена"))
    }

    @Test
    fun testLorebookSelectiveMode() {
        val selectiveEntry = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("дракон"),
            secondaryKeys = listOf("пещера", "логово"),
            content = "Красный дракон спит в восточной пещере.",
            enabled = true,
            selective = true,
            position = LorebookEntryPosition.AFTER_CHAR
        )

        val lorebook = CharacterLorebookDetailsDto(
            id = Uuid.random(),
            characterId = Uuid.random(),
            name = "Монстры",
            entries = listOf(selectiveEntry)
        )

        // Есть только 'дракон', но нет secondaryKeys
        val res1 = matcher.match(lorebook, listOf("Я видел как летел дракон над холмом"))
        assertTrue(res1.afterChar.isEmpty())

        // Есть и 'дракон', и 'логово'
        val res2 = matcher.match(lorebook, listOf("Говорят, дракон вернулся в свое логово!"))
        assertEquals(1, res2.afterChar.size)
        assertTrue(res2.afterChar.first().contains("Красный дракон"))
    }

    @Test
    fun testPreventsFalsePositiveSubstringMatch() {
        val catEntry = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("кот"),
            content = "Кот — домашнее животное.",
            enabled = true,
            position = LorebookEntryPosition.BEFORE_CHAR
        )

        val lorebook = CharacterLorebookDetailsDto(
            id = Uuid.random(),
            characterId = Uuid.random(),
            name = "Животные",
            entries = listOf(catEntry)
        )

        // Слово 'бойкот' содержит подстроку 'кот', но целое слово не совпадает
        val res1 = matcher.match(lorebook, listOf("Мы объявляем бойкот этой таверне!"))
        assertTrue(res1.beforeChar.isEmpty(), "Подстрока в слове 'бойкот' не должна триггерить статью 'кот'")

        // Настоящее слово 'кот'
        val res2 = matcher.match(lorebook, listOf("У камина спал рыжий кот."))
        assertEquals(1, res2.beforeChar.size)
        assertTrue(res2.beforeChar.first().contains("домашнее животное"))
    }
}
