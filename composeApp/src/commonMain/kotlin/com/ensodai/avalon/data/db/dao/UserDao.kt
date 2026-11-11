package com.ensodai.avalon.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.ensodai.avalon.data.db.dto.UserTariffInfoDto
import com.ensodai.avalon.data.db.models.user.Persona
import com.ensodai.avalon.data.db.models.user.User
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.avalon.domain.models.PersonaInfo
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
abstract class UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertUser(user: User)

    @Update
    protected abstract suspend fun _update_insertUser(user: User)

    @Transaction
    open suspend fun insertUser(user: User) {
        _insert_insertUser(user)
        _update_insertUser(user)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertPersonas(personas: List<Persona>)

    @Update
    protected abstract suspend fun _update_insertPersonas(personas: List<Persona>)

    @Transaction
    open suspend fun insertPersonas(personas: List<Persona>) {
        _insert_insertPersonas(personas)
        _update_insertPersonas(personas)
    }

    @Update
    abstract suspend fun update(user: User)

    @Query("UPDATE user SET default_persona_id = :personaId WHERE id = :userId")
    abstract suspend fun updateUserDefaultPersona(userId: Uuid, personaId: Uuid?)

    @Query("UPDATE user SET nickname = :nickname WHERE id = :userId")
    abstract suspend fun updateNickname(userId: String, nickname: String)

    @Query("UPDATE user SET is_outreach_enabled = :isOutreachEnabled, daily_outreach_limit = :dailyOutreachLimit WHERE id = :userId")
    abstract suspend fun updateOutreachSettings(
        userId: Uuid,
        isOutreachEnabled: Boolean,
        dailyOutreachLimit: Int
    )

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertPersona(persona: Persona)

    @Update
    protected abstract suspend fun _update_insertPersona(persona: Persona)

    @Transaction
    open suspend fun insertPersona(persona: Persona) {
        _insert_insertPersona(persona)
        _update_insertPersona(persona)
    }

    @Query("SELECT * FROM user WHERE id = :userId")
    abstract fun getUserInfoFlow(userId: Uuid): Flow<User?>

    @Query("SELECT * FROM user WHERE id = :userId")
    abstract suspend fun getUserInfo(userId: Uuid): User?

    @Query(
        """
        SELECT p.id, p.name, p.description, 1 AS isSelected
        FROM persona p
        INNER JOIN user u ON p.id = u.default_persona_id
        WHERE u.id = :userId
        """
    )
    abstract fun getSelectedPersonaFlow(userId: Uuid?): Flow<PersonaInfo?>

    @Query(
        """
        SELECT p.id, p.name, p.description, 1 AS isSelected
        FROM persona p
        INNER JOIN user u ON p.id = u.default_persona_id
        WHERE u.id = :userId
        """
    )
    abstract suspend fun getSelectedPersona(userId: String?): PersonaInfo?

    @Query(
        """
    SELECT p.id, p.name, p.description,
           CASE WHEN p.id = u.default_persona_id THEN 1 ELSE 0 END AS isSelected
    FROM persona p
    LEFT JOIN user u ON u.id = :userId
    ORDER BY p.created_at ASC
    """
    )
    abstract fun getAllPersonasFlow(userId: String?): Flow<List<PersonaInfo>>

    @Query("SELECT id, name, description, 1 AS isSelected FROM persona WHERE id = :id")
    abstract suspend fun getPersonaInfoById(id: Uuid): PersonaInfo?

    @Query("DELETE FROM persona WHERE id = :id")
    abstract suspend fun deletePersonaById(id: Uuid): Int

    @Query("UPDATE user SET current_quota = :newQuota")
    abstract suspend fun updateQuota(newQuota: Int)

    @Query(
        """
        UPDATE user SET 
            payment_provider = :paymentProvider,
            premium_status = :premiumStatus,
            max_quota = :maxQuota,
            current_quota = :currentQuota,
            next_billing_date = :nextBillingDate
        WHERE id = :userId
        """
    )
    abstract suspend fun updateSubscriptionInfo(
        userId: Uuid,
        paymentProvider: SubscriptionProvider?,
        premiumStatus: PremiumStatus,
        maxQuota: Int,
        currentQuota: Int,
        nextBillingDate: Instant?
    )

    @Query("SELECT premium_status AS premiumStatus, payment_provider AS paymentProvider FROM user WHERE id = :userId")
    abstract suspend fun getUserTariff(userId: Uuid): UserTariffInfoDto?

    @Query("SELECT premium_status AS premiumStatus, payment_provider AS paymentProvider FROM user WHERE id = :userId")
    abstract fun getUserTariffFlow(userId: Uuid): Flow<UserTariffInfoDto?>

    @Query("SELECT * FROM user WHERE id = :userId")
    abstract fun getUserFlow(userId: Uuid): Flow<User?>

    @Query("UPDATE user SET reserve_balance = :reserveBalance")
    abstract suspend fun updateReserveBalance(reserveBalance: Int)

    @Query("UPDATE user SET is_flow_mode_enabled = :isEnabled")
    abstract suspend fun updateFlowMode(isEnabled: Boolean)
}
