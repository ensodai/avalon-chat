package app.apiv4.services.impl

import app.domain.category.usecases.GetCategoriesUseCase
import app.domain.category.usecases.GetCategoryCountsUseCase
import app.domain.category.usecases.GetCombinedCategoriesUseCase
import app.domain.category.usecases.GetSelectableCategoriesUseCase
import com.ensodai.shared.avalon.category.models.SelectableCategoryResponse
import com.ensodai.shared.avalon.category.models.ServerCategoryResponse
import com.ensodai.shared.avalon.category.models.CombinedCategoriesResponse
import com.ensodai.shared.avalon.rpc.services.CategoryRpcService
import app.apiv4.infrastructure.BaseRpcService
import app.apiv4.infrastructure.RpcSessionContext
import org.koin.core.component.KoinComponent
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
class CategoryServiceImpl(
    @InjectedParam session: RpcSessionContext,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getSelectableCategoriesUseCase: GetSelectableCategoriesUseCase,
    private val getCombinedCategoriesUseCase: GetCombinedCategoriesUseCase,
    private val getCategoryCountsUseCase: GetCategoryCountsUseCase
) : BaseRpcService(session), CategoryRpcService, KoinComponent {

    override suspend fun getCategories(): List<SelectableCategoryResponse> {
        ensureAuthorized()
        return getCategoriesUseCase.execute()
    }

    override suspend fun getSelectableCategories(): List<SelectableCategoryResponse> {
        ensureAuthorized()
        return getSelectableCategoriesUseCase.execute()
    }

    override suspend fun getCombinedCategories(): CombinedCategoriesResponse {
        ensureAuthorized()
        return getCombinedCategoriesUseCase.execute()
    }

    override suspend fun getCategoryCounts(): List<ServerCategoryResponse> {
        ensureAuthorized()
        return getCategoryCountsUseCase.execute()
    }
}
