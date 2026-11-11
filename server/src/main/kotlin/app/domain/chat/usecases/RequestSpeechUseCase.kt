package app.domain.chat.usecases

import app.data.network.openai.OpenAiException
import app.data.network.openai.toSystemEventType
import app.data.repository.ChatRepository
import app.data.repository.UserRepository
import app.domain.user.usecases.GetUserAiSettingsUseCase
import com.ensodai.shared.avalon.chat.models.AudioPayload
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.SystemEventType
import com.ensodai.shared.avalon.chat.models.TextPayload
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import app.data.db.tables.chat.MessageContents
import app.data.db.tables.chat.MessageContent
import app.data.models.EnergyDeductionResult
import app.service.TtsService
import app.service.chat.MessageNotifier
import app.utils.dbQuery
import app.utils.toServerUrl
import app.utils.getUTCTimeNow
import app.utils.logger.LlmLogger
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class RequestSpeechUseCase(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val ttsService: TtsService,
    private val messageNotifier: MessageNotifier,
    private val getUserAiSettingsUseCase: GetUserAiSettingsUseCase
) {
    private data class TtsRequestInfo(
        val text: String,
        val chatSessionId: Uuid,
        val chatVariantId: Uuid,
        val characterId: Uuid,
        val containerId: Uuid,
        val voice: VoiceModel
    )

    fun execute(userId: Uuid, messageContentId: Uuid, voice: VoiceModel?): Flow<ByteArray> = flow {
        LlmLogger.inf("RequestSpeechUseCase", "🚀 Executing RequestSpeech for userId=$userId, messageContentId=$messageContentId")

        // 1. Проверяем, есть ли уже озвучка для этого сообщения на бэкенде
        LlmLogger.inf("RequestSpeechUseCase", "🔍 Checking if audio already exists in DB for this container...")
        val existingAudioInfo = dbQuery {
            val contentEntity = MessageContent.findById(messageContentId)
                ?: throw IllegalArgumentException("Message content not found")

            val containerEntity = contentEntity.messageContainer
            val audioContent = MessageContent.find {
                (MessageContents.containerId eq containerEntity.id) and
                (MessageContents.type eq AvalonMessageContentType.AUDIO)
            }.firstOrNull()

            if (audioContent != null) {
                val payload = MessageContentCodec.decodeAs<AudioPayload>(audioContent.content)
                if (payload != null) {
                    Triple(payload.url, audioContent.id.value, audioContent.orderIndex)
                } else null
            } else null
        }

        // 2. Если озвучка уже существует и файл есть на диске — просто стримим его чанками
        if (existingAudioInfo != null) {
            val (url, audioContentId, orderIndex) = existingAudioInfo
            LlmLogger.inf("RequestSpeechUseCase", "🎉 Found existing audio in DB. URL: $url, ID: $audioContentId")
            val uploadsIndex = url.indexOf("/uploads/")
            if (uploadsIndex != -1) {
                val relPath = url.substring(uploadsIndex + "/uploads/".length)
                var file = File("/var/uploads/$relPath")
                if (!file.exists()) {
                    file = File("uploads/$relPath")
                }
                if (file.exists()) {
                    LlmLogger.inf("RequestSpeechUseCase", "💾 File exists on server disk: ${file.absolutePath}. Streaming cached file (skipping WAV header)...")
                    val buffer = ByteArray(4096)
                    file.inputStream().use { input ->
                        input.skip(44) // Пропускаем 44-байтный WAV-заголовок
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            emit(buffer.copyOf(read))
                        }
                    }

                    val fileSize = file.length()
                    val pcmSize = if (fileSize > 44) fileSize - 44 else 0
                    val durationMs = (pcmSize * 1000L) / 48000L
                    val audioPayload = AudioPayload(
                        id = audioContentId,
                        url = url,
                        durationMs = durationMs,
                        sizeBytes = fileSize,
                        mimeType = "audio/wav"
                    )
                    val encodedContent = MessageContentCodec.encode(AvalonMessageContentType.AUDIO, audioPayload)
                    val containerId = dbQuery {
                        val contentEntity = MessageContent.findById(messageContentId)
                            ?: throw IllegalArgumentException("Message content not found")
                        contentEntity.messageContainer.id.value
                    }

                    // Отправляем точечное сокет-событие повторно, чтобы клиент гарантированно восстановил БД Room
                    messageNotifier.notifyUserWithMessage(
                        userId = userId,
                        message = ServerMessage.MessageContentAdded(
                            containerId = containerId,
                            contentId = audioContentId,
                            contentType = AvalonMessageContentType.AUDIO,
                            content = encodedContent,
                            orderIndex = orderIndex
                        )
                    )
                    LlmLogger.inf("RequestSpeechUseCase", "✅ Notification sent.")
                    return@flow
                } else {
                    LlmLogger.inf("RequestSpeechUseCase", "⚠️ DB entry exists but file is MISSING on disk at path: ${file.absolutePath}. Regenerating...")
                }
            }
        }

        // 3. Если озвучки нет (или файл пропал с диска) — переходим к генерации
        // 🌧️💔 Эх, прощайте платные тарифы и золотые горы подписок... Уходим в закат и спим спокойно до лучших времен 🥀😢
        /*
        LlmLogger.inf("RequestSpeechUseCase", "🔑 Checking premium status for user $userId...")
        val premiumStatus = userRepository.getUserPremiumStatus(userId)
        LlmLogger.inf("RequestSpeechUseCase", "User premium status: $premiumStatus")
        if (premiumStatus == com.ensodai.shared.avalon.gifts.models.PremiumStatus.FREE) {
            LlmLogger.error("RequestSpeechUseCase", "❌ Access denied: user is FREE")
            throw IllegalStateException("Озвучка сообщений доступна только пользователям с Premium подпиской.")
        }
        */

        LlmLogger.inf("RequestSpeechUseCase", "📝 Retrieving message text from DB...")
        val info = dbQuery {
            val contentEntity = MessageContent.findById(messageContentId)
                ?: throw IllegalArgumentException("Message content not found")

            if (contentEntity.type != AvalonMessageContentType.TEXT) {
                throw IllegalArgumentException("Only text content can be voiced")
            }

            val decodedPayload = MessageContentCodec.decode(AvalonMessageContentType.TEXT, contentEntity.content)
            val text = (decodedPayload as? TextPayload)?.text ?: contentEntity.content

            val containerEntity = contentEntity.messageContainer
            val slotEntity = containerEntity.slot
            val variantEntity = slotEntity.chatVariant
            val characterEntity = variantEntity.chatSession.aiCharacter
            val characterVoice = characterEntity.voice

            TtsRequestInfo(
                text = text,
                chatSessionId = variantEntity.chatSession.id.value,
                chatVariantId = variantEntity.id.value,
                characterId = characterEntity.id.value,
                containerId = containerEntity.id.value,
                voice = characterVoice
            )
        }
        LlmLogger.inf("RequestSpeechUseCase", "📝 Found text length: ${info.text.length} chars")

        val selectedVoice = voice ?: info.voice
        LlmLogger.inf("RequestSpeechUseCase", "🗣️ Selected Voice model: ${selectedVoice.modelName}")

        val metaInfo = mapOf(
            "chat_session_id" to info.chatSessionId.toString(),
            "chat_variant_id" to info.chatVariantId.toString()
        )

        val aiSettings = getUserAiSettingsUseCase.execute(userId)
        val isCustomProvider = aiSettings?.isCustomProviderEnabled == true && !aiSettings.apiKey.isNullOrBlank()

        if (!isCustomProvider) {
            LlmLogger.inf("RequestSpeechUseCase", "⚡ Deducting 1 energy from user $userId...")
            val deductionResult = userRepository.deductEnergyForTts(
                userId = userId,
                voiceModelName = selectedVoice.modelName,
                metaInfo = metaInfo
            )
            LlmLogger.inf("RequestSpeechUseCase", "Energy deduction result: $deductionResult")

            when (deductionResult) {
                is EnergyDeductionResult.InsufficientFunds -> {
                    LlmLogger.error("RequestSpeechUseCase", "❌ Energy deduction failed: InsufficientFunds")
                    throw IllegalStateException("Недостаточно энергии для озвучивания сообщения.")
                }
                is EnergyDeductionResult.RouteError -> {
                    throw IllegalStateException("RouteError not supported in this flow yet")
                }
                EnergyDeductionResult.UserNotFound -> {
                    LlmLogger.error("RequestSpeechUseCase", "❌ Energy deduction failed: UserNotFound")
                    throw IllegalStateException("Пользователь не найден.")
                }
                is EnergyDeductionResult.Success -> {
                    LlmLogger.inf("RequestSpeechUseCase", "✅ Energy deducted successfully.")
                }
            }
        } else {
            LlmLogger.inf("RequestSpeechUseCase", "⚡ User has custom AI Provider enabled (${aiSettings?.providerId}), skipping energy deduction.")
        }

        LlmLogger.inf("RequestSpeechUseCase", "🎙️ Requesting TtsService.generateSpeechStream...")
        val startTime = System.currentTimeMillis()
        val bos = java.io.ByteArrayOutputStream()
        try {
            ttsService.generateSpeechStream(userId, info.text, selectedVoice, aiSettings)
                .collect { bytes ->
                    val amplified = amplifyPcm16(bytes, 3.0f)
                    bos.write(amplified)
                    emit(amplified) // Стримим чистые PCM байты клиенту в реальном времени!
                }
            val collectorDuration = System.currentTimeMillis() - startTime
            LlmLogger.inf("RequestSpeechUseCase", "🎙️ TtsService.generateSpeechStream collector successfully completed in $collectorDuration ms")
        } catch (e: Exception) {
            LlmLogger.error("RequestSpeechUseCase", "❌ Speech generation failed inside stream collection: ${e.message}")
            e.printStackTrace()

            val eventType = (e as? OpenAiException)?.toSystemEventType() ?: SystemEventType.SERVER_ERROR

            try {
                val systemMessage = chatRepository.createSystemMessage(userId, info.chatSessionId, info.chatVariantId, eventType)
                val energyBalance = userRepository.getUserEnergyBalance(userId)
                messageNotifier.notifyUserWithMessage(
                    userId = userId,
                    excludeSessionId = null,
                    message = ServerMessage.NewChatMessage(
                        characterId = info.characterId,
                        slot = systemMessage,
                        currentUserQuota = energyBalance.dailyEnergy,
                        reserveEnergy = energyBalance.reserveEnergy
                    )
                )
            } catch (notifyEx: Exception) {
                LlmLogger.error("RequestSpeechUseCase", "❌ Failed to create/notify system message for TTS error: ${notifyEx.message}")
            }

            throw e
        }

        val pcmBytes = bos.toByteArray()
        val durationMs = (pcmBytes.size * 1000L) / 48000L
        LlmLogger.inf("RequestSpeechUseCase", "📦 Total PCM bytes collected from Gemini: ${pcmBytes.size} bytes. Calculated duration: ${durationMs}ms")

        if (pcmBytes.isNotEmpty()) {
            val subPath = "users/user_$userId/audio"
            var folder = File("/var/uploads/$subPath")
            if (!folder.exists()) {
                val created = folder.mkdirs()
                if (!created) {
                    folder = File("uploads/$subPath")
                    folder.mkdirs()
                }
            }
            val fileName = "tts_${Uuid.random()}.wav"
            val destFile = File(folder, fileName)
            
            // Генерируем 44-байтный WAV заголовок
            val wavHeader = createWavHeader(pcmBytes.size.toLong())
            val wavBytes = wavHeader + pcmBytes
            
            LlmLogger.inf("RequestSpeechUseCase", "💾 Saving WAV file to server disk: ${destFile.absolutePath} (Header size: ${wavHeader.size} bytes, PCM size: ${pcmBytes.size} bytes)...")
            try {
                destFile.writeBytes(wavBytes)
                LlmLogger.inf("RequestSpeechUseCase", "💾 File saved successfully. Final disk size: ${destFile.length()} bytes")
            } catch (writeEx: Exception) {
                LlmLogger.error("RequestSpeechUseCase", "❌ Failed to write WAV file to disk: ${writeEx.message}")
                writeEx.printStackTrace()
                throw writeEx
            }
            val fileUrl = destFile.absolutePath.toServerUrl()
            val sizeBytes = (wavHeader.size + pcmBytes.size).toLong()
            val audioPayload = AudioPayload(
                id = Uuid.random(),
                url = fileUrl,
                durationMs = durationMs,
                sizeBytes = sizeBytes,
                mimeType = "audio/wav"
            )
            val encodedContent = MessageContentCodec.encode(AvalonMessageContentType.AUDIO, audioPayload)

            val audioContentId = Uuid.random()
            val now = getUTCTimeNow()
            LlmLogger.inf("RequestSpeechUseCase", "✍️ Saving Audio payload to DB (MessageContents table) with id=$audioContentId...")
            val insertedOrderIndex = dbQuery {
                val maxOrderIndex = MessageContents
                    .select(MessageContents.orderIndex)
                    .where { MessageContents.containerId eq info.containerId }
                    .map { it[MessageContents.orderIndex] }
                    .maxOrNull() ?: 0
                val nextOrder = maxOrderIndex + 1

                MessageContents.insert {
                    it[MessageContents.id] = audioContentId
                    it[this.containerId] = info.containerId
                    it[type] = AvalonMessageContentType.AUDIO
                    it[content] = encodedContent
                    it[orderIndex] = nextOrder
                    it[createdAt] = now
                    it[updatedAt] = now
                }
                nextOrder
            }
            LlmLogger.inf("RequestSpeechUseCase", "✍️ DB entry saved. Assigned orderIndex = $insertedOrderIndex")

            LlmLogger.inf("RequestSpeechUseCase", "🔔 Sending WebSocket notification to user $userId...")
            messageNotifier.notifyUserWithMessage(
                userId = userId,
                message = ServerMessage.MessageContentAdded(
                    containerId = info.containerId,
                    contentId = audioContentId,
                    contentType = AvalonMessageContentType.AUDIO,
                    content = encodedContent,
                    orderIndex = insertedOrderIndex
                )
            )
            LlmLogger.inf("RequestSpeechUseCase", "🔔 Notification sent successfully. Speech generation request completed.")
        } else {
            LlmLogger.error("RequestSpeechUseCase", "❌ Speech generation yielded 0 bytes. File not saved, DB not updated.")
        }
    }.flowOn(Dispatchers.IO)

    private fun createWavHeader(pcmSize: Long, sampleRate: Int = 24000, bitsPerSample: Int = 16, channels: Int = 1): ByteArray {
        val headerSize = 44
        val totalDataLen = pcmSize + 36
        val byteRate = (sampleRate * channels * bitsPerSample) / 8
        val blockAlign = (channels * bitsPerSample) / 8

        val header = ByteArray(headerSize)
        
        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        
        // Total length
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        
        // WAVE
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        
        // fmt subchunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        
        // fmt subchunk size (16 for PCM)
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        
        // Audio format (1 for PCM)
        header[20] = 1
        header[21] = 0
        
        // Channels
        header[22] = channels.toByte()
        header[23] = 0
        
        // Sample rate
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        
        // Byte rate
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        
        // Block align
        header[32] = blockAlign.toByte()
        header[33] = 0
        
        // Bits per sample
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        
        // data subchunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        
        // data subchunk size (pcmSize)
        header[40] = (pcmSize and 0xff).toByte()
        header[41] = ((pcmSize shr 8) and 0xff).toByte()
        header[42] = ((pcmSize shr 16) and 0xff).toByte()
        header[43] = ((pcmSize shr 24) and 0xff).toByte()
        
        return header
    }

    private fun amplifyPcm16(pcmBytes: ByteArray, gain: Float): ByteArray {
        if (gain == 1.0f || pcmBytes.isEmpty()) return pcmBytes
        val result = ByteArray(pcmBytes.size)
        val sampleCount = pcmBytes.size / 2
        for (i in 0 until sampleCount) {
            val idx = i * 2
            val low = pcmBytes[idx].toInt() and 0xFF
            val high = pcmBytes[idx + 1].toInt()
            
            val sample = ((high shl 8) or low).toShort()
            val amplified = (sample.toFloat() * gain).toInt()
            val clamped = amplified.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            
            result[idx] = (clamped.toInt() and 0xFF).toByte()
            result[idx + 1] = ((clamped.toInt() shr 8) and 0xFF).toByte()
        }
        if (pcmBytes.size % 2 != 0) {
            result[pcmBytes.size - 1] = pcmBytes[pcmBytes.size - 1]
        }
        return result
    }
}
