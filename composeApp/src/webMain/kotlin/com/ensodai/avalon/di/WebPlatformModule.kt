package com.ensodai.avalon.di

import com.ensodai.avalon.config.AppBuildConfig
import com.ensodai.avalon.data.provider.DeviceInfoProvider
import com.ensodai.avalon.data.provider.WebDeviceInfoProvider
import com.ensodai.avalon.domain.image.ImageCacheManager
import com.ensodai.avalon.domain.image.WebImageCacheManager
import com.ensodai.avalon.domain.model.PlatformMetadata
import com.ensodai.avalon.domain.paymentManager.BillingPlatformHelper
import com.ensodai.avalon.domain.paymentManager.PaymentProvider
import com.ensodai.avalon.domain.paymentManager.WebBillingPlatformHelper
import com.ensodai.avalon.domain.paymentManager.WebGooglePaymentProvider
import com.ensodai.avalon.domain.paymentManager.WebRuStorePaymentProvider
import com.ensodai.avalon.data.provider.PushTokenProvider
import com.ensodai.avalon.data.provider.WebPushTokenProvider
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
class WebPlatformModule {

    @Single
    fun provideDeviceInfoProvider(): DeviceInfoProvider = WebDeviceInfoProvider()

    @Single
    fun providePushTokenProvider(): PushTokenProvider = WebPushTokenProvider()

    @Single
    fun provideImageCacheManager(): ImageCacheManager = WebImageCacheManager()

    @Single
    fun provideBillingPlatformHelper(): BillingPlatformHelper = WebBillingPlatformHelper()

    @Single
    @Named("RuStore")
    fun provideRuStorePaymentProvider(): PaymentProvider = WebRuStorePaymentProvider()

    @Single
    @Named("Google")
    fun provideGooglePaymentProvider(): PaymentProvider = WebGooglePaymentProvider()

    @Single
    fun providePlatformMetadata(): PlatformMetadata {
        return PlatformMetadata(
            versionName = AppBuildConfig.VERSION_NAME,
            versionCode = AppBuildConfig.VERSION_CODE,
            platform = "Web"
        )
    }
}
