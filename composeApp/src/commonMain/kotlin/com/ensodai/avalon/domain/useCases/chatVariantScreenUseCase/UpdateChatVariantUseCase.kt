package com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase

import com.ensodai.shared.avalon.chat.models.UpdateChatVariantRequest
import com.ensodai.shared.avalon.chat.services.ChatRpcService
import com.ensodai.shared.avalon.rpc.services.FileUploadRpcService
import com.ensodai.avalon.data.db.dto.CharacterImage
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.utils.createUploadFile
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateChatVariantUseCase(
    private val chatRpcService: ChatRpcService,
    private val localCharacterRepository: LocalCharacterRepository,
    private val fileUploadRpcService: FileUploadRpcService
) {

    suspend fun updateImage(
        newImage: CharacterImageDTO?,
        variantId: Uuid,
        currentImage: CharacterImage?
    ): Result<Unit> {
        if (currentImage?.url == newImage?.url) return Result.success(Unit)

        val isRemoveImage = currentImage != null && newImage == null

        val imageToken = if (!isRemoveImage && newImage?.url != null) {
            val uploadFile = createUploadFile(newImage.url, "story_image")
                ?: return Result.failure(IllegalStateException("Failed to create upload file for ${newImage.url}"))
            runCatching {
                fileUploadRpcService.uploadImage(
                    type = "character",
                    fileName = uploadFile.fileName,
                    content = uploadFile.bytesProvider()
                ).token
            }.getOrElse { return Result.failure(it) }
        } else {
            null
        }

        return runCatching {
            chatRpcService.updateChatVariant(
                UpdateChatVariantRequest(
                    variantId = variantId,
                    name = null,
                    imageToken = imageToken,
                    removeImage = isRemoveImage
                )
            )
        }.fold(
            onSuccess = { result ->
                localCharacterRepository.updateChatVariantImage(result)
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun updateName(
        name: String,
        variantId: Uuid
    ): Result<Unit> {
        return runCatching {
            chatRpcService.updateChatVariant(
                UpdateChatVariantRequest(
                    variantId = variantId,
                    name = name,
                    imageToken = null,
                    removeImage = false
                )
            )
        }.fold(
            onSuccess = { result ->
                localCharacterRepository.updateChatVariantName(result)
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )
    }
}

