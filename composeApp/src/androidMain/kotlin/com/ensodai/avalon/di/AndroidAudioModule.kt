package com.ensodai.avalon.di

import android.app.Application
import com.ensodai.avalon.data.AppContextHolder
import com.ensodai.avalon.data.createProximityAudioManager
import com.ensodai.avalon.domain.audio.AndroidAudioCapture
import com.ensodai.avalon.domain.audio.AndroidAudioPlayback
import com.ensodai.avalon.domain.audio.AudioCapture
import com.ensodai.avalon.domain.audio.AudioPlayback
import com.ensodai.avalon.domain.audio.TtsPlayer
import com.ensodai.avalon.domain.audio.TtsCacheManager
import com.ensodai.avalon.domain.audio.AndroidTtsPlayer
import com.ensodai.avalon.domain.audio.AndroidTtsCacheManager
import com.ensodai.avalon.domain.proximity.ProximityAudioManager
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AndroidAudioModule {

    @Single
    fun provideProximityAudio(app: Application): ProximityAudioManager {
        AppContextHolder.application = app
        return createProximityAudioManager()
    }

    @Single
    fun provideAudioCapture(
        logger: AppLogger,
        @ApplicationScope scope: CoroutineScope
    ): AudioCapture {
        return AndroidAudioCapture(
            appLogger = logger,
            scope = scope
        )
    }

    @Single
    fun provideAudioPlayback(
        logger: AppLogger
    ): AudioPlayback {
        return AndroidAudioPlayback(
            appLogger = logger
        )
    }

    @Single
    fun provideTtsPlayer(
        logger: AppLogger
    ): TtsPlayer {
        return AndroidTtsPlayer(logger)
    }

    @Single
    fun provideTtsCacheManager(
        app: Application
    ): TtsCacheManager {
        return AndroidTtsCacheManager(app)
    }
}

