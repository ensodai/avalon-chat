package app.service.image.kgse.v12.pipeline

import app.data.models.AICharacterConfigLLMDto
import app.plugins.ApplicationJson
import app.service.image.TokenUsage
import app.service.image.kgse.v12.models.CameraCompositionV12
import app.service.image.kgse.v12.models.PhysicalDistanceV12
import app.service.image.kgse.v12.models.PromptGenerationResponseV12
import app.service.image.kgse.v12.models.SceneAnalysisResponseV12
import app.service.image.kgse.v12.models.ScenarioTypeV12
import app.service.image.kgse.v12.models.VisualRenderStyleV12
import app.utils.logger.LlmLogger
import app.utils.orNullOpt
import com.ensodai.shared.avalon.gifts.models.GiftActionType
import com.ensodai.shared.avalon.gifts.models.ImageGenerationConfig
import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Part
import com.google.genai.types.Schema
import com.google.genai.types.Type
import com.google.genai.types.GenerateContentResponse

class PromptRendererV12(private val client: Client) {

    private val generatorSchema = Schema.builder()
        .type(Type.Known.OBJECT)
        .properties(
            mapOf(
                "subjectDescription" to Schema.builder().type(Type.Known.STRING)
                    .description("Detailed description of the characters (hair, features, wardrobe, and fit) based on characterAppearanceReference and wardrobeState.").build(),
                "environment" to Schema.builder().type(Type.Known.STRING)
                    .description("Lighting, shadows, and location setting details from environmentLighting.").build(),
                "kinematics" to Schema.builder().type(Type.Known.STRING)
                    .description("Physical pose, hand coordinates, facial expression, and gaze focus from bodyPose, handsAction, and facialExpression. Ensure hand actions are simple, singular, and physically possible for exactly two hands. Do NOT stack multiple conflicting hand poses. No mental states.")
                    .build(),
                "vibeAndAtmosphere" to Schema.builder().type(Type.Known.STRING)
                    .description("Cinematic mood, color grading, and ambient tone from sceneAtmosphere.")
                    .build(),
                "visualDescription" to Schema.builder().type(Type.Known.STRING)
                    .description("Short, clean description of the image content for the chat UI.").build()
            )
        ).required(listOf("subjectDescription", "environment", "kinematics", "vibeAndAtmosphere", "visualDescription"))
        .build()

    fun renderPrompt(
        dto: AICharacterConfigLLMDto,
        analysis: SceneAnalysisResponseV12,
        actionType: GiftActionType
    ): Pair<ImageGenerationConfig, TokenUsage> {
        val effectiveCameraComposition = if (actionType == GiftActionType.CAMERA_SELFIE) {
            if (analysis.cameraComposition == CameraCompositionV12.MIRROR_SELFIE) {
                CameraCompositionV12.MIRROR_SELFIE
            } else {
                CameraCompositionV12.FRONT_CAMERA_SELFIE
            }
        } else {
            analysis.cameraComposition
        }

        val dynamicSystemPrompt = buildSystemPrompt(analysis, effectiveCameraComposition)

        val contextXmlForGenerator = """
            <kgse_render_pipeline_v12>
                <character_default_appearance>
                    Name: ${dto.characterName}
                    Appearance Reference: ${analysis.characterAppearanceReference}
                    User Reference: ${analysis.userAppearanceReference}
                </character_default_appearance>
                <evaluated_state>
                    <scenario_type>${analysis.scenarioType.name}</scenario_type>
                    <render_style>${analysis.renderStyle.name}</render_style>
                    <camera_composition>${effectiveCameraComposition.name}</camera_composition>
                    <physical_distance>${analysis.physicalDistance.name}</physical_distance>
                    <pose>${analysis.bodyPose}</pose>
                    <hands_action>${analysis.handsAction}</hands_action>
                    <expression>${analysis.facialExpression}</expression>
                    <atmosphere>${analysis.sceneAtmosphere}</atmosphere>
                    <wardrobe>${analysis.wardrobeState}</wardrobe>
                    <environment_lighting>${analysis.environmentLighting}</environment_lighting>
                    <other_subjects>${analysis.otherPresentSubjects.joinToString(", ")}</other_subjects>
                    <prop>${if (analysis.activeProp.equals("None", true) || analysis.activeProp.equals("N/A", true)) "Ignore this" else analysis.activeProp}</prop>
                    <override_art_style>${analysis.overrideArtStyle ?: "None"}</override_art_style>
                </evaluated_state>
            </kgse_render_pipeline_v12>
        """.trimIndent()

        val response = client.models.generateContent(
            "gemini-2.5-flash-lite",
            listOf(Content.builder().role("user").parts(listOf(Part.fromText("Render the frame description based on the evaluated state.\n\n$contextXmlForGenerator"))).build()),
            GenerateContentConfig.builder()
                .systemInstruction(Content.builder().parts(listOf(Part.fromText(dynamicSystemPrompt))).build())
                .responseMimeType("application/json")
                .responseSchema(generatorSchema)
                .temperature(0.6f)
                .build()
        )

        val generatorResponseText = response.text() ?: throw IllegalStateException("Generator returned empty")
        val tokenUsage = extractTokenUsage(response)

        val result = ApplicationJson.decodeFromString<PromptGenerationResponseV12>(cleanJsonString(generatorResponseText))

        val stylePrefix = getStylePrefix(analysis, effectiveCameraComposition)
        val overridePrefix = analysis.overrideArtStyle?.takeIf { !it.equals("None", ignoreCase = true) && it.isNotBlank() }?.let { "[$it style] " } ?: ""
        val finalImagePrompt = "$overridePrefix$stylePrefix ${result.subjectDescription.trim()} ${result.kinematics.trim()} ${result.environment.trim()} The atmosphere is ${result.vibeAndAtmosphere.trim()}"

        LlmLogger.inf("KGSE v12 Prompt rendered successfully (length: ${finalImagePrompt.length} chars)")

        return ImageGenerationConfig(
            imagePrompt = finalImagePrompt.trim(),
            visualDescription = result.visualDescription,
            era = if (analysis.renderStyle == VisualRenderStyleV12.FANTASY_ARTWORK) "FANTASY_OR_HISTORICAL" else "MODERN_OR_SCIFI",
            archetype = analysis.scenarioType.name
        ) to tokenUsage
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

    private fun getStylePrefix(analysis: SceneAnalysisResponseV12, cameraComposition: CameraCompositionV12): String {
        val isMulti = analysis.otherPresentSubjects.isNotEmpty()
        return when (analysis.renderStyle) {
            VisualRenderStyleV12.FANTASY_ARTWORK -> {
                if (isMulti) {
                    "A highly detailed digital fantasy art illustration of an authentic scene showing multiple characters. Detailed background, dramatic fantasy lighting, painterly style. "
                } else {
                    "A highly detailed digital fantasy art character illustration, painterly style, rich colors, dramatic lighting, detailed background. "
                }
            }
            VisualRenderStyleV12.CYBERPUNK_SCIFI_ARTWORK -> {
                if (isMulti) {
                    "A highly detailed sci-fi cyberpunk illustration of an authentic scene showing multiple characters. Detailed futuristic background, neon lighting, cinematic aesthetic. "
                } else {
                    "A highly detailed sci-fi cyberpunk character illustration, neon lighting, metallic and holographic textures, futuristic detailed background. "
                }
            }
            VisualRenderStyleV12.SMARTPHONE_SNAPSHOT -> {
                when (cameraComposition) {
                    CameraCompositionV12.FRONT_CAMERA_SELFIE -> {
                        if (isMulti) "A casual, unposed group selfie taken on an iPhone, wide-angle lens. Candid smartphone photo, natural lighting, raw slice-of-life snap. "
                        else "A casual, unposed selfie taken on an iPhone front camera. Candid smartphone snapshot, natural raw lighting, everyday slice-of-life. "
                    }
                    CameraCompositionV12.MIRROR_SELFIE -> {
                        "A casual, unposed mirror selfie taken on an iPhone. Raw smartphone photo of a reflection in a mirror, everyday slice-of-life snapshot. "
                    }
                    CameraCompositionV12.POV_CLOSE_UP, CameraCompositionV12.POV_MEDIUM_SHOT -> {
                        "First-person perspective (POV) unposed snapshot, captured from the viewer's own eyes. Natural and authentic realism, raw ambient lighting. "
                    }
                    CameraCompositionV12.OBSERVER_MEDIUM_SHOT -> {
                        if (isMulti) "Candid documentary-style snapshot taken by an observer from a natural distance, wide-angle medium shot, characters visible in the environment, raw ambient lighting. "
                        else "Candid documentary-style smartphone snapshot taken by an observer from a natural distance, medium shot, raw ambient lighting. "
                    }
                }
            }
        }
    }

    private fun buildSystemPrompt(analysis: SceneAnalysisResponseV12, cameraComposition: CameraCompositionV12): String {
        val sb = StringBuilder()

        sb.append("<role>You are the KGSE (Kinematic Game State Engine) Renderer (v12). Your job is to convert structured scene evaluation parameters into a beautiful, cohesive, cinematic prompt for an image generator.</role>\n")

        sb.append("<render_style_protocol>\n")
        val overrideStyle = analysis.overrideArtStyle?.takeIf { !it.equals("None", ignoreCase = true) && it.isNotBlank() }
        if (overrideStyle != null) {
            sb.append("CRITICAL: The art style is: '$overrideStyle'. Adapt the visual descriptions to match this specific art style perfectly (e.g. if 'anime', describe anime features; if 'cartoon', describe cartoon aesthetics).\n")
        } else {
            when (analysis.renderStyle) {
                VisualRenderStyleV12.FANTASY_ARTWORK -> {
                    sb.append("CRITICAL: The style is FANTASY_ARTWORK. Generate descriptions tailored for a digital fantasy illustration or concept art. Focus on painterly textures, dramatic lighting, deep shadows, and rich atmospheric colors. The frame must look like high-quality digital fantasy art.\n")
                }
                VisualRenderStyleV12.CYBERPUNK_SCIFI_ARTWORK -> {
                    sb.append("CRITICAL: The style is CYBERPUNK_SCIFI_ARTWORK. Generate descriptions tailored for a sci-fi, cyberpunk, or futuristic illustration. Focus on neon lights, metallic surfaces, high-tech clothing, holographic elements, and moody atmospheric lighting. The frame must look like high-quality sci-fi art.\n")
                }
                VisualRenderStyleV12.SMARTPHONE_SNAPSHOT -> {
                    sb.append("CRITICAL: The style is SMARTPHONE_SNAPSHOT. Enforce raw, unpolished realism. The subject must look like an authentic, candid snapshot taken on a modern smartphone camera. Ambient light, natural framing, everyday realism.\n")
                }
            }
        }
        sb.append("</render_style_protocol>\n")

        sb.append("<scenario_type_protocol>\n")
        when (analysis.scenarioType) {
            ScenarioTypeV12.DAILY_CASUAL -> {
                sb.append("CRITICAL: The scenario is DAILY_CASUAL. Focus on natural, everyday realism and casual body language. The environment and interactions should feel cozy, authentic, and ordinary.\n")
            }
            ScenarioTypeV12.CONVERSATIONAL -> {
                sb.append("CRITICAL: The scenario is CONVERSATIONAL. Focus on warm engaging presence, approachable posture, thoughtful listening, and friendly connection.\n")
            }
            ScenarioTypeV12.DRAMATIC_EMOTIONAL -> {
                sb.append("CRITICAL: The scenario is DRAMATIC_EMOTIONAL. Focus on high cinematic depth, expressive and compelling gaze, and dramatic atmospheric lighting (e.g. moody shadows, warm ambient backlight, or cinematic chiaroscuro). Convey strong storytelling and emotional resonance through natural posture, thoughtful expression, and elegant styling.\n")
            }
            ScenarioTypeV12.RPG_ADVENTURE -> {
                sb.append("CRITICAL: The scenario is RPG_ADVENTURE. Enhance the prompt with adventure or fantasy motifs. Incorporate medieval/fantasy armor, gear details, authentic textures (rough leather, weathered iron, dark wood), and dramatic fantasy lighting (torches, campfire, glowing ruins) to fit the RPG setting.\n")
            }
            ScenarioTypeV12.SCI_FI_CYBERPUNK -> {
                sb.append("CRITICAL: The scenario is SCI_FI_CYBERPUNK. Enhance the prompt with futuristic, sci-fi, or cyberpunk motifs. Incorporate high-tech gear, cybernetic details, authentic textures (metal, synthetic fabric, neon glow), and dramatic lighting to fit the sci-fi setting.\n")
            }
        }
        sb.append("</scenario_type_protocol>\n")

        sb.append("<physical_distance_protocol>\n")
        when (analysis.physicalDistance) {
            PhysicalDistanceV12.CLOSE_PROXIMITY -> {
                sb.append("CRITICAL: The physical distance is CLOSE_PROXIMITY (tight framing, under 1 meter). Focus closely on facial details, eye expression, and shoulder line.\n")
            }
            PhysicalDistanceV12.CONVERSATIONAL -> {
                sb.append("CRITICAL: The physical distance is CONVERSATIONAL (1-2 meters). Capture a standard medium shot from the waist up with natural gestures and surrounding immediate environment.\n")
            }
            PhysicalDistanceV12.MEDIUM_DISTANCE -> {
                sb.append("CRITICAL: The physical distance is MEDIUM_DISTANCE (2-3 meters). Show the character integrated comfortably within the room or environment.\n")
            }
            PhysicalDistanceV12.WIDE_ENVIRONMENT -> {
                sb.append("CRITICAL: The physical distance is WIDE_ENVIRONMENT. Show the character at full-body scale, giving significant attention to the surrounding landscape, architecture, or room layout.\n")
            }
        }
        sb.append("</physical_distance_protocol>\n")

        sb.append("<camera_and_anatomy_protocol>\n")
        val isMulti = analysis.otherPresentSubjects.isNotEmpty()
        sb.append("CRITICAL NAME PROTOCOL: NEVER use specific character names in your generated prompt. Use descriptive generic nouns like 'A woman', 'A man', 'The character'.\n")
        if (isMulti) {
            sb.append("CRITICAL MULTI-SUBJECT BINDING: Since there are multiple characters in the scene, isolate their descriptions to prevent feature bleeding. Describe each character individually: [The woman/man] + [appearance details] + [clothing] + [exact action].\n")
        }
        sb.append("CRITICAL ANATOMY RULE (NO MUTATIONS): You must rigorously ensure ONLY TWO arms/hands are described per character. Never write descriptions that imply extra limbs. If holding an item, explicitly mention which hand holds it.\n")
        when (cameraComposition) {
            CameraCompositionV12.FRONT_CAMERA_SELFIE -> {
                sb.append("CRITICAL SELFIE GEOMETRY: The character is taking a handheld selfie. One arm is extended forward towards the camera lens, with the forearm and hand disappearing off-frame at the bottom corner. Never mention a floating phone device in their hand. The other arm is in a relaxed, natural posture.\n")
            }
            CameraCompositionV12.MIRROR_SELFIE -> {
                sb.append("CRITICAL MIRROR SELFIE: The character is taking a mirror selfie. The smartphone is visible in one hand, capturing their reflection in a mirror. The other hand is relaxed.\n")
            }
            CameraCompositionV12.POV_CLOSE_UP, CameraCompositionV12.POV_MEDIUM_SHOT -> {
                sb.append("CRITICAL POV SHOT: The shot is from the first-person perspective of the viewer. The character makes direct, natural eye contact. Ensure hands rest on the environment or the character's own posture, not reaching unnaturally into the camera.\n")
            }
            CameraCompositionV12.OBSERVER_MEDIUM_SHOT -> {
                sb.append("CRITICAL OBSERVER SHOT: A candid shot showing the character and surroundings from a natural distance. Natural body language and interaction with the scene.\n")
            }
        }
        sb.append("</camera_and_anatomy_protocol>\n")
        
        if (!isMulti) {
            sb.append("<ANTI_PHANTOM_LIMB_PROTOCOL>\n")
            sb.append("CRITICAL: This is a SOLO character shot. Describe actions relative only to the character's own posture or the visible environment (e.g. 'resting hand on the table', 'hand in pocket'). Do not describe phantom interactions with unseen people.\n")
            sb.append("</ANTI_PHANTOM_LIMB_PROTOCOL>\n")
        }

        sb.append("<safety_and_standards_protocol>\n")
        sb.append("1. TASTEFUL ARTISTIC STANDARDS: Generate exclusively tasteful, safe-for-work (SFW) imagery suitable for general audiences. Focus on elegant, stylish fashion, expressive character acting, and cinematic atmosphere.\n")
        sb.append("2. ADULT MATURE CHARACTERS: All characters depicted must appear as fully developed mature adults (20s or older) with mature facial features.\n")
        sb.append("3. NATURAL CANDID BEAUTY: Avoid generic plastic doll aesthetics, heavy artificial CGI, or excessive airbrushing. Emphasize natural, believable human traits and authentic lighting:\n")
        sb.append("   - Skin: Natural skin textures under ambient lighting.\n")
        sb.append("   - Hair: Naturally styled with authentic movement and texture.\n")
        sb.append("   - Posture: Relaxed, believable body language.\n")
        sb.append("   - Expression: Subtle, genuine facial acting grounded in the scene.\n")
        sb.append("</safety_and_standards_protocol>\n")

        return sb.toString()
    }

    private fun cleanJsonString(raw: String): String {
        return raw.replace(Regex("```(?:json)?"), "").trim()
    }
}
