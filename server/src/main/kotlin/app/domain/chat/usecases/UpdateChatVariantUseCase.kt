package app.domain.chat.usecases

import app.cache.RedisService
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariant
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.chatSessions.toResponse
import app.data.db.tables.images.ChatVariantImage
import app.data.db.tables.images.ChatVariantImages
import app.utils.dbQuery
import app.utils.getUTCTimeNow
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import com.ensodai.shared.avalon.chat.models.UpdateChatVariantRequest
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.select
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class UpdateChatVariantUseCase(
    private val redisService: RedisService
) {
    suspend fun execute(
        request: UpdateChatVariantRequest,
        userId: Uuid
    ): ChatVariantResponse {
        val tempImage = request.imageToken?.let { token ->
            redisService.popTempImage(token)
        }

        return dbQuery {
            val isOwned = (ChatVariants innerJoin ChatSessions)
                .select(ChatVariants.id)
                .where {
                    (ChatVariants.id eq request.variantId) and
                        (ChatSessions.userId eq userId) and
                        (ChatVariants.deletedAt.isNull()) and
                        (ChatSessions.deletedAt.isNull())
                }
                .count() > 0

            if (!isOwned) {
                throw IllegalStateException("Chat variant not found or access denied")
            }

            val variant = ChatVariant.findById(request.variantId)
                ?: throw IllegalStateException("Chat variant not found")

            val now = getUTCTimeNow()

            if (request.name != null) {
                val cleanName = request.name?.trim().orEmpty().take(255)
                if (cleanName.isNotBlank()) {
                    variant.name = cleanName
                }
            }

            if (request.removeImage) {
                ChatVariantImage.find { ChatVariantImages.chatVariantsId eq variant.id }
                    .forEach { it.delete() }
            } else if (tempImage != null) {
                val existingImage = ChatVariantImage.find { ChatVariantImages.chatVariantsId eq variant.id }
                    .singleOrNull()

                if (existingImage != null) {
                    existingImage.imagePath = tempImage.path
                    existingImage.thumbHash = tempImage.thumbHash
                    existingImage.updatedAt = now
                } else {
                    ChatVariantImage.new {
                        this.chatVariantsId = variant.id
                        this.imagePath = tempImage.path
                        this.thumbHash = tempImage.thumbHash
                        this.createdAt = now
                        this.updatedAt = now
                    }
                }
            }

            variant.updatedAt = now
            variant.toResponse()
        }
    }
}
