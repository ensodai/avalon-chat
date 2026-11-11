package app.data.db.tables.users

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import com.ensodai.shared.avalon.persona.models.PersonaResponse
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant
import kotlin.uuid.Uuid

object UserPersonas : BaseUuidTable("user_personas") {
    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE)
    val personaName = varchar("persona_name", 255)
    val personaDescription = text("persona_description")

    init {
        index("idx_user_personas_sync", isUnique = false, userId, updatedAt)
    }
}

class UserPersona(id: EntityID<Uuid>) : BaseUuidEntity(id, UserPersonas) {
    companion object : BaseUuidEntityClass<UserPersona>(UserPersonas)

    var appUser by AppUser referencedOn UserPersonas.userId
    var personaName: String by UserPersonas.personaName
    var personaDescription: String by UserPersonas.personaDescription
}

data class PersonaInternalDto(
    val id: Uuid,
    val userId: Uuid,
    val name: String,
    val description: String,
    val createdAt: Instant
)

fun UserPersona.toInternalDto(): PersonaInternalDto {
    return PersonaInternalDto(
        id = this.id.value,
        userId = this.appUser.id.value,
        name = this.personaName,
        description = this.personaDescription,
        createdAt = this.createdAt
    )
}

fun UserPersona.toResponse(): PersonaResponse {
    return PersonaResponse(
        id = this.id.value,
        name = this.personaName,
        description = this.personaDescription,
        createdAt = this.createdAt
    )
}
