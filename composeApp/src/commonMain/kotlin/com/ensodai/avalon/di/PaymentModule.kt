package com.ensodai.avalon.di

import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class PaymentProvidersModule {

    @Single
    @BillingScope
    fun provideBillingScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + IODispatcher)
    }
}
