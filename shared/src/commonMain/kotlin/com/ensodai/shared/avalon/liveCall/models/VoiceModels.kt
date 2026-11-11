package com.ensodai.shared.avalon.liveCall.models

import kotlinx.serialization.Serializable

@Serializable
enum class VoiceModel(
    val modelName: String,
    val emoji: String,
    val gender: Gender,
    val description: String
) {
    // MALE voices
    ZEPHYR("Zephyr", "🧑", Gender.MALE, "Bright"),
    ORUS("Orus", "🧔", Gender.MALE, "Firm"),
    UMBRIEL("Umbriel", "🧑‍🦱", Gender.MALE, "Easy-going"),
    SCHEDAR("Schedar", "👨‍🦰", Gender.MALE, "Even"),
    ACHIRD("Achird", "👨", Gender.MALE, "Friendly"),
    PUCK("Puck", "🧑‍🎤", Gender.MALE, "Upbeat"),
    FENRIR("Fenrir", "🧔‍♂️", Gender.MALE, "Excitable"),
    ENCELADUS("Enceladus", "👨‍🦳", Gender.MALE, "Breathy"),
    ALGENIB("Algenib", "🧔", Gender.MALE, "Gravelly"),
    GACRUX("Gacrux", "👴", Gender.MALE, "Mature"),
    SADALTAGER("Sadaltager", "🧑‍🏫", Gender.MALE, "Knowledgeable"),
    CHARON("Charon", "🧑‍🚀", Gender.MALE, "Informative"),
    IAPETUS("Iapetus", "👨‍💼", Gender.MALE, "Clear"),
    ALNILAM("Alnilam", "🧑‍✈️", Gender.MALE, "Firm"),
    PULCHERRIMA("Pulcherrima", "🧑‍🎨", Gender.MALE, "Forward"),

    // FEMALE voices
    KORE("Kore", "👩", Gender.FEMALE, "Firm"),
    AUTONOE("Autonoe", "👩‍🦰", Gender.FEMALE, "Bright"),
    ERINOME("Erinome", "👧", Gender.FEMALE, "Clear"),
    LAOMEDEIA("Laomedeia", "💃", Gender.FEMALE, "Upbeat"),
    SADACHBIA("Sadachbia", "👩‍🎤", Gender.FEMALE, "Lively"),
    AOEDE("Aoede", "👩‍🦱", Gender.FEMALE, "Breezy"),
    ALGIEBA("Algieba", "🎶", Gender.FEMALE, "Smooth"),
    ACHERNAR("Achernar", "👩‍🦳", Gender.FEMALE, "Soft"),
    ZUBENELGENUBI("Zubenelgenubi", "👩‍🦲", Gender.FEMALE, "Casual"),
    LEDA("Leda", "👧", Gender.FEMALE, "Youthful"),
    CALLIRRHOE("Callirrhoe", "👩‍🦱", Gender.FEMALE, "Easy-going"),
    DESPINA("Despina", "👩", Gender.FEMALE, "Smooth"),
    RASALGETHI("Rasalgethi", "👩‍🏫", Gender.FEMALE, "Informative"),
    VINDEMIATRIX("Vindemiatrix", "👩‍🌾", Gender.FEMALE, "Gentle"),
    SULAFAT("Sulafat", "👩‍🎨", Gender.FEMALE, "Warm");

    @Serializable
    enum class Gender { MALE, FEMALE }

    val displayName: String
        get() = "$emoji $modelName — $description"

    override fun toString(): String = displayName
}

@Serializable
enum class Language(
    val code: String,
    val flag: String,
    val displayName: String
) {
    RU("RU", "🇷🇺", "Русский"),
    EN("EN", "🇬🇧", "English"),
    DE("DE", "🇩🇪", "Deutsch"),
    FR("FR", "🇫🇷", "Français"),
    ES("ES", "🇪🇸", "Español"),
    IT("IT", "🇮🇹", "Italiano"),
    PT("PT", "🇵🇹", "Português"),
    ZH("ZH", "🇨🇳", "中文"),
    JA("JA", "🇯🇵", "日本語"),
    KO("KO", "🇰🇷", "한국어");

    override fun toString(): String = "$flag $code"
}
