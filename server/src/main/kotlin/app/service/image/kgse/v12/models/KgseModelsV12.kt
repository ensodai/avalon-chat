package app.service.image.kgse.v12.models

import kotlinx.serialization.Serializable

enum class ScenarioTypeV12 {
    DAILY_CASUAL,         // Будничный диалог, прогулка, уютная обстановка
    CONVERSATIONAL,       // Дружеская беседа, обсуждение, теплая атмосфера
    DRAMATIC_EMOTIONAL,   // Драматический накал, серьезный разговор, напряжение сцены
    RPG_ADVENTURE,        // RPG, фэнтези, приключения, таверна, бой
    SCI_FI_CYBERPUNK      // Sci-fi, киберпанк, космос, неон, футуризм
}

enum class VisualRenderStyleV12 {
    SMARTPHONE_SNAPSHOT,  // Естественное фото на камеру смартфона (для современного реализма)
    FANTASY_ARTWORK,      // Детальная цифровая фэнтези-иллюстрация (для RPG)
    CYBERPUNK_SCIFI_ARTWORK // Неоновый киберпанк / сай-фай арт
}

enum class CameraCompositionV12 {
    FRONT_CAMERA_SELFIE,    // Селфи на вытянутую руку
    MIRROR_SELFIE,          // Селфи в зеркале (телефон виден)
    POV_CLOSE_UP,           // Крупный план лица от первого лица (глаза в глаза)
    POV_MEDIUM_SHOT,        // Средний план от первого лица
    OBSERVER_MEDIUM_SHOT    // Взгляд со стороны (средний или групповой план)
}

enum class PhysicalDistanceV12 {
    CLOSE_PROXIMITY,        // Близкая дистанция (крупный план лица/плеч, до 1 метра)
    CONVERSATIONAL,         // Разговорная дистанция (поясной портрет, 1-2 метра)
    MEDIUM_DISTANCE,        // Средняя дистанция (фигура в окружении, 2-3 метра)
    WIDE_ENVIRONMENT        // Общий план (фигура в полный рост или пейзаж)
}

@Serializable
data class SceneAnalysisResponseV12(
    val scenarioType: ScenarioTypeV12,
    val renderStyle: VisualRenderStyleV12,
    val cameraComposition: CameraCompositionV12,
    val physicalDistance: PhysicalDistanceV12,
    val characterAppearanceReference: String,
    val userAppearanceReference: String,
    val bodyPose: String,
    val handsAction: String,
    val facialExpression: String,
    val sceneAtmosphere: String,
    val wardrobeState: String,
    val environmentLighting: String,
    val otherPresentSubjects: List<String>,
    val activeProp: String,
    val overrideArtStyle: String? = null
)

@Serializable
data class PromptGenerationResponseV12(
    val subjectDescription: String,
    val environment: String,
    val kinematics: String,
    val vibeAndAtmosphere: String,
    val visualDescription: String
)
