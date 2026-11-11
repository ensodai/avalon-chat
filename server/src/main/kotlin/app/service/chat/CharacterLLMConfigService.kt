package app.service.chat

import app.data.db.tables.GlobalSettings
import app.data.db.tables.LlmConfig
import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.users.AppUsers
import app.data.db.tables.users.UserPersonas
import app.data.models.AICharacterConfigLLMDto
import app.data.models.GlobalSettingsLLMDto
import app.data.models.SafetySettingsDto
import app.data.models.UserPersonaLLMDto
import app.utils.dbQuery
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single
import app.data.db.tables.users.UserCharacterRelationships
import app.data.db.tables.aiCharacter.CharacterEvents
import org.jetbrains.exposed.v1.core.SortOrder

import app.data.repository.CharacterSpecRepository
import app.data.repository.ChatRepository

@Single
class CharacterLLMConfigService(
    private val characterSpecRepository: CharacterSpecRepository,
    private val lorebookMatcher: LorebookMatcher,
    private val chatRepository: ChatRepository
) {

    suspend fun get(id: Uuid, llmConfig: LlmConfig): AICharacterConfigLLMDto? {
        val baseConfig = dbQuery {

            val globalRow = GlobalSettings.selectAll().limit(1).singleOrNull()
            if (globalRow == null) return@dbQuery null

            val globalConfigDto = GlobalSettingsLLMDto(
                mainRules = globalRow[GlobalSettings.mainCharacterRules],
                maxOutputTokens = globalRow[GlobalSettings.maxOutputTokens]
            )

            ChatVariants
                .innerJoin(ChatSessions)
                .innerJoin(AICharacters)
                .join(AppUsers, JoinType.INNER, ChatSessions.userId, AppUsers.id)
                .join(UserPersonas, JoinType.LEFT, AppUsers.defaultPersona, UserPersonas.id)
                .select(
                    AICharacters.id,
                    AICharacters.characterName,
                    AICharacters.geminiModel,
                    AICharacters.temperature,
                    AICharacters.interactionFormat,
                    AICharacters.topK,
                    AICharacters.topP,
                    AICharacters.maxOutputTokens,
                    AICharacters.maxContextTokens,
                    AICharacters.shortDescription,
                    AICharacters.fullDescription,
                    AICharacters.firstMessage,
                    AICharacters.harassment,
                    AICharacters.hate,
                    AICharacters.sexuallyExplicit,
                    AICharacters.dangerousContent,

                    ChatSessions.userId,

                    UserPersonas.personaName,
                    UserPersonas.personaDescription
                )
                .where { ChatVariants.id eq id }
                .singleOrNull()
                ?.let { row ->
                    val userPersonaDto = if (row.getOrNull(UserPersonas.personaName) != null) {
                        UserPersonaLLMDto(
                            personaName = row[UserPersonas.personaName],
                            personaDescription = row[UserPersonas.personaDescription]
                        )
                    } else null

                    val characterIdVal = row[AICharacters.id].value
                    val userIdVal = row[ChatSessions.userId].value

                    val relationshipRow = UserCharacterRelationships.select(
                        UserCharacterRelationships.relationshipLevel,
                        UserCharacterRelationships.pendingEventLevel
                    ).where {
                        (UserCharacterRelationships.userId eq userIdVal) and
                        (UserCharacterRelationships.characterId eq characterIdVal)
                    }.singleOrNull()

                    var unlockedTraitsStr: String? = null
                    var pendingDirectiveStr: String? = null

                    if (relationshipRow != null) {
                        val currentLevel = relationshipRow[UserCharacterRelationships.relationshipLevel]
                        val pendingLevel = relationshipRow[UserCharacterRelationships.pendingEventLevel]

                        val events = CharacterEvents
                            .select(
                                CharacterEvents.requiredLevel,
                                CharacterEvents.eventPermanentTrait,
                                CharacterEvents.eventTriggerScenario
                            )
                            .where {
                                (CharacterEvents.characterId eq characterIdVal) and
                                (CharacterEvents.requiredLevel lessEq currentLevel)
                            }
                            .orderBy(CharacterEvents.requiredLevel, SortOrder.ASC)
                            .toList()

                        if (events.isNotEmpty()) {
                            unlockedTraitsStr = events.joinToString("\n\n") { it[CharacterEvents.eventPermanentTrait] }
                        }

                        if (pendingLevel != null) {
                            val triggerEvent = events.find { it[CharacterEvents.requiredLevel] == pendingLevel }
                            if (triggerEvent != null) {
                                pendingDirectiveStr = triggerEvent[CharacterEvents.eventTriggerScenario]
                            }
                        }
                    }

                    AICharacterConfigLLMDto(
                        characterId = row[AICharacters.id].value,
                        userId = row[ChatSessions.userId].value,
                        characterName = row[AICharacters.characterName],
                        llmConfig = llmConfig,
                        temperature = row[AICharacters.temperature],
                        interactionFormat = row[AICharacters.interactionFormat],
                        topK = row[AICharacters.topK],
                        topP = row[AICharacters.topP],
                        maxOutputTokens = row[AICharacters.maxOutputTokens],
                        maxContextTokens = row[AICharacters.maxContextTokens],
                        shortDescription = row[AICharacters.shortDescription],
                        fullDescription = row[AICharacters.fullDescription],
                        firstMessage = row[AICharacters.firstMessage],
                        safetySettings = SafetySettingsDto(
                            harassment = row[AICharacters.harassment],
                            hate = row[AICharacters.hate],
                            sexuallyExplicit = row[AICharacters.sexuallyExplicit],
                            dangerousContent = row[AICharacters.dangerousContent]
                        ),
                        globalSettings = globalConfigDto,
                        userPersona = userPersonaDto,
                        unlockedTraits = unlockedTraitsStr,
                        pendingEventDirective = pendingDirectiveStr
                    )
                }
        } ?: return null

        val characterIdVal = baseConfig.characterId
        val userIdVal = baseConfig.userId
        val promptDetails = characterSpecRepository.getPromptDetails(characterIdVal)
        val activeLorebooks = characterSpecRepository.getActiveLorebooksForUserAndCharacter(
            characterId = characterIdVal,
            userId = userIdVal
        )

        val matchedResult = if (activeLorebooks.isNotEmpty()) {
            val maxScanDepth = activeLorebooks.maxOfOrNull { it.scanDepth } ?: 5
            val history = chatRepository.getHistoryForVariant(variantId = id, limit = maxScanDepth)
            val recentTexts = history.messages.flatMap { it.contents }.map { it.content }
            val lastBotContainerId = chatRepository.getLastActiveBotContainerId(id)
            val previousTimedStates = lastBotContainerId?.let { chatRepository.getLoreTimedStates(it) } ?: emptyMap()
            lorebookMatcher.matchMultiple(activeLorebooks, recentTexts, previousTimedStates)
        } else {
            MatchedLorebookResult()
        }

        return baseConfig.copy(
            scenario = promptDetails?.scenario,
            mesExample = promptDetails?.mesExample,
            customSystemPrompt = promptDetails?.customSystemPrompt,
            postHistoryInstructions = promptDetails?.postHistoryInstructions,
            lorebookBeforeChar = matchedResult.beforeChar,
            lorebookAfterChar = matchedResult.afterChar,
            lorebookBeforeDesc = matchedResult.beforeDesc,
            lorebookAfterDesc = matchedResult.afterDesc,
            lorebookPersonality = matchedResult.personality,
            lorebookScenario = matchedResult.scenario,
            lorebookAtDepth = matchedResult.atDepth,
            nextLoreTimedStates = matchedResult.nextTimedStates
        )
    }
}