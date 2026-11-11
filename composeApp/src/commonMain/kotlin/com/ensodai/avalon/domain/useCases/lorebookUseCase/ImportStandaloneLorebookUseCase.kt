package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.CharacterLorebookDetailsDto
import com.ensodai.shared.avalon.character.models.ImportStandaloneLorebookRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.utils.AppLogger
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ImportStandaloneLorebookUseCase(
    private val characterRpcService: CharacterRpcService,
    private val logger: AppLogger
) {
    suspend fun execute(
        fileBase64: String,
        attachToCharacterId: Uuid? = null,
        isPublic: Boolean = false
    ): Result<CharacterLorebookDetailsDto> = runCatching {
        logger.d("ImportStandaloneLorebookUseCase", "Отправка запроса на импорт лорбука...")
        val request = ImportStandaloneLorebookRequest(
            fileBase64 = fileBase64,
            attachToCharacterId = attachToCharacterId,
            isPublic = isPublic
        )
        characterRpcService.importStandaloneLorebook(request)
    }
}
