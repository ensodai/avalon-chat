package com.ensodai.avalon.domain.useCases.commonUseCase

import com.ensodai.avalon.data.db.models.user.User
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetCurrentUserUseCase(
    private val localUserRepository: LocalUserRepository
) {
    operator fun invoke(): Flow<User?> {
        return localUserRepository.getCurrentUserFlow()
    }
}
