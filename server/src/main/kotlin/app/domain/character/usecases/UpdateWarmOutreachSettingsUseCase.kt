package app.domain.character.usecases

import app.data.db.tables.users.UserCharacterRelationships
import app.utils.dbQuery
import com.ensodai.shared.avalon.character.models.UpdateWarmOutreachSettingsRequest
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Factory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Factory
class UpdateWarmOutreachSettingsUseCase {

    suspend fun execute(request: UpdateWarmOutreachSettingsRequest, userId: Uuid) {
        val settings = request.settings
        dbQuery {
            val updatedCount = UserCharacterRelationships.update({
                (UserCharacterRelationships.userId eq userId) and
                (UserCharacterRelationships.characterId eq request.characterId)
            }) {
                it[isOutreachEnabled] = settings.isOutreachEnabled
                it[outreachMorningEnabled] = settings.outreachMorningEnabled
                it[outreachMorningPrompt] = settings.outreachMorningPrompt
                it[outreachAfternoonEnabled] = settings.outreachAfternoonEnabled
                it[outreachAfternoonPrompt] = settings.outreachAfternoonPrompt
                it[outreachEveningEnabled] = settings.outreachEveningEnabled
                it[outreachEveningPrompt] = settings.outreachEveningPrompt
            }

            if (updatedCount == 0) {
                UserCharacterRelationships.insert {
                    it[id] = Uuid.random()
                    it[UserCharacterRelationships.userId] = userId
                    it[UserCharacterRelationships.characterId] = request.characterId
                    it[relationshipLevel] = 1
                    it[experiencePoints] = 0L
                    it[isOutreachEnabled] = settings.isOutreachEnabled
                    it[outreachMorningEnabled] = settings.outreachMorningEnabled
                    it[outreachMorningPrompt] = settings.outreachMorningPrompt
                    it[outreachAfternoonEnabled] = settings.outreachAfternoonEnabled
                    it[outreachAfternoonPrompt] = settings.outreachAfternoonPrompt
                    it[outreachEveningEnabled] = settings.outreachEveningEnabled
                    it[outreachEveningPrompt] = settings.outreachEveningPrompt
                }
            }
        }
    }
}
