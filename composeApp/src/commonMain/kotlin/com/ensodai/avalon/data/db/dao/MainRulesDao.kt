package com.ensodai.avalon.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.ensodai.avalon.data.db.models.MainRulesEntity

@Dao
abstract class MainRulesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insert(mainRules: MainRulesEntity)

    @Update
    protected abstract suspend fun _update_insert(mainRules: MainRulesEntity)

    @Transaction
    open suspend fun insert(mainRules: MainRulesEntity) {
        _insert_insert(mainRules)
        _update_insert(mainRules)
    }

    @Update
    abstract suspend fun update(mainRules: MainRulesEntity)

    @Query("SELECT * FROM main_rules LIMIT 1")
    abstract suspend fun getMainRules(): MainRulesEntity?

    open suspend fun upsert(mainRules: MainRulesEntity) {
        val existing = getMainRules()
        if (existing == null) {
            insert(mainRules)
        } else {

            val updatedRules = mainRules.copy(id = existing.id)
            update(updatedRules)
        }
    }
}