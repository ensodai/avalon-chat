package app.domain.category.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.category.models.CombinedCategoriesResponse
import app.data.repository.SelectableCategoriesRepository
import app.data.repository.ServerCategoriesRepository
import app.service.CharacterService

@Factory
class GetCombinedCategoriesUseCase(
    private val selectableCategoriesRepository: SelectableCategoriesRepository,
    private val serverCategoriesRepository: ServerCategoriesRepository,
    private val characterService: CharacterService
) {
    suspend fun execute(): CombinedCategoriesResponse {
        val selectableCategories = selectableCategoriesRepository.getSelectableCategories()
        val serverCategories = serverCategoriesRepository.getServerCategories()

        val allCount = characterService.getAllCharactersCount()
        val newCount = characterService.getNewCharactersCount()

        val updatedServerCategories = serverCategories.map { category ->
            when (category.categoryCode) {
                "all" -> category.copy(count = allCount)
                "new" -> category.copy(count = newCount)
                else -> category
            }
        }

        return CombinedCategoriesResponse(
            selectableCategories = selectableCategories,
            serverCategories = updatedServerCategories
        )
    }
}
