package app.domain.gifts.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.gifts.models.*
import app.domain.utils.ServiceResult
import app.service.GiftsService

@Factory
class CreateGiftItemUseCase(private val giftsService: GiftsService) {
    suspend fun execute(request: CreateGiftItemRequest): GiftItemResponse {
        val result = giftsService.createGift(
            packId = request.packId,
            fileUrl = request.fileUrl,
            price = request.price,
            actionType = request.actionType.name,
            giftName = request.giftName,
            giftDescription = request.giftDescription
        )
        return when (result) {
            is ServiceResult.Success -> GiftItemResponse(result.data)
            is ServiceResult.Error -> throw Exception(result.message ?: "Unknown error")
        }
    }
}
