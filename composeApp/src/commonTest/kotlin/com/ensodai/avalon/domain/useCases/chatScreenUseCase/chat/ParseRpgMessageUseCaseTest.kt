package com.ensodai.avalon.domain.useCases.chatScreenUseCase.chat

import com.ensodai.avalon.domain.models.chat.RpgMessageBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParseRpgMessageUseCaseTest {

    private val parseUseCase = ParseRpgMessageUseCase()

    @Test
    fun testScenario1() {
        val rawText = "Был обычный июньский денёк. \"Привет, незнакомец!\" — сказал он, *улыбаясь*, и пошёл дальше по улице."
        val blocks = parseUseCase(rawText)

        // Ожидаемые блоки: PLAIN, SPEECH, PLAIN, ACTION, PLAIN
        assertEquals(5, blocks.size)
        assertTrue(blocks[0] is RpgMessageBlock.Plain)
        assertEquals("Был обычный июньский денёк. ", blocks[0].text)

        assertTrue(blocks[1] is RpgMessageBlock.Speech)
        assertEquals("Привет, незнакомец!", blocks[1].text)

        assertTrue(blocks[2] is RpgMessageBlock.Plain)
        assertEquals(" — сказал он, ", blocks[2].text)

        assertTrue(blocks[3] is RpgMessageBlock.Action)
        assertEquals("улыбаясь", blocks[3].text)

        assertTrue(blocks[4] is RpgMessageBlock.Plain)
        assertEquals(", и пошёл дальше по улице.", blocks[4].text)
    }

    @Test
    fun testScenario2() {
        val rawText = "- Ты опоздал, - произнёс мастер сухо. (Опять он начинает...) Я промолчал. «Прости, дорога была забита» — выдавил я наконец."
        val blocks = parseUseCase(rawText)

        // Ожидаемые блоки: SPEECH, PLAIN, THOUGHT, PLAIN, SPEECH, PLAIN
        assertEquals(6, blocks.size)
        assertTrue(blocks[0] is RpgMessageBlock.Speech)
        assertEquals("- Ты опоздал, ", blocks[0].text)

        assertTrue(blocks[1] is RpgMessageBlock.Plain)
        assertEquals("- произнёс мастер сухо. ", blocks[1].text)

        assertTrue(blocks[2] is RpgMessageBlock.Thought)
        assertEquals("Опять он начинает...", blocks[2].text)

        assertTrue(blocks[3] is RpgMessageBlock.Plain)
        assertEquals(" Я промолчал. ", blocks[3].text)

        assertTrue(blocks[4] is RpgMessageBlock.Speech)
        assertEquals("Прости, дорога была забита", blocks[4].text)

        assertTrue(blocks[5] is RpgMessageBlock.Plain)
        assertEquals(" — выдавил я наконец.", blocks[5].text)
    }

    @Test
    fun testScenario3() {
        val rawText = "Бой подходил к концу. **Уровень повышен!** Противник пал. *достаёт меч из ножен* Теперь твоя очередь. \"Сдавайся,\" — произнёс он холодно."
        val blocks = parseUseCase(rawText)

        // Ожидаемые блоки: PLAIN, EVENT, PLAIN, ACTION, PLAIN, SPEECH, PLAIN
        assertEquals(7, blocks.size)
        assertTrue(blocks[0] is RpgMessageBlock.Plain)
        assertEquals("Бой подходил к концу. ", blocks[0].text)

        assertTrue(blocks[1] is RpgMessageBlock.Event)
        assertEquals("Уровень повышен!", blocks[1].text)

        assertTrue(blocks[2] is RpgMessageBlock.Plain)
        assertEquals(" Противник пал. ", blocks[2].text)

        assertTrue(blocks[3] is RpgMessageBlock.Action)
        assertEquals("достаёт меч из ножен", blocks[3].text)

        assertTrue(blocks[4] is RpgMessageBlock.Plain)
        assertEquals(" Теперь твоя очередь. ", blocks[4].text)

        assertTrue(blocks[5] is RpgMessageBlock.Speech)
        assertEquals("Сдавайся,", blocks[5].text)

        assertTrue(blocks[6] is RpgMessageBlock.Plain)
        assertEquals(" — произнёс он холодно.", blocks[6].text)
    }

    @Test
    fun testScenario4() {
        val rawText = "Она замерла. *медленно поворачивается, **глаза сверкают*** Что происходит? (Это не может быть правдой...) \"Кто ты такой?!\" — закричала она. **Критический урон!**"
        val blocks = parseUseCase(rawText)

        // Ожидаемые блоки: PLAIN, ACTION, PLAIN, THOUGHT, SPEECH, PLAIN, EVENT
        assertEquals(7, blocks.size)
        assertTrue(blocks[0] is RpgMessageBlock.Plain)
        assertEquals("Она замерла. ", blocks[0].text)

        assertTrue(blocks[1] is RpgMessageBlock.Action)
        assertEquals("медленно поворачивается, **глаза сверкают**", blocks[1].text)

        assertTrue(blocks[2] is RpgMessageBlock.Plain)
        assertEquals(" Что происходит? ", blocks[2].text)

        assertTrue(blocks[3] is RpgMessageBlock.Thought)
        assertEquals("Это не может быть правдой...", blocks[3].text)

        assertTrue(blocks[4] is RpgMessageBlock.Speech)
        assertEquals("Кто ты такой?!", blocks[4].text)

        assertTrue(blocks[5] is RpgMessageBlock.Plain)
        assertEquals(" — закричала она. ", blocks[5].text)

        assertTrue(blocks[6] is RpgMessageBlock.Event)
        assertEquals("Критический урон!", blocks[6].text)
    }

    @Test
    fun testScenario5() {
        val rawText = "\"Первая фраза.\" \"Вторая сразу следом!\" Воцарилась тишина. *встаёт* *отходит к окну* Ветер шумел за стеклом. (Зачем я это сказал?) (Теперь поздно жалеть.) Ночь обещала быть долгой."
        val blocks = parseUseCase(rawText)

        // Ожидаемые блоки: SPEECH, SPEECH, PLAIN, ACTION, ACTION, PLAIN, THOUGHT, THOUGHT, PLAIN
        assertEquals(9, blocks.size)
        assertTrue(blocks[0] is RpgMessageBlock.Speech)
        assertEquals("Первая фраза.", blocks[0].text)

        assertTrue(blocks[1] is RpgMessageBlock.Speech)
        assertEquals("Вторая сразу следом!", blocks[1].text)

        assertTrue(blocks[2] is RpgMessageBlock.Plain)
        assertEquals(" Воцарилась тишина. ", blocks[2].text)

        assertTrue(blocks[3] is RpgMessageBlock.Action)
        assertEquals("встаёт", blocks[3].text)

        assertTrue(blocks[4] is RpgMessageBlock.Action)
        assertEquals("отходит к окну", blocks[4].text)

        assertTrue(blocks[5] is RpgMessageBlock.Plain)
        assertEquals(" Ветер шумел за стеклом. ", blocks[5].text)

        assertTrue(blocks[6] is RpgMessageBlock.Thought)
        assertEquals("Зачем я это сказал?", blocks[6].text)

        assertTrue(blocks[7] is RpgMessageBlock.Thought)
        assertEquals("Теперь поздно жалеть.", blocks[7].text)

        assertTrue(blocks[8] is RpgMessageBlock.Plain)
        assertEquals(" Ночь обещала быть долгой.", blocks[8].text)
    }

    @Test
    fun testScenario6() {
        val rawText = """## Глава 3: Подземелье

Факелы потрескивали на стенах. **Новый предмет получен: Меч Рассвета!**

- Урон: 15-20
- Вес: 3.5 кг

"Наконец-то," — выдохнул герой, разглядывая клинок. *поднимает меч и проверяет баланс* Острое лезвие блеснуло в свете факелов."""

        val blocks = parseUseCase(rawText)

        // Ожидаемые блоки: PLAIN, EVENT, PLAIN, SPEECH, PLAIN, ACTION, PLAIN
        // Списки с двоеточием "- Урон: 15-20" должны остаться в PLAIN, а не распарситься как SPEECH
        assertEquals(7, blocks.size)
        assertTrue(blocks[0] is RpgMessageBlock.Plain)
        assertTrue(blocks[0].text.contains("## Глава 3: Подземелье"))

        assertTrue(blocks[1] is RpgMessageBlock.Event)
        assertEquals("Новый предмет получен: Меч Рассвета!", blocks[1].text)

        assertTrue(blocks[2] is RpgMessageBlock.Plain)
        assertTrue(blocks[2].text.contains("- Урон: 15-20"))
        assertTrue(blocks[2].text.contains("- Вес: 3.5 кг"))

        assertTrue(blocks[3] is RpgMessageBlock.Speech)
        assertEquals("Наконец-то,", blocks[3].text)

        assertTrue(blocks[4] is RpgMessageBlock.Plain)
        assertEquals(" — выдохнул герой, разглядывая клинок. ", blocks[4].text)

        assertTrue(blocks[5] is RpgMessageBlock.Action)
        assertEquals("поднимает меч и проверяет баланс", blocks[5].text)

        assertTrue(blocks[6] is RpgMessageBlock.Plain)
        assertEquals(" Острое лезвие блеснуло в свете факелов.", blocks[6].text)
    }

    @Test
    fun testScenario7() {
        val rawText = "Тишина повисла в воздухе. *медленно достаёт **сияющий** артефакт из мешка, его глаза расширяются* Никто не шевелился. (Это же... **Камень Душ**? Не может быть, его уничтожили сто лет назад. Или... *кто-то солгал*?)"
        val blocks = parseUseCase(rawText)

        // Ожидаемые блоки: PLAIN, ACTION, PLAIN, THOUGHT
        assertEquals(4, blocks.size)
        assertTrue(blocks[0] is RpgMessageBlock.Plain)
        assertEquals("Тишина повисла в воздухе. ", blocks[0].text)

        assertTrue(blocks[1] is RpgMessageBlock.Action)
        assertEquals("медленно достаёт **сияющий** артефакт из мешка, его глаза расширяются", blocks[1].text)

        assertTrue(blocks[2] is RpgMessageBlock.Plain)
        assertEquals(" Никто не шевелился. ", blocks[2].text)

        assertTrue(blocks[3] is RpgMessageBlock.Thought)
        assertEquals("Это же... **Камень Душ**? Не может быть, его уничтожили сто лет назад. Или... *кто-то солгал*?", blocks[3].text)
    }

    @Test
    fun testScenario8() {
        // В pareser_example.txt текст Теста 8 дублирует Тест 7.
        // Мы запускаем тот же сценарий, чтобы проверить детерминированность и стабильность парсера.
        val rawText = "Тишина повисла в воздухе. *медленно достаёт **сияющий** артефакт из мешка, его глаза расширяются* Никто не шевелился. (Это же... **Камень Душ**? Не может быть, его уничтожили сто лет назад. Или... *кто-то солгал*?)"
        val blocks = parseUseCase(rawText)

        assertEquals(4, blocks.size)
        assertTrue(blocks[0] is RpgMessageBlock.Plain)
        assertTrue(blocks[1] is RpgMessageBlock.Action)
        assertTrue(blocks[2] is RpgMessageBlock.Plain)
        assertTrue(blocks[3] is RpgMessageBlock.Thought)
    }

    @Test
    fun testScenario9() {
        val rawText = """Дождь барабанил по крыше...

   "Сколько нам ещё идти?!"   -- спросила она устало, отряхивая плащ.


Внезапно прямо перед ними возникла надпись:
**ВНИМАНИЕ!!!** *резко выхватывает оружие*
- Назад! - крикнул проводник. - Здесь ловушка!!!"""

        val blocks = parseUseCase(rawText)

        // Проверяем корректность парсинга
        assertTrue(blocks[0] is RpgMessageBlock.Plain)
        assertTrue(blocks[0].text.contains("Дождь барабанил по крыше..."))

        // Вторая прямая речь "Сколько нам..."
        val speechBlock1 = blocks.first { it.text.contains("Сколько нам") }
        assertTrue(speechBlock1 is RpgMessageBlock.Speech)
        assertEquals("Сколько нам ещё идти?!", speechBlock1.text)

        // EVENT "ВНИМАНИЕ!!!"
        val eventBlock = blocks.first { it.text.contains("ВНИМАНИЕ!!!") }
        assertTrue(eventBlock is RpgMessageBlock.Event)
        assertEquals("ВНИМАНИЕ!!!", eventBlock.text)

        // ACTION "резко выхватывает..."
        val actionBlock = blocks.first { it.text.contains("резко выхватывает") }
        assertTrue(actionBlock is RpgMessageBlock.Action)
        assertEquals("резко выхватывает оружие", actionBlock.text)

        // Проверяем разделение речи и слов автора в конце: "- Назад! - крикнул... - Здесь ловушка!!!"
        val lastBlocks = blocks.takeLast(3)
        assertTrue(lastBlocks[0] is RpgMessageBlock.Speech)
        assertEquals("- Назад! ", lastBlocks[0].text)

        assertTrue(lastBlocks[1] is RpgMessageBlock.Plain)
        assertEquals("- крикнул проводник. ", lastBlocks[1].text)

        assertTrue(lastBlocks[2] is RpgMessageBlock.Speech)
        assertEquals("- Здесь ловушка!!!", lastBlocks[2].text)
    }

    @Test
    fun testScenario10() {
        val rawText = "Шанс критического удара составляет 15%*. \"Ты *уверен*, что справишься с этим?\" — спросил наставник, скрестив руки. **Здоровье: 100/100 -> 45/100** Урон оказался выше ожидаемого. *шатается, опираясь на стену* (45 хп... это критично. Нужно зелье *прямо сейчас*.) Время будто остановилось."
        val blocks = parseUseCase(rawText)

        // Ожидаемые блоки: PLAIN, SPEECH, PLAIN, EVENT, PLAIN, ACTION, THOUGHT, PLAIN
        // 15%* не должно триггерить ACTION
        assertEquals(8, blocks.size)
        assertTrue(blocks[0] is RpgMessageBlock.Plain)
        assertEquals("Шанс критического удара составляет 15%*. ", blocks[0].text)

        assertTrue(blocks[1] is RpgMessageBlock.Speech)
        assertEquals("Ты *уверен*, что справишься с этим?", blocks[1].text)

        assertTrue(blocks[2] is RpgMessageBlock.Plain)
        assertEquals(" — спросил наставник, скрестив руки. ", blocks[2].text)

        assertTrue(blocks[3] is RpgMessageBlock.Event)
        assertEquals("Здоровье: 100/100 -> 45/100", blocks[3].text)

        assertTrue(blocks[4] is RpgMessageBlock.Plain)
        assertEquals(" Урон оказался выше ожидаемого. ", blocks[4].text)

        assertTrue(blocks[5] is RpgMessageBlock.Action)
        assertEquals("шатается, опираясь на стену", blocks[5].text)

        assertTrue(blocks[6] is RpgMessageBlock.Thought)
        assertEquals("45 хп... это критично. Нужно зелье *прямо сейчас*.", blocks[6].text)

        assertTrue(blocks[7] is RpgMessageBlock.Plain)
        assertEquals(" Время будто остановилось.", blocks[7].text)
    }

    @Test
    fun testScenario11() {
        val rawText = """### Финальная битва

Земля содрогнулась от удара. *вскидывает руки, формируя щит из чистой энергии* "Ты не пройдёшь дальше — *никогда*!" — голос героя дрожал, но не от страха.

**КРИТИЧЕСКИЙ МОМЕНТ: -50% к защите противника**

(Если щит не выдержит... всё было напрасно. *Все жертвы...*)

*щит трескается с громким звуком**, осколки света рассыпаются по полу*

Наступила тишина. -Это... конец? - прошептал кто-то в темноте."""

        val blocks = parseUseCase(rawText)

        // Ожидаемые блоки: PLAIN, ACTION, SPEECH, PLAIN, EVENT, THOUGHT, ACTION, PLAIN, SPEECH, PLAIN
        assertEquals(10, blocks.size)
        assertTrue(blocks[0] is RpgMessageBlock.Plain)
        assertTrue(blocks[0].text.contains("### Финальная битва"))

        assertTrue(blocks[1] is RpgMessageBlock.Action)
        assertEquals("вскидывает руки, формируя щит из чистой энергии", blocks[1].text)

        assertTrue(blocks[2] is RpgMessageBlock.Speech)
        assertEquals("Ты не пройдёшь дальше — *никогда*!", blocks[2].text)

        assertTrue(blocks[3] is RpgMessageBlock.Plain)
        assertEquals(" — голос героя дрожал, но не от страха.\n\n", blocks[3].text)

        assertTrue(blocks[4] is RpgMessageBlock.Event)
        assertEquals("КРИТИЧЕСКИЙ МОМЕНТ: -50% к защите противника", blocks[4].text)

        assertTrue(blocks[5] is RpgMessageBlock.Thought)
        assertEquals("Если щит не выдержит... всё было напрасно. *Все жертвы...*", blocks[5].text)

        assertTrue(blocks[6] is RpgMessageBlock.Action)
        assertEquals("щит трескается с громким звуком**, осколки света рассыпаются по полу", blocks[6].text)

        assertTrue(blocks[7] is RpgMessageBlock.Plain)
        assertEquals("\n\nНаступила тишина. ", blocks[7].text)

        assertTrue(blocks[8] is RpgMessageBlock.Speech)
        assertEquals("-Это... конец? ", blocks[8].text)

        assertTrue(blocks[9] is RpgMessageBlock.Plain)
        assertEquals("- прошептал кто-то в темноте.", blocks[9].text)
    }

    @Test
    fun testEdgeCases() {
        // 1. Все четыре маркера подряд без пробелов: *action* "speech" (thought) **event**
        val rawText1 = "*action*\"speech\"(thought)**event**"
        val blocks1 = parseUseCase(rawText1)
        assertEquals(4, blocks1.size)
        assertTrue(blocks1[0] is RpgMessageBlock.Action)
        assertEquals("action", blocks1[0].text)
        assertTrue(blocks1[1] is RpgMessageBlock.Speech)
        assertEquals("speech", blocks1[1].text)
        assertTrue(blocks1[2] is RpgMessageBlock.Thought)
        assertEquals("thought", blocks1[2].text)
        assertTrue(blocks1[3] is RpgMessageBlock.Event)
        assertEquals("event", blocks1[3].text)

        // 2. Жир с курсивом внутри (nested markdown) - не должен разбиваться
        val rawText2 = "**Жир с *курсивом* внутри**"
        val blocks2 = parseUseCase(rawText2)
        assertEquals(1, blocks2.size)
        assertTrue(blocks2[0] is RpgMessageBlock.Event)
        assertEquals("Жир с *курсивом* внутри", blocks2[0].text)

        // 3. Реплика с выделенным словом и -- тире-паузой внутри
        val rawText3 = "\"Реплика с *выделенным* словом и -- тире-паузой внутри\""
        val blocks3 = parseUseCase(rawText3)
        assertEquals(1, blocks3.size)
        assertTrue(blocks3[0] is RpgMessageBlock.Speech)
        assertEquals("Реплика с *выделенным* словом и -- тире-паузой внутри", blocks3[0].text)

        // 4. Пустые скобки, пустые маркеры
        val rawText4 = "( ) ** ** \"\" * *"
        val blocks4 = parseUseCase(rawText4)
        // Все они должны отфильтроваться как пустые ( trim().isNotEmpty() )
        assertEquals(true, blocks4.isEmpty())

        // 5. Слово с дефисами не должно триггерить SPEECH
        val rawText5 = "Слово-с-дефисами-внутри"
        val blocks5 = parseUseCase(rawText5)
        assertEquals(1, blocks5.size)
        assertTrue(blocks5[0] is RpgMessageBlock.Plain)
        assertEquals("Слово-с-дефисами-внутри", blocks5[0].text)

        // 6. Inline код не должен парсить кавычки внутри как SPEECH
        val rawText6 = "`print(\"Привет, мир!\")`"
        val blocks6 = parseUseCase(rawText6)
        assertEquals(1, blocks6.size)
        assertTrue(blocks6[0] is RpgMessageBlock.Plain)
        assertEquals("`print(\"Привет, мир!\")`", blocks6[0].text)

        // 7. Многострочный блок кода не должен парсить ролевые маркеры внутри
        val rawText7 = """
            ```kotlin
            fun main() {
                // *действие* "речь"
                println("Hello")
            }
            ```
        """.trimIndent()
        val blocks7 = parseUseCase(rawText7)
        assertEquals(1, blocks7.size)
        assertTrue(blocks7[0] is RpgMessageBlock.Plain)
        assertTrue(blocks7[0].text.contains("*действие*"))
        assertTrue(blocks7[0].text.contains("\"речь\""))

        // 8. Мысли в скобках, начинающиеся с кавычек
        val rawText8 = "(\"нет все не так\" громко про себя подумал)"
        val blocks8 = parseUseCase(rawText8)
        assertEquals(1, blocks8.size)
        assertTrue(blocks8[0] is RpgMessageBlock.Thought)
        assertEquals("\"нет все не так\" громко про себя подумал", blocks8[0].text)
    }
}
