package com.ensodai.avalon.domain.useCases.mainScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Single

@Single
class CheckSavedUserChatsUseCase(
    private val localCharacterRepository: LocalCharacterRepository
) {

    fun check(): Flow<Boolean> {
        return flowOf(true)
    }
}