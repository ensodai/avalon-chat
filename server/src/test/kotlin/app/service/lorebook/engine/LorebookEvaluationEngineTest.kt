package app.service.lorebook.engine

import app.service.lorebook.matcher.CompiledAhoCorasick
import app.service.lorebook.model.EntryTimedState
import app.service.lorebook.model.LorebookEntrySpec
import com.ensodai.shared.avalon.character.models.SecondaryFilterLogic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LorebookEvaluationEngineTest {

    private fun buildEngine(
        entries: List<LorebookEntrySpec>,
        globalScanDepth: Int = 2,
        recursiveScanning: Boolean = true,
        maxRecursionSteps: Int = 3,
        builderBlock: CompiledAhoCorasick.Builder.() -> Unit
    ): LorebookEvaluationEngine {
        val builder = CompiledAhoCorasick.Builder()
        builder.builderBlock()
        val automaton = builder.build()
        return LorebookEvaluationEngine(
            entries = entries,
            automaton = automaton,
            globalScanDepth = globalScanDepth,
            recursiveScanning = recursiveScanning,
            maxRecursionSteps = maxRecursionSteps
        )
    }

    private fun msg(speaker: String, text: String): ChatTurnMessage {
        return ChatTurnMessage(
            id = "msg-${text.hashCode()}",
            speakerName = speaker,
            text = text,
            isAssistant = speaker == "Assistant"
        )
    }

    @Test
    fun `test basic keyword match in pass 0`() {
        val entry = LorebookEntrySpec(
            id = 1,
            uid = "tavern",
            content = "Таверна 'Пьяный дракон' — лучшее заведение в городе.",
            primaryPatternIds = intArrayOf(0)
        )

        val engine = buildEngine(listOf(entry)) {
            addPattern("Пьяный дракон")
        }

        val chat = listOf(
            msg("User", "Давай заглянем в таверну 'Пьяный дракон', там шумно.")
        )

        val output = engine.evaluate(chat, emptyMap())
        assertEquals(1, output.activatedEntries.size)
        assertEquals(1, output.activatedEntries[0].id)
        assertEquals(1, output.recursionPassesExecuted)
    }

    @Test
    fun `test sticky and cooldown transitions`() {
        val entry = LorebookEntrySpec(
            id = 10,
            uid = "magic-shield",
            content = "Магический щит отражает урон.",
            primaryPatternIds = intArrayOf(0),
            stickyTurns = 2,
            cooldownTurns = 1
        )

        val engine = buildEngine(listOf(entry)) {
            addPattern("щит")
        }

        // Ход 1: Ключ присутствует -> Активация -> stickyRemaining = 2
        val turn1Chat = listOf(msg("User", "Я активирую щит!"))
        val turn1Output = engine.evaluate(turn1Chat, emptyMap())
        assertEquals(1, turn1Output.activatedEntries.size)
        assertEquals(2, turn1Output.nextTimedStates[10]?.stickyRemaining)
        assertEquals(0, turn1Output.nextTimedStates[10]?.cooldownRemaining)

        // Ход 2: Ключа НЕТ -> Активация по Sticky -> stickyRemaining = 1
        val turn2Chat = listOf(msg("User", "Бегу вперед."))
        val turn2Output = engine.evaluate(turn2Chat, turn1Output.nextTimedStates)
        assertEquals(1, turn2Output.activatedEntries.size, "Должен быть активен по Sticky")
        assertEquals(1, turn2Output.nextTimedStates[10]?.stickyRemaining)
        assertEquals(0, turn2Output.nextTimedStates[10]?.cooldownRemaining)

        // Ход 3: Ключа НЕТ -> Активация по Sticky -> stickyRemaining = 0, переход в cooldown = 1
        val turn3Chat = listOf(msg("User", "Продолжаю бег."))
        val turn3Output = engine.evaluate(turn3Chat, turn2Output.nextTimedStates)
        assertEquals(1, turn3Output.activatedEntries.size, "Последний ход Sticky")
        assertEquals(0, turn3Output.nextTimedStates[10]?.stickyRemaining)
        assertEquals(1, turn3Output.nextTimedStates[10]?.cooldownRemaining, "Должен включиться кулдаун")

        // Ход 4: Ключ СНОВА ЕСТЬ -> Блокировка по кулдауну! -> cooldownRemaining = 0
        val turn4Chat = listOf(msg("User", "Снова щит!"))
        val turn4Output = engine.evaluate(turn4Chat, turn3Output.nextTimedStates)
        assertTrue(turn4Output.activatedEntries.isEmpty(), "Заблокировано кулдауном!")
        assertEquals(0, turn4Output.nextTimedStates[10]?.cooldownRemaining)

        // Ход 5: Кулдаун остыл -> Активация успешна
        val turn5Chat = listOf(msg("User", "И снова щит!"))
        val turn5Output = engine.evaluate(turn5Chat, turn4Output.nextTimedStates)
        assertEquals(1, turn5Output.activatedEntries.size)
    }

    @Test
    fun `test delay gates entry until chat length reached`() {
        val entry = LorebookEntrySpec(
            id = 5,
            uid = "secret",
            content = "Древний секрет открывается только доверенным путникам.",
            primaryPatternIds = intArrayOf(0),
            delayMessages = 3
        )

        val engine = buildEngine(listOf(entry)) {
            addPattern("секрет")
        }

        // 1 сообщение < 3 delay
        val shortChat = listOf(msg("User", "Расскажи мне секрет."))
        val shortOutput = engine.evaluate(shortChat, emptyMap())
        assertTrue(shortOutput.activatedEntries.isEmpty(), "Должен быть заблокирован по Delay")

        // 3 сообщения >= 3 delay
        val longChat = listOf(
            msg("User", "Привет"),
            msg("Assistant", "Здравствуй"),
            msg("User", "Расскажи мне секрет.")
        )
        val longOutput = engine.evaluate(longChat, emptyMap())
        assertEquals(1, longOutput.activatedEntries.size)
    }

    @Test
    fun `test secondary filter AND_ALL and NOT_ANY`() {
        val entryAndAll = LorebookEntrySpec(
            id = 1,
            uid = "king-sword",
            content = "Король с мечом и короной.",
            primaryPatternIds = intArrayOf(0), // король
            secondaryPatternIds = intArrayOf(1, 2), // меч, корона
            selective = true,
            filterLogic = SecondaryFilterLogic.AND_ALL,
            preventRecursion = true
        )

        val entryNotAny = LorebookEntrySpec(
            id = 2,
            uid = "king-no-poison",
            content = "Король без яда.",
            primaryPatternIds = intArrayOf(0), // король
            secondaryPatternIds = intArrayOf(3), // яд
            selective = true,
            filterLogic = SecondaryFilterLogic.NOT_ANY,
            excludeRecursion = true
        )

        val engine = buildEngine(listOf(entryAndAll, entryNotAny)) {
            addPattern("король")  // 0
            addPattern("меч")     // 1
            addPattern("корона")  // 2
            addPattern("яд")      // 3
        }

        // Текст содержит только "король" и "меч" (нет "корона")
        val partialChat = listOf(msg("User", "Шел король, держа в руке меч."))
        val partialOutput = engine.evaluate(partialChat, emptyMap())
        val partialIds = partialOutput.activatedEntries.map { it.id }.toSet()
        assertFalse(partialIds.contains(1), "AND_ALL требует и меч, и корону")
        assertTrue(partialIds.contains(2), "NOT_ANY требует отсутствия яда — яда нет, значит активируется")

        // Текст содержит "король", "меч", "корона" и "яд"
        val poisonChat = listOf(msg("User", "Король уронил меч, и его корона упала в яд!"))
        val poisonOutput = engine.evaluate(poisonChat, emptyMap())
        val poisonIds = poisonOutput.activatedEntries.map { it.id }.toSet()
        assertTrue(poisonIds.contains(1), "AND_ALL совпал (есть и меч, и корона)")
        assertFalse(poisonIds.contains(2), "NOT_ANY провален (яд присутствует в тексте)")
    }

    @Test
    fun `test word proximity window verification`() {
        val entry = LorebookEntrySpec(
            id = 1,
            uid = "arthur-excalibur",
            content = "Артур обнажил Экскалибур.",
            primaryPatternIds = intArrayOf(0),
            secondaryPatternIds = intArrayOf(1),
            selective = true,
            filterLogic = SecondaryFilterLogic.AND_ANY,
            proximityWindowWords = 2 // Слова должны быть на расстоянии не более 2 слов
        )

        val engine = buildEngine(listOf(entry)) {
            addPattern("Артур")
            addPattern("Экскалибур")
        }

        // "Артур" (слово 1) "достал" (слово 2) "Экскалибур" (слово 3) -> дистанция = |1 - 3| = 2 <= 2
        val closeChat = listOf(msg("User", "Артур достал Экскалибур!"))
        val closeOutput = engine.evaluate(closeChat, emptyMap())
        assertEquals(1, closeOutput.activatedEntries.size)

        // "Артур" (1) ... 6 слов ... "Экскалибур" -> дистанция > 2
        val farChat = listOf(msg("User", "Артур шел по темному густому лесу и нашел Экскалибур."))
        val farOutput = engine.evaluate(farChat, emptyMap())
        assertTrue(farOutput.activatedEntries.isEmpty(), "Слова слишком далеко друг от друга")
    }

    @Test
    fun `test recursive cascade between entries`() {
        // Статья 1: триггерится словом "Артур", в контенте содержит "Орден Феникса"
        val entry1 = LorebookEntrySpec(
            id = 1,
            uid = "arthur",
            content = "Артур является верховным паладином гильдии Орден Феникса.",
            primaryPatternIds = intArrayOf(0)
        )

        // Статья 2: триггерится словами "Орден Феникса", в чате этого слова нет!
        val entry2 = LorebookEntrySpec(
            id = 2,
            uid = "order",
            content = "Орден Феникса защищает границы королевства.",
            primaryPatternIds = intArrayOf(1)
        )

        val engine = buildEngine(listOf(entry1, entry2)) {
            addPattern("Артур")
            addPattern("Орден Феникса")
        }

        val chat = listOf(msg("User", "Приветствую тебя, благородный Артур!"))
        val output = engine.evaluate(chat, emptyMap())

        assertEquals(2, output.activatedEntries.size)
        val ids = output.activatedEntries.map { it.id }.toSet()
        assertTrue(ids.contains(1))
        assertTrue(ids.contains(2), "Статья 2 должна активироваться рекурсивно из текста статьи 1")
        assertEquals(2, output.recursionPassesExecuted)
    }

    @Test
    fun `test preventRecursion blocks further cascades`() {
        val entry1 = LorebookEntrySpec(
            id = 1,
            uid = "arthur",
            content = "Артур состоит в Орден Феникса.",
            primaryPatternIds = intArrayOf(0),
            preventRecursion = true // Запрещаем сканировать контент этой статьи
        )

        val entry2 = LorebookEntrySpec(
            id = 2,
            uid = "order",
            content = "Орден Феникса базируется в столице.",
            primaryPatternIds = intArrayOf(1)
        )

        val engine = buildEngine(listOf(entry1, entry2)) {
            addPattern("Артур")
            addPattern("Орден Феникса")
        }

        val chat = listOf(msg("User", "Привет, Артур!"))
        val output = engine.evaluate(chat, emptyMap())

        assertEquals(1, output.activatedEntries.size)
        assertEquals(1, output.activatedEntries[0].id)
    }
}
