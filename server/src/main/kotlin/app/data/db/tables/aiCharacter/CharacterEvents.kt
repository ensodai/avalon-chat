package app.data.db.tables.aiCharacter

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid

object CharacterEvents : BaseUuidTable("character_events") {
    val characterId = reference("character_id", AICharacters, onDelete = ReferenceOption.CASCADE)
    val requiredLevel = integer("required_level")
    val eventName = varchar("event_name", 255).default("")
    
    // Скрытая директива, которая отправляется один раз при наступлении эвента
    val eventTriggerScenario = text("event_trigger_scenario")
    
    // Черта характера, которая навсегда добавляется в системный промпт после эвента
    val eventPermanentTrait = text("event_permanent_trait")
    
    init {
        uniqueIndex("unique_character_event_level_idx", characterId, requiredLevel)
    }
}

class CharacterEvent(id: EntityID<Uuid>) : BaseUuidEntity(id, CharacterEvents) {
    companion object : BaseUuidEntityClass<CharacterEvent>(CharacterEvents)

    var character by AICharacter referencedOn CharacterEvents.characterId
    var requiredLevel by CharacterEvents.requiredLevel
    var eventName by CharacterEvents.eventName
    var eventTriggerScenario by CharacterEvents.eventTriggerScenario
    var eventPermanentTrait by CharacterEvents.eventPermanentTrait
}
