package com.ensodai.avalon.domain.proximity

import kotlinx.coroutines.flow.StateFlow

interface ProximityAudioManager {
    /** Есть ли proximity датчик на устройстве */
    val isAvailable: Boolean

    /** Телефон у уха (true) или нет (false) */
    val isNear: StateFlow<Boolean>

    /** Запустить слушатель датчика и переключить аудио на earpiece */
    fun start()

    /** Остановить слушатель и вернуть аудио на speaker */
    fun stop()
}
