package app.domain.category.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.category.models.ServerCategoryResponse
import app.data.repository.ServerCategoriesRepository
import app.service.CharacterService

@Factory
class GetCategoryCountsUseCase(
    private val serverCategoriesRepository: ServerCategoriesRepository,
    private val characterService: CharacterService
) {
    suspend fun execute(): List<ServerCategoryResponse> {
        return try {
            val allCount = characterService.getAllCharactersCount()
            val newCount = characterService.getNewCharactersCount()

            val serverCategories = serverCategoriesRepository.getServerCategories()

            serverCategories.map { category ->
                when (category.categoryCode) {
                    "all" -> category.copy(count = allCount)
                    "new" -> category.copy(count = newCount)
                    else -> category
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Failed to get category counts")
        }
    }
}
