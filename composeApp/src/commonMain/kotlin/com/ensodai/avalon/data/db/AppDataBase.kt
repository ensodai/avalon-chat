@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.ensodai.avalon.data.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.ColumnTypeConverters
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.async.executeSQL
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.data.db.cache.CategoryEntity
import com.ensodai.avalon.data.db.cache.ServerCategoryEntity
import com.ensodai.avalon.data.db.dao.AppSettingsDao
import com.ensodai.avalon.data.db.dao.CacheDao
import com.ensodai.avalon.data.db.dao.CharacterDao
import com.ensodai.avalon.data.db.dao.ChatHistoryDao
import com.ensodai.avalon.data.db.dao.ChatSessionDao
import com.ensodai.avalon.data.db.dao.ChatVariantDao
import com.ensodai.avalon.data.db.dao.GiftDao
import com.ensodai.avalon.data.db.dao.LogoutDao
import com.ensodai.avalon.data.db.dao.MainRulesDao
import com.ensodai.avalon.data.db.dao.MessageQueueRestorerDao
import com.ensodai.avalon.data.db.dao.MessageSlotDao
import com.ensodai.avalon.data.db.dao.UpdateCharacterDao
import com.ensodai.avalon.data.db.dao.UserDao
import com.ensodai.avalon.data.db.models.AppSettingsEntity
import com.ensodai.avalon.data.db.models.ChatBackgroundImageEntity
import com.ensodai.avalon.data.db.models.MainRulesEntity
import com.ensodai.avalon.data.db.models.ShowPromoFlagsEntity
import com.ensodai.avalon.data.db.models.character.CharacterAvatarEntity
import com.ensodai.avalon.data.db.models.character.CharacterEntity
import com.ensodai.avalon.data.db.models.character.CharacterImageEntity
import com.ensodai.avalon.data.db.models.character.CharacterEventRoadmapEntity
import com.ensodai.avalon.data.db.models.character.WelcomeCardEntity
import com.ensodai.avalon.data.db.models.character.WelcomeCardImageEntity
import com.ensodai.avalon.data.db.models.chat.ChatSessionEntity
import com.ensodai.avalon.data.db.models.chat.ChatVariantEntity
import com.ensodai.avalon.data.db.models.chat.ChatVariantImageEntity
import com.ensodai.avalon.data.db.models.gift.GiftItemEntity
import com.ensodai.avalon.data.db.models.gift.GiftPackEntity
import com.ensodai.avalon.data.db.models.message.MessageContainerEntity
import com.ensodai.avalon.data.db.models.message.MessageContentEntity
import com.ensodai.avalon.data.db.models.message.MessageSlotEntity
import com.ensodai.avalon.data.db.models.user.Persona
import com.ensodai.avalon.data.db.models.user.User
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        AppSettingsEntity::class,
        CharacterEntity::class,
        CharacterImageEntity::class,
        ChatSessionEntity::class,
        CategoryEntity::class,
        CharacterAvatarEntity::class,
        User::class,
        Persona::class,
        ShowPromoFlagsEntity::class,
        MainRulesEntity::class,
        ChatVariantEntity::class,
        ChatVariantImageEntity::class,
        ServerCategoryEntity::class,
        WelcomeCardImageEntity::class,
        WelcomeCardEntity::class,
        MessageSlotEntity::class,
        MessageContainerEntity::class,
        MessageContentEntity::class,
        AvailableLlmConfigs::class,
        GiftPackEntity::class,
        GiftItemEntity::class,
        ChatBackgroundImageEntity::class,
        CharacterEventRoadmapEntity::class
    ],
    version = 36,
    exportSchema = true
)
@ColumnTypeConverters(ColumnTypeConverter::class)
@ConstructedBy(AppDataBaseConstructor::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun chatModelConfigDao(): CharacterDao
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun cacheDao(): CacheDao
    abstract fun logoutDao(): LogoutDao
    abstract fun userDao(): UserDao
    abstract fun mainRulesDao(): MainRulesDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun messageSlotDao(): MessageSlotDao
    abstract fun chatVariantDao(): ChatVariantDao
    abstract fun updateCharacterDao(): UpdateCharacterDao
    abstract fun messageQueueRestorerDao(): MessageQueueRestorerDao
    abstract fun giftDao(): GiftDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDataBaseConstructor : RoomDatabaseConstructor<AppDataBase> {
    override fun initialize(): AppDataBase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDataBase>
): AppDataBase {
    return builder
        .fallbackToDestructiveMigration(dropAllTables = true)
        .addMigrations(

            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_15,
            MIGRATION_15_16,
            MIGRATION_16_17,
            MIGRATION_17_18,
            MIGRATION_18_19,
            MIGRATION_19_20,
            MIGRATION_20_21,
            MIGRATION_21_22,
            MIGRATION_22_23,
            MIGRATION_23_24,
            MIGRATION_24_25,
            MIGRATION_25_26,
            MIGRATION_26_27,
            MIGRATION_27_28,
            MIGRATION_28_29,
            MIGRATION_29_30,
            MIGRATION_30_31,
            MIGRATION_31_32,
            MIGRATION_32_33,
            MIGRATION_33_34,
            MIGRATION_34_35,
            MIGRATION_35_36
        )
        .addCallback(object : RoomDatabase.Callback() {
            override suspend fun onOpen(connection: SQLiteConnection) {
                super.onOpen(connection)
                connection.executeSQL(
                    "UPDATE `app_settings` SET `bubble_shape_type` = 'Tail' WHERE `bubble_shape_type` = 'tail'"
                )
                connection.executeSQL(
                    """
                    INSERT OR IGNORE INTO `app_settings` (
                        `id`, 
                        `auto_replace_api_keys`, 
                        `local_mode`, 
                        `language`, 
                        `is_safety_reminder_accepted`, 
                        `flow_mode`, 
                        `theme_id`, 
                        `user_bubble_fill_type`, 
                        `bot_bubble_fill_type`, 
                        `chat_background_type`, 
                        `chat_background_blur`, 
                        `chat_background_dim_amount`,
                        `bubble_font_size`,
                        `bubble_shape_type`,
                        `bubble_corner_radius`,
                        `show_ai_warning`,
                        `chat_roleplay_preset`
                    ) VALUES (
                        1, 1, 0, 'en', 0, 0, 'tech_noir', 'solid', 'solid', 'default', 0, 0.0, 16, 'Tail', 16, 1, 'standard_bubble'
                    )
                    """.trimIndent()
                )
                connection.executeSQL(
                    """
                    INSERT OR IGNORE INTO `show_promo_flags_entity` (
                        `id`,
                        `telegramReminder`,
                        `openApiTransitionAnnouncement`
                    ) VALUES (
                        1, 1, 1
                    )
                    """.trimIndent()
                )
            }
        })
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL(
            """
            CREATE TABLE IF NOT EXISTS `llm_configs` (
                `code_name` TEXT NOT NULL, 
                `display_name` TEXT NOT NULL, 
                `price` INTEGER NOT NULL, 
                PRIMARY KEY(`code_name`)
            )
            """
        )
        connection.executeSQL("ALTER TABLE `chat_session_entity` ADD COLUMN `selected_llm_config_code_name` TEXT")
        connection.executeSQL("ALTER TABLE `user` ADD COLUMN `reserve_balance` INTEGER NOT NULL DEFAULT 0")
        connection.executeSQL("ALTER TABLE `user` ADD COLUMN `max_reserve_balance` INTEGER NOT NULL DEFAULT 0")
        connection.executeSQL("ALTER TABLE `user` ADD COLUMN `premium_status` TEXT NOT NULL DEFAULT 'FREE'")
        connection.executeSQL("ALTER TABLE `user` ADD COLUMN `payment_provider` TEXT")
        connection.executeSQL("ALTER TABLE `user` ADD COLUMN `next_billing_date` TEXT")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `character_entity` ADD COLUMN `sync_state` TEXT NOT NULL DEFAULT 'NONE'")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("CREATE TABLE IF NOT EXISTS `gift_packs` (`id` TEXT NOT NULL, `authorId` TEXT, `name` TEXT NOT NULL, `description` TEXT, `coverUrl` TEXT NOT NULL, `isPublic` INTEGER NOT NULL, `price` INTEGER NOT NULL, `isOwned` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.executeSQL("CREATE TABLE IF NOT EXISTS `gift_items` (`id` TEXT NOT NULL, `packId` TEXT NOT NULL, `fileUrl` TEXT NOT NULL, `price` INTEGER NOT NULL, `actionType` TEXT NOT NULL, `actionPayload` TEXT, PRIMARY KEY(`id`))")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL(
            """
            CREATE TABLE IF NOT EXISTS `gift_items_new` (
                `id` TEXT NOT NULL, 
                `pack_id` TEXT NOT NULL, 
                `file_url` TEXT NOT NULL, 
                `price` INTEGER NOT NULL, 
                `action_type` TEXT NOT NULL, 
                `gift_name` TEXT NOT NULL DEFAULT '', 
                `gift_description` TEXT NOT NULL DEFAULT '', 
                PRIMARY KEY(`id`)
            )
            """
        )
        connection.executeSQL(
            """
            INSERT INTO `gift_items_new` (id, pack_id, file_url, price, action_type, gift_name, gift_description)
            SELECT id, packId, fileUrl, price, actionType, '', '' FROM gift_items
            """
        )
        connection.executeSQL("DROP TABLE `gift_items`")
        connection.executeSQL("ALTER TABLE `gift_items_new` RENAME TO `gift_items`")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `character_image_entity` ADD COLUMN `thumb_hash` TEXT")
        connection.executeSQL("ALTER TABLE `character_avatar_entity` ADD COLUMN `thumb_hash` TEXT")
        connection.executeSQL("ALTER TABLE `welcome_card_image_entity` ADD COLUMN `thumb_hash` TEXT")
        connection.executeSQL("ALTER TABLE `chat_variant_image` ADD COLUMN `thumb_hash` TEXT")
    }
}
val MIGRATION_11_12 = object : Migration(11, 12) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("DROP TABLE IF EXISTS `api_keys_table`")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override suspend fun migrate(connection: SQLiteConnection) {
        // Пересоздаём таблицу app_settings без DEFAULT у flow_mode
        connection.executeSQL(
            """
            CREATE TABLE IF NOT EXISTS `app_settings_new` (
                `id` INTEGER NOT NULL,
                `auto_replace_api_keys` INTEGER NOT NULL,
                `local_mode` INTEGER NOT NULL,
                `language` TEXT NOT NULL,
                `is_safety_reminder_accepted` INTEGER NOT NULL,
                `flow_mode` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """
        )
        connection.executeSQL(
            """
            INSERT INTO `app_settings_new` (
                `id`,
                `auto_replace_api_keys`,
                `local_mode`,
                `language`,
                `is_safety_reminder_accepted`,
                `flow_mode`
            )
            SELECT
                `id`,
                `auto_replace_api_keys`,
                `local_mode`,
                `language`,
                `is_safety_reminder_accepted`,
                `flow_mode`
            FROM `app_settings`
            """
        )
        connection.executeSQL("DROP TABLE `app_settings`")
        connection.executeSQL("ALTER TABLE `app_settings_new` RENAME TO `app_settings`")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `app_settings` ADD COLUMN `theme_id` TEXT")
        connection.executeSQL("ALTER TABLE `app_settings` ADD COLUMN `custom_user_bubble_color` TEXT")
        connection.executeSQL("ALTER TABLE `app_settings` ADD COLUMN `custom_bot_bubble_color` TEXT")
        connection.executeSQL("ALTER TABLE `app_settings` ADD COLUMN `chat_background_url` TEXT")
        connection.executeSQL("ALTER TABLE `app_settings` ADD COLUMN `chat_background_blur` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override suspend fun migrate(connection: SQLiteConnection) {
        // Создаем временную таблицу с ИДЕАЛЬНОЙ структурой (которую требует Room)
        // ВАЖНО: Мы не пишем "DEFAULT", так как Room ждет 'undefined' (дефолт берется из Kotlin-кода)
        connection.executeSQL(
            """
            CREATE TABLE IF NOT EXISTS `app_settings_new` (
                `id` INTEGER NOT NULL,
                `auto_replace_api_keys` INTEGER NOT NULL,
                `local_mode` INTEGER NOT NULL,
                `language` TEXT NOT NULL,
                `is_safety_reminder_accepted` INTEGER NOT NULL,
                `flow_mode` INTEGER NOT NULL,
                `theme_id` TEXT NOT NULL,
                `user_bubble_fill_type` TEXT NOT NULL,
                `custom_user_bubble_color` TEXT,
                `user_bubble_gradient_start_color` TEXT,
                `user_bubble_gradient_end_color` TEXT,
                `bot_bubble_fill_type` TEXT NOT NULL,
                `custom_bot_bubble_color` TEXT,
                `bot_bubble_gradient_start_color` TEXT,
                `bot_bubble_gradient_end_color` TEXT,
                `chat_background_type` TEXT NOT NULL,
                `chat_background_color` TEXT,
                `chat_background_url` TEXT,
                `chat_background_pattern_id` TEXT,
                `chat_background_blur` INTEGER NOT NULL,
                `chat_background_dim_amount` REAL NOT NULL,
                PRIMARY KEY(`id`)
            )
            """
        )

        // Копируем данные из старой таблицы в новую, исправляя типы на ходу
        // COALESCE гарантирует, что NOT NULL колонки получат значение, даже если в старой таблице был NULL
        connection.executeSQL(
            """
            INSERT INTO `app_settings_new` (
                `id`, `auto_replace_api_keys`, `local_mode`, `language`, 
                `is_safety_reminder_accepted`, `flow_mode`, `theme_id`, 
                `user_bubble_fill_type`, `custom_user_bubble_color`, `user_bubble_gradient_start_color`, `user_bubble_gradient_end_color`,
                `bot_bubble_fill_type`, `custom_bot_bubble_color`, `bot_bubble_gradient_start_color`, `bot_bubble_gradient_end_color`,
                `chat_background_type`, `chat_background_color`, `chat_background_url`, `chat_background_pattern_id`, 
                `chat_background_blur`, `chat_background_dim_amount`
            )
            SELECT 
                `id`, `auto_replace_api_keys`, `local_mode`, `language`, 
                `is_safety_reminder_accepted`, `flow_mode`, COALESCE(`theme_id`, 'tech_noir'), 
                'solid', `custom_user_bubble_color`, NULL, NULL,
                'solid', `custom_bot_bubble_color`, NULL, NULL,
                CASE WHEN `chat_background_url` IS NOT NULL THEN 'image' ELSE 'default' END, NULL, `chat_background_url`, NULL,
                COALESCE(`chat_background_blur`, 0), 0.0
            FROM `app_settings`
            """
        )

        // Удаляем старую "неправильную" таблицу и переименовываем новую в рабочую
        connection.executeSQL("DROP TABLE `app_settings`")
        connection.executeSQL("ALTER TABLE `app_settings_new` RENAME TO `app_settings`")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `app_settings` ADD COLUMN `bubble_font_size` INTEGER NOT NULL DEFAULT 16")
        connection.executeSQL("ALTER TABLE `app_settings` ADD COLUMN `bubble_shape_type` TEXT NOT NULL DEFAULT 'tail'")
        connection.executeSQL("ALTER TABLE `app_settings` ADD COLUMN `bubble_corner_radius` INTEGER NOT NULL DEFAULT 16")
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL(
            """
            CREATE TABLE IF NOT EXISTS `chat_background_images` (
                `id` TEXT NOT NULL, 
                `url` TEXT NOT NULL, 
                PRIMARY KEY(`id`)
            )
            """
        )
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `chat_session_entity` ADD COLUMN `draft_message` TEXT")
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `character_entity` ADD COLUMN `unclaimed_energy` REAL")
        connection.executeSQL("ALTER TABLE `character_entity` ADD COLUMN `total_earned_energy` REAL")
    }
}

val MIGRATION_19_20 = object : Migration(19, 20) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `message_containers` ADD COLUMN `is_read` INTEGER NOT NULL DEFAULT 0")
        connection.executeSQL("ALTER TABLE `message_containers` ADD COLUMN `show_outreach_feedback` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_20_21 = object : Migration(20, 21) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `chat_variant_entity` ADD COLUMN `unread_count` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_21_22 = object : Migration(21, 22) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `character_entity` ADD COLUMN `is_outreach_enabled` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_22_23 = object : Migration(22, 23) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `user` ADD COLUMN `is_outreach_enabled` INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_23_24 = object : Migration(23, 24) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `user` ADD COLUMN `daily_outreach_limit` INTEGER NOT NULL DEFAULT 3")
    }
}

val MIGRATION_24_25 = object : Migration(24, 25) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `character_entity` ADD COLUMN `voice` TEXT NOT NULL DEFAULT 'ZEPHYR'")
    }
}

val MIGRATION_25_26 = object : Migration(25, 26) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `character_entity` ADD COLUMN `relationship_level` INTEGER NOT NULL DEFAULT 1")
        connection.executeSQL("ALTER TABLE `character_entity` ADD COLUMN `experience_points` INTEGER NOT NULL DEFAULT 0")
        connection.executeSQL("ALTER TABLE `character_entity` ADD COLUMN `events_count` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_26_27 = object : Migration(26, 27) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `character_entity` ADD COLUMN `interaction_format` TEXT NOT NULL DEFAULT 'LITERARY_RPG'")
    }
}

val MIGRATION_27_28 = object : Migration(27, 28) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `character_image_entity` ADD COLUMN `is_private` INTEGER NOT NULL DEFAULT 0")
        connection.executeSQL("ALTER TABLE `character_image_entity` ADD COLUMN `energy_cost` INTEGER NOT NULL DEFAULT 0")
        connection.executeSQL("ALTER TABLE `character_image_entity` ADD COLUMN `required_level` INTEGER")
        connection.executeSQL("ALTER TABLE `character_image_entity` ADD COLUMN `is_unlocked` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_28_29 = object : Migration(28, 29) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL(
            """
            CREATE TABLE IF NOT EXISTS `character_event_roadmap_entity` (
                `character_id` TEXT NOT NULL,
                `level` INTEGER NOT NULL,
                `has_reward` INTEGER NOT NULL,
                `is_unlocked` INTEGER NOT NULL,
                `trigger_scenario` TEXT,
                `reward_photo_id` TEXT,
                `reward_photo_url` TEXT,
                `reward_photo_thumb_hash` TEXT,
                `reward_photo_is_private` INTEGER NOT NULL,
                `reward_photo_energy_cost` INTEGER NOT NULL,
                `reward_photo_required_level` INTEGER,
                `reward_photo_is_unlocked` INTEGER NOT NULL,
                PRIMARY KEY(`character_id`, `level`),
                FOREIGN KEY(`character_id`) REFERENCES `character_entity`(`id`) ON DELETE CASCADE
            )
            """
        )
    }
}

val MIGRATION_29_30 = object : Migration(29, 30) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `app_settings` ADD COLUMN `show_ai_warning` INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_30_31 = object : Migration(30, 31) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `user` ADD COLUMN `is_flow_mode_enabled` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_31_32 = object : Migration(31, 32) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `app_settings` ADD COLUMN `chat_roleplay_preset` TEXT NOT NULL DEFAULT 'standard_bubble'")
    }
}

val MIGRATION_32_33 = object : Migration(32, 33) {
    override suspend fun migrate(connection: SQLiteConnection) {
        // message_slots
        connection.executeSQL(
            "CREATE TABLE IF NOT EXISTS `message_slots_new` (`id` TEXT NOT NULL, `sync_action` TEXT NOT NULL, `chat_variant_id` TEXT NOT NULL, `active_container_id` TEXT, `created_at` TEXT NOT NULL, `updated_at` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`chat_variant_id`) REFERENCES `chat_variant_entity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        connection.executeSQL(
            "INSERT INTO `message_slots_new` (`id`, `sync_action`, `chat_variant_id`, `active_container_id`, `created_at`, `updated_at`) SELECT `id`, `sync_action`, `chat_variant_id`, `active_container_id`, `created_at`, `updated_at` FROM `message_slots`"
        )
        connection.executeSQL("DROP TABLE `message_slots`")
        connection.executeSQL("ALTER TABLE `message_slots_new` RENAME TO `message_slots`")
        connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_message_slots_chat_variant_id` ON `message_slots` (`chat_variant_id`)")
        connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_message_slots_created_at` ON `message_slots` (`created_at`)")

        // message_containers
        connection.executeSQL(
            "CREATE TABLE IF NOT EXISTS `message_containers_new` (`id` TEXT NOT NULL, `slot_id` TEXT NOT NULL, `role` TEXT NOT NULL, `rarity` TEXT NOT NULL, `status` TEXT NOT NULL, `sync_action` TEXT NOT NULL, `user_reaction` TEXT, `character_reaction` TEXT, `is_read` INTEGER NOT NULL, `show_outreach_feedback` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `updated_at` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`slot_id`) REFERENCES `message_slots`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        connection.executeSQL(
            "INSERT INTO `message_containers_new` (`id`, `slot_id`, `role`, `rarity`, `status`, `sync_action`, `user_reaction`, `character_reaction`, `is_read`, `show_outreach_feedback`, `created_at`, `updated_at`) SELECT `id`, `slot_id`, `role`, `rarity`, `status`, `sync_action`, `user_reaction`, `character_reaction`, `is_read`, `show_outreach_feedback`, `created_at`, `updated_at` FROM `message_containers`"
        )
        connection.executeSQL("DROP TABLE `message_containers`")
        connection.executeSQL("ALTER TABLE `message_containers_new` RENAME TO `message_containers`")
        connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_message_containers_slot_id` ON `message_containers` (`slot_id`)")
        connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_message_containers_status` ON `message_containers` (`status`)")
        connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_message_containers_sync_action` ON `message_containers` (`sync_action`)")

        // message_contents
        connection.executeSQL(
            "CREATE TABLE IF NOT EXISTS `message_contents_new` (`id` TEXT NOT NULL, `container_id` TEXT NOT NULL, `avalon_message_content_type` TEXT NOT NULL, `content` TEXT NOT NULL, `order_index` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`container_id`) REFERENCES `message_containers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        connection.executeSQL(
            "INSERT INTO `message_contents_new` (`id`, `container_id`, `avalon_message_content_type`, `content`, `order_index`) SELECT `id`, `container_id`, `avalon_message_content_type`, `content`, `order_index` FROM `message_contents`"
        )
        connection.executeSQL("DROP TABLE `message_contents`")
        connection.executeSQL("ALTER TABLE `message_contents_new` RENAME TO `message_contents`")
        connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_message_contents_container_id` ON `message_contents` (`container_id`)")
    }
}

val MIGRATION_33_34 = object : Migration(33, 34) {
    override suspend fun migrate(connection: SQLiteConnection) {
        // message_slots
        connection.executeSQL(
            "CREATE TABLE IF NOT EXISTS `message_slots_new` (`id` TEXT NOT NULL, `sync_action` TEXT NOT NULL, `chat_variant_id` TEXT NOT NULL, `active_container_id` TEXT, `created_at` TEXT NOT NULL, `updated_at` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`chat_variant_id`) REFERENCES `chat_variant_entity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        connection.executeSQL(
            "INSERT INTO `message_slots_new` (`id`, `sync_action`, `chat_variant_id`, `active_container_id`, `created_at`, `updated_at`) SELECT `id`, `sync_action`, `chat_variant_id`, `active_container_id`, `created_at`, `updated_at` FROM `message_slots`"
        )
        connection.executeSQL("DROP TABLE `message_slots`")
        connection.executeSQL("ALTER TABLE `message_slots_new` RENAME TO `message_slots`")
        connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_message_slots_chat_variant_id` ON `message_slots` (`chat_variant_id`)")
        connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_message_slots_created_at` ON `message_slots` (`created_at`)")

        // message_containers
        connection.executeSQL(
            "CREATE TABLE IF NOT EXISTS `message_containers_new` (`id` TEXT NOT NULL, `slot_id` TEXT NOT NULL, `role` TEXT NOT NULL, `rarity` TEXT NOT NULL, `status` TEXT NOT NULL, `sync_action` TEXT NOT NULL, `user_reaction` TEXT, `character_reaction` TEXT, `is_read` INTEGER NOT NULL, `show_outreach_feedback` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `updated_at` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`slot_id`) REFERENCES `message_slots`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        connection.executeSQL(
            "INSERT INTO `message_containers_new` (`id`, `slot_id`, `role`, `rarity`, `status`, `sync_action`, `user_reaction`, `character_reaction`, `is_read`, `show_outreach_feedback`, `created_at`, `updated_at`) SELECT `id`, `slot_id`, `role`, `rarity`, `status`, `sync_action`, `user_reaction`, `character_reaction`, `is_read`, `show_outreach_feedback`, `created_at`, `updated_at` FROM `message_containers`"
        )
        connection.executeSQL("DROP TABLE `message_containers`")
        connection.executeSQL("ALTER TABLE `message_containers_new` RENAME TO `message_containers`")
        connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_message_containers_slot_id` ON `message_containers` (`slot_id`)")
        connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_message_containers_status` ON `message_containers` (`status`)")
        connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_message_containers_sync_action` ON `message_containers` (`sync_action`)")

        // message_contents
        connection.executeSQL(
            "CREATE TABLE IF NOT EXISTS `message_contents_new` (`id` TEXT NOT NULL, `container_id` TEXT NOT NULL, `avalon_message_content_type` TEXT NOT NULL, `content` TEXT NOT NULL, `order_index` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`container_id`) REFERENCES `message_containers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        connection.executeSQL(
            "INSERT INTO `message_contents_new` (`id`, `container_id`, `avalon_message_content_type`, `content`, `order_index`) SELECT `id`, `container_id`, `avalon_message_content_type`, `content`, `order_index` FROM `message_contents`"
        )
        connection.executeSQL("DROP TABLE `message_contents`")
        connection.executeSQL("ALTER TABLE `message_contents_new` RENAME TO `message_contents`")
        connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_message_contents_container_id` ON `message_contents` (`container_id`)")
    }
}

val MIGRATION_34_35 = object : Migration(34, 35) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL(
            "ALTER TABLE `show_promo_flags_entity` ADD COLUMN `openApiTransitionAnnouncement` INTEGER NOT NULL DEFAULT 1"
        )
    }
}

val MIGRATION_35_36 = object : Migration(35, 36) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL("ALTER TABLE `character_entity` ADD COLUMN `scenario` TEXT")
        connection.executeSQL("ALTER TABLE `character_entity` ADD COLUMN `mes_example` TEXT")
    }
}