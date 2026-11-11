package app.service.liveCall

import app.data.models.AICharacterConfigLLMDto
import com.google.genai.types.ActivityHandling
import com.google.genai.types.AudioTranscriptionConfig
import com.google.genai.types.AutomaticActivityDetection
import com.google.genai.types.Content
import com.google.genai.types.ContextWindowCompressionConfig
import com.google.genai.types.EndSensitivity
import com.google.genai.types.LiveConnectConfig
import com.google.genai.types.Modality
import com.google.genai.types.Part
import com.google.genai.types.PrebuiltVoiceConfig
import com.google.genai.types.ProactivityConfig
import com.google.genai.types.RealtimeInputConfig
import com.google.genai.types.SessionResumptionConfig
import com.google.genai.types.SlidingWindow
import com.google.genai.types.SpeechConfig
import com.google.genai.types.StartSensitivity
import com.google.genai.types.VoiceConfig
import org.koin.core.annotation.Single

@Single
class LiveCallConnectConfigFactory(
    private val liveCallPromptFactory: LiveCallPromptFactory
) {

    /**
     * Собирает [LiveConnectConfig] для голосовых звонков через Gemini live API.
     *
     * Включённые фичи:
     * - Аудио-ответы (voice output)
     * - Встроенный голос (prebuilt voice) с настраиваемым именем и языком
     * - Автоматический VAD с высокой чувствительностью к началу/концу речи
     * - Barge-in: пользователь может перебить модель на полуслове
     * - Транскрипция входного и выходного аудио
     * - Проактивное аудио и аффективный диалог
     * - Сжатие контекстного окна (sliding window, 80k токенов)
     *
     * Доступно в SDK v1.47.0, но пока НЕ используется:
     *
     * **Восстановление сессии** — `LiveConnectConfig.sessionResumption()`
     * Переживает обрывы WebSocket без потери истории разговора.
     * Сервер присылает `handle` через `sessionResumptionUpdate()`, передаём при реконнекте.
     *
     * **Обработка Go-away** — `LiveServerMessage.goAway()`
     * Сервер предупреждает перед закрытием соединения (балансировка нагрузки).
     * Используем `timeLeft` чтобы переподключиться до отключения.
     *
     * **Вызов функций** — `LiveConnectConfig.tools()`
     * Даём модели инструменты: GoogleSearch, CodeExecution, кастомные функции.
     * Обрабатываем `LiveServerMessage.toolCall()` и отвечаем через `LiveSendToolResponseParameters`.
     *
     * **Расширенное мышление** — `LiveConnectConfig.thinkingConfig()`
     * Более обдуманные ответы с `thinkingBudget` и `thinkingLevel`.
     *
     * **Видео-ввод** — `LiveSendRealtimeInputParameters.video(Blob)`
     * Отправка кадров камеры во время звонка для мультимодального взаимодействия.
     *
     * **Текстовый ввод во время звонка** — `LiveSendRealtimeInputParameters.text(String)`
     * Пользователь печатает сообщение во время голосового звонка.
     *
     * **Клонирование голоса** — `VoiceConfig.replicatedVoiceConfig()`
     * Клонирование кастомного голоса из аудио-семплов вместо встроенных голосов.
     *
     * **Ручной VAD** — `LiveConnectConfig.explicitVadSignal(true)`
     * Ручное управление сигналами начала/конца речи через `activityStart`/`activityEnd`.
     * Полезно когда автоматический VAD недостаточно точен.
     *
     * **Метаданные grounding** — `LiveServerContent.groundingMetadata()`
     * Поисковые запросы, источники и информация grounding при использовании GoogleSearch.
     *
     * **Потоковая транскрипция** — `LiveServerContent.inputTranscription()` / `outputTranscription()`
     * Инкрементальная текстовая транскрипция речи пользователя и ответа модели в реальном времени.
     *
     * **Разрешение медиа** — `LiveConnectConfig.mediaResolution(MediaResolution)`
     * Управление качеством обработки видео при отправке кадров камеры.
     *
     * **Seed** — `LiveConnectConfig.seed(Integer)`
     * Детерминированная/воспроизводимая генерация ответов.
     *
     * **Язык транскрипции** — `AudioTranscriptionConfig.builder().languageCodes(...)`
     * Явное указание языка транскрипции для лучшей точности (сейчас автоопределение).
     */
    suspend fun build(
        characterConfig: AICharacterConfigLLMDto,
        voiceName: String
    ): LiveConnectConfig {
        val characterAudioSystemPromt = liveCallPromptFactory.buildPrompt(characterConfig = characterConfig)
        val systemInstruction = Content.builder()
            .role("system")
            .parts(listOf(Part.fromText(characterAudioSystemPromt)))
            .build()

        return LiveConnectConfig.builder()
            .responseModalities(Modality.Known.AUDIO)
            .temperature(characterConfig.temperature.coerceIn(0.1f, 1.2f))
            .topP(characterConfig.topP.coerceIn(0.1f, 1.0f))
            .topK((characterConfig.topK ?: 32).coerceIn(1, 40).toFloat())
            .maxOutputTokens(characterConfig.maxOutputTokens.coerceAtMost(256))
            .speechConfig(
                SpeechConfig.builder()
                    .voiceConfig(
                        VoiceConfig.builder()
                            .prebuiltVoiceConfig(
                                PrebuiltVoiceConfig.builder()
                                    .voiceName(voiceName)
                            )
                    )
//                    .languageCode(languageCode)
            )
//            .inputAudioTranscription(AudioTranscriptionConfig.builder())
//            .outputAudioTranscription(AudioTranscriptionConfig.builder())
            .realtimeInputConfig(
                RealtimeInputConfig.builder()
                    .automaticActivityDetection(
                        AutomaticActivityDetection.builder()
                            .startOfSpeechSensitivity(StartSensitivity.Known.START_SENSITIVITY_HIGH)
                            .endOfSpeechSensitivity(EndSensitivity.Known.END_SENSITIVITY_HIGH)
                            .prefixPaddingMs(20)
                            .silenceDurationMs(250)
                    )
                    .activityHandling(ActivityHandling.Known.START_OF_ACTIVITY_INTERRUPTS)
            )
            .proactivity(
                ProactivityConfig.builder()
                    .proactiveAudio(true)
            )
            .enableAffectiveDialog(true)
            .contextWindowCompression(
                ContextWindowCompressionConfig.builder()
                    .slidingWindow(SlidingWindow.builder().targetTokens(8_000L))
            )
            .sessionResumption(
                SessionResumptionConfig.builder()
                    .transparent(true)
            )
            .systemInstruction(systemInstruction)
            .build()
    }
}
