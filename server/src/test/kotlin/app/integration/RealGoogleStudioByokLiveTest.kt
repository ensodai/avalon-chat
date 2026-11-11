package app.integration

import com.google.genai.Client
import com.google.genai.types.*
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class RealGoogleStudioByokLiveTest {

    private val outputDir = File("output").apply { mkdirs() }
    private var geminiApiKey: String? = null

    @BeforeEach
    fun setup() {
        val dotenv = dotenv { ignoreIfMissing = true }
        geminiApiKey = dotenv["GEMINI_API_KEY"]
            ?: System.getenv("GEMINI_API_KEY")
    }

    @Test
    fun `query available models in Google AI Studio`() = runBlocking {
        val apiKey = geminiApiKey
        if (apiKey.isNullOrBlank()) {
            println("⚠️ GEMINI_API_KEY not found in .env / environment. Skipping live test.")
            return@runBlocking
        }

        println("=== 🔍 Querying Google AI Studio Available Models ===")
        val client = Client.builder().apiKey(apiKey).build()
        try {
            val list = runInterruptible(Dispatchers.IO) {
                client.models.list(null)
            }
            val modelNames = list.mapNotNull { it.name().orElse(null) }
            println("📋 Total models available in Google AI Studio: ${modelNames.size}")
            modelNames.sorted().forEach { println(" - $it") }
        } finally {
            try { client.close() } catch (_: Exception) {}
        }
    }

    @Test
    fun `generate live chat completion stream with real Google AI Studio API key`() = runBlocking {
        val apiKey = geminiApiKey
        if (apiKey.isNullOrBlank()) {
            println("⚠️ GEMINI_API_KEY not found in .env / environment. Skipping live test.")
            return@runBlocking
        }

        println("=== 💬 Testing Live Chat Streaming via Google GenAI SDK (BYOK) ===")
        val client = Client.builder().apiKey(apiKey).build()

        try {
            val systemInstruction = Content.builder()
                .role("system")
                .parts(listOf(Part.fromText("You are an expressive and witty anime character named Aiko in Avalon messenger. Always reply in character.")))
                .build()

            val safetySettings = listOf(
                SafetySetting.builder()
                    .category(HarmCategory.Known.HARM_CATEGORY_HARASSMENT)
                    .threshold(HarmBlockThreshold.Known.OFF)
                    .build(),
                SafetySetting.builder()
                    .category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
                    .threshold(HarmBlockThreshold.Known.OFF)
                    .build(),
                SafetySetting.builder()
                    .category(HarmCategory.Known.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                    .threshold(HarmBlockThreshold.Known.OFF)
                    .build(),
                SafetySetting.builder()
                    .category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
                    .threshold(HarmBlockThreshold.Known.OFF)
                    .build()
            )

            val config = GenerateContentConfig.builder()
                .temperature(0.8f)
                .systemInstruction(systemInstruction)
                .safetySettings(safetySettings)
                .build()

            val history = listOf(
                Content.builder()
                    .role("user")
                    .parts(listOf(Part.fromText("Привет, Айко! Как твоё настроение сегодня? Расскажи кратко в пару предложений!")))
                    .build()
            )

            val modelName = "gemini-2.5-flash"
            println("🚀 Calling generateContentStream on model: $modelName ...")

            val fullResponseBuilder = StringBuilder()
            val stream = runInterruptible(Dispatchers.IO) {
                client.models.generateContentStream(modelName, history, config)
            }

            var totalTokens = 0L
            val iterator = stream.iterator()
            while (iterator.hasNext()) {
                val chunk = iterator.next()
                val textChunk = chunk.candidates().orElse(null)?.firstOrNull()
                    ?.content()?.orElse(null)?.parts()?.orElse(null)?.firstOrNull()
                    ?.text()?.orElse(null) ?: ""

                if (textChunk.isNotEmpty()) {
                    print(textChunk)
                    fullResponseBuilder.append(textChunk)
                }

                chunk.usageMetadata().orElse(null)?.totalTokenCount()?.orElse(null)?.let {
                    totalTokens = it.toLong()
                }
            }
            println("\n\n✅ Stream finished. Received ${fullResponseBuilder.length} chars, total tokens: $totalTokens")
            assertTrue(fullResponseBuilder.isNotBlank(), "Model should return non-empty text")

        } finally {
            try { client.close() } catch (_: Exception) {}
        }
    }

    @Test
    fun `generate live TTS audio speech with real Google AI Studio API key`() = runBlocking {
        val apiKey = geminiApiKey
        if (apiKey.isNullOrBlank()) {
            println("⚠️ GEMINI_API_KEY not found in .env / environment. Skipping live test.")
            return@runBlocking
        }

        println("=== 🎙️ Testing Live TTS via Google GenAI SDK (BYOK) ===")
        val client = Client.builder().apiKey(apiKey).build()
        val outputFile = File(outputDir, "real_google_gemini_tts_sample.pcm")

        try {
            val textToSpeak = "Привет, мой дорогой друг! Это тестовое голосовое сообщение прямо из Avalon Avatars!"

            val config = GenerateContentConfig.builder()
                .responseModalities(listOf("AUDIO"))
                .speechConfig(
                    SpeechConfig.builder()
                        .languageCode("ru-RU")
                        .voiceConfig(
                            VoiceConfig.builder()
                                .prebuiltVoiceConfig(
                                    PrebuiltVoiceConfig.builder()
                                        .voiceName("Puck")
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build()

            val content = Content.builder()
                .role("user")
                .parts(listOf(Part.fromText(textToSpeak)))
                .build()

            val modelName = "gemini-2.5-flash-preview-tts"
            println("🔊 Generating TTS audio for text: '$textToSpeak' with voice 'Puck'...")

            val response = runInterruptible(Dispatchers.IO) {
                client.models.generateContent(modelName, content, config)
            }

            val audioBytes = response.candidates().orElse(null)?.firstOrNull()
                ?.content()?.orElse(null)?.parts()?.orElse(null)?.firstOrNull()
                ?.inlineData()?.orElse(null)?.data()?.orElse(null)

            assertNotNull(audioBytes, "Gemini-TTS should return audio bytes")
            assertTrue(audioBytes!!.isNotEmpty(), "Audio bytes must not be empty")

            outputFile.writeBytes(audioBytes)
            println("✅ Audio generated successfully! Saved ${audioBytes.size} bytes to: ${outputFile.absolutePath}")

        } finally {
            try { client.close() } catch (_: Exception) {}
        }
    }

    @Test
    fun `generate live Image with Imagen via real Google AI Studio API key`() = runBlocking {
        val apiKey = geminiApiKey
        if (apiKey.isNullOrBlank()) {
            println("⚠️ GEMINI_API_KEY not found in .env / environment. Skipping live test.")
            return@runBlocking
        }

        println("=== 🎨 Testing Live Imagen Generation via Google GenAI SDK (BYOK) ===")
        val client = Client.builder().apiKey(apiKey).build()
        val outputFile = File(outputDir, "real_google_imagen_sample.png")

        try {
            val imagePrompt = "Cinematic digital anime portrait of a beautiful cute girl with sparkling turquoise eyes and soft pink hair looking at cherry blossoms, masterpiece, high quality, soft studio lighting"
            val modelId = "gemini-2.5-flash-image"

            println("🎨 Generating image using model '$modelId' for prompt: '$imagePrompt'...")

            val config = GenerateContentConfig.builder()
                .responseModalities(listOf("IMAGE", "TEXT"))
                .build()

            val content = Content.builder()
                .role("user")
                .parts(listOf(Part.fromText(imagePrompt)))
                .build()

            val response = runInterruptible(Dispatchers.IO) {
                client.models.generateContent(modelId, content, config)
            }

            val imagePart = response.candidates().orElse(null)?.firstOrNull()
                ?.content()?.orElse(null)?.parts()?.orElse(null)
                ?.firstOrNull { it.inlineData().isPresent }

            val imageBytes = imagePart?.inlineData()?.orElse(null)?.data()?.orElse(null)

            assertNotNull(imageBytes, "Image bytes must not be null")
            assertTrue(imageBytes!!.isNotEmpty(), "Image bytes must not be empty")

            outputFile.writeBytes(imageBytes)
            println("✅ Image generated successfully! Saved ${imageBytes.size} bytes to: ${outputFile.absolutePath}")

        } finally {
            try { client.close() } catch (_: Exception) {}
        }
    }

    @Test
    fun `summarize chat dialogue with real Google AI Studio API key`() = runBlocking {
        val apiKey = geminiApiKey
        if (apiKey.isNullOrBlank()) {
            println("⚠️ GEMINI_API_KEY not found in .env / environment. Skipping live test.")
            return@runBlocking
        }

        println("=== 🧠 Testing Long-Term Memory / Dialogue Summarization ===")
        val client = Client.builder().apiKey(apiKey).build()

        try {
            val sampleDialogue = """
                User: Привет! Меня зовут Слава, я разработчик на Kotlin и Ktor.
                Character: Привет, Слава! Очень приятно познакомиться! Я обожаю космос и технологии.
                User: Скоро мы запускаем обновление нашего мессенджера с ИИ.
                Character: Это невероятно круто! Желаю тебе огромного успеха с запуском!
            """.trimIndent()

            val config = GenerateContentConfig.builder()
                .temperature(0.3f)
                .systemInstruction(
                    Content.builder()
                        .role("system")
                        .parts(listOf(Part.fromText("You are an expert memory assistant. Write a concise factual summary in 3rd person about the user.")))
                        .build()
                )
                .build()

            val content = Content.builder()
                .role("user")
                .parts(listOf(Part.fromText("Summarize this conversation:\n$sampleDialogue")))
                .build()

            val response = runInterruptible(Dispatchers.IO) {
                client.models.generateContent("gemini-2.5-flash", content, config)
            }

            val summaryText = response.candidates().orElse(null)?.firstOrNull()
                ?.content()?.orElse(null)?.parts()?.orElse(null)?.firstOrNull()
                ?.text()?.orElse(null)

            println("📝 Summary Result:\n$summaryText")
            assertNotNull(summaryText, "Summary text must not be null")
            assertTrue(summaryText!!.isNotBlank(), "Summary text must not be blank")

        } finally {
            try { client.close() } catch (_: Exception) {}
        }
    }

    @Test
    fun `test connection check use case with real Google AI Studio key and model`() = runBlocking {
        val apiKey = geminiApiKey
        if (apiKey.isNullOrBlank()) {
            println("⚠️ GEMINI_API_KEY not found in .env / environment. Skipping live test.")
            return@runBlocking
        }

        println("=== 🔍 Testing TestUserAiConnectionUseCase for Google BYOK ===")
        val openAiClient = app.service.openai.OpenAiCompatibleClient(app.data.network.openai.KtorOpenAiApiClient())
        val providerService = app.service.ai.AiProviderService(kotlinx.coroutines.CoroutineScope(Dispatchers.Default))
        val useCase = app.domain.user.usecases.TestUserAiConnectionUseCase(openAiClient, providerService)

        val req = com.ensodai.shared.avalon.user.models.TestUserAiConnectionRequest(
            providerId = "google",
            baseUrl = null,
            apiKey = apiKey,
            model = "gemini-2.5-flash"
        )

        val res = useCase.execute(req)
        println("📡 Test Connection Result: success=${res.success}\nLog output:\n${res.message}")
        assertTrue(res.success, "Test connection should succeed for valid Google key and model")
    }
}
