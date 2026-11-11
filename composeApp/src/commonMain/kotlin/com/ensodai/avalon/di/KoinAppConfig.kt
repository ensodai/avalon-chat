package com.ensodai.avalon.di

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(
    includes = [
        CoroutineScopesProvidersModule::class,
        DataSupportProvidersModule::class,
        DatabaseProvidersModule::class,
        LocalRepositoryProvidersModule::class,
        NetworkProvidersModule::class,
        CoreProvidersModule::class,
        PaymentProvidersModule::class,
        DomainAuthModule::class,
        DomainGlobalModule::class,
        DomainMessageSenderModule::class,
        DomainSessionWorkerModule::class,
        DomainPaymentRuntimeModule::class,
        DomainLorebookModule::class,
        DomainUseCasesModule::class,
        ViewModelModule::class,
        RpcModule::class
    ]
)
@Configuration
class AppModule

