package app.domain.category.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.category.models.SelectableCategoryResponse
import app.data.repository.SelectableCategoriesRepository

@Factory
class GetSelectableCategoriesUseCase(private val selectableCategoriesRepository: SelectableCategoriesRepository) {
    suspend fun execute(): List<SelectableCategoryResponse> {
        return selectableCategoriesRepository.getSelectableCategories()
    }
}
