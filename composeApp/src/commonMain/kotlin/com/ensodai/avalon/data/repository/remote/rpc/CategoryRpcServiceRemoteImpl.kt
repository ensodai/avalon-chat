package com.ensodai.avalon.data.repository.remote.rpc

import com.ensodai.shared.avalon.category.models.*
import com.ensodai.shared.avalon.rpc.services.CategoryRpcService
import com.ensodai.avalon.di.RpcConnectionManager
import kotlinx.rpc.withService

open class CategoryRpcServiceRemoteImpl(
    private val manager: RpcConnectionManager,
    private val executor: RpcCallExecutor
) : CategoryRpcService {

    open suspend fun getService(): CategoryRpcService = manager.getActiveClient().withService<CategoryRpcService>()

    override suspend fun getCategories(): List<SelectableCategoryResponse> =
        executor.execute("getCategories", getService = { getService() }) { getCategories() }

    override suspend fun getSelectableCategories(): List<SelectableCategoryResponse> =
        executor.execute("getSelectableCategories", getService = { getService() }) { getSelectableCategories() }

    override suspend fun getCombinedCategories(): CombinedCategoriesResponse =
        executor.execute("getCombinedCategories", getService = { getService() }) { getCombinedCategories() }

    override suspend fun getCategoryCounts(): List<ServerCategoryResponse> =
        executor.execute("getCategoryCounts", getService = { getService() }) { getCategoryCounts() }
}
