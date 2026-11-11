package app.domain.gifts.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.gifts.models.*
import app.domain.utils.ServiceResult
import app.service.GiftsService
import kotlin.uuid.Uuid

@Factory
class CreateGiftPackUseCase(private val giftsService: GiftsService) {
    suspend fun execute(request: CreateGiftPackRequest, userId: Uuid): GiftPackResponse {
        val result = giftsService.createGiftPack(
            authorId = userId,
            name = request.name,
            description = request.description,
            coverUrl = request.coverUrl,
            isPublic = request.isPublic,
            price = request.price
        )
        return when (result) {
            is ServiceResult.Success -> GiftPackResponse(result.data)
            is ServiceResult.Error -> throw Exception(result.message ?: "Unknown error")
        }
    }
}
