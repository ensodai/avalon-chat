package app.service
import com.ensodai.shared.avalon.gifts.models.*

import app.data.db.tables.ApiKeys
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import app.domain.models.DomainApiKey
import app.service.GeminiError
import app.utils.EnvProvider
import app.utils.dbQuery
import app.utils.getUTCTimeNow
import app.utils.logger.WssLogger
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.Uuid

import org.koin.core.annotation.Single

@Single
class ApiKeyProvider {

    private val specialKey: String = EnvProvider.getGeminiApiKey()

    suspend fun getSpecialKey(): DomainApiKey {

        return DomainApiKey(
                id = Uuid.random(),
                key = specialKey
            )
    }


    suspend fun getAvailableKey(userStatus: PremiumStatus): DomainApiKey? {
        if (userStatus != PremiumStatus.FREE) return getSpecialKey()

        return dbQuery {
            val now = getUTCTimeNow()

            // 1. Сбрасываем истекшие кулдауны.
            // (cooldownUntil <= now)
            ApiKeys.update({ (ApiKeys.cooldownUntil lessEq now) }) {
                it[cooldownUntil] = null
            }

            // Ищем кандидата.
            // Условие:
            // - Не забанен
            // - Нет кулдауна
            // - Не удален (deletedAt is null, так как BaseUUIDTable)
            val row = ApiKeys
                .select(ApiKeys.id, ApiKeys.key)
                .where {
                    (ApiKeys.isBanned eq false) and
                            (ApiKeys.cooldownUntil.isNull()) and
                            (ApiKeys.deletedAt.isNull())
                }
                .orderBy(ApiKeys.lastSelected to SortOrder.ASC)
                .limit(1)
                .forUpdate()
                .singleOrNull()

            if (row == null) return@dbQuery null

            val keyId = row[ApiKeys.id].value
            val keyString = row[ApiKeys.key]

            ApiKeys.update({ ApiKeys.id eq keyId }) {
                it.update(requestCount, requestCount + 1)
                it[lastSelected] = now
            }

            DomainApiKey(
                id = keyId,
                key = keyString
            )
        }
    }

    suspend fun reportKeyFailure(keyId: Uuid, error: GeminiError) {
        dbQuery {
            val now = Clock.System.now()

            when (error) {
                // БАН НА 1 ЧАС
                is GeminiError.QuotaExceeded -> {
                    ApiKeys.update({ ApiKeys.id eq keyId }) {
                        it[cooldownUntil] = now.plus(1.hours)
                        it.update(failureCount, failureCount + 1)
                    }
                    WssLogger.info("🔑 Key $keyId -> COOLDOWN (1h)")
                }

                // ВЕЧНЫЙ БАН
                is GeminiError.KeyBanned -> {
                    ApiKeys.update({ ApiKeys.id eq keyId }) {
                        it[isBanned] = true
                        it[cooldownUntil] = null // Кулдаун больше не имеет смысла
                        it.update(failureCount, failureCount + 1)
                    }
                    WssLogger.fail("🚫 Key $keyId -> BANNED PERMANENTLY")
                }

                else -> {
                    // Для остальных ошибок (500) просто инкрементим счетчик ошибок,
                    // но не баним ключ, так как виноват сервер Гугла.
                    ApiKeys.update({ ApiKeys.id eq keyId }) {
                        it.update(failureCount, failureCount + 1)
                    }
                }
            }
        }
    }
}