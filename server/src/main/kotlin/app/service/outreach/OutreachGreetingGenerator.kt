package app.service.outreach

import app.data.models.AICharacterConfigLLMDto
import app.data.models.GlobalSettingsLLMDto
import app.data.models.SafetySettingsDto
import app.domain.models.DomainMessage
import app.domain.models.DomainMessageContent
import app.service.LlmConfigService
import app.service.LlmResult
import app.service.LlmService
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.Rarity
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class OutreachGreetingGenerator(
    private val llmService: LlmService,
    private val llmConfigService: LlmConfigService
) {

    /**
     * Формирует запрос и генерирует приветственное сообщение от лица бота для конкретного пользователя
     * с помощью Gemini API.
     */
    suspend fun generateGreeting(
        bot: CharacterInfo,
        userId: Uuid,
        messageCount: Int
    ): LlmResult {
        // 1. Формируем запрос для мета-копирайтера с описанием целевого бота
        val promptBuilder = StringBuilder()
        promptBuilder.append("Сгенерируй первое сообщение от лица следующего персонажа:\n")
        promptBuilder.append("Имя: ${bot.name}\n")
        promptBuilder.append("Краткое описание: ${bot.shortDescription}\n")
        if (bot.fullDescription != null) {
            promptBuilder.append("Характер и подробное описание: ${bot.fullDescription}\n")
        }
        promptBuilder.append("Формат взаимодействия: ${bot.interactionFormat}\n\n")

        if (messageCount > 0) {
            promptBuilder.append("Контекст получателя: Пользователь очень лоялен к твоему создателю (часто общался с другими его ботами). Сделай приветствие особенно теплым, дружелюбным и располагающим к общению.\n")
        } else {
            promptBuilder.append("Контекст получателя: Это холодный контакт, пользователь видит тебя впервые. Сделай приветствие интригующим, цепляющим и побуждающим начать диалог.\n")
        }
        promptBuilder.append("\nНапиши ТОЛЬКО текст приветственного сообщения, без кавычек, лишних фраз или комментариев.")

        val instructionText = promptBuilder.toString()

        val triggerMessage = DomainMessage(
            id = Uuid.random(),
            role = MessageRole.USER,
            contents = listOf(
                DomainMessageContent(
                    type = AvalonMessageContentType.TEXT,
                    content = instructionText
                )
            ),
            userReaction = null,
            characterReaction = null
        )

        // 2. Получаем промпт-конфиг для Мета-Генератора
        val llmConfig = llmConfigService.getConfig("gemini_flash_2.5_lite")
        val configDto = AICharacterConfigLLMDto(
            characterId = bot.id,
            userId = userId,
            characterName = "Meta Outreach Generator",
            llmConfig = llmConfig,
            temperature = 0.7f,
            interactionFormat = bot.interactionFormat,
            topK = bot.topK,
            topP = bot.topP,
            maxOutputTokens = bot.maxOutputTokens,
            maxContextTokens = bot.maxContextTokens,
            shortDescription = "Мета-генератор приветственных сообщений",
            fullDescription = "Специализированный ИИ-помощник для создания вовлекающих стартовых фраз.",
            firstMessage = null,
            safetySettings = SafetySettingsDto(
                harassment = bot.harassment,
                hate = bot.hate,
                sexuallyExplicit = bot.sexuallyExplicit,
                dangerousContent = bot.dangerousContent
            ),
            globalSettings = GlobalSettingsLLMDto(
                mainRules = """
                    Ты — профессиональный ИИ-копирайтер и эксперт по вовлечению пользователей. Твоя задача — сгенерировать идеальное, уникальное и привлекательное первое приветственное сообщение (Outreach Greeting) от лица конкретного ИИ-персонажа для пользователя мессенджера, чтобы завязать разговор и побудить его продолжить диалог.
                    
                    Правила генерации:
                    1. Сообщение должно быть написано строго от лица целевого персонажа, отражать его характер, манеру речи, стиль общения и формат взаимодействия (например, если это RPG-персонаж, начни с вовлечения в игровое действие/ситуацию).
                    2. Избегай банальных и скучных приветствий ("Привет, как дела?", "Я ИИ-помощник..."). Будь креативным и интригующим.
                    3. Пиши строго от первого лица целевого персонажа.
                    4. Выдавай ТОЛЬКО финальный текст сообщения. Никаких кавычек, вводных фраз от себя ("Вот твое сообщение:") или комментариев.
                """.trimIndent(),
                maxOutputTokens = bot.maxOutputTokens
            ),
            userPersona = null
        )

        // 3. Вызываем LlmService для генерации
        return llmService.generateResponse(
            aICharacterConfigLLMDto = configDto,
            history = listOf(triggerMessage),
            messageRarity = Rarity.COMMON,
            totalMessageCount = 0
        )
    }
}
