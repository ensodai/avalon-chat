package com.ensodai.avalon.domain.useCases.userAccountScreenUseCase

import com.ensodai.avalon.data.network.Resource
import com.ensodai.avalon.domain.models.UserAccountInfo
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.shared.avalon.rpc.services.UserRpcService
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.koin.core.annotation.Single

@Single
class GetUserAccountInfoUseCase(
    private val userRpcService: UserRpcService,
    private val localUserRepository: LocalUserRepository,
    private val localCharacterRepository: LocalCharacterRepository
) {

    operator fun invoke(): Flow<Resource<UserAccountInfo>> = flow {
        // Сразу сообщаем о начале загрузки
        emit(Resource.Loading)

        val apiResult = runCatching {
            userRpcService.getUserAccountInfo()
        }

        apiResult.onSuccess { response ->
            // ПРИ УСПЕХЕ: Сохраняем свежие данные в базу через local-репозиторий
            localUserRepository.saveUserAccountInfo(response)

            // Данные, которые мы не храним в базе, но они нужны для UI
            val liveAvatarUrl = response.avatarUrl

            // Начинаем слушать БАЗУ ДАННЫХ и количество персонажей через combine
            val combinedFlow = combine(
                localUserRepository.getUserInfoFlow(response.id).filterNotNull(),
                localCharacterRepository.getUserCharactersCountFlow()
            ) { userFromDb, count ->
                // Объединяем реактивные данные из базы с "живыми" данными из сети и счётчиком
                val finalUserAccountInfo = UserAccountInfo(
                    id = userFromDb.id,
                    name = userFromDb.name,
                    nickName = userFromDb.nickname,
                    avatarUrl = liveAvatarUrl,
                    currentQuota = userFromDb.currentQuota,
                    maxQuota = userFromDb.maxQuota,
                    reserveBalance = userFromDb.reserveBalance,
                    maxReserveBalance = userFromDb.maxReserveBalance,
                    premiumStatus = userFromDb.premiumStatus,
                    nextBillingDate = userFromDb.nextBillingDate,
                    paymentProvider = userFromDb.paymentProvider,
                    charactersCount = count,
                    totalUnclaimedCreatorEnergy = response.totalUnclaimedCreatorEnergy,
                    isOutreachEnabled = userFromDb.isOutreachEnabled,
                    dailyOutreachLimit = userFromDb.dailyOutreachLimit,
                    isEarlySupporter = response.isEarlySupporter
                )

                Resource.Success(finalUserAccountInfo)
            }

            emitAll(combinedFlow)

        }.onFailure { exception ->
            emit(Resource.Error(exception))
        }

    }.flowOn(IODispatcher)
}
