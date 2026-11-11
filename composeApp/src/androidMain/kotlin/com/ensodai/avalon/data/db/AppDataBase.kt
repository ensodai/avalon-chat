@file:JvmName("AndroidAppDataBaseKt")

package com.ensodai.avalon.data.db

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDataBase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("AppDataBase")
    return Room.databaseBuilder<AppDataBase>(
        context = appContext,
        name = dbFile.absolutePath
    ).setDriver(BundledSQLiteDriver())
}
