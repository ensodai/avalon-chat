package com.ensodai.avalon.data.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.async.executeSQL
import com.eygraber.sqldelight.androidx.driver.opfs.androidxSqliteOpfsDriver
import com.eygraber.sqldelight.androidx.driver.opfs.OpfsMultiTabMode
import kotlinx.coroutines.Dispatchers

/**
 * Creates a [RoomDatabase.Builder] for [AppDataBase]
 */
fun getDatabaseBuilder(): RoomDatabase.Builder<AppDataBase> {
    return Room.databaseBuilder<AppDataBase>(
        name = "app_db.db",
        factory = { AppDataBaseConstructor.initialize() }
    ).setDriver(
        androidxSqliteOpfsDriver(
            multiTabMode = OpfsMultiTabMode.PauseOnHidden
        )
    )
    .setQueryCoroutineContext(Dispatchers.Default)
    .addCallback(object : RoomDatabase.Callback() {
        override suspend fun onOpen(connection: SQLiteConnection) {
            super.onOpen(connection)
            connection.executeSQL("PRAGMA journal_mode = TRUNCATE;")
            connection.executeSQL("PRAGMA synchronous = NORMAL;")
            connection.executeSQL("PRAGMA cache_size = -16384;")
        }
    })
}
