package app.integration

import app.data.db.DatabaseFactory
import app.data.network.models.openai.OpenAiImageRequest
import app.data.network.models.openai.OpenAiTtsRequest
import app.data.network.openai.KtorOpenAiApiClient
import app.service.openai.OpenAiCompatibleClient
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Base64

class RealOpenRouterMediaLiveTest {

    private lateinit var ktorOpenAiApiClient: KtorOpenAiApiClient
    private lateinit var openAiCompatibleClient: OpenAiCompatibleClient
    private lateinit var httpClient: HttpClient
    private val outputDir = File("output").apply { mkdirs() }
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private var liveApiKey: String? = null

    @BeforeEach
    fun setup() {
        httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                    explicitNulls = false
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 180_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 180_000
            }
        }
        ktorOpenAiApiClient = KtorOpenAiApiClient()
        openAiCompatibleClient = OpenAiCompatibleClient(ktorOpenAiApiClient)

        val dotenv = dotenv { ignoreIfMissing = true }
        liveApiKey = dotenv["OPENROUTER_API_KEY"]
            ?: System.getenv("OPENROUTER_API_KEY")
    }

    @Test
    fun `query OpenRouter models catalog to find real image and speech model IDs`() = runBlocking {
        println("=== 🔍 Querying OpenRouter Live Models Catalog ===")
        val response = httpClient.get("https://openrouter.ai/api/v1/models") {
            if (liveApiKey != null) {
                header(HttpHeaders.Authorization, "Bearer $liveApiKey")
            }
        }
        val body = response.bodyAsText()
        val parsed = json.parseToJsonElement(body).jsonObject
        val data = parsed["data"]?.jsonArray ?: emptyList()

        val allIds = data.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.content }
        println("📋 Total models returned from OpenRouter: ${allIds.size}")

        val imageModels = allIds.filter { id ->
            id.contains("image", ignoreCase = true) ||
            id.contains("flux", ignoreCase = true) ||
            id.contains("dall-e", ignoreCase = true)
        }
        println("🎨 Found Image Models (${imageModels.size}): ${imageModels.take(10)}")

        val audioModels = allIds.filter { id ->
            id.contains("audio", ignoreCase = true) ||
            id.contains("speech", ignoreCase = true) ||
            id.contains("tts", ignoreCase = true)
        }
        println("🎙️ Found Audio Models (${audioModels.size}): ${audioModels.take(10)}")

        assertTrue(allIds.isNotEmpty(), "Catalog should return active models")
    }

    @Test
    fun `generate real Image with OpenRouter pipeline and save to output`() = runBlocking {
        println("=== 🎨 Testing Kotlin Image Generation Pipeline ===")
        val targetImageFile = File(outputDir, "real_neon_space_cat.png")

        assertNotNull(liveApiKey, "OpenRouter API Key must be present in .env")
        println("🔑 Using OpenRouter Key: ${liveApiKey!!.take(12)}...")

        val modelName = "google/gemini-2.5-flash-image"
        val prompt = "A cool stylish cyberpunk cat wearing futuristic neon pilot goggles and leather jacket, driving a glowing spaceship cockpit flying through a colorful starry neon galaxy, synthwave aesthetic, 8k resolution, cinematic lighting, masterpiece digital painting"

        val request = OpenAiImageRequest(
            model = modelName,
            prompt = prompt,
            n = 1,
            size = "1024x1024",
            responseFormat = "b64_json"
        )

        // Запуск через настоящий Kotlin-клиент KtorOpenAiApiClient (с fallback логикой)
        val result = ktorOpenAiApiClient.createImageGeneration(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = liveApiKey!!,
            request = request
        )

        assertTrue(result.isSuccess, "Kotlin Image Pipeline must succeed: ${result.exceptionOrNull()?.message}")
        val imageResponse = result.getOrThrow()
        val base64Data = imageResponse.data.firstOrNull()?.b64Json
        assertNotNull(base64Data, "Base64 data should not be null")

        val imageBytes = Base64.getDecoder().decode(base64Data)
        targetImageFile.writeBytes(imageBytes)

        println("🚀 [SUCCESS] Image generated via Kotlin Pipeline! Saved to: ${targetImageFile.absolutePath} (${imageBytes.size} bytes)")
        assertTrue(targetImageFile.exists() && targetImageFile.length() > 0)
    }

    @Test
    fun `generate real Audio Speech with OpenRouter pipeline and save to output`() = runBlocking {
        println("=== 🎙️ Testing Kotlin Audio Speech Pipeline ===")
        val targetAudioFile = File(outputDir, "openrouter_direct_cat_voice.wav")
        val targetMp3File = File(outputDir, "real_tts_cat_space.mp3")

        assertNotNull(liveApiKey, "OpenRouter API Key must be present in .env")
        println("🔑 Using OpenRouter Key: ${liveApiKey!!.take(12)}...")

        val speechText = "Мяу! Привет, Слава! Я твой космический кот водитель, мчусь через неоновую галактику на полной скорости! Пристегни ремни, взлетаем!"

        val request = OpenAiTtsRequest(
            model = "openai/gpt-audio",
            input = speechText,
            voice = "alloy",
            responseFormat = "pcm"
        )

        // Запуск через настоящий Kotlin-клиент createSpeechStream (с fallback и стримингом)
        val audioChunks = ktorOpenAiApiClient.createSpeechStream(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = liveApiKey!!,
            request = request
        ).toList()

        val totalPcmBytes = audioChunks.fold(ByteArray(0)) { acc, bytes -> acc + bytes }
        println("📦 Received total ${totalPcmBytes.size} PCM bytes from Kotlin stream")
        assertTrue(totalPcmBytes.isNotEmpty(), "Audio stream should receive audio bytes")

        // Записываем WAV файл
        val header = ByteArray(44)
        val totalAudioLen = totalPcmBytes.size.toLong()
        val totalDataLen = totalAudioLen + 36
        val sampleRate = 24000L
        val channels = 1
        val byteRate = sampleRate * channels * 2

        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte(); header[5] = ((totalDataLen shr 8) and 0xff).toByte(); header[6] = ((totalDataLen shr 16) and 0xff).toByte(); header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // 16 for PCM
        header[20] = 1; header[21] = 0 // PCM format = 1
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte(); header[25] = ((sampleRate shr 8) and 0xff).toByte(); header[26] = ((sampleRate shr 16) and 0xff).toByte(); header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte(); header[29] = ((byteRate shr 8) and 0xff).toByte(); header[30] = ((byteRate shr 16) and 0xff).toByte(); header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 2).toByte(); header[33] = 0 // block align
        header[34] = 16; header[35] = 0 // bits per sample
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte(); header[41] = ((totalAudioLen shr 8) and 0xff).toByte(); header[42] = ((totalAudioLen shr 16) and 0xff).toByte(); header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        targetAudioFile.outputStream().use { fos ->
            fos.write(header)
            fos.write(totalPcmBytes)
        }

        println("🚀 [SUCCESS] Audio generated via Kotlin Pipeline! Saved to: ${targetAudioFile.absolutePath} (${targetAudioFile.length()} bytes)")
        assertTrue(targetAudioFile.exists() && targetAudioFile.length() > 0)
    }
}
