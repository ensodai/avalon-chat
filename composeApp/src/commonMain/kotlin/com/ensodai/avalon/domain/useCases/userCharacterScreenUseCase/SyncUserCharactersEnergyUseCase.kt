@file:OptIn(ExperimentalUuidApi::class)

package com.ensodai.avalon.domain.useCases.userCharacterScreenUseCase

import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi

@Single
class SyncUserCharactersEnergyUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localCharacterRepository: LocalCharacterRepository
) {
    suspend operator fun invoke() {
        runCatching {
            val energies = characterRpcService.getCharactersEnergy()
            energies.forEach { energy ->
                localCharacterRepository.updateCharacterUnclaimedEnergy(
                    characterId = energy.characterId,
                    unclaimedEnergy = energy.unclaimedEnergy
                )
            }
        }
    }
}
