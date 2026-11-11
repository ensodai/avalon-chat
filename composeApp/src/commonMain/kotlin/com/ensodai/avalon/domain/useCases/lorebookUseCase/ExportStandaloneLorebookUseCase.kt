@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.ExportStandaloneLorebookResponse
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.FileSaver
import org.koin.core.annotation.Single
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

@Single
class ExportStandaloneLorebookUseCase(
    private val characterRpcService: CharacterRpcService,
    private val fileSaver: FileSaver,
    private val logger: AppLogger
) {
    suspend fun exportAndSave(lorebookId: Uuid): Result<String> = runCatching {
        logger.d("ExportStandaloneLorebookUseCase", "Запрос экспорта книги знаний $lorebookId")
        val response = characterRpcService.exportStandaloneLorebook(lorebookId)
        val fileBytes = Base64.Default.decode(response.fileBase64)

        val saveResult = fileSaver.saveFile(
            bytes = fileBytes,
            fileName = response.fileName,
            mimeType = response.mimeType
        )
        saveResult.getOrThrow()
    }

    suspend fun execute(lorebookId: Uuid): ExportStandaloneLorebookResponse {
        logger.d("ExportStandaloneLorebookUseCase", "Запрос экспорта книги знаний $lorebookId")
        return characterRpcService.exportStandaloneLorebook(lorebookId)
    }
}
