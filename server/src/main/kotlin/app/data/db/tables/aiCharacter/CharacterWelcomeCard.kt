package app.data.db.tables.aiCharacter

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid

object CharacterWelcomeCard : BaseUuidTable("character_welcome_card") {

    val character = reference("character_id", AICharacters)
        .uniqueIndex()
    val welcomeCardDescription = text("welcome_card_description").nullable()
    val cardColorStart = varchar("card_color_start", 20).default("DEFAULT_COLOR")
    val cardColorEnd = varchar("card_color_end", 20).default("DEFAULT_COLOR")
    val cardColorLight = varchar("card_color_light", 20).default("DEFAULT_COLOR")
    val isEnableLight = bool("is_enable_light").default(false)
}

class CharacterWelcomeCardEntity(id: EntityID<Uuid>) : BaseUuidEntity(id, CharacterWelcomeCard) {
    companion object : BaseUuidEntityClass<CharacterWelcomeCardEntity>(CharacterWelcomeCard)

    var character by AICharacter referencedOn CharacterWelcomeCard.character
    var welcomeCardDescription: String? by CharacterWelcomeCard.welcomeCardDescription
    var cardColorStart: String by CharacterWelcomeCard.cardColorStart
    var cardColorEnd: String by CharacterWelcomeCard.cardColorEnd
    var cardColorLight: String by CharacterWelcomeCard.cardColorLight
    var isEnableLight: Boolean by CharacterWelcomeCard.isEnableLight
}

fun CharacterWelcomeCardEntity.toResponse(
    welcomeCardImages: List<com.ensodai.shared.avalon.character.models.WelcomeCardImageResponse>
): com.ensodai.shared.avalon.character.models.WelcomeCardResponse {
    return com.ensodai.shared.avalon.character.models.WelcomeCardResponse(
        id = this.id.value,
        welcomeCardDescription = this.welcomeCardDescription,
        welcomeCardImages = welcomeCardImages,
        startColor = this.cardColorStart,
        endColor = this.cardColorEnd,
        cardColorLight = this.cardColorLight,
        isEnableLight = this.isEnableLight
    )
}
