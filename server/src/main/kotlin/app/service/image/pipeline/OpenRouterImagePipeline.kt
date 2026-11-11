package app.service.image.pipeline

import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import app.data.models.AICharacterConfigLLMDto
import app.domain.models.DomainMessage
import app.service.LlmResult
import app.service.image.TokenUsage
import app.service.image.kgse.v12.models.SceneAnalysisResponseV12
import app.service.openai.OpenAiCompatibleClient
import app.data.network.models.openai.OpenAiChatRequest
import app.data.network.models.openai.OpenAiChatMessage
import app.data.network.models.openai.OpenAiResponseFormat
import app.data.network.models.openai.OpenAiImageRequest
import app.plugins.ApplicationJson
import app.utils.logger.LlmLogger
import com.ensodai.shared.avalon.gifts.models.GiftActionType
import com.ensodai.shared.avalon.gifts.models.ImageGenerationConfig
import org.koin.core.annotation.Single
import java.util.Base64

@Single
class OpenRouterImagePipeline(
    private val openAiCompatibleClient: OpenAiCompatibleClient
) : ImagePipelineProvider {

    override suspend fun generate(
        configDto: AICharacterConfigLLMDto,
        giftName: String,
        history: List<DomainMessage>,
        actionType: GiftActionType,
        reqId: String,
        aiSettings: UserAiSettingsResponse?
    ): GeneratedImageData {
        if (aiSettings == null || aiSettings.apiKey.isNullOrBlank()) {
            throw Exception("OpenRouter settings are missing or API key is empty")
        }

        val baseUrl = aiSettings.baseUrl
        val apiKey = aiSettings.apiKey!!
        val promptModel = aiSettings.imagePromptModel
        val imageModel = aiSettings.imageModel

        LlmLogger.inf(reqId, "Using OpenRouterImagePipeline. PromptModel: $promptModel, ImageModel: $imageModel")

        // Step 1: Analyze Scene
        val analysisSystemPrompt = """
            You are an expert AI Scene Director. Analyze the context and return a JSON object EXACTLY matching this structure:
            {
              "scenarioType": "DAILY_CASUAL",
              "renderStyle": "SMARTPHONE_SNAPSHOT",
              "cameraComposition": "FRONT_CAMERA_SELFIE",
              "physicalDistance": "CONVERSATIONAL",
              "characterAppearanceReference": "string",
              "userAppearanceReference": "string",
              "bodyPose": "string",
              "handsAction": "string",
              "facialExpression": "string",
              "sceneAtmosphere": "string",
              "wardrobeState": "string",
              "environmentLighting": "string",
              "otherPresentSubjects": ["string"],
              "activeProp": "string",
              "overrideArtStyle": null
            }
            Use appropriate Enum values for the first 4 fields based on the context.
            Respond ONLY with valid JSON.
        """.trimIndent()

        val userContext = "Character: ${configDto.characterName}\nContext: ${history.takeLast(3).joinToString { it.contents.joinToString(" ") { c -> c.content } }}"

        val analyzeRequest = OpenAiChatRequest(
            model = promptModel,
            messages = listOf(
                OpenAiChatMessage(role = "system", content = analysisSystemPrompt),
                OpenAiChatMessage(role = "user", content = userContext)
            ),
            responseFormat = OpenAiResponseFormat(type = "json_object")
        )

        val analyzeResult = openAiCompatibleClient.generateChatCompletion(baseUrl, apiKey, analyzeRequest, "$reqId-analyze")
        val analyzeText = (analyzeResult as? LlmResult.Success)?.text ?: throw Exception("Failed to analyze scene: $analyzeResult")
        val analyzeTokens = (analyzeResult as? LlmResult.Success)?.tokenStats?.totalTokens ?: 0

        // We decode just to validate, but we can pass the raw text to step 2 directly
        val analysis = ApplicationJson.decodeFromString<SceneAnalysisResponseV12>(analyzeText)

        // Step 2: Render Prompt
        val renderSystemPrompt = """
            You are an expert AI Prompt Generator. Using the scene analysis, return a JSON object EXACTLY matching this structure:
            {
              "imagePrompt": "string (the final prompt for the image generation model, highly detailed)",
              "visualDescription": "string (a descriptive caption of what happens in the image)",
              "era": "string",
              "archetype": "string"
            }
            Respond ONLY with valid JSON.
        """.trimIndent()

        val renderRequest = OpenAiChatRequest(
            model = promptModel,
            messages = listOf(
                OpenAiChatMessage(role = "system", content = renderSystemPrompt),
                OpenAiChatMessage(role = "user", content = "Analysis: $analyzeText")
            ),
            responseFormat = OpenAiResponseFormat(type = "json_object")
        )

        val renderResult = openAiCompatibleClient.generateChatCompletion(baseUrl, apiKey, renderRequest, "$reqId-render")
        val renderText = (renderResult as? LlmResult.Success)?.text ?: throw Exception("Failed to render prompt: $renderResult")
        val renderTokens = (renderResult as? LlmResult.Success)?.tokenStats?.totalTokens ?: 0

        val config = ApplicationJson.decodeFromString<ImageGenerationConfig>(renderText)

        val totalTokenUsage = TokenUsage(
            totalTokens = analyzeTokens + renderTokens,
            promptTokens = 0,
            outputTokens = 0,
            cachedTokens = 0,
            thoughtsTokens = 0
        )

        // Step 3: Generate Image
        val imageRequest = OpenAiImageRequest(
            prompt = config.imagePrompt,
            model = imageModel,
            n = 1,
            responseFormat = "b64_json"
        )

        val imageResult = openAiCompatibleClient.generateImage(baseUrl, apiKey, imageRequest, reqId)
        val imageResponse = imageResult.getOrElse { throw Exception("Image generation failed: ${it.message}", it) }
        
        val base64 = imageResponse.data.firstOrNull()?.b64Json ?: throw Exception("Empty image generated")
        val imageBytes = Base64.getDecoder().decode(base64)

        return GeneratedImageData(
            imageBytes = imageBytes,
            visualDescription = config.visualDescription,
            tokenUsage = totalTokenUsage
        )
    }
}
