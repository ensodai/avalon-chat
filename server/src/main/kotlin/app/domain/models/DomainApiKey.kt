package app.domain.models

import kotlin.uuid.Uuid

/**
 * DTO для API-ключа, с которым работает доменный слой.
 * Содержит только необходимую информацию.
 */
data class DomainApiKey(
    val id: Uuid,
    val key: String
)