package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.cache.RedisService
import app.data.repository.CharacterUpdateRepository
import app.service.ImageSaveResult
import com.ensodai.shared.avalon.character.models.UpdateWelcomeCardRequest
import app.domain.utils.ServiceResult
import kotlin.uuid.Uuid

@Factory
class UpdateWelcomeCardUseCase(
    private val characterUpdateRepository: CharacterUpdateRepository,
    private val redisService: RedisService
) {
    suspend fun execute(request: UpdateWelcomeCardRequest, userId: Uuid) {
        val imageResults = request.imageTokens?.mapNotNull { token ->
            redisService.popTempImage(token)?.toImageSaveResult()
        } ?: emptyList()

        val result = characterUpdateRepository.updateWelcomeCard(
            userId = userId,
            characterId = request.characterId,
            config = request,
            newImagePaths = imageResults
        )

        if (result is ServiceResult.Error) {
            throw Exception(result.message ?: "Failed to update welcome card")
        }
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
