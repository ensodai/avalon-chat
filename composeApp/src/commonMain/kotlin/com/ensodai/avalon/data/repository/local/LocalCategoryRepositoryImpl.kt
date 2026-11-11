package com.ensodai.avalon.data.repository.local

import com.ensodai.avalon.data.repository.local.base.BaseRepository
import com.ensodai.avalon.data.repository.local.base.DatabaseExecutor

import com.ensodai.avalon.data.db.cache.CategoryEntity
import com.ensodai.avalon.data.db.cache.ServerCategoryEntity
import com.ensodai.avalon.data.db.dao.CacheDao
import com.ensodai.avalon.data.db.dto.CategoryItemDto
import com.ensodai.avalon.data.db.dto.SelectableCategoryDto
import com.ensodai.avalon.data.db.dto.ServerCategoryDto
import com.ensodai.avalon.domain.repository.local.LocalCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalCategoryRepositoryImpl(
    dbExecutor: DatabaseExecutor,
    private val cacheDao: CacheDao
) : BaseRepository(dbExecutor), LocalCategoryRepository {

    override fun getCombinedCategoriesFlow(): Flow<List<CategoryItemDto>> {
        return dbObserve { cacheDao.getCombinedCategoriesFlow() }
            .map { combinedList ->
                combinedList.map { entity ->
                    when (entity) {
                        is ServerCategoryEntity -> ServerCategoryDto(
                            id = entity.id,
                            categoryCode = entity.categoryCode,
                            localizedName = entity.localizedName,
                            count = entity.count
                        )

                        is CategoryEntity -> SelectableCategoryDto(
                            id = entity.id,
                            categoryCode = entity.categoryCode,
                            localizedName = entity.localizedName
                        )

                        else -> throw IllegalArgumentException("Unknown entity type")
                    }
                }
            }
    }

    override suspend fun getSelectableCategories(): List<CategoryItemDto> {
        return dbRead {
            cacheDao.getSelectableCategories()
                .map { entity ->
                    SelectableCategoryDto(
                        id = entity.id,
                        categoryCode = entity.categoryCode,
                        localizedName = entity.localizedName
                    )
                }
        }
    }

    override suspend fun saveCategories(
        selectableCategories: List<CategoryEntity>,
        serverCategories: List<ServerCategoryEntity>
    ) {
        dbWrite {
            cacheDao.insertCategories(
                selectableCategories = selectableCategories,
                serverCategories = serverCategories
            )
        }
    }

    override suspend fun updateServerCategories(serverCategories: List<ServerCategoryEntity>) {
        dbWrite { cacheDao.upsertServerCategories(serverCategories) }
    }
}
