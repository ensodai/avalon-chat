package tgBot.repository

data class DatabaseStats(
    val totalUsers: Long,
    val totalAliveUsers: Long,
    val totalDeletedUsers: Long,
    val usersByPremiumStatus: Map<String, Long>,
    val totalCharacters: Long,
    val totalPublicCharacters: Long,
    val totalPrivateCharacters: Long,
    val totalCharacterImages: Long,
    val totalCategories: Long,
    val totalGeneratedMessages: Long,
    val totalApiKeys: Long
)