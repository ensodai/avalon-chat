package com.ensodai.avalon.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.room3.Update
import com.ensodai.avalon.data.db.models.gift.GiftItemEntity
import com.ensodai.avalon.data.db.models.gift.GiftPackEntity
import com.ensodai.avalon.data.db.models.gift.GiftPackWithItems
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
abstract class GiftDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertPacks(packs: List<GiftPackEntity>)

    @Update
    protected abstract suspend fun _update_insertPacks(packs: List<GiftPackEntity>)

    @Transaction
    open suspend fun insertPacks(packs: List<GiftPackEntity>) {
        _insert_insertPacks(packs)
        _update_insertPacks(packs)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertItems(items: List<GiftItemEntity>)

    @Update
    protected abstract suspend fun _update_insertItems(items: List<GiftItemEntity>)

    @Transaction
    open suspend fun insertItems(items: List<GiftItemEntity>) {
        _insert_insertItems(items)
        _update_insertItems(items)
    }

    @Query("DELETE FROM gift_packs WHERE isOwned = 1")
    abstract suspend fun clearMyPacks()

    @Query("DELETE FROM gift_items WHERE pack_id IN (SELECT id FROM gift_packs WHERE isOwned = 1)")
    abstract suspend fun clearMyPackItems()

    @Transaction
    @Query("SELECT * FROM gift_packs WHERE isOwned = 1")
    abstract fun getMyPacks(): Flow<List<GiftPackWithItems>>

    @Transaction
    @Query("SELECT * FROM gift_packs WHERE isOwned = 1")
    abstract fun getMyPacksPagingSource(): Flow<List<GiftPackWithItems>>

    @Transaction
    @Query("SELECT * FROM gift_packs WHERE id = :id")
    abstract suspend fun getPackById(id: Uuid): GiftPackWithItems?
}
