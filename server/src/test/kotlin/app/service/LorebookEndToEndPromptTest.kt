package app.service

import app.data.db.tables.LlmConfig
import app.data.db.tables.LlmProvider
import app.data.models.AICharacterConfigLLMDto
import app.data.models.GlobalSettingsLLMDto
import app.data.models.SafetySettingsDto
import app.data.models.UserPersonaLLMDto
import app.domain.models.DomainMessage
import app.domain.models.DomainMessageContent
import app.service.chat.LorebookMatcher
import app.service.openai.OpenAiRequestBuilder
import com.ensodai.shared.avalon.character.models.CharacterLorebookDetailsDto
import com.ensodai.shared.avalon.character.models.CharacterLorebookEntryItemDto
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.character.models.LorebookEntryPosition
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class LorebookEndToEndPromptTest {

    private val matcher = LorebookMatcher()
    private val geminiRequestFactory = GeminiRequestFactory()
    private val openAiRequestBuilder = OpenAiRequestBuilder(geminiRequestFactory)

    @Test
    fun testLorebookActivationAndSystemPromptGeneration() {
        // 1. Создаем тестовый лорбук с 3 статьями
        // Статья 1: BEFORE_CHAR (Локация: Авалон)
        val entryAvalon = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("Авалон", "Авалоне", "парящий город"),
            content = "Авалон — древняя парящая цитадель магов, скрытая в вечных облаках.",
            comment = "Локация: Парящий город Авалон",
            enabled = true,
            constant = false,
            position = LorebookEntryPosition.BEFORE_CHAR
        )

        // Статья 2: AFTER_CHAR с вторичным фильтром (Артефакт: Кристалл Эфира)
        // Сработает, только если в контексте есть ("кристалл" ИЛИ "эфир") И ("меч" ИЛИ "клинок")
        val entryCrystal = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("кристалл", "эфир"),
            secondaryKeys = listOf("меч", "клинок"),
            selective = true,
            content = "Кристалл Эфира вмонтирован в эфес меча и хранит тайны Ордена Хранителей.",
            comment = "Артефакт: Кристалл Эфира",
            enabled = true,
            position = LorebookEntryPosition.AFTER_CHAR
        )

        // Статья 3: AFTER_CHAR (Фракция: Орден Хранителей)
        // В сообщении юзера нет слова "орден", но оно есть в тексте Статьи 2!
        // Статья должна активироваться по каскадной рекурсии (Pass 1).
        val entryOrder = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("Орден Хранителей", "Ордена Хранителей", "орден", "ордена"),
            content = "Орден Хранителей — элитные стражи, давшие клятву защищать кристаллы неба.",
            comment = "Фракция: Орден Хранителей",
            enabled = true,
            position = LorebookEntryPosition.AFTER_CHAR
        )

        val lorebook = CharacterLorebookDetailsDto(
            id = Uuid.random(),
            characterId = Uuid.random(),
            name = "Лор Небесного Архипелага",
            scanDepth = 4,
            tokenBudget = 2048,
            recursiveScanning = true,
            entries = listOf(entryAvalon, entryCrystal, entryOrder)
        )

        // 2. Имитируем историю чата и новое сообщение пользователя
        val chatHistory = listOf(
            DomainMessage(
                id = Uuid.random(),
                role = MessageRole.USER,
                contents = listOf(
                    DomainMessageContent(AvalonMessageContentType.TEXT, "Здравствуй, Селеста!")
                )
            ),
            DomainMessage(
                id = Uuid.random(),
                role = MessageRole.MODEL,
                contents = listOf(
                    DomainMessageContent(AvalonMessageContentType.TEXT, "Приветствую, путник. Что привело тебя в небеса?")
                )
            ),
            // Новое входящее сообщение от пользователя
            DomainMessage(
                id = Uuid.random(),
                role = MessageRole.USER,
                contents = listOf(
                    DomainMessageContent(
                        AvalonMessageContentType.TEXT,
                        "Я ищу древний клинок в Авалоне. Говорят, ты носишь священный кристалл?"
                    )
                )
            )
        )

        val recentTexts = chatHistory.flatMap { it.contents }.map { it.content }

        // 3. Выполняем сопоставление через LorebookMatcher (наш фасад ядра движка)
        val matchedResult = matcher.matchMultiple(listOf(lorebook), recentTexts)

        // 4. Собираем DTO конфигурации персонажа
        val characterConfig = AICharacterConfigLLMDto(
            characterId = Uuid.random(),
            userId = Uuid.random(),
            characterName = "Селеста",
            llmConfig = LlmConfig(
                code = "gemini_flash",
                name = "Gemini Flash",
                modelAPIID = "google/gemini-2.5-flash",
                provider = LlmProvider.GOOGLE,
                energyCost = 1,
                minStatus = PremiumStatus.FREE,
                isActive = true
            ),
            temperature = 0.7f,
            interactionFormat = InteractionFormat.LITERARY_RPG,
            topK = 40,
            topP = 0.95f,
            maxOutputTokens = 1000,
            maxContextTokens = 8192,
            shortDescription = "Верховная чародейка Авалона",
            fullDescription = "Спокойная, рассудительная и непреклонная хранительница парящих архивов.",
            firstMessage = "Приветствую тебя в Авалоне.",
            safetySettings = SafetySettingsDto(0, 0, 0, 0),
            globalSettings = GlobalSettingsLLMDto(mainRules = "Веди диалог от лица персонажа.", maxOutputTokens = 1000),
            userPersona = UserPersonaLLMDto(personaName = "Искатель", personaDescription = "Странник между мирами"),
            scenario = "Встреча в Небесном Архиве Авалона",
            lorebookBeforeChar = matchedResult.beforeChar,
            lorebookAfterChar = matchedResult.afterChar
        )

        // 5. Генерируем системный промпт (GeminiRequestFactory)
        val systemPrompt = geminiRequestFactory.buildNormalSystemInstructionText(
            dto = characterConfig,
            rarity = Rarity.COMMON,
            messageCount = 3L,
            relationshipLevel = 1
        )

        // 6. Генерируем запрос OpenRouter (OpenAiRequestBuilder)
        val openAiRequest = openAiRequestBuilder.buildRequest(
            dto = characterConfig,
            history = chatHistory,
            modelOverride = null,
            messageRarity = Rarity.COMMON,
            totalMessageCount = 3L,
            relationshipLevel = 1,
            eventScenario = null
        )

        // Выводим в лог для наглядности
        println("=================================================================")
        println("=== ИТОГОВЫЙ СИСТЕМНЫЙ ПРОМПТ (Gemini & OpenRouter System): ===")
        println("=================================================================")
        println(systemPrompt)
        println("=================================================================")
        println("=== СООБЩЕНИЯ OPENROUTER REQUEST (${openAiRequest.messages.size} шт): ===")
        openAiRequest.messages.forEachIndexed { i, msg ->
            val contentStr = msg.content ?: ""
            val preview = if (contentStr.length > 120) contentStr.take(120) + "..." else contentStr
            println("[$i] Role: ${msg.role} | Content: $preview")
        }
        println("=================================================================")

        // 7. Проверяем утверждения (Assertions)
        // Авалон должен быть в BEFORE_CHAR
        assertEquals(1, matchedResult.beforeChar.size)
        assertTrue(matchedResult.beforeChar.first().contains("Авалон — древняя парящая цитадель"))

        // Кристалл Эфира и Орден Хранителей должны быть в AFTER_CHAR (Кристалл через фильтр, Орден по рекурсии)
        assertEquals(2, matchedResult.afterChar.size)
        assertTrue(matchedResult.afterChar.any { it.contains("Кристалл Эфира вмонтирован") })
        assertTrue(matchedResult.afterChar.any { it.contains("Орден Хранителей — элитные стражи") })

        // Проверяем наличие всех тегов и правильный порядок в итоговом системном промпте
        assertTrue(systemPrompt.contains("<world_lore>"))
        assertTrue(systemPrompt.contains("Авалон — древняя парящая цитадель"))
        assertTrue(systemPrompt.contains("<character_profile>"))
        assertTrue(systemPrompt.contains("<name>Селеста</name>"))
        assertTrue(systemPrompt.contains("Кристалл Эфира вмонтирован в эфес"))
        assertTrue(systemPrompt.contains("Орден Хранителей — элитные стражи"))

        // В запросе OpenRouter первое сообщение должно быть system с этим же текстом
        assertEquals("system", openAiRequest.messages.first().role)
        assertEquals(systemPrompt, openAiRequest.messages.first().content)

        // В запросе OpenRouter последнее сообщение должно быть от user с текстом входящего сообщения
        val lastUserMessage = openAiRequest.messages.last()
        assertEquals("user", lastUserMessage.role)
        assertTrue((lastUserMessage.content ?: "").contains("Я ищу древний клинок в Авалоне"))
    }

    @Test
    fun testMultiTurnTimedStatesStickyCooldownAndDelay() {
        // Создаем статью со stickyTurns = 2 и cooldownTurns = 1
        val grailEntryId = Uuid.random()
        val entryGrail = CharacterLorebookEntryItemDto(
            id = grailEntryId,
            keys = listOf("Грааль", "Чаша"),
            content = "Священный Грааль исцеляет любые раны.",
            enabled = true,
            stickyTurns = 2,
            cooldownTurns = 1,
            position = LorebookEntryPosition.AFTER_CHAR
        )

        // Создаем статью с delayMessages = 3
        val dragonEntryId = Uuid.random()
        val entryDragon = CharacterLorebookEntryItemDto(
            id = dragonEntryId,
            keys = listOf("дракон", "дракона"),
            content = "Древний дракон спит в недрах вулкана.",
            enabled = true,
            delayMessages = 3,
            position = LorebookEntryPosition.AFTER_CHAR
        )

        val lorebook = CharacterLorebookDetailsDto(
            id = Uuid.random(),
            characterId = Uuid.random(),
            name = "Лор приключений",
            scanDepth = 2,
            entries = listOf(entryGrail, entryDragon)
        )

        // ХОД 1: 1 сообщение в истории. Юзер упоминает "Грааль" и "дракона".
        // Дракон заблокирован delay (chatLength = 1 < 3). Грааль активируется и получает stickyRemaining = 2.
        val turn1Texts = listOf("Я слышал легенду про дракона и Грааль.")
        val res1 = matcher.matchMultiple(
            lorebooks = listOf(lorebook),
            recentMessagesText = turn1Texts,
            previousTimedStates = emptyMap()
        )

        assertTrue(res1.afterChar.any { it.contains("Священный Грааль") }, "Грааль должен сработать по ключу")
        assertTrue(res1.afterChar.none { it.contains("Древний дракон") }, "Дракон должен быть отложен по delay")
        val state1 = res1.nextTimedStates[grailEntryId]
        assertEquals(2, state1?.stickyRemaining, "Sticky должен быть равен 2")
        assertEquals(0, state1?.cooldownRemaining, "Cooldown еще не начался")

        // ХОД 2: 2 сообщения в истории. Юзер НЕ упоминает Грааль ("просто идем дальше").
        // Грааль должен активироваться принудительно по sticky!
        val turn2Texts = listOf(
            "Я слышал легенду про дракона и Грааль.",
            "Мы просто идем дальше через густой темный лес."
        )
        val res2 = matcher.matchMultiple(
            lorebooks = listOf(lorebook),
            recentMessagesText = turn2Texts,
            previousTimedStates = res1.nextTimedStates
        )

        assertTrue(res2.afterChar.any { it.contains("Священный Грааль") }, "Грааль должен сработать по Sticky")
        val state2 = res2.nextTimedStates[grailEntryId]
        assertEquals(1, state2?.stickyRemaining, "Sticky уменьшился с 2 до 1")
        assertEquals(0, state2?.cooldownRemaining)

        // ХОД 3: 3 сообщения в истории. Юзер снова ничего не упоминает про Грааль.
        // Грааль срабатывает последний ход по sticky (1 -> 0), и после этого запускается кулдаун (cooldown = 1)!
        // Теперь chatLength = 3 >= delayMessages, но слово дракон не упомянуто.
        val turn3Texts = listOf(
            "Мы просто идем дальше через густой темный лес.",
            "Здесь тихо и спокойно."
        )
        val res3 = matcher.matchMultiple(
            lorebooks = listOf(lorebook),
            recentMessagesText = turn3Texts,
            previousTimedStates = res2.nextTimedStates
        )

        assertTrue(res3.afterChar.any { it.contains("Священный Грааль") }, "Грааль сработал на последнем тике Sticky")
        val state3 = res3.nextTimedStates[grailEntryId]
        assertEquals(0, state3?.stickyRemaining, "Sticky исчерпан")
        assertEquals(1, state3?.cooldownRemaining, "Кулдаун активировался на 1 ход")

        // ХОД 4: Юзер явно упоминает "Грааль" и "дракона"!
        // Грааль заблокирован активным кулдауном (cooldownRemaining = 1).
        // Дракон теперь проходит delay (chatLength >= 3) и упоминается в тексте!
        val turn4Texts = listOf(
            "Мы просто идем дальше через густой темный лес.",
            "Здесь тихо и спокойно.",
            "Смотри, там спит дракон! А где же наш Грааль?"
        )
        val res4 = matcher.matchMultiple(
            lorebooks = listOf(lorebook),
            recentMessagesText = turn4Texts,
            previousTimedStates = res3.nextTimedStates
        )

        assertTrue(res4.afterChar.none { it.contains("Священный Грааль") }, "Грааль заблокирован кулдауном!")
        assertTrue(res4.afterChar.any { it.contains("Древний дракон") }, "Дракон сработал после снятия delay")
        val state4 = res4.nextTimedStates[grailEntryId]
        assertEquals(0, state4?.stickyRemaining)
        assertEquals(0, state4?.cooldownRemaining, "Кулдаун оттикал с 1 до 0")

        // ХОД 5: Юзер снова спрашивает про Грааль. Кулдаун завершен, Грааль снова активируется!
        val turn5Texts = listOf(
            "Смотри, там спит дракон! А где же наш Грааль?",
            "Теперь расскажи мне про Грааль!"
        )
        val res5 = matcher.matchMultiple(
            lorebooks = listOf(lorebook),
            recentMessagesText = turn5Texts,
            previousTimedStates = res4.nextTimedStates
        )

        assertTrue(res5.afterChar.any { it.contains("Священный Грааль") }, "Грааль снова активен после кулдауна!")
        val state5 = res5.nextTimedStates[grailEntryId]
        assertEquals(2, state5?.stickyRemaining, "Sticky перезапустился на 2")
    }

    @Test
    fun testAllSevenInjectionPositions() {
        val historyBuilder = HistoryBuilder()

        // 1. Создаем статьи для всех 7 позиций инъекции стандарта CCv3
        val entryBeforeChar = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("небо", "небе"),
            content = "Лор 1: Небо над миром расколото на три сферы.",
            comment = "World Sky",
            position = LorebookEntryPosition.BEFORE_CHAR
        )

        val entryBeforeDesc = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("титул"),
            content = "Лор 2: Титул Хранительницы передается через века.",
            comment = "Pre-Desc Title",
            position = LorebookEntryPosition.BEFORE_DESC
        )

        val entryPersonality = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("характер"),
            content = "Лор 3: В моменты стресса проявляет железную выдержку.",
            comment = "Personality Trait",
            position = LorebookEntryPosition.PERSONALITY
        )

        val entryAfterDesc = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("одеяние"),
            content = "Лор 4: Носит звездную мантию, сотканную из лунного света.",
            comment = "Post-Desc Clothing",
            position = LorebookEntryPosition.AFTER_DESC
        )

        val entryScenario = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("битва"),
            content = "Лор 5: Текущая битва разворачивается у Небесного Престола.",
            comment = "Active Battle Scene",
            position = LorebookEntryPosition.SCENARIO
        )

        val entryAfterChar = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("орден"),
            content = "Лор 6: Орден Хранителей подчиняется древнему кодексу.",
            comment = "World Order",
            position = LorebookEntryPosition.AFTER_CHAR
        )

        val entryAtDepth = CharacterLorebookEntryItemDto(
            id = Uuid.random(),
            keys = listOf("тайна", "тайну"),
            content = "Лор 7: Враг знает тайну слабости кристалла!",
            comment = "Critical Depth Context",
            position = LorebookEntryPosition.AT_DEPTH,
            depth = 2
        )

        val lorebook = CharacterLorebookDetailsDto(
            id = Uuid.random(),
            characterId = Uuid.random(),
            name = "Полный CCv3 Лорбук",
            scanDepth = 5,
            entries = listOf(
                entryBeforeChar,
                entryBeforeDesc,
                entryPersonality,
                entryAfterDesc,
                entryScenario,
                entryAfterChar,
                entryAtDepth
            )
        )

        // 2. Матчим по сообщению, содержащему все ключевые слова
        val userText = "В этом небе наш титул и характер проверит битва, одеяние и орден скроют тайну!"
        val matchedResult = matcher.matchMultiple(
            lorebooks = listOf(lorebook),
            recentMessagesText = listOf(userText)
        )

        // Проверяем, что движок разложил статьи по всем спискам
        assertEquals(1, matchedResult.beforeChar.size)
        assertEquals(1, matchedResult.beforeDesc.size)
        assertEquals(1, matchedResult.personality.size)
        assertEquals(1, matchedResult.afterDesc.size)
        assertEquals(1, matchedResult.scenario.size)
        assertEquals(1, matchedResult.afterChar.size)
        assertEquals(1, matchedResult.atDepth.size)
        assertEquals(2, matchedResult.atDepth.first().depth)

        // 3. Формируем DTO персонажа
        val config = AICharacterConfigLLMDto(
            characterId = Uuid.random(),
            userId = Uuid.random(),
            characterName = "Селеста",
            llmConfig = LlmConfig(
                code = "gemini_flash",
                name = "Gemini Flash",
                modelAPIID = "google/gemini-2.5-flash",
                provider = LlmProvider.GOOGLE,
                energyCost = 1,
                minStatus = PremiumStatus.FREE,
                isActive = true
            ),
            temperature = 0.7f,
            interactionFormat = InteractionFormat.LITERARY_RPG,
            topK = 40,
            topP = 0.95f,
            maxOutputTokens = 1000,
            maxContextTokens = 8192,
            shortDescription = "Верховная чародейка",
            fullDescription = "Хранительница небесного архива.",
            firstMessage = "Приветствую.",
            safetySettings = SafetySettingsDto(0, 0, 0, 0),
            globalSettings = GlobalSettingsLLMDto("Правила", 1000),
            userPersona = null,
            scenario = "Встреча в архиве",
            lorebookBeforeChar = matchedResult.beforeChar,
            lorebookAfterChar = matchedResult.afterChar,
            lorebookBeforeDesc = matchedResult.beforeDesc,
            lorebookAfterDesc = matchedResult.afterDesc,
            lorebookPersonality = matchedResult.personality,
            lorebookScenario = matchedResult.scenario,
            lorebookAtDepth = matchedResult.atDepth
        )

        // 4. Проверяем системный промпт
        val systemPrompt = geminiRequestFactory.buildNormalSystemInstructionText(
            dto = config,
            rarity = Rarity.COMMON,
            messageCount = 1L,
            relationshipLevel = 1
        )

        println("=== СГЕНЕРИРОВАННЫЙ ПРОМПТ СО ВСЕМИ 7 ПОЗИЦИЯМИ CCv3 ===")
        println(systemPrompt)

        // Проверяем все блоки на своих местах:
        // 1. BEFORE_CHAR в первом <world_lore>
        assertTrue(systemPrompt.contains("<world_lore>\n### World Sky\nЛор 1: Небо над миром"))
        // 2. BEFORE_DESC перед detailed_personality
        assertTrue(systemPrompt.contains("<lore_context_pre_desc>\n### Pre-Desc Title\nЛор 2: Титул Хранительницы"))
        // 3. PERSONALITY внутри detailed_personality
        assertTrue(systemPrompt.contains("<detailed_personality>"))
        assertTrue(systemPrompt.contains("<personality_lore>\n### Personality Trait\nЛор 3: В моменты стресса"))
        // 4. AFTER_DESC после detailed_personality
        assertTrue(systemPrompt.contains("<lore_context_post_desc>\n### Post-Desc Clothing\nЛор 4: Носит звездную мантию"))
        // 5. SCENARIO внутри scenario
        assertTrue(systemPrompt.contains("<scenario>"))
        assertTrue(systemPrompt.contains("<scenario_lore>\n### Active Battle Scene\nЛор 5: Текущая битва"))
        // 6. AFTER_CHAR в последнем <world_lore>
        assertTrue(systemPrompt.contains("### World Order\nЛор 6: Орден Хранителей"))

        // 5. Проверяем внедрение AT_DEPTH в историю чата через HistoryBuilder
        val rawChat = listOf(
            DomainMessage(id = Uuid.random(), role = MessageRole.USER, contents = listOf(DomainMessageContent(AvalonMessageContentType.TEXT, "Сообщение 1"))),
            DomainMessage(id = Uuid.random(), role = MessageRole.MODEL, contents = listOf(DomainMessageContent(AvalonMessageContentType.TEXT, "Сообщение 2"))),
            DomainMessage(id = Uuid.random(), role = MessageRole.USER, contents = listOf(DomainMessageContent(AvalonMessageContentType.TEXT, "Сообщение 3"))),
            DomainMessage(id = Uuid.random(), role = MessageRole.MODEL, contents = listOf(DomainMessageContent(AvalonMessageContentType.TEXT, "Сообщение 4"))),
            DomainMessage(id = Uuid.random(), role = MessageRole.USER, contents = listOf(DomainMessageContent(AvalonMessageContentType.TEXT, userText)))
        )

        val builtHistory = historyBuilder.build(
            firstMessage = config.firstMessage,
            conversationSummary = null,
            rawHistory = rawChat,
            maxHistorySize = 10,
            lorebookAtDepth = matchedResult.atDepth
        )

        // AT_DEPTH с depth = 2 должен внедриться за 2 сообщения до конца
        // В истории из 5 сообщений: индексы 0, 1, 2, 3, 4. Индекс target = 5 - 1 - 2 = 2 (Сообщение 3)
        val targetMessage = builtHistory.firstOrNull { msg ->
            msg.contents.any { it.content.contains("<context_reminder>") }
        }
        assertTrue(targetMessage != null, "Сообщение с инъекцией AT_DEPTH должно присутствовать в истории")
        assertTrue(targetMessage!!.contents.any { it.content.contains("Лор 7: Враг знает тайну слабости кристалла!") })
    }
}

