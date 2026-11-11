package app.domain.syncmanager

import app.data.db.tables.users.UserPersonas
import com.ensodai.shared.avalon.persona.models.PersonaResponse
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class PersonaSyncService {

    fun fetchChanges(userId: Uuid, since: Instant): SyncEntityChanges<PersonaResponse> {

        // 1. Делаем ОДИН запрос ко всем измененным записям (и живым, и удаленным)
        val allChanges = UserPersonas
            .select(
                UserPersonas.id,
                UserPersonas.personaName,
                UserPersonas.personaDescription,
                UserPersonas.createdAt,
                UserPersonas.deletedAt
            )
            .where {
                (UserPersonas.userId eq userId) and
                        (UserPersonas.updatedAt greater since)
            }
            .toList()

        val updated = ArrayList<PersonaResponse>()
        val deleted = ArrayList<Uuid>()

        // 2. Сортируем в памяти
        for (row in allChanges) {
            val isDeleted = row[UserPersonas.deletedAt] != null

            if (isDeleted) {
                deleted.add(row[UserPersonas.id].value)
            } else {
                updated.add(
                    PersonaResponse(
                        id = row[UserPersonas.id].value,
                        name = row[UserPersonas.personaName],
                        description = row[UserPersonas.personaDescription],
                        createdAt = row[UserPersonas.createdAt]
                    )
                )
            }
        }

        return SyncEntityChanges(updated, deleted)
    }
}