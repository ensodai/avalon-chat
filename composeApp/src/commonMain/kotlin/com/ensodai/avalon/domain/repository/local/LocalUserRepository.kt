package com.ensodai.avalon.domain.repository.local

import com.ensodai.avalon.data.db.dto.AppUserDto
import com.ensodai.avalon.data.db.dto.PersonaDto
import com.ensodai.avalon.data.db.dto.UserTariffInfoDto
import com.ensodai.avalon.data.db.models.user.Persona
import com.ensodai.avalon.data.db.models.user.User
import com.ensodai.shared.avalon.user.models.*
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.avalon.domain.models.PersonaInfo
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface LocalUserRepository {

    suspend fun updateUser(appUser: AppUserDto?)

    suspend fun updatePersonas(personas: List<PersonaDto>)

    suspend fun getSelectedPersonaFlow(): Flow<PersonaInfo?>

    suspend fun getSelectedPersona(): PersonaInfo?

    suspend fun getAllPersonasFlow(): Flow<List<PersonaInfo>>

    suspend fun getPersonaById(id: Uuid): PersonaInfo?

    suspend fun insertPersona(persona: Persona)

    suspend fun deletePersona(id: Uuid)

    suspend fun selectPersona(id: Uuid?)

    suspend fun saveUserAccountInfo(response: UserAccountInfoResponse)

    fun getUserInfoFlow(userId: Uuid): Flow<User?>

    suspend fun getUserInfo(userId: Uuid): User?

    suspend fun updateNickname(nickname: String)

    suspend fun updateUserQuota(newQuota: Int)

    suspend fun updateUserTariffSubscriptionInfo(tariffStatus: SubscriptionStatusResponse)

    suspend fun getUserTariff(): UserTariffInfoDto?

    fun getUserTariffFlow(): Flow<UserTariffInfoDto?>

    fun getCurrentUserFlow(): Flow<User?>

    suspend fun updateReserveBalance(reserveBalance: Int)
    
    suspend fun updateUserOutreachSettings(isOutreachEnabled: Boolean, dailyOutreachLimit: Int)
    
    suspend fun updateUserFlowMode(isEnabled: Boolean)
}