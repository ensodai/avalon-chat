package app.data.db

import app.data.db.tables.*
import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.aiCharacter.CharacterEvents
import app.data.db.tables.aiCharacter.CharacterWelcomeCard
import app.data.db.tables.aiCharacter.OutreachHistoryTable
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.images.CharacterAvatars
import app.data.db.tables.images.CharacterImages
import app.data.db.tables.images.ChatVariantImages
import app.data.db.tables.images.WelcomeCardImages
import app.data.db.tables.images.UnlockedCharacterImages
import app.data.db.tables.users.AppUsers
import app.data.db.tables.users.UserPersonas
import app.data.db.tables.users.UserPityCounters
import app.data.db.tables.users.CreatorAffinityTable
import app.data.db.tables.chat.MessageContainers
import app.data.db.tables.chat.MessageContainerLoreStates
import app.data.db.tables.chat.MessageContents
import app.data.db.tables.chat.MessageSlots
import app.data.db.tables.chat.MessageReadReceiptsTable
import app.data.db.tables.gifts.GiftPacks
import app.data.db.tables.gifts.Gifts
import app.data.db.tables.ai.AiProviders
import app.data.db.tables.aiCharacter.AICharacterAlternateGreetings
import app.data.db.tables.aiCharacter.AICharacterLorebookEntries
import app.data.db.tables.aiCharacter.AICharacterLorebooks
import app.data.db.tables.aiCharacter.AICharacterPromptDetails
import app.data.db.tables.aiCharacter.AICharacterTags
import app.data.db.tables.aiCharacter.UserCharacterLorebooks
import app.data.db.tables.users.UserAiSettings
import app.data.db.tables.gifts.UserSavedGiftPacks
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object AllTables {
    val list = listOf(
        AppUsers,
        UserPersonas,
        ChatSessions,
        ChatVariants,
        UserPityCounters,
        AICharacters,
        ApiKeys,
        Categories,
        CharacterWelcomeCard,
        GlobalSettings,
        ServerCategories,
        WelcomeCardImages,
        ChatVariantImages,
        CharacterImages,
        CharacterAvatars,
        CharacterEvents,
        UnlockedCharacterImages,

        AuthSessions,
        MessageContainers,
        MessageContainerLoreStates,
        MessageSlots,
        MessageContents,
        MessageReadReceiptsTable,
        
        CreatorAffinityTable,
        OutreachHistoryTable,

        GiftPacks,
        Gifts,
        UserSavedGiftPacks,
        AiProviders,
        UserAiSettings,

        AICharacterPromptDetails,
        AICharacterAlternateGreetings,
        AICharacterTags,
        AICharacterLorebooks,
        AICharacterLorebookEntries,
        UserCharacterLorebooks
    )
}

object DatabaseValidator {
    fun validateAllTables() {
        transaction {
            println("Запускаю проверку схемы через Exposed + Flyway...")

            var hasErrors = false

            AllTables.list.forEach { table ->
                print("Проверяю ${table.tableName}... ")

                try {
                    SchemaUtils.checkMappingConsistence(table)
                    println("ОК")
                } catch (e: Exception) {
                    println("ОШИБКА")
                    println("   ${e.message}")
                    hasErrors = true
                }
            }

            if (hasErrors) {
                throw IllegalStateException("Схема БД не совпадает с кодом! Исправь миграции.")
            } else {
                println("Всё идеально синхронизировано с Flyway!")
            }
        }
    }
}