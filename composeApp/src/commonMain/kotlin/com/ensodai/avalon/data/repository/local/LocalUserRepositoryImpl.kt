package com.ensodai.avalon.data.repository.local

import com.ensodai.avalon.data.repository.local.base.BaseRepository
import com.ensodai.avalon.data.repository.local.base.DatabaseExecutor

import com.ensodai.avalon.data.db.dao.UserDao
import com.ensodai.avalon.data.db.dto.AppUserDto
import com.ensodai.avalon.data.db.dto.PersonaDto
import com.ensodai.avalon.data.db.dto.UserTariffInfoDto
import com.ensodai.avalon.data.db.models.user.Persona
import com.ensodai.avalon.data.db.models.user.User
import com.ensodai.shared.avalon.user.models.*
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.avalon.domain.models.PersonaInfo
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.avalon.utils.toUUIDorNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

class LocalUserRepositoryImpl(
    dbExecutor: DatabaseExecutor,
    private val userDao: UserDao,
    private val datastoreManager: DatastoreManager,
) : BaseRepository(dbExecutor), LocalUserRepository {

    override suspend fun updateUser(appUser: AppUserDto?) {
        if (appUser != null) {
            val entity = User(
                id = appUser.id,
                name = appUser.name,
                nickname = appUser.nickName,
                avatarUrl = appUser.avatarUrl,
                currentQuota = appUser.currentQuotaLimit,
                maxQuota = appUser.maxQuotaLimit,
                defaultPersonaId = appUser.defaultPersonaId,
                reserveBalance = appUser.reserveBalance,
                maxReserveBalance = appUser.maxReserveBalance,
                premiumStatus = appUser.premiumStatus,
                paymentProvider = appUser.paymentProvider,
                nextBillingDate = appUser.nextBillingDate,
                isOutreachEnabled = appUser.isOutreachEnabled,
                dailyOutreachLimit = appUser.dailyOutreachLimit,
                isFlowModeEnabled = appUser.isFlowModeEnabled
            )
            dbWrite { userDao.insertUser(entity) }
        }
    }

    override suspend fun updatePersonas(personas: List<PersonaDto>) {
        if (personas.isNotEmpty()) {
            val entities = personas.map { dto ->
                Persona(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    createdAt = dto.createdAt
                )
            }
            dbWrite { userDao.insertPersonas(entities) }
        }
    }

    override suspend fun getSelectedPersonaFlow(): Flow<PersonaInfo?> {
        val userId = datastoreManager.getUserId()?.toUUIDorNull()
        return dbObserve { userDao.getSelectedPersonaFlow(userId) }
    }

    override suspend fun getAllPersonasFlow(): Flow<List<PersonaInfo>> {
        val userId = datastoreManager.getUserId()
        return dbObserve { userDao.getAllPersonasFlow(userId) }
    }

    override suspend fun getPersonaById(id: Uuid): PersonaInfo? {
        return dbRead { userDao.getPersonaInfoById(id) }
    }

    override suspend fun insertPersona(persona: Persona) {
        dbWrite { userDao.insertPersona(persona) }
    }

    override suspend fun deletePersona(id: Uuid) {
        dbWrite { userDao.deletePersonaById(id) }
    }

    override suspend fun selectPersona(id: Uuid?) {
        val userId = datastoreManager.getUserId()?.toUUIDorNull()
        if (userId != null) {
            dbWrite { userDao.updateUserDefaultPersona(userId = userId, personaId = id) }
        }
    }

    override suspend fun getSelectedPersona(): PersonaInfo? {
        val userId = datastoreManager.getUserId()
        return dbRead { userDao.getSelectedPersona(userId) }
    }

    override suspend fun saveUserAccountInfo(response: UserAccountInfoResponse) {
        dbWrite {
            userDao.insertUser(
                User(
                    id = response.id,
                    defaultPersonaId = response.defaultPersona?.id,
                    name = response.name,
                    nickname = response.nickName,
                    avatarUrl = response.avatarUrl,
                    currentQuota = response.currentQuota,
                    maxQuota = response.maxQuota,
                    reserveBalance = response.reserveBalance,
                    maxReserveBalance = response.maxReserveBalance,
                    premiumStatus = response.premiumStatus,
                    paymentProvider = response.paymentProvider,
                    nextBillingDate = response.nextBillingDate,
                    isOutreachEnabled = response.isOutreachEnabled,
                    dailyOutreachLimit = response.dailyOutreachLimit,
                    isFlowModeEnabled = response.isFlowModeEnabled
                )
            )
            val defaultPersona = response.defaultPersona
            if (defaultPersona != null) {
                userDao.insertPersona(
                    Persona(
                        id = defaultPersona.id,
                        name = defaultPersona.name,
                        description = defaultPersona.description,
                        createdAt = defaultPersona.createdAt
                    )
                )
            }
            response.userPersonas.forEach { personaResponse ->
                userDao.insertPersona(
                    Persona(
                        id = personaResponse.id,
                        name = personaResponse.name,
                        description = personaResponse.description,
                        createdAt = personaResponse.createdAt
                    )
                )
            }
        }
    }

    override fun getUserInfoFlow(userId: Uuid): Flow<User?> {
        return cachedObserve("getUserInfoFlow_${userId}") { userDao.getUserInfoFlow(userId) }
    }

    override suspend fun getUserInfo(userId: Uuid): User? {
        return dbRead { userDao.getUserInfo(userId) }
    }

    override suspend fun updateNickname(nickname: String) {
        val userId = datastoreManager.getUserId()
        if (userId != null) {
            dbWrite { userDao.updateNickname(userId = userId, nickname = nickname) }
        }
    }

    override suspend fun updateUserQuota(newQuota: Int) {
        dbWrite { userDao.updateQuota(newQuota) }
    }

    override suspend fun updateUserTariffSubscriptionInfo(tariffStatus: SubscriptionStatusResponse) {
        dbWrite {
            userDao.updateSubscriptionInfo(
                userId = tariffStatus.userId,
                paymentProvider = tariffStatus.paymentProvider,
                premiumStatus = tariffStatus.premiumStatus,
                maxQuota = tariffStatus.maxDailyEnergy,
                currentQuota = tariffStatus.currentDailyEnergy,
                nextBillingDate = tariffStatus.nextBillingDate
            )
        }
    }

    override suspend fun getUserTariff(): UserTariffInfoDto? {
        val userId = datastoreManager.getUserId()?.toUUIDorNull() ?: return null
        return dbRead { userDao.getUserTariff(userId) }
    }

    override fun getUserTariffFlow(): Flow<UserTariffInfoDto?> {
        return datastoreManager.getUserIdFlow()
            .map { it?.toUUIDorNull() }
            .flatMapLatest { userId ->
                if (userId != null) {
                    dbObserve { userDao.getUserTariffFlow(userId) }
                } else {
                    flowOf(null)
                }
            }
    }

    override fun getCurrentUserFlow(): Flow<User?> {
        return datastoreManager.getUserIdFlow()
            .map { it?.toUUIDorNull() }
            .flatMapLatest { userId ->
                if (userId != null) {
                    dbObserve { userDao.getUserFlow(userId) }
                } else {
                    flowOf(null)
                }
            }
    }

    override suspend fun updateReserveBalance(reserveBalance: Int) {
        dbWrite { userDao.updateReserveBalance(reserveBalance) }
    }

    override suspend fun updateUserOutreachSettings(isOutreachEnabled: Boolean, dailyOutreachLimit: Int) {
        val userId = datastoreManager.getUserId()?.toUUIDorNull()
        if (userId != null) {
            dbWrite { userDao.updateOutreachSettings(userId = userId, isOutreachEnabled = isOutreachEnabled, dailyOutreachLimit = dailyOutreachLimit) }
        }
    }

    override suspend fun updateUserFlowMode(isEnabled: Boolean) {
        dbWrite { userDao.updateFlowMode(isEnabled) }
    }
}
