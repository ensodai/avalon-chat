package com.ensodai.avalon.domain.useCases.liveCall

import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetLiveCallCharacterInfoUseCase(
    private val localCharacterRepository: LocalCharacterRepository
) {

    suspend operator fun invoke(characterId: Uuid): LiveCallCharacterInfo {
        val data = localCharacterRepository.getCharacterNameAndAvatar(characterId)
        return LiveCallCharacterInfo(
            characterId = characterId,
            name = data?.name ?: "Unknown",
            avatarUrl = data?.avatarUrl
        )
    }
}

data class LiveCallCharacterInfo(
    val characterId: Uuid,
    val name: String,
    val avatarUrl: String?
)
