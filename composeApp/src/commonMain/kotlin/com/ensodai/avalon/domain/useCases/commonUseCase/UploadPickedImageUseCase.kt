package com.ensodai.avalon.domain.useCases.commonUseCase

import com.ensodai.shared.avalon.rpc.services.FileUploadRpcService
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.compressImageIfNeeded
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

data class PreparedUploadedImage(
    val localId: String,
    val fileName: String,
    val contentType: String,
    val bytes: ByteArray,
    val uploadToken: String,
    val imageUrl: String,
    val thumbHash: String?
)

@Single
class UploadPickedImageUseCase(
    private val fileUploadRpcService: FileUploadRpcService,
    private val logger: AppLogger
) {

    private val supportedContentTypes = setOf(
        "image/jpeg",
        "image/png",
        "image/webp"
    )

    suspend fun uploadCharacterImage(file: PlatformFile): Result<PreparedUploadedImage> {
        return uploadImage(
            file = file,
            type = "character"
        )
    }

    suspend fun uploadWelcomeCardImage(file: PlatformFile): Result<PreparedUploadedImage> {
        return uploadImage(
            file = file,
            type = "character" // На бэкенде тип 'character' используется и для велкам карт
        )
    }

    suspend fun uploadLorebookCoverImage(file: PlatformFile): Result<PreparedUploadedImage> {
        return uploadImage(
            file = file,
            type = "character"
        )
    }

    private suspend fun uploadImage(
        file: PlatformFile,
        type: String
    ): Result<PreparedUploadedImage> {
        return runCatching {
            val originalBytes = file.readBytes()
            val bytes = compressImageIfNeeded(originalBytes)
            val isCompressed = bytes.size != originalBytes.size
            
            val localId = Uuid.random().toString()
            val fileName = if (isCompressed) {
                val baseName = file.name.ifBlank { "image-$localId" }.substringBeforeLast('.')
                "$baseName.jpg"
            } else {
                file.name.ifBlank { "image-$localId" }
            }
            
            val contentType = if (isCompressed) {
                "image/jpeg"
            } else {
                file.mimeType().toString().substringBefore(';').lowercase()
                    .ifBlank { "application/octet-stream" }
            }

            require(contentType in supportedContentTypes) {
                "Поддерживаются только JPEG и PNG. Выбранный формат: $contentType"
            }

            val uploadResult = fileUploadRpcService.uploadImage(
                type = type,
                fileName = fileName,
                content = bytes
            )

            logger.d(
                "UploadPickedImageUseCase",
                "Uploaded image $fileName via RPC, contentType=$contentType, size=${bytes.size}"
            )

            PreparedUploadedImage(
                localId = localId,
                fileName = fileName,
                contentType = contentType,
                bytes = bytes,
                uploadToken = uploadResult.token,
                imageUrl = uploadResult.imageUrl,
                thumbHash = uploadResult.thumbHash
            )
        }
    }
}
