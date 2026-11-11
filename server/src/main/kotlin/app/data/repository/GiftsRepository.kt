package app.data.repository

import com.ensodai.shared.avalon.gifts.models.*
import app.data.db.tables.gifts.GiftPacks
import app.data.db.tables.gifts.Gifts
import app.data.db.tables.gifts.UserSavedGiftPacks
import app.utils.dbQuery
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class GiftsRepository {

    suspend fun getUserSavedPacks(userId: Uuid, limit: Int, offset: Int): List<GiftPack> = dbQuery {
        val userSavedRows = UserSavedGiftPacks
            .selectAll()
            .where { UserSavedGiftPacks.userId eq userId }
            .orderBy(UserSavedGiftPacks.updatedAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .toList()

        if (userSavedRows.isEmpty()) return@dbQuery emptyList()

        val packIds = userSavedRows.map { it[UserSavedGiftPacks.packId].value }

        val packRows = GiftPacks
            .selectAll()
            .where { GiftPacks.id inList packIds }
            .toList()

        val giftsByPack = Gifts.selectAll()
            .where { Gifts.packId inList packIds }
            .map { it.toGift() }
            .groupBy { it.packId }

        val packsMap = packRows.associate { row ->
            val packId = row[GiftPacks.id].value
            packId to row.toGiftPack(giftsByPack[packId] ?: emptyList())
        }

        packIds.mapNotNull { packsMap[it] }
    }

    suspend fun savePackForUser(userId: Uuid, packId: Uuid) = dbQuery {
        val exists = UserSavedGiftPacks.selectAll()
            .where { (UserSavedGiftPacks.userId eq userId) and (UserSavedGiftPacks.packId eq packId) }
            .count() > 0

        if (!exists) {
            UserSavedGiftPacks.insert {
                it[this.userId] = userId
                it[this.packId] = packId
            }
        }
        Unit
    }

    suspend fun createGiftPack(
        authorId: Uuid?,
        name: String,
        description: String?,
        coverUrl: String,
        isPublic: Boolean,
        price: Int
    ): GiftPack = dbQuery {
        val insertStatement = GiftPacks.insert {
            it[this.authorId] = authorId
            it[this.name] = name
            it[this.description] = description
            it[this.coverUrl] = coverUrl
            it[this.isPublic] = isPublic
            it[this.price] = price
        }

        val id = insertStatement[GiftPacks.id].value

        GiftPack(
            id = id,
            authorId = authorId,
            name = name,
            description = description,
            coverUrl = coverUrl,
            isPublic = isPublic,
            price = price,
            gifts = emptyList()
        )
    }

    suspend fun createGift(
        packId: Uuid,
        fileUrl: String,
        price: Int,
        actionType: String,
        giftName: String,
        giftDescription: String,
        effectType: String = "NONE"
    ): Gift = dbQuery {
        val typeEnum = GiftActionType.valueOf(actionType)
        val effectEnum = GiftEffectType.valueOf(effectType)

        val insertStatement = Gifts.insert {
            it[this.packId] = packId
            it[this.fileUrl] = fileUrl
            it[this.price] = price
            it[this.actionType] = typeEnum
            it[this.giftName] = giftName
            it[this.giftDescription] = giftDescription
            it[this.effectType] = effectEnum
        }

        val id = insertStatement[Gifts.id].value

        Gift(
            id = id,
            packId = packId,
            fileUrl = fileUrl,
            price = price,
            actionType = typeEnum,
            giftName = giftName,
            giftDescription = giftDescription,
            effectType = effectEnum
        )
    }

    suspend fun getGiftPacks(limit: Int, offset: Int): List<GiftPack> = dbQuery {
        val packRows = GiftPacks.selectAll()
            .limit(limit)
            .offset(offset.toLong())
            .orderBy(GiftPacks.createdAt to SortOrder.DESC)
            .toList()

        if (packRows.isEmpty()) return@dbQuery emptyList()

        val packIds = packRows.map { it[GiftPacks.id].value }
        val giftsByPack = Gifts.selectAll()
            .where { Gifts.packId inList packIds }
            .map { it.toGift() }
            .groupBy { it.packId }

        packRows.map { row ->
            val packId = row[GiftPacks.id].value
            row.toGiftPack(giftsByPack[packId] ?: emptyList())
        }
    }

    suspend fun getGiftPackById(id: Uuid): GiftPack? = dbQuery {
        val packRow = GiftPacks.selectAll()
            .where { GiftPacks.id eq id }
            .singleOrNull() ?: return@dbQuery null

        val gifts = Gifts.selectAll()
            .where { Gifts.packId eq id }
            .map { it.toGift() }

        packRow.toGiftPack(gifts)
    }

    suspend fun getGiftById(id: Uuid): Gift? = dbQuery {
        Gifts.selectAll()
            .where { Gifts.id eq id }
            .singleOrNull()?.toGift()
    }

    private fun ResultRow.toGiftPack(gifts: List<Gift>): GiftPack {
        return GiftPack(
            id = this[GiftPacks.id].value,
            authorId = this[GiftPacks.authorId]?.value,
            name = this[GiftPacks.name],
            description = this[GiftPacks.description],
            coverUrl = this[GiftPacks.coverUrl],
            isPublic = this[GiftPacks.isPublic],
            price = this[GiftPacks.price],
            gifts = gifts
        )
    }

    private fun ResultRow.toGift(): Gift {
        return Gift(
            id = this[Gifts.id].value,
            packId = this[Gifts.packId].value,
            fileUrl = this[Gifts.fileUrl],
            price = this[Gifts.price],
            actionType = this[Gifts.actionType],
            giftName = this[Gifts.giftName],
            giftDescription = this[Gifts.giftDescription],
            effectType = this[Gifts.effectType]
        )
    }
}
