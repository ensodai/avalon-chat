package com.ensodai.avalon.di

import android.content.Context
import com.ensodai.avalon.domain.paymentManager.AndroidBillingPlatformHelper
import com.ensodai.avalon.domain.paymentManager.BillingPlatformHelper
import com.ensodai.avalon.domain.paymentManager.CurrentActivityProvider
import com.ensodai.avalon.domain.paymentManager.NoOpPaymentProvider
import com.ensodai.avalon.domain.paymentManager.PaymentProvider
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
@Configuration
class AndroidPaymentModule {

    @Single
    fun provideCurrentActivityProvider(): CurrentActivityProvider {
        return CurrentActivityProvider()
    }

    @Single
    fun provideBillingPlatformHelper(context: Context): BillingPlatformHelper {
        return AndroidBillingPlatformHelper(context)
    }

    @Single
    @Named("RuStore")
    fun provideRuStoreBillingProvider(): PaymentProvider {
        return NoOpPaymentProvider()
    }

    @Single
    @Named("Google")
    fun provideGoogleBillingProvider(): PaymentProvider {
        return NoOpPaymentProvider()
    }
}
