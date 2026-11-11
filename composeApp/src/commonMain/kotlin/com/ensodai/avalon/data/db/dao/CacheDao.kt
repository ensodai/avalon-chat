package com.ensodai.avalon.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import androidx.room3.Upsert
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.data.db.cache.CategoryEntity
import com.ensodai.avalon.data.db.cache.ServerCategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
abstract class CacheDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertSelectableCategories(categories: List<CategoryEntity>)

    @Update
    protected abstract suspend fun _update_insertSelectableCategories(categories: List<CategoryEntity>)

    @Transaction
    open suspend fun insertSelectableCategories(categories: List<CategoryEntity>) {
        _insert_insertSelectableCategories(categories)
        _update_insertSelectableCategories(categories)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertServerCategories(categories: List<ServerCategoryEntity>)

    @Update
    protected abstract suspend fun _update_insertServerCategories(categories: List<ServerCategoryEntity>)

    @Transaction
    open suspend fun insertServerCategories(categories: List<ServerCategoryEntity>) {
        _insert_insertServerCategories(categories)
        _update_insertServerCategories(categories)
    }

    @Query("DELETE FROM categories")
    abstract suspend fun deleteAllSelectableCategories()

    @Query("DELETE FROM server_categories")
    abstract suspend fun deleteAllServerCategories()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_upsertServerCategory(serverCategory: ServerCategoryEntity)

    @Update
    protected abstract suspend fun _update_upsertServerCategory(serverCategory: ServerCategoryEntity)

    @Transaction
    open suspend fun upsertServerCategory(serverCategory: ServerCategoryEntity) {
        _insert_upsertServerCategory(serverCategory)
        _update_upsertServerCategory(serverCategory)
    }

    @Update
    abstract suspend fun upsertServerCategories(serverCategories: List<ServerCategoryEntity>)

    @Query("SELECT * FROM server_categories WHERE category_code = :categoryCode")
    abstract suspend fun getServerCategoryByCode(categoryCode: String): ServerCategoryEntity?

    @Query("SELECT * FROM server_categories")
    abstract suspend fun getAllServerCategories(): List<ServerCategoryEntity>

    @Query("DELETE FROM server_categories WHERE category_code = :categoryCode")
    abstract suspend fun deleteServerCategory(categoryCode: String)

    @Query("DELETE FROM server_categories")
    abstract suspend fun clearServerCategories()

    @Transaction
    open suspend fun insertCategories(
        selectableCategories: List<CategoryEntity>,
        serverCategories: List<ServerCategoryEntity>
    ) {
        deleteAllSelectableCategories()
        deleteAllServerCategories()
        insertSelectableCategories(selectableCategories)
        insertServerCategories(serverCategories)
    }

    @Query("SELECT * FROM server_categories ORDER BY id")
    abstract fun getServerCategoriesFlow(): Flow<List<ServerCategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY id")
    abstract fun getSelectableCategoriesFlow(): Flow<List<CategoryEntity>>

    open fun getCombinedCategoriesFlow(): Flow<List<Any>> {
        return combine(
            getServerCategoriesFlow(),
            getSelectableCategoriesFlow()
        ) { serverCategories, selectableCategories ->
            val combined = mutableListOf<Any>()
            combined.addAll(serverCategories)
            combined.addAll(selectableCategories)
            combined
        }
    }

    @Query("SELECT * FROM categories")
    abstract suspend fun getSelectableCategories(): List<CategoryEntity>

    @Query("DELETE FROM categories")
    abstract suspend fun clearCategories()

    @Query("SELECT * FROM llm_configs")
    abstract suspend fun getAvailableLlmConfigs(): List<AvailableLlmConfigs>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertOrUpdate(configs: List<AvailableLlmConfigs>)

    @Update
    protected abstract suspend fun _update_insertOrUpdate(configs: List<AvailableLlmConfigs>)

    @Transaction
    open suspend fun insertOrUpdate(configs: List<AvailableLlmConfigs>) {
        _insert_insertOrUpdate(configs)
        _update_insertOrUpdate(configs)
    }

    @Query("DELETE FROM llm_configs WHERE code_name NOT IN (:activeCodeNames)")
    abstract suspend fun deleteObsoleteConfigs(activeCodeNames: List<String>)

    @Query("DELETE FROM llm_configs")
    abstract suspend fun clearAll()

    @Transaction
    open suspend fun updateConfigsTransaction(configs: List<AvailableLlmConfigs>) {
        if (configs.isEmpty()) {
            clearAll()
        } else {

            insertOrUpdate(configs)

            val activeCodes = configs.map { it.codeName }
            deleteObsoleteConfigs(activeCodes)
        }
    }

    @Query("SELECT * FROM llm_configs")
    abstract fun getAvailableLlmConfigsFlow(): Flow<List<AvailableLlmConfigs>>
}