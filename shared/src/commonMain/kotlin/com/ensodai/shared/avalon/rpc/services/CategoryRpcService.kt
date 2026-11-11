package com.ensodai.shared.avalon.rpc.services

import com.ensodai.shared.avalon.category.models.*
import kotlinx.rpc.annotations.Rpc

@Rpc
interface CategoryRpcService {
    suspend fun getCategories(): List<SelectableCategoryResponse>
    suspend fun getSelectableCategories(): List<SelectableCategoryResponse>
    suspend fun getCombinedCategories(): CombinedCategoriesResponse
    suspend fun getCategoryCounts(): List<ServerCategoryResponse>
}
