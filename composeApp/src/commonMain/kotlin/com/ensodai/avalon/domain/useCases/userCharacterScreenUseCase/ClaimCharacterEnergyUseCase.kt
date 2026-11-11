@file:OptIn(ExperimentalUuidApi::class)

package com.ensodai.avalon.domain.useCases.userCharacterScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.shared.avalon.character.models.ClaimEnergyResponse
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class ClaimCharacterEnergyUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localCharacterRepository: LocalCharacterRepository,
    private val localUserRepository: LocalUserRepository
) {
    suspend operator fun invoke(characterId: Uuid): Result<ClaimEnergyResponse> {
        return runCatching {
            val response = characterRpcService.claimEnergy(characterId)
            
            // Обновляем балансы локально в Room
            localCharacterRepository.updateCharacterUnclaimedEnergy(
                characterId = characterId,
                unclaimedEnergy = response.remainingUnclaimedEnergy
            )
            localUserRepository.updateReserveBalance(
                reserveBalance = response.userReserveBalance
            )
            
            response
        }
    }
}
