package com.ensodai.avalon.domain.useCases.gift

import com.ensodai.avalon.domain.repository.local.LocalGiftsRepository
import com.ensodai.shared.avalon.gifts.models.GetGiftPacksRequest
import com.ensodai.shared.avalon.rpc.services.GiftsRpcService
import org.koin.core.annotation.Single

@Single
class SyncMyGiftsUseCase(
    private val localRepository: LocalGiftsRepository,
    private val giftsRpcService: GiftsRpcService
) {
    suspend operator fun invoke(): Result<Unit> {
        return runCatching {
            val response = giftsRpcService.getMyGiftPacks(
                GetGiftPacksRequest(limit = 100, offset = 0)
            )
            localRepository.insertPacks(response.packs)
        }
    }
}
