package app.data.repository

import app.data.db.tables.GlobalSetting
import app.utils.dbQuery
import org.koin.core.annotation.Single

@Single
class MainRulesRepository {

    suspend fun getMainRules(): GlobalSetting? {
        return dbQuery {
            GlobalSetting.all().firstOrNull()
        }
    }
}