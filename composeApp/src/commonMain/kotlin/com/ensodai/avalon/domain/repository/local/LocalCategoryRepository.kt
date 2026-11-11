package com.ensodai.avalon.domain.repository.local

import com.ensodai.avalon.data.db.dto.CategoryItemDto
import kotlinx.coroutines.flow.Flow

interface LocalCategoryRepository {
    fun getCombinedCategoriesFlow(): Flow<List<CategoryItemDto>>
    suspend fun getSelectableCategories(): List<CategoryItemDto>
    suspend fun saveCategories(
        selectableCategories: List<com.ensodai.avalon.data.db.cache.CategoryEntity>,
        serverCategories: List<com.ensodai.avalon.data.db.cache.ServerCategoryEntity>
    )
    suspend fun updateServerCategories(serverCategories: List<com.ensodai.avalon.data.db.cache.ServerCategoryEntity>)
}
