package com.ensodai.avalon.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@ComponentScan("com.ensodai.avalon.domain.sessionWorker")
@Configuration
class DomainSessionWorkerModule
