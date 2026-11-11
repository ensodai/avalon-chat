package app.domain.character.usecases

import app.data.db.tables.users.UserCharacterRelationships
import app.utils.dbQuery
import com.ensodai.shared.avalon.character.models.WarmOutreachSettingsDto
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.koin.core.annotation.Factory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Factory
class GetWarmOutreachSettingsUseCase {

    suspend fun execute(characterId: Uuid, userId: Uuid): WarmOutreachSettingsDto {
        return dbQuery {
            UserCharacterRelationships
                .select(
                    UserCharacterRelationships.isOutreachEnabled,
                    UserCharacterRelationships.outreachMorningEnabled,
                    UserCharacterRelationships.outreachMorningPrompt,
                    UserCharacterRelationships.outreachAfternoonEnabled,
                    UserCharacterRelationships.outreachAfternoonPrompt,
                    UserCharacterRelationships.outreachEveningEnabled,
                    UserCharacterRelationships.outreachEveningPrompt
                )
                .where {
                    (UserCharacterRelationships.userId eq userId) and
                    (UserCharacterRelationships.characterId eq characterId)
                }
                .singleOrNull()
                ?.let { row ->
                    WarmOutreachSettingsDto(
                        isOutreachEnabled = row[UserCharacterRelationships.isOutreachEnabled],
                        outreachMorningEnabled = row[UserCharacterRelationships.outreachMorningEnabled],
                        outreachMorningPrompt = row[UserCharacterRelationships.outreachMorningPrompt],
                        outreachAfternoonEnabled = row[UserCharacterRelationships.outreachAfternoonEnabled],
                        outreachAfternoonPrompt = row[UserCharacterRelationships.outreachAfternoonPrompt],
                        outreachEveningEnabled = row[UserCharacterRelationships.outreachEveningEnabled],
                        outreachEveningPrompt = row[UserCharacterRelationships.outreachEveningPrompt]
                    )
                } ?: WarmOutreachSettingsDto(
                    isOutreachEnabled = true,
                    outreachMorningEnabled = true,
                    outreachMorningPrompt = null,
                    outreachAfternoonEnabled = true,
                    outreachAfternoonPrompt = null,
                    outreachEveningEnabled = true,
                    outreachEveningPrompt = null
                )
        }
    }
}
