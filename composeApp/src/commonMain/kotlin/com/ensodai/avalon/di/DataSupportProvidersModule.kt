package com.ensodai.avalon.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ensodai.avalon.data.repository.local.DatastoreManager
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class DataSupportProvidersModule {

    @Single
    fun provideDatastoreManager(
        dataStore: DataStore<Preferences>,
        @ApplicationScope scope: CoroutineScope
    ): DatastoreManager {
        return DatastoreManager(
            dataStore = dataStore,
            scope = scope
        )
    }

}
