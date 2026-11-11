package app.domain.character.usecases

import app.data.repository.CharacterRepository
import app.utils.toServerUrl
import com.ensodai.shared.avalon.character.models.CharacterEventRoadmapDto
import com.ensodai.shared.avalon.character.models.CharacterImageResponse
import org.koin.core.annotation.Factory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Factory
class GetCharacterEventsRoadmapUseCase(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(characterId: Uuid, userId: Uuid): List<CharacterEventRoadmapDto> {
        val character = characterRepository.getCharacterById(characterId, userId)
        val events = characterRepository.getEventsByCharacterId(characterId)
        val levelImages = characterRepository.getCharacterImages(characterId)
            .filter { it.requiredLevel != null }
        
        val userLevel = character.relationshipLevel
        
        return (1..65).map { level ->
            val matchingEvent = events.find { it.requiredLevel == level }
            val matchingPhoto = levelImages.find { it.requiredLevel == level }

            val rewardPhotoDto = matchingPhoto?.let { photo ->
                val isUnlocked = userLevel >= level
                CharacterImageResponse(
                    id = photo.id.value,
                    imageUrl = if (isUnlocked) photo.imagePath.toServerUrl() else "",
                    thumbHash = photo.thumbHash,
                    isPrivate = photo.isPrivate,
                    energyCost = photo.energyCost,
                    requiredLevel = photo.requiredLevel,
                    isUnlocked = isUnlocked
                )
            }

            val hasReward = matchingEvent != null || rewardPhotoDto != null
            val isUnlocked = userLevel >= level

            CharacterEventRoadmapDto(
                level = level,
                hasReward = hasReward,
                isUnlocked = isUnlocked,
                triggerScenario = if (isUnlocked && matchingEvent != null) matchingEvent.eventTriggerScenario else null,
                rewardPhoto = rewardPhotoDto
            )
        }
    }
}
