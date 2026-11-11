package app.domain.gifts.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.gifts.models.*
import app.domain.utils.ServiceResult
import app.service.GiftsService
import kotlin.uuid.Uuid

@Factory
class GetMyGiftPacksUseCase(private val giftsService: GiftsService) {
    suspend fun execute(request: GetGiftPacksRequest, userId: Uuid): GiftPacksResponse {
        val result = giftsService.getUserSavedPacks(userId, request.limit, request.offset)
        return when (result) {
            is ServiceResult.Success -> GiftPacksResponse(result.data.packs)
            is ServiceResult.Error -> throw Exception(result.message ?: "Unknown error")
        }
    }
}
