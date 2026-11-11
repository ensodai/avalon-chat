package com.ensodai.avalon.domain.useCases.addCharacterScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalCategoryRepository
import org.koin.core.annotation.Single

@Single
class GetCategoriesUseCase(private val localCategoryRepository: LocalCategoryRepository) {

    suspend fun get() = localCategoryRepository.getSelectableCategories()

}
