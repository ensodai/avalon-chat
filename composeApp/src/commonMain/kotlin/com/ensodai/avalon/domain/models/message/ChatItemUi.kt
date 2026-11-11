package com.ensodai.avalon.domain.models.message

import com.ensodai.avalon.data.db.models.chat.SlotWithContainers
import com.ensodai.avalon.domain.models.ChatMessageModel
import com.ensodai.avalon.domain.models.MessageContainerModel
import com.ensodai.avalon.domain.models.MessageContentModel
import com.ensodai.shared.avalon.chat.services.MessageContentCodec

fun SlotWithContainers.toChatMessageModel(): ChatMessageModel {
    val containerModels = this.containers
        .sortedBy { it.container.createdAt }
        .map { nested ->

            val contentModels = nested.contents
                .sortedBy { it.orderIndex }
                .map { contentEntity ->

                    val parsedPayload = MessageContentCodec.decode(
                        type = contentEntity.type,
                        contentString = contentEntity.content
                    )

                    MessageContentModel(
                        id = contentEntity.id,
                        type = contentEntity.type,
                        payload = parsedPayload
                    )
                }

            MessageContainerModel(
                id = nested.container.id,
                slotId = nested.container.slotId,
                rarity = nested.container.rarity,
                role = nested.container.role,
                status = nested.container.status,
                createdAt = nested.container.createdAt,
                userReaction = nested.container.userReaction,
                characterReaction = nested.container.characterReaction,
                isRead = nested.container.isRead,
                showOutreachFeedback = nested.container.showOutreachFeedback,
                content = contentModels
            )
        }

    return ChatMessageModel(
        slotId = this.slot.id,
        createdAt = this.slot.createdAt,
        activeContainerId = this.slot.activeContainerId,
        containers = containerModels
    )
}