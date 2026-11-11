@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package com.ensodai.shared.avalon.rpc.services

import com.ensodai.shared.avalon.gifts.models.*
import kotlinx.rpc.annotations.Rpc

@Rpc
interface GiftsRpcService {
    suspend fun createGiftPack(request: CreateGiftPackRequest): GiftPackResponse
    suspend fun createGiftItem(request: CreateGiftItemRequest): GiftItemResponse
    suspend fun getMyGiftPacks(request: GetGiftPacksRequest): GiftPacksResponse
    suspend fun getAllGiftPacks(request: GetGiftPacksRequest): GiftPacksResponse
    
    suspend fun validateSubscription(request: ValidateSubscriptionRequest): SubscriptionStatusResponse
    suspend fun validateConsumable(request: ValidateConsumableRequest): ConsumableResultResponse
    suspend fun getAvailableProducts(): ProductsListResponse
}
