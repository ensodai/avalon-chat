package com.ensodai.avalon.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room3.RoomDatabase
import com.ensodai.avalon.data.db.AppDataBase
import com.ensodai.avalon.data.db.getDatabaseBuilder
import com.ensodai.avalon.data.db.getRoomDatabase
import com.ensodai.avalon.data.provider.AndroidDeviceInfoProvider
import com.ensodai.avalon.data.provider.DeviceInfoProvider
import com.ensodai.avalon.data.provider.AndroidPushTokenProvider
import com.ensodai.avalon.data.provider.PushTokenProvider
import com.ensodai.avalon.utils.AndroidFileSaver
import com.ensodai.avalon.utils.FileSaver
import kotlinx.coroutines.CoroutineScope
import okio.Path.Companion.toPath
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AndroidDataModule {

    @Single
    fun provideDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDataBase> {
        return getDatabaseBuilder(context)
    }

    @Single
    fun provideAppDatabase(builder: RoomDatabase.Builder<AppDataBase>): AppDataBase {
        return getRoomDatabase(builder)
    }

    @Single
    fun provideDeviceInfoProvider(context: Context): DeviceInfoProvider {
        return AndroidDeviceInfoProvider(context)
    }

    @Single
    fun providePushTokenProvider(): PushTokenProvider {
        return AndroidPushTokenProvider()
    }

    @Single
    fun provideDataStorePathProvider(context: Context): DataStorePathProvider {
        return object : DataStorePathProvider {
            override fun getPath(): String {
                return context.dataStoreFile("app_preferences.preferences_pb").absolutePath
            }
        }
    }

    @Single
    fun providePreferencesDataStore(
        pathProvider: DataStorePathProvider,
        @ApplicationScope scope: CoroutineScope
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            scope = scope,
            produceFile = { pathProvider.getPath().toPath() }
        )
    }

    @Single
    fun provideFileSaver(context: Context): FileSaver {
        return AndroidFileSaver(context)
    }
}
