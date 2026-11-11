package app.data.models

import app.data.db.tables.LlmConfig
import app.service.lorebook.model.EntryTimedState
import app.service.lorebook.model.LorebookDepthEntry
import com.ensodai.shared.avalon.character.models.InteractionFormat
import kotlin.uuid.Uuid

data class AICharacterConfigLLMDto(
    val characterId: Uuid,
    val userId: Uuid,
    val characterName: String,
    val llmConfig: LlmConfig,
    val temperature: Float,
    val interactionFormat: InteractionFormat,
    val topK: Int?,
    val topP: Float,
    val maxOutputTokens: Int,
    val maxContextTokens: Int,
    val shortDescription: String,
    val fullDescription: String?,
    val firstMessage: String?,
    val safetySettings: SafetySettingsDto,
    val globalSettings: GlobalSettingsLLMDto,
    val userPersona: UserPersonaLLMDto?,
    val conversationSummary: String? = null,
    val unlockedTraits: String? = null,
    val pendingEventDirective: String? = null,
    val scenario: String? = null,
    val mesExample: String? = null,
    val customSystemPrompt: String? = null,
    val postHistoryInstructions: String? = null,
    val lorebookBeforeChar: List<String> = emptyList(),
    val lorebookAfterChar: List<String> = emptyList(),
    val lorebookBeforeDesc: List<String> = emptyList(),
    val lorebookAfterDesc: List<String> = emptyList(),
    val lorebookPersonality: List<String> = emptyList(),
    val lorebookScenario: List<String> = emptyList(),
    val lorebookAtDepth: List<LorebookDepthEntry> = emptyList(),
    val nextLoreTimedStates: Map<Uuid, EntryTimedState> = emptyMap()
)

data class GlobalSettingsLLMDto(
    val mainRules: String,
    val maxOutputTokens: Int
)

data class UserPersonaLLMDto(
    val personaName: String,
    val personaDescription: String
)

data class SafetySettingsDto(
    val harassment: Int,
    val hate: Int,
    val sexuallyExplicit: Int,
    val dangerousContent: Int
)