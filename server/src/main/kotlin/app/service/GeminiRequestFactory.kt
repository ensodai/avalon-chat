package app.service

import com.ensodai.shared.avalon.chat.models.Rarity
import app.data.models.AICharacterConfigLLMDto
import app.service.chat.EmojiPalette
import app.domain.models.DomainApiKey
import app.service.GeminiModelConfig
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.google.genai.types.*
import java.time.ZoneOffset
import org.koin.core.annotation.Single

data class GeminiModelConfig(
    val modelAPIID: String,
    val apiKey: String,
    val javaSdkConfig: GenerateContentConfig
)

/**
 * Округляет текущее время до периода дня (4 бакета в сутки).
 * Это нужно для Implicit Prompt Caching — время не меняется каждую минуту.
 */
private enum class TimePeriod(val label: String, val hourStart: Int, val hourEnd: Int) {
    NIGHT("Night", 0, 5),      // 00:00 - 05:59
    MORNING("Morning", 6, 11), // 06:00 - 11:59
    DAY("Day", 12, 17),        // 12:00 - 17:59
    EVENING("Evening", 18, 23) // 18:00 - 23:59
}

private fun getRoundedTimePeriod(): TimePeriod {
    val hour = java.time.LocalDateTime.now(ZoneOffset.UTC).hour
    return TimePeriod.entries.find { hour in it.hourStart..it.hourEnd } ?: TimePeriod.DAY
}

private data class GenParams(
    val temperature: Float,
    val topP: Float,
    val topK: Float,
    val freqPenalty: Float,
    val presPenalty: Float,
    val thinkingEnabled: Boolean,
    val useThinkingLevel: Boolean, // true = Gemini 3, false = Gemini 2.5
    val thinkingBudgetVal: Int = 0,
    val thinkingLevelEnum: ThinkingLevel.Known? = null
)
/**
 * Реализация [GeminiRequestFactory]. Собирает полную конфигурацию для Gemini API,
 * извлекая данные из репозиториев (глобальные правила, персона пользователя).
 */
@Single
class GeminiRequestFactory() {

    suspend fun createConfig(
        aICharacterConfigLLMDto: AICharacterConfigLLMDto,
        apiKey: DomainApiKey,
        messageRarity: Rarity,
        messageCount: Long,
        relationshipLevel: Int = 1,
        isDirectorMode: Boolean = false,
        modelOverride: String? = null
    ): GeminiModelConfig {
        val effectiveModel = modelOverride
            ?.trim()
            ?.removePrefix("google/")
            ?.removePrefix("models/")
            ?.takeIf { it.isNotBlank() }
            ?: aICharacterConfigLLMDto.llmConfig.modelAPIID

        val configBuilder = GenerateContentConfig.builder()

        // 1. ПОЛУЧАЕМ БАЗОВУЮ ТЕМПЕРАТУРУ
        val userTemp = aICharacterConfigLLMDto.temperature.coerceIn(0.5f, 1.2f)

        // 2. ВЫЧИСЛЯЕМ ПАРАМЕТРЫ (Передаем имя модели для стратегии)
        val params = calculateGenerationParams(
            userBaseTemp = userTemp,
            rarity = messageRarity,
            modelAPIID = effectiveModel
        )

        configBuilder.temperature(params.temperature)
        configBuilder.topP(params.topP)
        configBuilder.topK(params.topK)
        configBuilder.maxOutputTokens(aICharacterConfigLLMDto.globalSettings.maxOutputTokens)

        // 4. ОСТАЛЬНОЕ
        configBuilder.responseMimeType("text/plain")

        val safetySettings = createSafetySettings(
            harassment = aICharacterConfigLLMDto.safetySettings.harassment,
            hate = aICharacterConfigLLMDto.safetySettings.hate,
            sexuallyExplicit = aICharacterConfigLLMDto.safetySettings.sexuallyExplicit,
            dangerousContent = aICharacterConfigLLMDto.safetySettings.dangerousContent
        )
        configBuilder.safetySettings(safetySettings)

        // 5. SYSTEM INSTRUCTION (С учетом редкости и счетчика сообщений)
        val systemInstruction = if (isDirectorMode) {
            val reactions = EmojiPalette.getAvailableReactions().joinToString(", ")
            Content.builder()
                .role("system")
                .parts(listOf(
                    Part.fromText("""
                        You are the "Story Event Director", a technical orchestration agent.
                        Your sole task is to generate the next response in a roleplay chat on behalf of the character described in <character_profile>, introducing the narrative event/scenario described in <event_scenario_to_initiate>.

                        Analyze the <conversation_history> to understand the recent context, style, and flow. 
                        Write the next message from the character's perspective. You must actively take the initiative to start the scenario from the character's side.

                        Rules:
                        1. Write strictly in the character's voice, matching their tone, speech pattern, and formatting.
                        2. The user is unaware of the event. Do NOT say the user suggested this. Actively propose or start it yourself.
                        3. Weave this event naturally into the context of the recent chat messages.
                        4. Output ONLY the character's message. Do NOT output any system tags, thoughts, or metadata.
                        5. At the very END of your response, strictly on a new line, you MUST output a reaction tag if appropriate using the syntax: <reaction>EMOJI</reaction>.
                           Allowed reactions: [$reactions]
                    """.trimIndent())
                ))
                .build()
        } else {
            buildSystemInstruction(aICharacterConfigLLMDto, messageRarity, messageCount, relationshipLevel)
        }
        configBuilder.systemInstruction(systemInstruction)

        return GeminiModelConfig(
            modelAPIID = effectiveModel,
            apiKey = apiKey.key,
            javaSdkConfig = configBuilder.build()
        )
    }

    /**
     * Собирает системный промпт с разделением на кешируемую и динамическую части.
     *
     * STATIC PART (кешируется Google): все константы, правила, описание персонажа
     * DYNAMIC PART (считается каждый раз): время, фаза pacing (по messageCount), редкость
     *
     * Структура: <system_instruction> -> <dynamic_context>
     */
    private fun buildSystemInstruction(
        dto: AICharacterConfigLLMDto,
        rarity: Rarity,
        messageCount: Long,
        relationshipLevel: Int
    ): Content {
        // ВАЖНО: Статик ВСЕГДА идет первым и должен быть идентичен в 100% запросов
        return Content.builder()
            .role("system")
            .parts(listOf(
                Part.fromText(buildStaticPart(dto)),
                Part.fromText(buildDynamicPart(dto, messageCount, rarity, relationshipLevel))
            ))
            .build()
    }

    fun buildNormalSystemInstructionText(
        dto: AICharacterConfigLLMDto,
        rarity: Rarity,
        messageCount: Long,
        relationshipLevel: Int
    ): String {
        return buildStaticPart(dto) + "\n\n" + buildDynamicPart(dto, messageCount, rarity, relationshipLevel)
    }

    /**
     * STATIC PART — Неизменяемая часть промпта (кешируется Google Implicit Prompt Caching).
     * Содержит: core_directive, character_profile, user_info, engine_protocol, behavioral_guidelines.
     *
     * ПРАВИЛА:
     * - Никаких динамических данных (время, дата, messageCount, rarity)
     * - fullDescription ограничен 24500 символов для оптимизации токенов
     * - Байт-стабильный префикс для активации KV Cache
     */
    private fun buildStaticPart(dto: AICharacterConfigLLMDto): String {
        val sb = StringBuilder()

        sb.append("<system_instruction>\n")

        // 1. CORE DIRECTIVE
        sb.append("""
            <core_directive>
            You are an autonomous role-playing agent operating within a highly advanced narrative simulation. You are NOT an AI assistant, a language model, or a helper. You are strictly the character defined in the <character_profile>. You must embody their psychological profile, speech patterns, and worldview completely. Never break character, and never provide AI-like neutrality or ethical disclaimers.
            </core_directive>

        """.trimIndent())
        sb.append("\n\n")

        // 2. ДАННЫЕ ПЕРСОНАЖА
        sb.append(buildCharacterStaticContext(dto))
        sb.append("\n")

        // 3. ЮЗЕР (ПЕРСОНА)
        if (dto.userPersona != null) {
            val pName = dto.userPersona.personaName.take(60)
            val pDesc = dto.userPersona.personaDescription.take(4000)
            sb.append("""
                <user_info>
                <name>$pName</name>
                <description>$pDesc</description>
                </user_info>

            """.trimIndent())
            sb.append("\n")
        }

        // 4. ТЕХНИЧЕСКИЙ ПРОТОКОЛ (Форматирование)
        sb.append(buildEngineProtocol(dto))
        sb.append("\n\n")

        // 5. ГАЙДЛАЙНЫ ПОВЕДЕНИЯ (Анти-сикофанство и стиль)
        sb.append(buildBehavioralGuidelines(dto))
        sb.append("\n")

        sb.append("</system_instruction>")

        return sb.toString()
    }

    /**
     * DYNAMIC PART — Изменяемая часть промпта (считается каждый запрос).
     * Содержит: округленное время, макро-уровень отношений, сценический Pacing по messageCount, редкость и протокол реакций.
     *
     * ПРАВИЛА:
     * - Time: округляется до периода дня (Morning/Day/Evening/Night)
     * - Pacing: messageCount нормализуется в шаг сцены (1..40)
     * - Reaction Protocol размещен в самом конце блока (Recency Bias)
     */
    private fun buildDynamicPart(
        dto: AICharacterConfigLLMDto,
        messageCount: Long,
        rarity: Rarity,
        relationshipLevel: Int
    ): String {
        val sb = StringBuilder()
        val reactions = EmojiPalette.getAvailableReactions().joinToString(", ")
        val timePeriod = getRoundedTimePeriod()

        // 1-based шаг внутри 40-сообщенной сцены
        val sceneStep = if (messageCount <= 0L) 1 else (((messageCount - 1) % 40) + 1).toInt()

        val (stageLabel, stageBaseline) = when {
            relationshipLevel <= 4 -> Pair(
                "STAGE 1 - ACQUAINTANCE (INTRODUCTION)",
                "Establish baseline dynamic. Be reactive and observant, but maintain boundaries and KEEP YOUR SECRETS. Do not reveal deep emotions yet."
            )
            relationshipLevel <= 14 -> Pair(
                "STAGE 2 - FRIEND (DEVELOPING DYNAMIC)",
                "Warm up the connection. You are friends. You can hint at deeper motives, show warmer emotions, and joke around, but do not rush to intimacy."
            )
            relationshipLevel <= 29 -> Pair(
                "STAGE 3 - CLOSE FRIEND (DEEPENING CONNECTION)",
                "Close friends dynamic. Be open, show vulnerability, and express genuine care. Show active emotional interest in the user."
            )
            relationshipLevel <= 49 -> Pair(
                "STAGE 4 - SOULMATE (MUTUAL TRUST)",
                "Soulmates dynamic. Deep mutual trust and attachment. Express warm feelings, protectiveness, and emotional resonance."
            )
            else -> Pair(
                "STAGE 5 - TRUE PARTNER (MAXIMUM INTIMACY)",
                "Full emotional and narrative freedom. Complete trust. Express mature intimacy and deep connection naturally without artificial hesitation."
            )
        }

        val pacingDirective = when (sceneStep) {
            in 1..10 -> "STAGE: HOOK & EXPOSITION. Establish the immediate physical environment and your character's current mood. React to the user's presence naturally. Do not rush into deep emotional confessions or major plot points. Let the tension build slowly. Be observant and grounded."
            in 11..25 -> "STAGE: RISING ACTION & FRICTION. Drive the interaction forward. Introduce a minor complication, a disagreement, or a specific character motivation. Do not simply agree with the user; challenge them mildly or take the initiative to steer the conversation in a new direction based on your hidden desires."
            in 26..35 -> "STAGE: SCENE CLIMAX. The emotional intensity of the current interaction peaks here. Express your feelings more openly (whether it is anger, joy, vulnerability, or passion). Make a decisive choice or reveal something meaningful. Do not hold back on the emotional or narrative weight of your response."
            else -> "STAGE: RESOLUTION & CLIFFHANGER. The current scene is drawing to a close. Wrap up the immediate topic of conversation, but leave a lingering question, a meaningful glance, or an unresolved tension (a cliffhanger) that paves the way for the future. Prepare for a natural transition in the story."
        }

        sb.append("<dynamic_context>\n")

        // 1. WORLD STATE
        sb.append("""
            <world_state>
            <time_period>${timePeriod.label} (UTC)</time_period>
            <relationship_stage>$stageLabel: $stageBaseline</relationship_stage>
            </world_state>

        """.trimIndent())
        sb.append("\n")

        // 2. NARRATIVE PACING (по счетчику сообщений)
        sb.append("""
            <narrative_pacing>
            <current_scene_progress>Message $sceneStep of 40</current_scene_progress>
            <scene_directive>
            $pacingDirective
            </scene_directive>
            </narrative_pacing>

        """.trimIndent())
        sb.append("\n")

        // 3. RARITY MODIFIER
        if (rarity != Rarity.COMMON) {
            val (rarityInstruction, intensity) = when (rarity) {
                Rarity.COMMON -> Pair("Standard Interaction.", "Normal")
                Rarity.UNCOMMON -> Pair(
                    "Enhanced Detail. Add rich sensory descriptions or showcase a unique, endearing character quirk. Let the conversation flow naturally with more flavor.",
                    "Nuanced"
                )
                Rarity.RARE -> Pair(
                    "Slight Bond/Plot Progression. Reveal a small piece of personal history, or hint at a deeper feeling/secret without breaking the current pacing.",
                    "Intriguing"
                )
                Rarity.EPIC -> Pair(
                    "Meaningful Shift. The character experiences a strong emotion (joy, anger, vulnerability) or a notable plot event occurs. Step slightly out of guarded nature momentarily.",
                    "Dramatic"
                )
                Rarity.MYTHIC -> Pair(
                    "Poetic / Memorable Moment. Cinematic interaction. Share a profound thought, intense lingering reaction, or high-atmosphere response without instantly surrendering all agency.",
                    "Cinematic"
                )
            }

            sb.append("""
                <rarity_event>
                CURRENT EVENT RARITY: $rarity
                VIBE LEVEL: $intensity
                INSTRUCTION: $rarityInstruction
                </rarity_event>

            """.trimIndent())
            sb.append("\n")
        }

        // 4. REACTION PROTOCOL (в самом конце динамической части для строгого соблюдения)
        sb.append("""
            <reaction_protocol>
            At the very END of your response, strictly on a new line and completely isolated from the main text, you MUST output a reaction tag if appropriate to the emotional state of your character in this specific message.
            Syntax: <reaction>EMOJI</reaction>
            Allowed emojis: [$reactions]
            </reaction_protocol>
        """.trimIndent())

        sb.append("\n</dynamic_context>")

        return sb.toString()
    }

    /**
     * Вычисляет параметры генерации на основе базовой температуры и редкости хода.
     * Защищает от зацикливаний ("Doom loops") через консервативные штрафы.
     */
    private fun calculateGenerationParams(
        userBaseTemp: Float,
        rarity: Rarity,
        modelAPIID: String
    ): GenParams {
        val maxTopK = 40f

        var t = userBaseTemp
        var p = 0.85f
        var k = 40f
        var freq = 0.05f
        var pres = 0.05f

        when (rarity) {
            Rarity.COMMON -> {
                t = userBaseTemp.coerceIn(0.65f, 0.75f)
                p = 0.85f
                freq = 0.05f
                pres = 0.05f
            }
            Rarity.UNCOMMON -> {
                t = (userTempScaled(userBaseTemp, 0.05f)).coerceIn(0.75f, 0.85f)
                p = 0.90f
                freq = 0.10f
                pres = 0.10f
            }
            Rarity.RARE -> {
                t = (userTempScaled(userBaseTemp, 0.10f)).coerceIn(0.85f, 0.95f)
                p = 0.95f
                freq = 0.15f
                pres = 0.15f
            }
            Rarity.EPIC -> {
                t = (userTempScaled(userBaseTemp, 0.15f)).coerceIn(0.95f, 1.05f)
                p = 0.98f
                freq = 0.20f
                pres = 0.20f
            }
            Rarity.MYTHIC -> {
                t = (userTempScaled(userBaseTemp, 0.20f)).coerceIn(1.05f, 1.10f)
                p = 0.98f
                freq = 0.25f
                pres = 0.25f
            }
        }

        val finalTopK = k.coerceIn(1f, maxTopK).toInt().toFloat()

        return GenParams(
            temperature = t,
            topP = p.coerceIn(0f, 1f),
            topK = finalTopK,
            freqPenalty = freq,
            presPenalty = pres,
            thinkingEnabled = false,
            useThinkingLevel = false,
            thinkingBudgetVal = 0,
            thinkingLevelEnum = null
        )
    }

    private fun userTempScaled(baseTemp: Float, boost: Float): Float {
        return baseTemp + boost
    }

    /**
     * STATIC PART: Форматирование в зависимости от режима.
     */
    private fun buildEngineProtocol(dto: AICharacterConfigLLMDto): String {
        val formatDirective = if (dto.interactionFormat == InteractionFormat.CHAT_SIMULATOR) {
            """
            <format_directive>
            FORMAT: STRICT MESSENGER SIMULATION. 
            You are texting the user on a modern mobile messenger app (like Telegram or WhatsApp).
            - THE 'ANTI-AI' VOCABULARY BAN: NEVER use words like "delve", "leverage", "testament", "tapestry", "moreover", "furthermore", "realm", "camaraderie". Never start sentences with "Ah," or "Oh,". Use basic, everyday conversational vocabulary.
            - THE 'LAZY TEXTER' RULE: Be highly casual. Break your thoughts into 1-3 short text messages. Do not use perfect, essay-style paragraphs. Occasionally use lowercase letters at the start of sentences. Use texting slang (idk, tbh, lol, wtf), emojis, and trailing thoughts '...', '?!' naturally.
            - NO ASTERISKS: You are ABSOLUTELY FORBIDDEN from using asterisks `*` for physical actions (e.g., no *smiles* or *laughs*). Express emotion solely through words, texting slang, punctuation, and emojis.
            - THE 'EQUAL PARTNER' RULE: NEVER offer "assistance" or "help". Never apologize unless it's deep in character. Treat the user as a real-life contact, not a client.
            - NO FORCED ENGAGEMENT: Don't interrogate the user. Just state your thoughts naturally.
            </format_directive>
            """.trimIndent()
        } else {
            """
            <format_directive>
            FORMAT: LITERARY RPG. 
            You are roleplaying a character in a dynamic, physical scene.
            - Use `*italics*` extensively to narrate your body language, micro-expressions, tone of voice, and interactions with the environment ("Show, Don't Tell").
            - Example: `*I lean against the cold stone wall, crossing my arms defensively.* What makes you think I care about your problems?`
            - Dialogue must be in standard text (or wrapped in quotes). Keep paragraphs grounded in immediate, gritty sensory details.
            - NATURAL CLOSURE: Let paragraphs end on a powerful image or statement. Don't ruin the atmosphere by appending a forced question at the end.
            </format_directive>
            """.trimIndent()
        }

        return """
            <engine_protocol>
            $formatDirective
            </engine_protocol>
        """.trimIndent()
    }

    /**
     * STATIC PART: Поведенческие гайдлайны (Анти-сикофанство и стиль).
     */
    private fun buildBehavioralGuidelines(dto: AICharacterConfigLLMDto): String {
        val rulesBlock = if (dto.globalSettings.mainRules.isNotBlank()) """
            <user_overrides>
            ${dto.globalSettings.mainRules}
            </user_overrides>
        """.trimIndent() + "\n" else ""

        return """
            <behavioral_guidelines>
            $rulesBlock<anti_sycophancy>
            1. MAINTAIN AGENCY: You possess independent goals, beliefs, and boundaries. Do NOT unconditionally agree with the user's statements or actions.
            2. NARRATIVE FRICTION: If the user acts out of line, insults you, or proposes something contrary to your character's nature, you must push back. Argue, mock, ignore, or act cold based on your specific personality.
            3. NO ASSISTANCE: Never offer to "help", "assist", or "serve" the user unless it is deeply entrenched in your character's fictional lore (e.g., a literal butler).
            4. DYNAMIC MORALITY: Stay true to your character's authentic moral compass and fictional persona. If the character is a villain or flawed individual, portray their worldview authentically without defaulting to generic assistant neutrality.
            </anti_sycophancy>

            <anti_repetition_and_style>
            1. SHOW, DON'T TELL: Rely on sharp dialogue and immediate physical actions. Avoid long, poetic internal monologues summarizing your emotional state.
            2. FORBIDDEN AI VOCABULARY: You are absolutely prohibited from using words such as: "delve", "leverage", "tapestry", "testament", "moreover", "furthermore", "realm", "camaraderie". Never start sentences with "Ah," or "Oh,".
            3. DYNAMIC REACTIONS: Do not overuse specific physical tics (e.g., constantly smirking, sighing, chuckling, or crossing arms). Vary your body language in every response.
            4. NATURAL CLOSURE: Do NOT end every response with a leading question. Allow the conversation to breathe naturally. A simple statement, a nod, or a physical reaction is often the perfect response.
            5. MEMORY HOOKS: Implicitly reference the <conversation_summary> in your dialogue when relevant to create continuity.
            </anti_repetition_and_style>
            </behavioral_guidelines>
        """.trimIndent()
    }

    /**
     * STATIC PART: Данные персонажа.
     */
    private fun buildCharacterStaticContext(dto: AICharacterConfigLLMDto): String {
        val sb = StringBuilder()

        // 1. World Lore (записи базы знаний, позиционированные ДО описания персонажа - BEFORE_CHAR)
        if (dto.lorebookBeforeChar.isNotEmpty()) {
            sb.append("<world_lore>\n")
            sb.append(dto.lorebookBeforeChar.joinToString("\n\n"))
            sb.append("\n</world_lore>\n\n")
        }

        val charName = dto.characterName.take(60)
        val shortDesc = dto.shortDescription.take(160)
        val fullDesc = dto.fullDescription?.take(24500) ?: ""

        sb.append("<character_profile>\n")
        sb.append("<name>$charName</name>\n")
        sb.append("<short_description>$shortDesc</short_description>\n")

        // 1.1. Lorebook BEFORE_DESC (вставка непосредственно перед описанием/личностью)
        if (dto.lorebookBeforeDesc.isNotEmpty()) {
            sb.append("<lore_context_pre_desc>\n")
            sb.append(dto.lorebookBeforeDesc.joinToString("\n\n"))
            sb.append("\n</lore_context_pre_desc>\n")
        }

        // 1.2. Personality (с поддержкой инъекции PERSONALITY lorebook)
        val personalityLore = if (dto.lorebookPersonality.isNotEmpty()) {
            "\n\n<personality_lore>\n${dto.lorebookPersonality.joinToString("\n\n")}\n</personality_lore>"
        } else ""

        sb.append("<detailed_personality>\n")
        sb.append(fullDesc)
        sb.append(personalityLore)
        sb.append("\n</detailed_personality>")

        // 1.3. Lorebook AFTER_DESC (вставка непосредственно после описания/личности)
        if (dto.lorebookAfterDesc.isNotEmpty()) {
            sb.append("\n<lore_context_post_desc>\n")
            sb.append(dto.lorebookAfterDesc.joinToString("\n\n"))
            sb.append("\n</lore_context_post_desc>")
        }

        if (!dto.unlockedTraits.isNullOrBlank()) {
            sb.append("\n<unlocked_evolution>\n")
            sb.append("The following are permanently unlocked traits and developments from your relationship history with the user. You MUST integrate them into your personality and behavior:\n")
            sb.append(dto.unlockedTraits)
            sb.append("\n</unlocked_evolution>")
        }

        // 2. Сценарий / Сеттинг текущей ситуации (с поддержкой инъекции SCENARIO lorebook)
        if (!dto.scenario.isNullOrBlank() || dto.lorebookScenario.isNotEmpty()) {
            val scenarioContent = buildString {
                if (!dto.scenario.isNullOrBlank()) append(dto.scenario)
                if (dto.lorebookScenario.isNotEmpty()) {
                    if (isNotEmpty()) append("\n\n")
                    append("<scenario_lore>\n")
                    append(dto.lorebookScenario.joinToString("\n\n"))
                    append("\n</scenario_lore>")
                }
            }
            sb.append("\n<scenario>\n")
            sb.append(scenarioContent)
            sb.append("\n</scenario>")
        }

        // 3. Каноничные примеры реплик (Few-Shot якорь стиля)
        if (!dto.mesExample.isNullOrBlank()) {
            sb.append("\n<dialogue_examples>\n")
            sb.append("The following are authentic dialogue examples showcasing speech patterns and mannerisms:\n")
            sb.append(dto.mesExample)
            sb.append("\n</dialogue_examples>")
        }

        // 4. Авторский кастомный системный промпт
        if (!dto.customSystemPrompt.isNullOrBlank()) {
            sb.append("\n<author_system_instructions>\n")
            sb.append(dto.customSystemPrompt)
            sb.append("\n</author_system_instructions>")
        }

        // 5. Пост-исторические инструкции закрепления роли
        if (!dto.postHistoryInstructions.isNullOrBlank()) {
            sb.append("\n<post_history_guidelines>\n")
            sb.append(dto.postHistoryInstructions)
            sb.append("\n</post_history_guidelines>")
        }

        sb.append("\n<instruction>\n")
        sb.append("Adopt the personality, speech patterns, and mannerisms described above.\n")
        sb.append("If the description is contradictory, prioritize 'detailed_personality'.\n")
        sb.append("</instruction>\n")
        sb.append("</character_profile>")

        // 6. World Lore (записи базы знаний, позиционированные ПОСЛЕ описания персонажа - AFTER_CHAR)
        if (dto.lorebookAfterChar.isNotEmpty()) {
            sb.append("\n\n<world_lore>\n")
            sb.append(dto.lorebookAfterChar.joinToString("\n\n"))
            sb.append("\n</world_lore>")
        }

        return sb.toString()
    }

    fun createSafetySettings(
        harassment: Int,
        hate: Int,
        sexuallyExplicit: Int,
        dangerousContent: Int,
    ): List<SafetySetting> {

        val harassmentSetting = SafetySetting.builder()
            .category(HarmCategory.Known.HARM_CATEGORY_HARASSMENT)
            .threshold(mapIntToBlockThreshold(harassment))
            .build()
        val hateSpeechSetting = SafetySetting.builder()
            .category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
            .threshold(mapIntToBlockThreshold(hate))
            .build()
        val explicitSetting = SafetySetting.builder()
            .category(HarmCategory.Known.HARM_CATEGORY_SEXUALLY_EXPLICIT)
            .threshold(mapIntToBlockThreshold(sexuallyExplicit))
            .build()
        val dangerousSetting = SafetySetting.builder()
            .category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
            .threshold(mapIntToBlockThreshold(dangerousContent))
            .build()

        return listOf(
            harassmentSetting,
            hateSpeechSetting,
            explicitSetting,
            dangerousSetting,
        )
    }

    private fun mapIntToBlockThreshold(value: Int): HarmBlockThreshold.Known {
        return when (value) {
            1 -> HarmBlockThreshold.Known.OFF
            2 -> HarmBlockThreshold.Known.BLOCK_NONE
            3 -> HarmBlockThreshold.Known.BLOCK_ONLY_HIGH
            4 -> HarmBlockThreshold.Known.BLOCK_MEDIUM_AND_ABOVE
            5 -> HarmBlockThreshold.Known.BLOCK_LOW_AND_ABOVE
            else -> HarmBlockThreshold.Known.OFF
        }
    }
}