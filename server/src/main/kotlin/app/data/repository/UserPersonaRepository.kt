package app.data.repository

import app.data.db.tables.users.AppUser
import app.data.db.tables.users.UserPersona
import app.utils.dbQuery
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class UserPersonaRepository {

    /**
     * Находит "выбранную" персону для пользователя, используя
     * поле `default_persona` в таблице `Users`.
     *
     * @param userId UUID пользователя.
     * @return DAO-объект [UserPersona], если персона выбрана, иначе `null`.
     */
    suspend fun findSelectedForUser(userId: Uuid): UserPersona? {
        return dbQuery {

            val appUser = AppUser.findById(userId)

            val personaId = appUser?.defaultPersona?.id?.value

            if (personaId != null) {
                UserPersona.findById(personaId)
            } else {
                null
            }
        }
    }
}