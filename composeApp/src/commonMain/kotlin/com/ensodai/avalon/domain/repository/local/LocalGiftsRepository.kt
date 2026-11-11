package com.ensodai.avalon.domain.repository.local

import com.ensodai.avalon.data.db.models.gift.GiftPackWithItems
import com.ensodai.shared.avalon.gifts.models.GiftPack
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface LocalGiftsRepository {
    fun getMyPacks(): Flow<List<GiftPack>>
    fun getMyPacksPagingSource(): Flow<List<GiftPackWithItems>>
    suspend fun insertPacks(packs: List<GiftPack>)
    suspend fun clearMyPacks()
    suspend fun getPackById(id: Uuid): GiftPack?
}
