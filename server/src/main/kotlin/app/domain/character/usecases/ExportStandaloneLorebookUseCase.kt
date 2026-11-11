@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package app.domain.character.usecases

import app.data.repository.CharacterSpecRepository
import com.ensodai.shared.avalon.character.models.ExportStandaloneLorebookResponse
import org.koin.core.annotation.Factory
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

@Factory
class ExportStandaloneLorebookUseCase(
    private val characterSpecRepository: CharacterSpecRepository
) {
    suspend fun execute(lorebookId: Uuid, userId: Uuid): ExportStandaloneLorebookResponse {
        val lorebook = characterSpecRepository.getLorebookById(lorebookId, userId)
            ?: throw NoSuchElementException("Книга знаний не найдена")

        val jsonString = characterSpecRepository.exportLorebookToJson(lorebookId, userId)
        val fileBytes = jsonString.encodeToByteArray()
        val base64Content = Base64.Default.encode(fileBytes)

        val safeName = lorebook.name.replace(Regex("[^a-zA-Z0-9а-яА-Я._-]"), "_").take(50)
        return ExportStandaloneLorebookResponse(
            fileBase64 = base64Content,
            fileName = "${safeName}_lorebook.json",
            mimeType = "application/json"
        )
    }
}
