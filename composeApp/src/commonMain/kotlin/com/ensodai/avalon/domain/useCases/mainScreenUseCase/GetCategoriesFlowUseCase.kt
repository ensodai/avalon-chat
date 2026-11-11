package com.ensodai.avalon.domain.useCases.mainScreenUseCase

import com.ensodai.avalon.data.db.dto.CategoryItemDto
import com.ensodai.avalon.domain.repository.local.LocalCategoryRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetCategoriesFlowUseCase(private val localCategoryRepository: LocalCategoryRepository) {

    fun get(): Flow<List<CategoryItemDto>> {
        return localCategoryRepository.getCombinedCategoriesFlow()
    }
}