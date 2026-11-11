package app.service.image.kgse.v12.pipeline

import app.data.models.AICharacterConfigLLMDto
import app.domain.models.DomainMessage
import app.plugins.ApplicationJson
import app.service.image.TokenUsage
import app.service.image.kgse.v12.models.SceneAnalysisResponseV12
import app.utils.logger.LlmLogger
import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Part
import com.google.genai.types.Schema
import com.google.genai.types.Type
import app.utils.orNullOpt
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.gifts.models.GiftActionType
import com.google.genai.types.GenerateContentResponse

class KgseAnalyzerV12(private val client: Client) {

    private val analyzerSchema = Schema.builder()
        .type(Type.Known.OBJECT)
        .properties(
            mapOf(
                "scenarioType" to Schema.builder().type(Type.Known.STRING)
                    .description("DAILY_CASUAL, CONVERSATIONAL, DRAMATIC_EMOTIONAL, RPG_ADVENTURE, or SCI_FI_CYBERPUNK")
                    .enum_(listOf("DAILY_CASUAL", "CONVERSATIONAL", "DRAMATIC_EMOTIONAL", "RPG_ADVENTURE", "SCI_FI_CYBERPUNK"))
                    .build(),
                "renderStyle" to Schema.builder().type(Type.Known.STRING)
                    .description("SMARTPHONE_SNAPSHOT, FANTASY_ARTWORK, or CYBERPUNK_SCIFI_ARTWORK")
                    .enum_(listOf("SMARTPHONE_SNAPSHOT", "FANTASY_ARTWORK", "CYBERPUNK_SCIFI_ARTWORK"))
                    .build(),
                "cameraComposition" to Schema.builder().type(Type.Known.STRING)
                    .description("FRONT_CAMERA_SELFIE, MIRROR_SELFIE, POV_CLOSE_UP, POV_MEDIUM_SHOT, or OBSERVER_MEDIUM_SHOT")
                    .enum_(listOf("FRONT_CAMERA_SELFIE", "MIRROR_SELFIE", "POV_CLOSE_UP", "POV_MEDIUM_SHOT", "OBSERVER_MEDIUM_SHOT"))
                    .build(),
                "physicalDistance" to Schema.builder().type(Type.Known.STRING)
                    .description("CLOSE_PROXIMITY, CONVERSATIONAL, MEDIUM_DISTANCE, or WIDE_ENVIRONMENT")
                    .enum_(listOf("CLOSE_PROXIMITY", "CONVERSATIONAL", "MEDIUM_DISTANCE", "WIDE_ENVIRONMENT"))
                    .build(),
                "characterAppearanceReference" to Schema.builder().type(Type.Known.STRING)
                    .description("Concise physical summary of the main character (hair color, hairstyle, eye color, facial features, distinctive build or clothing) extracted from lore.")
                    .build(),
                "userAppearanceReference" to Schema.builder().type(Type.Known.STRING)
                    .description("Concise physical summary of the user (e.g. 'Female, short dark hair, glasses') extracted from their user persona description. Write 'Unknown' if not specified.")
                    .build(),
                "bodyPose" to Schema.builder().type(Type.Known.STRING)
                    .description("Dynamic physical pose of the character, naturally matching the active scene log. E.g. 'leaning over the table', 'lying on side', 'standing tall'.")
                    .build(),
                "handsAction" to Schema.builder().type(Type.Known.STRING)
                    .description("EXPLICIT, SIMPLE anatomical action for both hands. E.g. 'Arms are crossed over her chest' or 'Right hand holds cup, left hand rests on the table'. Do not overcomplicate to avoid mutations. Write 'Hands are out of frame' for close-up shots.")
                    .build(),
                "facialExpression" to Schema.builder().type(Type.Known.STRING)
                    .description("Eyes, lips, and facial expression matching the character's emotion in this scene.")
                    .build(),
                "sceneAtmosphere" to Schema.builder().type(Type.Known.STRING)
                    .description("Cinematic mood, color palette, and atmospheric tone of the moment (e.g., 'warm engaging sunset glow, contemplative silence, crisp modern ambient lighting').")
                    .build(),
                "wardrobeState" to Schema.builder().type(Type.Known.STRING)
                    .description("Detailed description of their current clothes, fit, and texture appropriate to the setting.")
                    .build(),
                "environmentLighting" to Schema.builder().type(Type.Known.STRING)
                    .description("Detailed description of the location, materials, and diegetic light sources. E.g. 'cozy candlelit wooden tavern with flickering shadows' or 'bright modern cafe interior with sunlight streaming through large windows'.")
                    .build(),
                "otherPresentSubjects" to Schema.builder().type(Type.Known.ARRAY)
                    .items(Schema.builder().type(Type.Known.STRING).build())
                    .description("List of names of other characters physically present in the room/scene (excluding the main character and user).")
                    .build(),
                "activeProp" to Schema.builder().type(Type.Known.STRING)
                    .description("Name of the active item they are holding or interacting with (e.g. 'wooden mug', 'glasses', 'none').")
                    .build(),
                "overrideArtStyle" to Schema.builder().type(Type.Known.STRING)
                    .description("If the character lore explicitly dictates a specific art style (e.g. 'anime style', 'cartoon', '2D', '3D render', 'photorealistic'), extract it here. Otherwise, write 'None'.")
                    .build()
            )
        ).required(
            listOf(
                "scenarioType", "renderStyle", "cameraComposition", "physicalDistance",
                "characterAppearanceReference", "userAppearanceReference", 
                "bodyPose", "handsAction", "facialExpression", "sceneAtmosphere", 
                "wardrobeState", "environmentLighting", "otherPresentSubjects", "activeProp", "overrideArtStyle"
            )
        ).build()

    fun analyzeScene(
        dto: AICharacterConfigLLMDto,
        history: List<DomainMessage>,
        actionType: GiftActionType
    ): Pair<SceneAnalysisResponseV12, TokenUsage> {
        val pastHistory = if (history.size > 1) history.dropLast(1) else emptyList()
        val targetMessage = history.lastOrNull()
        
        val pastHistoryLog = prepareHistoryLog(pastHistory)
        val targetSceneLog = targetMessage?.let { prepareHistoryLog(listOf(it)) } ?: "No target scene"

        val isSelfie = actionType == GiftActionType.CAMERA_SELFIE
        val userPersonaInfo = dto.userPersona?.let { "User Persona: ${it.personaName} - ${it.personaDescription}" } ?: "Unknown user."

        val contextXmlForAnalyzer = """
            <input_data>
                <character_profile>
                    Name: ${dto.characterName}
                    Lore: ${dto.fullDescription}
                </character_profile>
                <user_context>$userPersonaInfo</user_context>
                <past_context>
$pastHistoryLog
                </past_context>
                <target_scene>
$targetSceneLog
                </target_scene>
                <camera_mode>${if (isSelfie) "SELFIE (One arm is holding the phone)" else "NON-SELFIE PHOTO (Both hands free)"}</camera_mode>
            </input_data>
        """.trimIndent()

        val systemInstructionText = """
            You are the KGSE (Kinematic Game State Engine) Cinematographer and Scene Director (v12).
            Your task is to analyze the chat history, user persona, and character profile to extract structural cinematography parameters for rendering an authentic scene frame.

            CRITICAL DIRECTIVE 0: TARGET SCENE FOCUS:
            Base the visual actions, poses, expressions, and framing primarily on the `<target_scene>`. The `<past_context>` provides background context, tone, and continuity.

            CRITICAL DIRECTIVE 1: SCENARIO & RENDER STYLE:
            - OVERRIDE ART STYLE: If `<character_profile>` explicitly dictates an art style (e.g., 'anime style', 'comic book', '3D render', 'pixel art', 'photorealistic'), output it in `overrideArtStyle`. Otherwise, output 'None'.
            - If the active scene features sci-fi, cyberpunk, futuristic tech, or neon aesthetics -> Choose scenarioType = "SCI_FI_CYBERPUNK" and renderStyle = "CYBERPUNK_SCIFI_ARTWORK".
            - If the active scene features fantasy, medieval setting, magic, swords, or taverns -> Choose scenarioType = "RPG_ADVENTURE" and renderStyle = "FANTASY_ARTWORK".
            - If the scene is an intense dramatic conversation, conflict, or high emotional confrontation -> Choose scenarioType = "DRAMATIC_EMOTIONAL".
            - If the scene is a warm, engaging personal discussion or friendly talk -> Choose scenarioType = "CONVERSATIONAL".
            - If the scene is casual everyday life -> Choose scenarioType = "DAILY_CASUAL" and renderStyle = "SMARTPHONE_SNAPSHOT".

            CRITICAL DIRECTIVE 2: CAMERA FRAMING & DISTANCE:
            - Choose cameraComposition according to camera_mode and scene focus:
              - FRONT_CAMERA_SELFIE: Handheld front camera shot.
              - MIRROR_SELFIE: Reflection in a mirror with smartphone visible.
              - POV_CLOSE_UP: First-person close portrait, direct eye contact.
              - POV_MEDIUM_SHOT: First-person medium shot showing arms/torso and environment.
              - OBSERVER_MEDIUM_SHOT: Candid third-person view of the character in context.
            - Choose physicalDistance:
              - CLOSE_PROXIMITY: Tight framing (face and shoulders).
              - CONVERSATIONAL: Waist-up portrait (1-2 meters).
              - MEDIUM_DISTANCE: Torso and surroundings (2-3 meters).
              - WIDE_ENVIRONMENT: Full-body scale showing the character in landscape or room.

            CRITICAL DIRECTIVE 3: ANATOMICAL REALISM FOR HANDS (NO MUTATIONS):
            Describe exactly what both hands are doing in `handsAction` based on physical feasibility:
            - Keep it simple, natural, and unambiguous. Never describe impossible or overlapping actions.
            - If `<target_scene>` specifies an arm pose (e.g., 'arms crossed', 'hand in pocket', 'resting on table'), output that exact pose.
            - If there is an `activeProp`, specify which hand holds it and what the other hand does.
            - If camera_mode is SELFIE: One arm extends forward towards the camera lens (forearm disappearing off-frame), while the other arm is in a relaxed posture. Never describe a floating camera.
            - If POV_CLOSE_UP: Set `handsAction` to 'Hands are out of frame'.
            - Strictly ensure exactly two arms are accounted for.

            CRITICAL DIRECTIVE 4: SCENE ATMOSPHERE & LIGHTING:
            - Describe the environmental mood, cinematic tone, and ambient lighting in `sceneAtmosphere` and `environmentLighting`.
            - Focus on diegetic light sources (e.g. golden hour sunlight, soft cafe interior, candlelit desk, neon signage) and natural color grading.

            CRITICAL DIRECTIVE 5: NATURAL FACIAL EXPRESSIONS:
            - Keep expressions subtle, natural, and grounded in the scene's emotional context (e.g., 'warm engaging smile', 'thoughtful contemplative gaze', 'composed neutral expression', 'quiet focus').
            - Avoid exaggerated or cartoonish emotions.

            CRITICAL DIRECTIVE 6: CHARACTER & ATTIRE FIDELITY:
            - In `characterAppearanceReference`, extract concise visual features (hair, eyes, face structure, distinctive build) directly from lore.
            - In `wardrobeState`, describe current attire with realistic fabric textures, natural drape, and tasteful styling appropriate to the setting.
        """.trimIndent()

        val response = client.models.generateContent(
            "gemini-2.5-flash-lite",
            listOf(Content.builder().role("user").parts(listOf(Part.fromText(contextXmlForAnalyzer))).build()),
            GenerateContentConfig.builder()
                .systemInstruction(Content.builder().parts(listOf(Part.fromText(systemInstructionText))).build())
                .responseMimeType("application/json")
                .responseSchema(analyzerSchema)
                .temperature(0.1f)
                .build()
        )

        val analyzerResponseText = response.text() ?: throw IllegalStateException("Analyzer returned empty")
        val tokenUsage = extractTokenUsage(response)

        val analysis = ApplicationJson.decodeFromString<SceneAnalysisResponseV12>(cleanJsonString(analyzerResponseText))
        LlmLogger.inf("KGSE v12 Scene analyzed: scenario=${analysis.scenarioType}, style=${analysis.renderStyle}")
        return analysis to tokenUsage
    }

    private fun extractTokenUsage(response: GenerateContentResponse): TokenUsage {
        val metadata = response.usageMetadata().orNullOpt() ?: return TokenUsage.ZERO
        val totalTokens = metadata.totalTokenCount()?.orNullOpt()?.toLong() ?: 0L
        val promptTokens = metadata.promptTokenCount()?.orNullOpt()?.toLong() ?: 0L
        val outputTokens = metadata.candidatesTokenCount()?.orNullOpt()?.toLong() ?: 0L
        val cachedTokens = metadata.cachedContentTokenCount()?.orNullOpt()?.toLong() ?: 0L
        val thoughtsTokens = metadata.thoughtsTokenCount()?.orNullOpt()?.toLong() ?: 0L

        return TokenUsage(
            totalTokens = totalTokens,
            promptTokens = promptTokens,
            outputTokens = outputTokens,
            cachedTokens = cachedTokens,
            thoughtsTokens = thoughtsTokens
        )
    }

    private fun prepareHistoryLog(history: List<DomainMessage>): String {
        return history.takeLast(50).mapNotNull { msg ->
            val role = if (msg.role == MessageRole.USER) "User" else "Character"
            val text = msg.contents.mapNotNull { part ->
                if (part.type == AvalonMessageContentType.TEXT) part.content else null
            }.joinToString(" ")
            if (text.isNotBlank()) "<$role>: $text" else null
        }.joinToString("\n")
    }

    private fun cleanJsonString(raw: String): String {
        return raw.replace(Regex("```(?:json)?"), "").trim()
    }
}
