package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.cache.RedisService
import app.data.repository.CharacterUpdateRepository
import app.service.ImageSaveResult
import com.ensodai.shared.avalon.character.models.UpdateImagesRequest
import com.ensodai.shared.avalon.character.models.UpdateImagesResponse
import app.domain.utils.ServiceResult
import kotlin.uuid.Uuid

import app.data.repository.NewImageWithSpec

@Factory
class UpdateCharacterImagesUseCase(
    private val characterUpdateRepository: CharacterUpdateRepository,
    private val redisService: RedisService
) {
    suspend fun execute(request: UpdateImagesRequest, userId: Uuid) {
        val imageResults = request.imageTokens?.mapNotNull { token ->
            popTempImage(token, redisService)
        } ?: emptyList()

        val newImagesWithSpecs = request.galleryImages?.mapNotNull { spec ->
            val result = spec.imageToken?.let { token ->
                popTempImage(token, redisService)
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
            popTempImage(token, redisService)
        }

        val result = characterUpdateRepository.updateImages(
            userId = userId,
            charId = request.characterId,
            newAvatar = avatarResult,
            newGalleryImages = imageResults,
            deletedImagesIds = request.deletedImagesIds,
            newAvatarId = request.newAvatarId,
            galleryImagesSpecs = request.galleryImages,
            newImagesWithSpecs = newImagesWithSpecs
        )

        if (result is ServiceResult.Error) {
            throw Exception(result.message ?: "Operation failed")
        }
    }

    private fun popTempImage(token: String, redisService: RedisService): ImageSaveResult? {
        return redisService.popTempImage(token)?.let {
            ImageSaveResult(
                path = it.path,
                width = it.width,
                height = it.height,
                thumbHash = it.thumbHash,
                sizeBytes = it.sizeBytes
            )
        }
    }
}
