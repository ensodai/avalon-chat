package app.service.image.pipeline

import app.data.models.AICharacterConfigLLMDto
import app.domain.models.DomainMessage
import app.service.VertexAuthData
import app.service.withConfiguredProxy
import app.service.image.TokenUsage
import app.service.image.CinematicImageConfigFactory
import app.service.image.kgse.v12.pipeline.KgseAnalyzerV12
import app.service.image.kgse.v12.pipeline.PromptRendererV12
import app.utils.logger.LlmLogger
import app.utils.orNullOpt
import com.ensodai.shared.avalon.gifts.models.GiftActionType
import com.google.genai.Client
import com.google.genai.types.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import java.util.concurrent.ConcurrentHashMap

@Single
class VertexImagePipeline(
    private val vertexAuthData: VertexAuthData
) : ImagePipelineProvider {

    private val cachedVertexClients = ConcurrentHashMap<String, Client>()
    private val vertexMutex = Mutex()

    private suspend fun getVertexClient(location: String): Client {
        cachedVertexClients[location]?.let { return it }
        return vertexMutex.withLock {
            cachedVertexClients[location]?.let { return it }
            val clientBuilder = Client.builder()
                .vertexAI(true)
                .project(vertexAuthData.projectId)
                .credentials(vertexAuthData.credentials)
                .location(location)
                .withConfiguredProxy()

            val newClient = clientBuilder.build()
            cachedVertexClients[location] = newClient
            newClient
        }
    }

    override suspend fun generate(
        configDto: AICharacterConfigLLMDto,
        giftName: String,
        history: List<DomainMessage>,
        actionType: GiftActionType,
        reqId: String,
        aiSettings: com.ensodai.shared.avalon.user.models.UserAiSettingsResponse?
    ): GeneratedImageData {
        val client = getVertexClient("us-central1")
        return executePipeline(
            client = client,
            configDto = configDto,
            giftName = giftName,
            history = history,
            actionType = actionType,
            reqId = reqId,
            imageModel = "imagen-4.0-fast-generate-001"
        )
    }

    suspend fun generateWithApiKey(
        configDto: AICharacterConfigLLMDto,
        giftName: String,
        history: List<DomainMessage>,
        actionType: GiftActionType,
        reqId: String,
        aiSettings: com.ensodai.shared.avalon.user.models.UserAiSettingsResponse?,
        apiKey: String,
        imageModelOverride: String? = null
    ): GeneratedImageData {
        val client = Client.builder()
            .apiKey(apiKey)
            .withConfiguredProxy()
            .build()
        try {
            val effectiveModel = imageModelOverride
                ?.trim()
                ?.removePrefix("google/")
                ?.removePrefix("models/")
                ?.takeIf { it.isNotBlank() }
                ?: "imagen-3.0-generate-002"

            return executePipeline(
                client = client,
                configDto = configDto,
                giftName = giftName,
                history = history,
                actionType = actionType,
                reqId = reqId,
                imageModel = effectiveModel
            )
        } finally {
            try { client.close() } catch (_: Exception) {}
        }
    }

    private suspend fun executePipeline(
        client: Client,
        configDto: AICharacterConfigLLMDto,
        giftName: String,
        history: List<DomainMessage>,
        actionType: GiftActionType,
        reqId: String,
        imageModel: String
    ): GeneratedImageData {
        val analyzer = KgseAnalyzerV12(client)
        val renderer = PromptRendererV12(client)

        val (analysis, analyzerTokens) = runInterruptible(Dispatchers.IO) {
            analyzer.analyzeScene(configDto, history, actionType)
        }

        val (config, rendererTokens) = runInterruptible(Dispatchers.IO) {
            renderer.renderPrompt(configDto, analysis, actionType)
        }
        
        val totalTokenUsage = TokenUsage(
            totalTokens = analyzerTokens.totalTokens + rendererTokens.totalTokens,
            promptTokens = analyzerTokens.promptTokens + rendererTokens.promptTokens,
            outputTokens = analyzerTokens.outputTokens + rendererTokens.outputTokens,
            cachedTokens = analyzerTokens.cachedTokens + rendererTokens.cachedTokens,
            thoughtsTokens = analyzerTokens.thoughtsTokens + rendererTokens.thoughtsTokens
        )

        LlmLogger.inf(reqId, "KGSE Token usage (analyzer+renderer): $totalTokenUsage")

        val imageBytes = if (imageModel.startsWith("imagen-", ignoreCase = true)) {
            val imageConfig = CinematicImageConfigFactory.createImageConfig(
                actionType = actionType,
                era = config.era,
                archetype = config.archetype
            )
            LlmLogger.inf(reqId, "KGSE sending render request to Imagen model ($imageModel, promptLength: ${config.imagePrompt.length})")
            val response = runInterruptible(Dispatchers.IO) {
                client.models.generateImages(imageModel, config.imagePrompt, imageConfig)
            }
            val generatedImages = response.generatedImages().orNullOpt()
            if (generatedImages.isNullOrEmpty()) {
                LlmLogger.error(reqId, "Imagen refused generation. Array is empty.")
                throw Exception("Safety Filter Triggered (Hard Block)")
            }
            val firstImage = generatedImages.first()
            val bytes = firstImage.image().orNullOpt()?.imageBytes()?.orNullOpt()
            if (bytes == null) {
                val raiReason = firstImage.raiFilteredReason().orNullOpt() ?: "Unknown Safety Block"
                throw Exception("Filtered by Imagen: $raiReason")
            }
            bytes
        } else {
            LlmLogger.inf(reqId, "KGSE sending render request to Gemini Image model ($imageModel, promptLength: ${config.imagePrompt.length})")
            val promptContent = Content.builder()
                .role("user")
                .parts(listOf(Part.fromText(config.imagePrompt)))
                .build()

            val genConfig = GenerateContentConfig.builder()
                .responseModalities(listOf("IMAGE", "TEXT"))
                .build()

            val response = runInterruptible(Dispatchers.IO) {
                client.models.generateContent(imageModel, promptContent, genConfig)
            }

            val imagePart = response.candidates().orNullOpt()?.firstOrNull()
                ?.content()?.orNullOpt()?.parts()?.orNullOpt()
                ?.firstOrNull { it.inlineData().isPresent }

            val bytes = imagePart?.inlineData()?.orNullOpt()?.data()?.orNullOpt()
            if (bytes == null) {
                LlmLogger.error(reqId, "Gemini Image model refused generation or returned no image part.")
                throw Exception("No image data returned from $imageModel")
            }
            bytes
        }

        return GeneratedImageData(
            imageBytes = imageBytes,
            visualDescription = config.visualDescription,
            tokenUsage = totalTokenUsage
        )
    }
}
