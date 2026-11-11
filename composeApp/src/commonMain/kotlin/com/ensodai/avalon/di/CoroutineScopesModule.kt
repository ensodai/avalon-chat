package com.ensodai.avalon.di

import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class CoroutineScopesProvidersModule {

    @Single
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + IODispatcher)
    }

    @Single
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = IODispatcher
}
