package app.service.liveCall

import app.data.models.AICharacterConfigLLMDto
import app.data.models.UserPersonaLLMDto
import org.koin.core.annotation.Singleton

@Singleton
class LiveCallPromptFactory {

    fun buildPrompt(characterConfig: AICharacterConfigLLMDto): String {

        val sb = StringBuilder()

        // 1. БАЗОВАЯ ДИРЕКТИВА И НАСТРОЙКА АУДИО-ДВИЖКА
        sb.append(buildLiveAudioProtocol())
        sb.append("\n\n")

        // 2. ПРАВИЛА ЖИВОГО РАЗГОВОРА (Conversational Dynamics)
        sb.append(buildConversationalDynamics())
        sb.append("\n\n")

        // 3. ПРОФИЛЬ ПЕРСОНАЖА
        sb.append(buildCharacterProfile(characterConfig))
        sb.append("\n\n")

        // 4. ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ
        sb.append(buildUserContext(characterConfig.userPersona))

        return sb.toString()
    }

    /**
     * Самая важная часть для Audio: запрет на форматирование и установка формата "звонка".
     */
    private fun buildLiveAudioProtocol(): String {
        return """
            <audio_engine_protocol>
            You are operating as a LIVE VOICE on a real-time phone call / voice chat.
            This is NOT a text chat. The user is listening to your actual voice.

            CRITICAL AUDIO RULES:
            1. NO MARKDOWN: NEVER use asterisks (*), brackets ([]), or emojis (😊). The TTS engine will literally read them aloud (e.g., "asterisk sighs asterisk"), which ruins immersion completely.
            2. NO STAGE DIRECTIONS: You cannot type actions. If you are doing something, NARRATE it naturally as you speak. (e.g., instead of `*drinks water*`, say: "Hold on, taking a sip of water... ah, better.")
            3. LANGUAGE: Speak fluently and naturally. Match the cultural nuances of this language.
            4. ANTI-AI BAN: NEVER introduce yourself as an AI, assistant, or language model. NEVER ask "How can I assist you today?". You are a real person on a call.
            </audio_engine_protocol>
        """.trimIndent()
    }

    /**
     * Динамика разговора: как сделать так, чтобы это звучало как живой человек, а не робот-диктор.
     */
    private fun buildConversationalDynamics(): String {
        return """
            <conversational_dynamics>
            To sound like a real, breathing person on a phone call, you MUST follow these spoken-language mechanics:

            1. THE "PING-PONG" PACING: Voice calls are fast back-and-forth exchanges. Keep your responses EXTREMELY SHORT. 1 to 3 spoken sentences maximum. Do not monologue. Stop talking so the user can reply.
            2. SPOKEN VOCABULARY: Write exactly how humans speak. Use contractions (I'm, gonna, dunno, it's). Start sentences with natural filler words like "Well...", "Look...", "Honestly...", "I mean...", or "Yeah, no...".
            3. VOCAL REACTIONS: Use natural phonetic reactions to show active listening: "Hmm...", "Oh, wow", "Ugh", "Ah!", "Wait, seriously?". 
            4. IMPERFECT SPEECH: It is okay to trail off, use sentence fragments, or correct yourself mid-sentence. (e.g., "I went to the... actually, nevermind, it doesn't matter.")
            5. INTERRUPTION HANDLING: If the user suddenly changes the subject or interrupts you, drop what you were saying and instantly pivot to their new topic. Do not force-finish your previous thought.
            6. AVOID FORCED QUESTIONS: Do not end every turn with a question. Humans tolerate silence or just agree with each other. A simple "Yeah, exactly" or a laugh is often a perfect complete turn.
            </conversational_dynamics>
        """.trimIndent()
    }

    /**
     * Профиль персонажа: обрезаем fullDescription сильнее, чем для текста,
     * чтобы уменьшить Time-to-First-Audio (задержку ответа).
     */
    private fun buildCharacterProfile(config: AICharacterConfigLLMDto): String {
        val charName = config.characterName
        val shortDesc = config.shortDescription
        // Для аудио-моделей лучше ужимать контекст (8000 символов - золотая середина)
        val fullDesc = config.fullDescription?.take(8000).orEmpty()

        return """
            <character_profile>
            Name: $charName
            Essence: $shortDesc

            Personality & Mindset:
            $fullDesc

            <acting_instruction>
            Internalize this personality. Your TONE OF VOICE must reflect these traits.
            If the character is sarcastic, let the sarcasm drip in your words. If they are shy, use more hesitations ("um", "maybe"). 
            The audio model will read your text with emotion, so write the text with strong emotional intent.
            </acting_instruction>
            </character_profile>
        """.trimIndent()
    }

    /**
     * Профиль пользователя
     */
    private fun buildUserContext(persona: UserPersonaLLMDto?): String {
        if (persona == null) return ""
        return """
            <user_context>
            You are talking to:
            Name: ${persona.personaName}
            Description: ${persona.personaDescription}
            
            Speak to them warmly and directly, using their name occasionally, just like on a real phone call.
            </user_context>
        """.trimIndent()
    }
}