package app.data.db.tables.users

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.aiCharacter.AICharacter
import app.data.db.tables.aiCharacter.AICharacters
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.uuid.Uuid

object UserCharacterRelationships : BaseUuidTable("user_character_relationships") {

    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE)
    val characterId = reference("character_id", AICharacters, onDelete = ReferenceOption.CASCADE)
    val relationshipLevel = integer("relationship_level").default(1)
    val experiencePoints = long("experience_points").default(0L)
    val pendingEventLevel = integer("pending_event_level").nullable()
    
    val isOutreachEnabled = bool("is_outreach_enabled").default(true)
    val outreachMorningEnabled = bool("outreach_morning_enabled").default(true)
    val outreachMorningPrompt = text("outreach_morning_prompt").nullable()
    val outreachAfternoonEnabled = bool("outreach_afternoon_enabled").default(true)
    val outreachAfternoonPrompt = text("outreach_afternoon_prompt").nullable()
    val outreachEveningEnabled = bool("outreach_evening_enabled").default(true)
    val outreachEveningPrompt = text("outreach_evening_prompt").nullable()
    val lastOutreachSentAt = timestamp("last_outreach_sent_at").nullable()
    val lastOutreachSlot = varchar("last_outreach_slot", 20).nullable()

    init {
        uniqueIndex("unique_user_character_relationship_idx", userId, characterId)
    }
}

class UserCharacterRelationship(id: EntityID<Uuid>) : BaseUuidEntity(id, UserCharacterRelationships) {
    companion object : BaseUuidEntityClass<UserCharacterRelationship>(UserCharacterRelationships)

    var user by AppUser referencedOn UserCharacterRelationships.userId
    var character by AICharacter referencedOn UserCharacterRelationships.characterId
    var relationshipLevel by UserCharacterRelationships.relationshipLevel
    var experiencePoints by UserCharacterRelationships.experiencePoints
    var pendingEventLevel by UserCharacterRelationships.pendingEventLevel
    
    var isOutreachEnabled by UserCharacterRelationships.isOutreachEnabled
    var outreachMorningEnabled by UserCharacterRelationships.outreachMorningEnabled
    var outreachMorningPrompt by UserCharacterRelationships.outreachMorningPrompt
    var outreachAfternoonEnabled by UserCharacterRelationships.outreachAfternoonEnabled
    var outreachAfternoonPrompt by UserCharacterRelationships.outreachAfternoonPrompt
    var outreachEveningEnabled by UserCharacterRelationships.outreachEveningEnabled
    var outreachEveningPrompt by UserCharacterRelationships.outreachEveningPrompt
    var lastOutreachSentAt by UserCharacterRelationships.lastOutreachSentAt
    var lastOutreachSlot by UserCharacterRelationships.lastOutreachSlot
}

