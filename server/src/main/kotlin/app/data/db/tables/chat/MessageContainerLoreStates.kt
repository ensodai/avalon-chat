package app.data.db.tables.chat

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.aiCharacter.AICharacterLorebookEntries
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid

/**
 * Реляционная таблица состояний таймеров лорбуков (sticky / cooldown) для контейнеров сообщений.
 * Обеспечивает полную типизацию без использования JSONB.
 */
object MessageContainerLoreStates : BaseUuidTable("message_container_lore_states") {
    val containerId = reference("container_id", MessageContainers, onDelete = ReferenceOption.CASCADE)
    val entryId = reference("entry_id", AICharacterLorebookEntries, onDelete = ReferenceOption.CASCADE)
    val stickyRemaining = integer("sticky_remaining").default(0)
    val cooldownRemaining = integer("cooldown_remaining").default(0)

    init {
        uniqueIndex("uq_container_entry_timed", containerId, entryId)
        index("idx_container_lore_states_cid", isUnique = false, containerId)
        index("idx_container_lore_states_eid", isUnique = false, entryId)
    }
}

class MessageContainerLoreState(id: EntityID<Uuid>) : BaseUuidEntity(id, MessageContainerLoreStates) {
    companion object : BaseUuidEntityClass<MessageContainerLoreState>(MessageContainerLoreStates)

    var container by MessageContainer referencedOn MessageContainerLoreStates.containerId
    var entryId by MessageContainerLoreStates.entryId
    var stickyRemaining by MessageContainerLoreStates.stickyRemaining
    var cooldownRemaining by MessageContainerLoreStates.cooldownRemaining
}
