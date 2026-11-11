package app.domain.gifts.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.gifts.models.*
import app.domain.utils.ServiceResult
import app.service.GiftsService

@Factory
class GetAllGiftPacksUseCase(private val giftsService: GiftsService) {
    suspend fun execute(request: GetGiftPacksRequest): GiftPacksResponse {
        val result = giftsService.getGiftPacks(request.limit, request.offset)
        return when (result) {
            is ServiceResult.Success -> GiftPacksResponse(result.data.packs)
            is ServiceResult.Error -> throw Exception(result.message ?: "Unknown error")
        }
    }
}
