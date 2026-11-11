package app.service.tts.pipeline

import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface TtsPipelineProvider {
    fun generateSpeechStream(
        userId: Uuid,
        text: String,
        voice: VoiceModel,
        aiSettings: UserAiSettingsResponse?
    ): Flow<ByteArray>
}
