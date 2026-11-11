package app.domain.category.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.category.models.SelectableCategoryResponse
import app.data.repository.CategoriesRepository

@Factory
class GetCategoriesUseCase(private val categoriesRepository: CategoriesRepository) {
    suspend fun execute(): List<SelectableCategoryResponse> {
        return categoriesRepository.getSelectableCategories()
    }
}
