package com.ensodai.avalon.data.repository.local

import com.ensodai.avalon.data.repository.local.base.BaseRepository
import com.ensodai.avalon.data.repository.local.base.DatabaseExecutor

import com.ensodai.avalon.data.db.dao.GiftDao
import com.ensodai.avalon.data.db.models.gift.GiftItemEntity
import com.ensodai.avalon.data.db.models.gift.GiftPackEntity
import com.ensodai.avalon.data.db.models.gift.GiftPackWithItems
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.avalon.domain.repository.local.LocalGiftsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

class LocalGiftsRepositoryImpl(
    dbExecutor: DatabaseExecutor,
    private val giftDao: GiftDao
) : BaseRepository(dbExecutor), LocalGiftsRepository {

    override fun getMyPacks(): Flow<List<GiftPack>> {
        return dbObserve { giftDao.getMyPacks() }.map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMyPacksPagingSource(): Flow<List<GiftPackWithItems>> {
        return dbObserve { giftDao.getMyPacksPagingSource() }
    }

    override suspend fun insertPacks(packs: List<GiftPack>) {
        dbWrite {
            val packEntities = packs.map { it.toEntity(isOwned = true) }
            val itemEntities = packs.flatMap { pack ->
                pack.gifts.map { it.toEntity(pack.id) }
            }
            giftDao.insertPacks(packEntities)
            giftDao.insertItems(itemEntities)
        }
    }

    override suspend fun clearMyPacks() {
        dbWrite {
            giftDao.clearMyPacks()
            giftDao.clearMyPackItems()
        }
    }

    override suspend fun getPackById(id: Uuid): GiftPack? {
        return dbRead { giftDao.getPackById(id)?.toDomain() }
    }

    private fun GiftPackWithItems.toDomain(): GiftPack {
        return GiftPack(
            id = pack.id,
            authorId = pack.authorId,
            name = pack.name,
            description = pack.description,
            coverUrl = pack.coverUrl,
            isPublic = pack.isPublic,
            price = pack.price,
            gifts = items.map { it.toDomain() }
        )
    }

    private fun GiftItemEntity.toDomain(): Gift {
        return Gift(
            id = id,
            packId = packId,
            fileUrl = fileUrl,
            price = price,
            actionType = actionType,
            giftName = giftName,
            giftDescription = giftDescription
        )
    }

    private fun GiftPack.toEntity(isOwned: Boolean): GiftPackEntity {
        return GiftPackEntity(
            id = id,
            authorId = authorId,
            name = name,
            description = description,
            coverUrl = coverUrl,
            isPublic = isPublic,
            price = price,
            isOwned = isOwned
        )
    }

    private fun Gift.toEntity(resolvedPackId: Uuid): GiftItemEntity {
        return GiftItemEntity(
            id = id,
            packId = resolvedPackId,
            fileUrl = fileUrl,
            price = price,
            actionType = actionType,
            giftName = giftName,
            giftDescription = giftDescription
        )
    }
}
