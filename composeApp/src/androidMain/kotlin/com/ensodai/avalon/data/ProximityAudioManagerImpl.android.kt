package com.ensodai.avalon.data

import android.app.Application
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import com.ensodai.avalon.domain.proximity.ProximityAudioManager
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Фабрика для Koin DI.
 */
actual fun createProximityAudioManager(): ProximityAudioManager {
    val context = AppContextHolder.application
    return ProximityAudioManagerImpl(context)
}

object AppContextHolder {
    lateinit var application: Application
}

internal class ProximityAudioManagerImpl(
    private val context: Context
) : ProximityAudioManager {

    private val audioManager: AudioManager? by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }
    private val powerManager: PowerManager? by lazy {
        context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    }

    // --- Proximity sensor ---
    private var sensorManager: android.hardware.SensorManager? = null
    private var proximitySensor: android.hardware.Sensor? = null

    init {
        @Suppress("DEPRECATION")
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? android.hardware.SensorManager
        proximitySensor = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_PROXIMITY)
    }

    private var proximityWakeLock: PowerManager.WakeLock? = null
    private var isRunning = false

    private val _isNear = MutableStateFlow(false)
    override val isNear: StateFlow<Boolean> = _isNear.asStateFlow()
    override val isAvailable: Boolean get() = proximitySensor != null

    // Debounce для sensor
    private var debounceRunnable: Runnable? = null
    private var pendingNear: Boolean? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val debounceMs = 300L

    private val sensorListener = object : android.hardware.SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent) {
            if (event.sensor.type != android.hardware.Sensor.TYPE_PROXIMITY) return

            val threshold = kotlin.math.min(
                event.sensor.maximumRange,
                3f
            )
            val isCurrentlyNear = event.values[0] < threshold

            debounceRunnable?.let { handler.removeCallbacks(it) }
            pendingNear = isCurrentlyNear

            debounceRunnable = Runnable {
                val target = pendingNear ?: return@Runnable
                L.d("ProximityAudio", "Sensor: isNear=$target, switching route...")
                _isNear.value = target
                applyAudioRoute(target)
            }
            handler.postDelayed(debounceRunnable!!, debounceMs)
        }

        override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
    }

    private fun applyAudioRoute(isNear: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val targetType = if (isNear) {
                AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
            } else {
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            }

            val availableDevices: List<AudioDeviceInfo> = audioManager?.availableCommunicationDevices ?: emptyList()
            val device: AudioDeviceInfo? = availableDevices.find { d -> d.type == targetType }

            if (device != null) {
                audioManager?.setCommunicationDevice(device)
                L.d("ProximityAudio", "setCommunicationDevice type=$targetType")
            } else {
                L.w("ProximityAudio", "Device type=$targetType not found in availableCommunicationDevices")
            }
        } else {
            // API < 31: fallback
            audioManager?.isSpeakerphoneOn = !isNear
            L.d("ProximityAudio", "Fallback: speakerphone=${!isNear}")
        }
    }

    override fun start() {
        L.d("ProximityAudio", "start() called, isAvailable=$isAvailable, isRunning=$isRunning")
        if (!isAvailable || isRunning) {
            L.w("ProximityAudio", "Skipping: isAvailable=$isAvailable, isRunning=$isRunning")
            return
        }
        isRunning = true

        // Проверяем внешние аудио-устройства
        val isBluetoothActive = audioManager?.isBluetoothScoOn == true
        val isWiredHeadset = audioManager?.isWiredHeadsetOn == true

        L.d("ProximityAudio", "Audio state: BT_SCO_active=$isBluetoothActive, wiredHeadset=$isWiredHeadset")

        if (isBluetoothActive || isWiredHeadset) {
            L.d("ProximityAudio", "External audio device active — skipping earpiece switch")
            return
        }

        // Переключаем аудио на communication mode (ОДИН раз!)
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION

        // API 31+: устанавливаем earpiece как устройство связи по умолчанию
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val devices: Array<AudioDeviceInfo> = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS) ?: emptyArray()
            val earpiece: AudioDeviceInfo? = devices.find { d -> d.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
            if (earpiece != null) {
                audioManager?.setCommunicationDevice(earpiece)
                L.d("ProximityAudio", "Initial route: earpiece (id=${earpiece.id})")
            }
        }

        audioManager?.isSpeakerphoneOn = false
        L.d("ProximityAudio", "Audio mode set to MODE_IN_COMMUNICATION, speakerphone=false")

        // Захватываем WakeLock — Android сам управляет экраном по датчику
        proximityWakeLock = powerManager?.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "Avalon:LiveCallProximity"
        )
        try {
            proximityWakeLock?.acquire()
            L.d("ProximityAudio", "WakeLock acquired")
        } catch (e: SecurityException) {
            L.e("ProximityAudio", "WAKE_LOCK permission denied: ${e.message}")
        }

        // Слушатель датчика
        sensorManager?.registerListener(
            sensorListener,
            proximitySensor,
            android.hardware.SensorManager.SENSOR_DELAY_NORMAL
        )

        L.d("ProximityAudio", "Proximity sensor STARTED successfully")
    }

    override fun stop() {
        if (!isRunning) return
        isRunning = false

        sensorManager?.unregisterListener(sensorListener)
        debounceRunnable?.let { handler.removeCallbacks(it) }
        debounceRunnable = null
        pendingNear = null

        // Освобождаем WakeLock
        try {
            if (proximityWakeLock?.isHeld == true) {
                proximityWakeLock?.release()
                L.d("ProximityAudio", "WakeLock released")
            }
        } catch (e: Exception) {
            L.w("ProximityAudio", "Failed to release wake lock: ${e.message}")
        }
        proximityWakeLock = null

        // Сбрасываем аудио
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager?.clearCommunicationDevice()
        }
        try {
            audioManager?.mode = AudioManager.MODE_NORMAL
            audioManager?.isSpeakerphoneOn = true
            L.d("ProximityAudio", "Audio reset to MODE_NORMAL, speakerphone=true")
        } catch (e: Exception) {
            L.w("ProximityAudio", "Failed to reset audio: ${e.message}")
        }

        _isNear.value = false
        L.d("ProximityAudio", "Proximity sensor STOPPED")
    }
}
