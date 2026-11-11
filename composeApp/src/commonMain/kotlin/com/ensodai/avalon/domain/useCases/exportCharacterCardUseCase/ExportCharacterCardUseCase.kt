package com.ensodai.avalon.domain.useCases.exportCharacterCardUseCase

import com.ensodai.shared.avalon.character.models.CharacterCardExportFormat
import com.ensodai.shared.avalon.character.models.ExportCharacterCardResponse
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.FileSaver
import org.koin.core.annotation.Single
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.Uuid

@Single
class ExportCharacterCardUseCase(
    private val characterRpcService: CharacterRpcService,
    private val fileSaver: FileSaver,
    private val logger: AppLogger
) {
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun exportAndSave(
        characterId: Uuid,
        format: CharacterCardExportFormat = CharacterCardExportFormat.PNG_V3
    ): Result<String> = runCatching {
        logger.d("ExportCharacterCardUseCase", "Запрос экспорта карточки $characterId в формате $format")
        val response = characterRpcService.exportCharacterCard(characterId, format)
        val fileBytes = Base64.Default.decode(response.fileBase64)

        val saveResult = fileSaver.saveFile(
            bytes = fileBytes,
            fileName = response.fileName,
            mimeType = response.mimeType
        )
        saveResult.getOrThrow()
    }

    suspend fun execute(
        characterId: Uuid,
        format: CharacterCardExportFormat = CharacterCardExportFormat.PNG_V3
    ): ExportCharacterCardResponse {
        logger.d("ExportCharacterCardUseCase", "Запрос экспорта карточки $characterId в формате $format")
        return characterRpcService.exportCharacterCard(characterId, format)
    }
}
