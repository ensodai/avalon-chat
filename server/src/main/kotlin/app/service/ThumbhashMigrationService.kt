package app.service

import app.data.db.tables.images.CharacterAvatars
import app.data.db.tables.images.CharacterImages
import app.data.db.tables.images.ChatVariantImages
import app.data.db.tables.images.WelcomeCardImages
import app.service.image.ImagePreviewGenerator
import app.utils.dbQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.slf4j.LoggerFactory
import java.io.File
import javax.imageio.ImageIO

import org.koin.core.annotation.Single

@Single
class ThumbhashMigrationService {
    private val logger = LoggerFactory.getLogger(ThumbhashMigrationService::class.java)

    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                logger.info("Starting ThumbhashMigrationService...")
                processCharacterAvatars()
                processCharacterImages()
                processChatVariantImages()
                processWelcomeCardImages()
                logger.info("ThumbhashMigrationService completed successfully.")
            } catch (e: Throwable) {
                logger.error("ThumbhashMigrationService failed", e)
            }
        }
    }

    private suspend fun processCharacterAvatars() {
        val rows = dbQuery {
            CharacterAvatars.selectAll().where { CharacterAvatars.thumbHash.isNull() }
                .map { Pair(it[CharacterAvatars.id], it[CharacterAvatars.imagePath]) }
        }

        var count = 0
        for ((id, path) in rows) {
            val thumbHash = generateThumbHashForFile(path)
            if (thumbHash != null) {
                dbQuery {
                    CharacterAvatars.update({ CharacterAvatars.id eq id }) {
                        it[CharacterAvatars.thumbHash] = thumbHash
                    }
                }
                count++
            }
        }
        if (count > 0) logger.info("Updated $count CharacterAvatars with thumbHash.")
    }

    private suspend fun processCharacterImages() {
        val rows = dbQuery {
            CharacterImages.selectAll().where { CharacterImages.thumbHash.isNull() }
                .map { Pair(it[CharacterImages.id], it[CharacterImages.imagePath]) }
        }

        var count = 0
        for ((id, path) in rows) {
            val thumbHash = generateThumbHashForFile(path)
            if (thumbHash != null) {
                dbQuery {
                    CharacterImages.update({ CharacterImages.id eq id }) {
                        it[CharacterImages.thumbHash] = thumbHash
                    }
                }
                count++
            }
        }
        if (count > 0) logger.info("Updated $count CharacterImages with thumbHash.")
    }

    private suspend fun processChatVariantImages() {
        val rows = dbQuery {
            ChatVariantImages.selectAll().where { ChatVariantImages.thumbHash.isNull() }
                .map { Pair(it[ChatVariantImages.id], it[ChatVariantImages.imagePath]) }
        }

        var count = 0
        for ((id, path) in rows) {
            val thumbHash = generateThumbHashForFile(path)
            if (thumbHash != null) {
                dbQuery {
                    ChatVariantImages.update({ ChatVariantImages.id eq id }) {
                        it[ChatVariantImages.thumbHash] = thumbHash
                    }
                }
                count++
            }
        }
        if (count > 0) logger.info("Updated $count ChatVariantImages with thumbHash.")
    }

    private suspend fun processWelcomeCardImages() {
        val rows = dbQuery {
            WelcomeCardImages.selectAll().where { WelcomeCardImages.thumbHash.isNull() }
                .map { Pair(it[WelcomeCardImages.id], it[WelcomeCardImages.imagePath]) }
        }

        var count = 0
        for ((id, path) in rows) {
            val thumbHash = generateThumbHashForFile(path)
            if (thumbHash != null) {
                dbQuery {
                    WelcomeCardImages.update({ WelcomeCardImages.id eq id }) {
                        it[WelcomeCardImages.thumbHash] = thumbHash
                    }
                }
                count++
            }
        }
        if (count > 0) logger.info("Updated $count WelcomeCardImages with thumbHash.")
    }

    private fun generateThumbHashForFile(path: String): String? {
        return try {
            val file = File(path)
            if (!file.exists() || !file.isFile) return null
            val image = ImageIO.read(file) ?: return null
            ImagePreviewGenerator.generateThumbHashString(image)
        } catch (e: Exception) {
            logger.warn("Failed to generate thumbHash for $path: ${e.message}")
            null
        }
    }
}
