package com.ensodai.avalon.data.repository.remote.rpc

import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.shared.avalon.rpc.services.GiftsRpcService
import com.ensodai.avalon.di.RpcConnectionManager
import kotlinx.rpc.withService

open class GiftsRpcServiceRemoteImpl(
    private val manager: RpcConnectionManager,
    private val executor: RpcCallExecutor
) : GiftsRpcService {

    open suspend fun getService(): GiftsRpcService = manager.getActiveClient().withService<GiftsRpcService>()

    override suspend fun createGiftPack(request: CreateGiftPackRequest): GiftPackResponse =
        executor.execute("createGiftPack", getService = { getService() }) { createGiftPack(request) }

    override suspend fun createGiftItem(request: CreateGiftItemRequest): GiftItemResponse =
        executor.execute("createGiftItem", getService = { getService() }) { createGiftItem(request) }

    override suspend fun getMyGiftPacks(request: GetGiftPacksRequest): GiftPacksResponse =
        executor.execute("getMyGiftPacks", getService = { getService() }) { getMyGiftPacks(request) }

    override suspend fun getAllGiftPacks(request: GetGiftPacksRequest): GiftPacksResponse =
        executor.execute("getAllGiftPacks", getService = { getService() }) { getAllGiftPacks(request) }

    override suspend fun validateSubscription(request: ValidateSubscriptionRequest): SubscriptionStatusResponse =
        executor.execute("validateSubscription", getService = { getService() }) { validateSubscription(request) }

    override suspend fun validateConsumable(request: ValidateConsumableRequest): ConsumableResultResponse =
        executor.execute("validateConsumable", getService = { getService() }) { validateConsumable(request) }

    override suspend fun getAvailableProducts(): ProductsListResponse =
        executor.execute("getAvailableProducts", getService = { getService() }) { getAvailableProducts() }
}
