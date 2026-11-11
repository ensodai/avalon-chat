package app.service

import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.shared.avalon.user.models.*
import app.data.repository.GiftsRepository
import app.domain.utils.ServiceResult
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class GiftsService(
    private val repository: GiftsRepository
) {
    suspend fun createGiftPack(
        authorId: Uuid?,
        name: String,
        description: String?,
        coverUrl: String,
        isPublic: Boolean,
        price: Int
    ): ServiceResult<GiftPack> {
        val pack = repository.createGiftPack(authorId, name, description, coverUrl, isPublic, price)
        return ServiceResult.Success(pack)
    }

    suspend fun createGift(
        packId: Uuid,
        fileUrl: String,
        price: Int,
        actionType: String,
        giftName: String,
        giftDescription: String
    ): ServiceResult<Gift> {
        val gift = repository.createGift(packId, fileUrl, price, actionType, giftName, giftDescription)
        return ServiceResult.Success(gift)
    }

    suspend fun getGiftPacks(limit: Int, offset: Int): ServiceResult<GiftPacksResponse> {
        val packs = repository.getGiftPacks(limit, offset)
        val response = GiftPacksResponse(packs)

        return ServiceResult.Success(response)
    }

    suspend fun getUserSavedPacks(userId: Uuid, limit: Int, offset: Int): ServiceResult<GiftPacksResponse> {
        val packs = repository.getUserSavedPacks(userId, limit, offset)
        val response = GiftPacksResponse(packs)

        return ServiceResult.Success(response)
    }

    suspend fun savePackForUser(userId: Uuid, packId: Uuid): ServiceResult<Unit> {
        repository.savePackForUser(userId, packId)
        return ServiceResult.Success(Unit)
    }
}
