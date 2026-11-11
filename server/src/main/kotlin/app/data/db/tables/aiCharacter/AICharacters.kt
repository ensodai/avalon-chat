package app.data.db.tables.aiCharacter

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.images.*
import app.data.db.tables.users.AppUser
import app.data.db.tables.users.AppUsers
import com.ensodai.shared.avalon.character.models.*
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

object AICharacters : BaseUuidTable("ai_characters") {
    val owner = reference("owner", AppUsers, onDelete = ReferenceOption.CASCADE)
    val geminiModel = varchar("gemini_model", 100)
    val characterName = varchar("character_name", 100)
    val fullDescription = text("full_description").nullable()
    val shortDescription = varchar("short_description", 255)
    val temperature = float("temperature")
    val interactionFormat = enumerationByName("interaction_format", 50, InteractionFormat::class).default(InteractionFormat.LITERARY_RPG)
    val harassment = integer("harassment")
    val hate = integer("hate")
    val sexuallyExplicit = integer("sexually_explicit")
    val dangerousContent = integer("dangerous_content")
    val topK = integer("top_k").nullable()
    val topP = float("top_p")
    val maxOutputTokens = integer("max_output_tokens")
    val maxContextTokens = integer("max_context_tokens")
    val visibility = enumerationByName("visibility", 20, CharacterVisibility::class)
    val isPublicFullDescription = bool("is_public_full_description")
    val isBanned = bool("is_banned").default(false)
    val category = varchar("category", 100)
    val firstMessage = text("first_message").nullable()
    val unclaimedEnergy = double("unclaimed_energy").default(0.0)
    val totalEarnedEnergy = double("total_earned_energy").default(0.0)
    // Включена ли функция первого AI-контакта (рассылка приветствий новым юзерам)
    val isOutreachEnabled = bool("is_outreach_enabled").default(false)
    val voice = enumerationByName("voice", 50, VoiceModel::class).default(VoiceModel.ZEPHYR)
    
    // Количество отправленных приветственных сообщений
    val outreachSentCount = integer("outreach_sent_count").default(0)
    
    // Количество успешных контактов (когда пользователи ответили взаимностью)
    val outreachRepliedCount = integer("outreach_replied_count").default(0)
    
    // Счетчик подряд идущих отказов (сбрасывается при ответе, растет при "Нет" или игноре)
    val consecutiveFailuresCount = integer("consecutive_failures_count").default(0)
    
    // Заблокирован ли аутрич у этого бота системой защиты от спама (при превышении лимита отказов)
    val isOutreachBlocked = bool("is_outreach_blocked").default(false)

    init {
        // 1. Для профиля "Мои персонажи"
        index("idx_ai_characters_owner", isUnique = false, owner, updatedAt)

        // 2. Для экрана "Новинки"
        index("idx_ai_characters_new", isUnique = false, visibility, createdAt)

        // 3. Для фильтра по категориям
        index("idx_ai_characters_category", isUnique = false, visibility, category)
    }
}

class AICharacter(id: EntityID<Uuid>) : BaseUuidEntity(id, AICharacters) {
    companion object : BaseUuidEntityClass<AICharacter>(AICharacters)

    var owner: EntityID<Uuid> by AICharacters.owner
    var geminiModel: String by AICharacters.geminiModel
    var characterName: String by AICharacters.characterName
    var fullDescription: String? by AICharacters.fullDescription
    var shortDescription: String by AICharacters.shortDescription
    var temperature: Float by AICharacters.temperature
    var interactionFormat: InteractionFormat by AICharacters.interactionFormat
    var harassment: Int by AICharacters.harassment
    var hate: Int by AICharacters.hate
    var sexuallyExplicit: Int by AICharacters.sexuallyExplicit
    var dangerousContent: Int by AICharacters.dangerousContent
    var topK: Int? by AICharacters.topK
    var topP: Float by AICharacters.topP
    var maxOutputTokens: Int by AICharacters.maxOutputTokens
    var maxContextTokens: Int by AICharacters.maxContextTokens
    var visibility: CharacterVisibility by AICharacters.visibility
    var isPublicFullDescription: Boolean by AICharacters.isPublicFullDescription
    var isBanned: Boolean by AICharacters.isBanned
    var category: String by AICharacters.category
    var firstMessage: String? by AICharacters.firstMessage
    var unclaimedEnergy: Double by AICharacters.unclaimedEnergy
    var totalEarnedEnergy: Double by AICharacters.totalEarnedEnergy
    var isOutreachEnabled: Boolean by AICharacters.isOutreachEnabled
    var outreachSentCount: Int by AICharacters.outreachSentCount
    var outreachRepliedCount: Int by AICharacters.outreachRepliedCount
    var consecutiveFailuresCount: Int by AICharacters.consecutiveFailuresCount
    var isOutreachBlocked: Boolean by AICharacters.isOutreachBlocked
    var voice: VoiceModel by AICharacters.voice
    val avatar by CharacterAvatar optionalBackReferencedOn CharacterAvatars.characterPublicId
    val images by CharacterImage referrersOn CharacterImages.characterPublicId
    val welcomeCard by CharacterWelcomeCardEntity optionalBackReferencedOn CharacterWelcomeCard.character
    val ownerEntity by AppUser referencedOn AICharacters.owner
    val welcomeCardImages by WelcomeCardImage referrersOn WelcomeCardImages.characterPublicId
    val promptDetailsEntity by AICharacterPromptDetailEntity optionalBackReferencedOn AICharacterPromptDetails.characterId
}

data class AICharacterData(
    val publicId: Uuid,
    val owner: Uuid,
    val characterName: String,
    val fullDescription: String?,
    val shortDescription: String,
    val temperature: Float,
    val visibility: CharacterVisibility,
    val isPublicFullDescription: Boolean,
    val category: String,
    val firstMessage: String?,
    val welcomeCard: WelcomeCardRequest?,
    val isOutreachEnabled: Boolean,
    val voice: VoiceModel = VoiceModel.ZEPHYR,
    val interactionFormat: InteractionFormat = InteractionFormat.LITERARY_RPG,
    val promptDetails: com.ensodai.shared.avalon.character.models.CharacterPromptDetailsInput? = null,
    val alternateGreetings: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val lorebook: com.ensodai.shared.avalon.character.models.CharacterLorebookInput? = null,
    val attachedLorebookIds: List<Uuid> = emptyList()
)

fun SaveCharacterRequest.toCharacterData(
    publicId: Uuid,
    visibility: CharacterVisibility,
    owner: Uuid,
    category: String,
    welcomeCard: WelcomeCardRequest?
): AICharacterData {
    return AICharacterData(
        publicId = publicId,
        owner = owner,
        characterName = this.characterName,
        fullDescription = this.fullDescription,
        shortDescription = this.shortDescription,
        temperature = this.temperature,
        visibility = visibility,
        isPublicFullDescription = this.isPublicFullDescription,
        category = category,
        firstMessage = this.firstMessage,
        welcomeCard = welcomeCard,
        isOutreachEnabled = this.isOutreachEnabled,
        voice = this.voice,
        interactionFormat = this.interactionFormat,
        promptDetails = this.promptDetails,
        alternateGreetings = this.alternateGreetings,
        tags = this.tags,
        lorebook = this.lorebook,
        attachedLorebookIds = this.attachedLorebookIds
    )
}

fun AICharacter.toResponse(
    userId: Uuid,
    relationshipLevel: Int = 1,
    experiencePoints: Long = 0L
): CharacterResponse {
    // 1. Преобразуем изображения для welcome card
    val welcomeCardImagesResponse = this.welcomeCardImages.map { it.toResponse() }

    // 2. Преобразуем сам welcome card, передавая в него список изображений
    val welcomeCardResponse = this.welcomeCard?.toResponse(welcomeCardImagesResponse)

    return CharacterResponse(
        id = this.id.value,
        ownerNick = this.ownerEntity.nickName,
        characterName = this.characterName,
        shortDescription = this.shortDescription,
        fullDescription = if (this.isPublicFullDescription) this.fullDescription else null,
        isPublicFullDescription = this.isPublicFullDescription,
        visibility = this.visibility,
        avatar = this.avatar?.toResponse(),
        images = this.images.sortedBy { it.orderIndex }.map { it.toResponse() },
        welcomeCard = welcomeCardResponse,
        firstMessage = this.firstMessage,
        canEdit = userId == this.owner.value,
        updatedAt = this.updatedAt,
        temperature = this.temperature,
        isDeleted = this.deletedAt != null,
        unclaimedEnergy = if (userId == this.owner.value) this.unclaimedEnergy else null,
        totalEarnedEnergy = if (userId == this.owner.value) this.totalEarnedEnergy else null,
        isOutreachEnabled = this.isOutreachEnabled,
        voice = this.voice,
        relationshipLevel = relationshipLevel,
        experiencePoints = experiencePoints,
        interactionFormat = this.interactionFormat,
        scenario = this.promptDetailsEntity?.scenario,
        mesExample = this.promptDetailsEntity?.mesExample
    )
}