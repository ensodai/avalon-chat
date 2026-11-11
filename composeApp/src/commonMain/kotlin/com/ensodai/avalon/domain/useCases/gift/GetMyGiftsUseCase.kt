package com.ensodai.avalon.domain.useCases.gift

import com.ensodai.shared.avalon.gifts.models.GiftPack
import com.ensodai.shared.avalon.gifts.models.GetGiftPacksRequest
import com.ensodai.avalon.core.pagination.Paginator
import com.ensodai.shared.avalon.rpc.services.GiftsRpcService
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetMyGiftsUseCase(
    private val giftsRpcService: GiftsRpcService
) {
    private val pageSize = 20

    operator fun invoke(): Paginator<Uuid, GiftPack> {
        return Paginator(
            getKey = { gift -> gift.id },
            loadPage = { page ->
                val offset = (page - 1) * pageSize
                runCatching {
                    giftsRpcService.getMyGiftPacks(
                        GetGiftPacksRequest(
                            limit = pageSize,
                            offset = offset
                        )
                    )
                }.map { response ->
                    Paginator.PageResult(
                        items = response.packs,
                        totalPages = if (response.packs.size < pageSize) page else page + 1
                    )
                }
            }
        )
    }
}