package app.data.models

import app.data.db.tables.chat.MessageContainerInternalDto
import app.data.db.tables.chat.MessageSlotSimpleInternalDto

data class CreatedTurnInternalDto(
    val slot: MessageSlotSimpleInternalDto,
    val message: MessageContainerInternalDto
)