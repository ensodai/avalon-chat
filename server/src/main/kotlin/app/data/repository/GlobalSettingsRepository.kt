package app.data.repository

import app.data.db.tables.GlobalSetting
import app.data.db.tables.GlobalSettings
import app.utils.dbQuery
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.koin.core.annotation.Single


/**
 * Репозиторий для управления глобальными настройками приложения.
 *
 * Предполагается, что в таблице `GlobalSettings` всегда существует
 * ровно одна запись. Этот репозиторий предоставляет методы для
 * инициализации и получения этой единственной записи.
 */
@Single
class GlobalSettingsRepository {


    private suspend fun row() =
        dbQuery {
            GlobalSettings.selectAll().limit(1).single()
        }


    /**
     * Гарантирует наличие единственной записи с настройками в базе данных.
     *
     * Вызывается при старте приложения. Если в таблице нет ни одной записи -
     * создает одну с дефолтными значениями. Если записей больше одной -
     * логирует предупреждение.
     */
    suspend fun ensureSettingsExist() {
        dbQuery {
            val settingsCount = GlobalSetting.all().count()

            if (settingsCount == 0L) {
                // Записей нет - создаем одну дефолтную
                GlobalSetting.new {
                    // Значения будут взяты из `default()` в определении таблицы
                }
                println("INFO: Initial GlobalSettings record has been inserted.")
            } else if (settingsCount > 1L) {
                // Ситуация, которой не должно быть
                println("WARNING: Multiple records ($settingsCount) found in GlobalSettings table. This may indicate an issue.")
            }
        }
    }

    /**
     * Получает текущие глобальные настройки.
     *
     * @return DAO-сущность [GlobalSetting] с актуальными настройками.
     * @throws IllegalStateException если запись с настройками по какой-то причине отсутствует в БД.
     *         Перед вызовом этого метода следует убедиться, что `ensureSettingsExist` был выполнен.
     */
    suspend fun getSettings(): GlobalSetting {
        return dbQuery {
            // `first()` выбросит исключение, если записей нет, что является
            // ожидаемым поведением, т.к. запись должна существовать всегда.
            GlobalSetting.all().first()
        }
    }
    suspend fun getMaxQuotaMessagesPerHour(): Int =
        row()[GlobalSettings.maxQuotaMessagesPerHour]

    suspend fun getMainCharacterRules(): String =
        row()[GlobalSettings.mainCharacterRules]

    suspend fun getDefaultGeminiModel(): String =
        row()[GlobalSettings.defaultGeminiModel]

    suspend fun getMaxOutputTokens(): Int =
        row()[GlobalSettings.maxOutputTokens]

    suspend fun isPaymentServiceEnabled(): Boolean =
        row()[GlobalSettings.isEnablePaymentService]

    suspend fun getVertexLocation(): String =
        row()[GlobalSettings.vertexLocation]

    suspend fun getGemini3VertexLocation(): String =
        row()[GlobalSettings.gemini3VertexLocation]

    suspend fun getAudioVertexLocation(): String =
        row()[GlobalSettings.audioVertexLocation]
}