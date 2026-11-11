package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.cache.RedisService
import app.data.CategoryFactory
import app.data.db.tables.aiCharacter.toCharacterData
import app.data.repository.CharacterCreationRepository
import app.data.repository.NewImageWithSpec
import app.data.repository.UserRepositoryApp
import app.service.ImageSaveResult
import com.ensodai.shared.avalon.character.models.PublishCharacterRequest
import com.ensodai.shared.avalon.character.models.CharacterPublishedResponse
import kotlin.uuid.Uuid

@Factory
class PublishCharacterUseCase(
    private val characterCreationRepository: CharacterCreationRepository,
    private val userRepository: UserRepositoryApp,
    private val redisService: RedisService
) {
    suspend fun execute(request: PublishCharacterRequest, userId: Uuid): CharacterPublishedResponse {
        val characterData = request.characterData
        val characterPublicId = Uuid.random()

        val isShadowBanned = userRepository.isUserShadowBanned(userId)

        val finalVisibility = if (isShadowBanned) {
            com.ensodai.shared.avalon.character.models.CharacterVisibility.PRIVATE
        } else {
            characterData.visibility
        }

        val character = characterData.toCharacterData(
            publicId = characterPublicId,
            owner = userId,
            visibility = finalVisibility,
            category = characterData.category ?: CategoryFactory.getDefaultCategory().categoryCode,
            welcomeCard = characterData.welcomeCard
        )

        val imageResults = request.imageTokens?.mapNotNull { token ->
            redisService.popTempImage(token)?.toImageSaveResult()
        }

        val newImagesWithSpecs = request.galleryImages?.mapNotNull { spec ->
            val result = spec.imageToken?.let { token ->
                redisService.popTempImage(token)?.toImageSaveResult()
            }
            if (result != null) {
                if (spec.isPrivate && (spec.energyCost < 50 || spec.energyCost > 50000)) {
                    throw IllegalArgumentException("Стоимость открытия приватного фото должна быть в пределах от 50 до 50 000 энергии.")
                }
                NewImageWithSpec(
                    saveResult = result,
                    isPrivate = spec.isPrivate,
                    energyCost = spec.energyCost,
                    requiredLevel = spec.requiredLevel
                )
            } else {
                null
            }
        }

        val avatarResult = request.avatarToken?.let { token ->
            redisService.popTempImage(token)?.toImageSaveResult()
        }

        val welcomeCardResults = request.welcomeCardImageTokens?.mapNotNull { token ->
            redisService.popTempImage(token)?.toImageSaveResult()
        }

        val creationResult = characterCreationRepository.createCharacterInTransaction(
            AICharacterData = character,
            publicIdCharacter = characterPublicId,
            characterImages = imageResults?.ifEmpty { null },
            characterAvatar = avatarResult,
            welcomeCardImages = welcomeCardResults?.ifEmpty { null },
            events = characterData.events.ifEmpty { null },
            userId = userId,
            newImagesWithSpecs = newImagesWithSpecs?.ifEmpty { null }
        )

        return CharacterPublishedResponse(
            character = creationResult.characterResponse,
            chatSession = creationResult.chatSessionResponse,
            chatVariant = creationResult.firstChatVariant
        )
    }

    private fun app.cache.TempImageMeta.toImageSaveResult(): ImageSaveResult {
        return ImageSaveResult(
            path = this.path,
            width = this.width,
            height = this.height,
            thumbHash = this.thumbHash,
            sizeBytes = this.sizeBytes
        )
    }
}
