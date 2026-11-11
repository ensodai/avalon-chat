package app.data.db.tables.aiCharacter

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.users.AppUsers
import com.ensodai.shared.avalon.character.models.LorebookEntryPosition
import com.ensodai.shared.avalon.character.models.SecondaryFilterLogic
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid

/**
 * Детали промпта и метаданные автора (1:1 к AICharacters).
 */
object AICharacterPromptDetails : BaseUuidTable("ai_character_prompt_details") {
    val characterId = reference("character_id", AICharacters, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val scenario = text("scenario").nullable()
    val mesExample = text("mes_example").nullable()
    val customSystemPrompt = text("custom_system_prompt").nullable()
    val postHistoryInstructions = text("post_history_instructions").nullable()
    val creator = varchar("creator", 120).nullable()
    val characterVersion = varchar("character_version", 50).nullable()
    val creatorNotes = text("creator_notes").nullable()
    val rawExtensions = text("raw_extensions").nullable()
    val isImported = bool("is_imported").default(false)
}

class AICharacterPromptDetailEntity(id: EntityID<Uuid>) : BaseUuidEntity(id, AICharacterPromptDetails) {
    companion object : BaseUuidEntityClass<AICharacterPromptDetailEntity>(AICharacterPromptDetails)

    var character by AICharacter referencedOn AICharacterPromptDetails.characterId
    var scenario by AICharacterPromptDetails.scenario
    var mesExample by AICharacterPromptDetails.mesExample
    var customSystemPrompt by AICharacterPromptDetails.customSystemPrompt
    var postHistoryInstructions by AICharacterPromptDetails.postHistoryInstructions
    var creator by AICharacterPromptDetails.creator
    var characterVersion by AICharacterPromptDetails.characterVersion
    var creatorNotes by AICharacterPromptDetails.creatorNotes
    var rawExtensions by AICharacterPromptDetails.rawExtensions
    var isImported by AICharacterPromptDetails.isImported
}

/**
 * Альтернативные приветствия персонажа (1:N к AICharacters).
 */
object AICharacterAlternateGreetings : BaseUuidTable("ai_character_alternate_greetings") {
    val characterId = reference("character_id", AICharacters, onDelete = ReferenceOption.CASCADE)
    val greetingText = text("greeting_text")
    val title = varchar("title", 150).nullable()
    val orderIndex = integer("order_index").default(0)

    init {
        index("idx_char_greetings_char_id", isUnique = false, characterId)
    }
}

class AICharacterAlternateGreetingEntity(id: EntityID<Uuid>) : BaseUuidEntity(id, AICharacterAlternateGreetings) {
    companion object : BaseUuidEntityClass<AICharacterAlternateGreetingEntity>(AICharacterAlternateGreetings)

    var character by AICharacter referencedOn AICharacterAlternateGreetings.characterId
    var greetingText by AICharacterAlternateGreetings.greetingText
    var title by AICharacterAlternateGreetings.title
    var orderIndex by AICharacterAlternateGreetings.orderIndex
}

/**
 * Теги персонажа (M:N).
 */
object AICharacterTags : Table("ai_character_tags") {
    val characterId = reference("character_id", AICharacters, onDelete = ReferenceOption.CASCADE)
    val tag = varchar("tag", 60)

    override val primaryKey = PrimaryKey(characterId, tag)

    init {
        index("idx_char_tags_tag", isUnique = false, tag)
    }
}

/**
 * Lorebook персонажа / модульная база знаний.
 */
object AICharacterLorebooks : BaseUuidTable("ai_character_lorebooks") {
    val characterId = reference("character_id", AICharacters, onDelete = ReferenceOption.CASCADE).nullable()
    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE).nullable()
    val isPublic = bool("is_public").default(false)
    val name = varchar("name", 150).default("Default Lorebook")
    val description = text("description").nullable()
    val scanDepth = integer("scan_depth").default(5)
    val tokenBudget = integer("token_budget").nullable()
    val recursiveScanning = bool("recursive_scanning").default(false)
    val avatarUrl = text("avatar_url").nullable()

    init {
        index("idx_lorebooks_char_id", isUnique = false, characterId)
        index("idx_lorebooks_user_id", isUnique = false, userId)
    }
}

class AICharacterLorebookEntity(id: EntityID<Uuid>) : BaseUuidEntity(id, AICharacterLorebooks) {
    companion object : BaseUuidEntityClass<AICharacterLorebookEntity>(AICharacterLorebooks)

    var character by AICharacter optionalReferencedOn AICharacterLorebooks.characterId
    var name by AICharacterLorebooks.name
    var description by AICharacterLorebooks.description
    var scanDepth by AICharacterLorebooks.scanDepth
    var tokenBudget by AICharacterLorebooks.tokenBudget
    var recursiveScanning by AICharacterLorebooks.recursiveScanning
    var avatarUrl by AICharacterLorebooks.avatarUrl
    val entries by AICharacterLorebookEntryEntity referrersOn AICharacterLorebookEntries.lorebookId
}

/**
 * Привязки Lorebook к персонажам (M:N).
 */
object AICharacterLorebookBindings : BaseUuidTable("ai_character_lorebook_bindings") {
    val characterId = reference("character_id", AICharacters, onDelete = ReferenceOption.CASCADE)
    val lorebookId = reference("lorebook_id", AICharacterLorebooks, onDelete = ReferenceOption.CASCADE)
    val isEnabled = bool("is_enabled").default(true)
    val isPrimary = bool("is_primary").default(false)
    val priority = integer("priority").default(0)

    init {
        uniqueIndex("uq_char_lorebook", characterId, lorebookId)
        index("idx_char_lb_bindings_char_id", isUnique = false, characterId)
        index("idx_char_lb_bindings_book_id", isUnique = false, lorebookId)
    }
}

/**
 * Сохраненные чужие публичные книги в библиотеке пользователя.
 */
object UserSavedLorebooks : BaseUuidTable("user_saved_lorebooks") {
    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE)
    val lorebookId = reference("lorebook_id", AICharacterLorebooks, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex("uq_user_saved_lorebook", userId, lorebookId)
        index("idx_user_saved_lb_user_id", isUnique = false, userId)
        index("idx_user_saved_lb_book_id", isUnique = false, lorebookId)
    }
}

/**
 * Привязки пользовательских книг знаний к персонажам (M:N).
 */
object UserCharacterLorebooks : BaseUuidTable("user_character_lorebooks") {
    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE)
    val characterId = reference("character_id", AICharacters, onDelete = ReferenceOption.CASCADE)
    val lorebookId = reference("lorebook_id", AICharacterLorebooks, onDelete = ReferenceOption.CASCADE)
    val isEnabled = bool("is_enabled").default(true)
    val priority = integer("priority").default(0)

    init {
        uniqueIndex("uq_user_char_lorebook", userId, characterId, lorebookId)
        index("idx_user_char_lb_user_char", isUnique = false, userId, characterId)
        index("idx_user_char_lb_book", isUnique = false, lorebookId)
    }
}

/**
 * Статьи / записи Lorebook (1:N к AICharacterLorebooks).
 */
object AICharacterLorebookEntries : BaseUuidTable("ai_character_lorebook_entries") {
    val lorebookId = reference("lorebook_id", AICharacterLorebooks, onDelete = ReferenceOption.CASCADE)
    val keys = text("keys")
    val secondaryKeys = text("secondary_keys").nullable()
    val content = text("content")
    val comment = varchar("comment", 255).nullable()
    val enabled = bool("enabled").default(true)
    val constant = bool("constant").default(false)
    val selective = bool("selective").default(false)
    val insertionOrder = integer("insertion_order").default(0)
    val position = enumerationByName("position", 20, LorebookEntryPosition::class).default(LorebookEntryPosition.BEFORE_CHAR)
    val useRegex = bool("use_regex").default(false)
    val priority = integer("priority").default(10)
    val probability = integer("probability").default(100)
    val scanDepthOverride = integer("scan_depth_override").nullable()
    val stickyTurns = integer("sticky_turns").default(0)
    val cooldownTurns = integer("cooldown_turns").default(0)
    val delayMessages = integer("delay_messages").default(0)
    val secondaryLogic = enumerationByName("secondary_logic", 20, SecondaryFilterLogic::class).default(SecondaryFilterLogic.AND_ANY)
    val excludeRecursion = bool("exclude_recursion").default(false)
    val preventRecursion = bool("prevent_recursion").default(false)
    val delayUntilRecursion = bool("delay_until_recursion").default(false)
    val depth = integer("depth").default(4)

    init {
        index("idx_lore_entries_book_id", isUnique = false, lorebookId)
    }
}

class AICharacterLorebookEntryEntity(id: EntityID<Uuid>) : BaseUuidEntity(id, AICharacterLorebookEntries) {
    companion object : BaseUuidEntityClass<AICharacterLorebookEntryEntity>(AICharacterLorebookEntries)

    var lorebook by AICharacterLorebookEntity referencedOn AICharacterLorebookEntries.lorebookId
    var keys by AICharacterLorebookEntries.keys
    var secondaryKeys by AICharacterLorebookEntries.secondaryKeys
    var content by AICharacterLorebookEntries.content
    var comment by AICharacterLorebookEntries.comment
    var enabled by AICharacterLorebookEntries.enabled
    var constant by AICharacterLorebookEntries.constant
    var selective by AICharacterLorebookEntries.selective
    var insertionOrder by AICharacterLorebookEntries.insertionOrder
    var position by AICharacterLorebookEntries.position
    var useRegex by AICharacterLorebookEntries.useRegex
    var priority by AICharacterLorebookEntries.priority
    var probability by AICharacterLorebookEntries.probability
    var scanDepthOverride by AICharacterLorebookEntries.scanDepthOverride
    var stickyTurns by AICharacterLorebookEntries.stickyTurns
    var cooldownTurns by AICharacterLorebookEntries.cooldownTurns
    var delayMessages by AICharacterLorebookEntries.delayMessages
    var secondaryLogic by AICharacterLorebookEntries.secondaryLogic
    var excludeRecursion by AICharacterLorebookEntries.excludeRecursion
    var preventRecursion by AICharacterLorebookEntries.preventRecursion
    var delayUntilRecursion by AICharacterLorebookEntries.delayUntilRecursion
    var depth by AICharacterLorebookEntries.depth
}
