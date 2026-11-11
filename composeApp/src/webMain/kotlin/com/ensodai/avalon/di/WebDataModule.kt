package com.ensodai.avalon.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room3.RoomDatabase
import com.ensodai.avalon.data.db.AppDataBase
import com.ensodai.avalon.data.db.getDatabaseBuilder
import com.ensodai.avalon.data.db.getRoomDatabase
import com.ensodai.avalon.utils.FileSaver
import com.ensodai.avalon.utils.WebFileSaver
import kotlinx.coroutines.CoroutineScope
import okio.Path.Companion.toPath
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class WebDataModule {

    @Single
    fun provideDatabaseBuilder(): RoomDatabase.Builder<AppDataBase> {
        return getDatabaseBuilder()
    }

    @Single
    fun provideAppDatabase(builder: RoomDatabase.Builder<AppDataBase>): AppDataBase {
        return getRoomDatabase(builder)
    }

    @Single
    fun provideDataStorePathProvider(): DataStorePathProvider {
        return object : DataStorePathProvider {
            override fun getPath(): String = "app_preferences.preferences_pb"
        }
    }

    @Single
    fun providePreferencesDataStore(
        @ApplicationScope scope: CoroutineScope
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            scope = scope,
            produceFile = { "app_preferences.preferences_pb".toPath() }
        )
    }

    @Single
    fun provideFileSaver(): FileSaver {
        return WebFileSaver()
    }
}
