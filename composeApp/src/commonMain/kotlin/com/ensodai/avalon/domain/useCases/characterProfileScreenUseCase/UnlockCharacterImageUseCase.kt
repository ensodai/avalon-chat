package com.ensodai.avalon.domain.useCases.characterProfileScreenUseCase

import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.shared.avalon.character.models.UnlockCharacterImageResult
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

class InsufficientEnergyException(
    val requiredEnergy: Int,
    val currentEnergy: Int
) : Exception("Insufficient energy. Required: $requiredEnergy, current: $currentEnergy")

@Single
class UnlockCharacterImageUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun invoke(imageId: Uuid): Result<Unit> {
        return runCatching {
            when (val result = characterRpcService.unlockCharacterImage(imageId)) {
                is UnlockCharacterImageResult.Success -> {
                    localUpdateCharacterRepository.updateImageUnlockState(
                        imageId = imageId,
                        imageUrl = result.image.imageUrl,
                        isUnlocked = result.image.isUnlocked
                    ).getOrThrow()
                }
                is UnlockCharacterImageResult.InsufficientEnergy -> {
                    throw InsufficientEnergyException(
                        requiredEnergy = result.requiredEnergy,
                        currentEnergy = result.currentEnergy
                    )
                }
                is UnlockCharacterImageResult.Error -> {
                    throw IllegalStateException(result.message)
                }
            }
        }
    }
}

