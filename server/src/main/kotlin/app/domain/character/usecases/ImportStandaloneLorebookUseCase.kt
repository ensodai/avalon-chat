@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package app.domain.character.usecases

import app.data.repository.CharacterRepository
import app.data.repository.CharacterSpecRepository
import com.ensodai.shared.avalon.character.models.CharacterLorebookDetailsDto
import com.ensodai.shared.avalon.character.models.ImportStandaloneLorebookRequest
import com.ensodai.shared.avalon.character.spec.StandaloneLorebookParser
import org.koin.core.annotation.Factory
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

@Factory
class ImportStandaloneLorebookUseCase(
    private val characterRepository: CharacterRepository,
    private val characterSpecRepository: CharacterSpecRepository
) {
    suspend fun execute(request: ImportStandaloneLorebookRequest, userId: Uuid): CharacterLorebookDetailsDto {
        val rawBase64 = request.fileBase64.substringAfter("base64,")
        val fileBytes = runCatching {
            Base64.Default.decode(rawBase64)
        }.getOrElse {
            throw IllegalArgumentException("Не удалось декодировать Base64 содержимое файла: ${it.message}")
        }

        val jsonString = runCatching { fileBytes.decodeToString() }.getOrElse {
            throw IllegalArgumentException("Файл не является валидным UTF-8 текстом: ${it.message}")
        }

        val createRequest = StandaloneLorebookParser.parse(
            jsonString = jsonString,
            fallbackName = "Импортированная база знаний",
            isPublic = request.isPublic,
            attachToCharacterId = request.attachToCharacterId
        )

        val createdBook = characterSpecRepository.createStandaloneLorebook(userId, createRequest)

        // Если было запрошено прикрепление к существующему персонажу
        if (request.attachToCharacterId != null) {
            val charId = request.attachToCharacterId!!
            runCatching {
                val character = characterRepository.getCharacterById(charId, userId)
                if (character.canEdit) {
                    // Автор персонажа -> каноническая привязка
                    characterSpecRepository.attachLorebook(
                        characterId = charId,
                        lorebookId = createdBook.id,
                        isEnabled = true,
                        isPrimary = false,
                        priority = 0,
                        userId = userId
                    )
                } else {
                    // Игрок/гость -> пользовательская привязка
                    characterSpecRepository.attachUserLorebook(
                        characterId = charId,
                        lorebookId = createdBook.id,
                        userId = userId
                    )
                }
            }
        }

        return createdBook
    }
}
