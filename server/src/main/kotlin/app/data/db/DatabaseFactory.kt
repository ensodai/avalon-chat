package app.data.db

import app.data.CategoryFactory
import app.data.repository.CategoriesRepository
import app.data.repository.GlobalSettingsRepository
import app.utils.EnvProvider
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import java.sql.DriverManager

object DatabaseFactory {
    fun init() {
        val hikariConfig = HikariConfig().apply {
            // Читаем из переменных окружения (для Докера) или берем дефолт (для локального запуска)
            val host = EnvProvider.getDbHost() ?: "localhost"
            val port = EnvProvider.getDbPort() ?: "5432"
            val dbName = EnvProvider.getDbName() ?: "avalon_chat"

            jdbcUrl = "jdbc:postgresql://$host:$port/$dbName"
            driverClassName = "org.postgresql.Driver"

            username = EnvProvider.getDbUser() ?: "postgres"
            password = EnvProvider.getDbPassword() ?: "postgres"
            maximumPoolSize = 10
            validate()
        }

        // Это "чистый" DataSource со стандартными настройками (autocommit = true).
        val flywayDataSource = HikariDataSource(hikariConfig)

        // Запускаем миграцию на чистом DataSource
        try {
            val flyway = Flyway.configure()
                .dataSource(flywayDataSource)
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load()
            flyway.repair()
            flyway.migrate() // Используем migrate. Если были ошибки, repair или ручная очистка.
        } catch (e: Exception) {
            println("Flyway миграция упала: ${e.message}")
            throw e
        } finally {
            // Важно: закрываем этот DataSource, он нам больше не нужен.
//            flywayDataSource.close()
        }

        // мы берем тот же конфиг, но переопределяем нужные нам настройки.
        val exposedHikariConfig = HikariConfig().apply {
            jdbcUrl = hikariConfig.jdbcUrl
            driverClassName = hikariConfig.driverClassName
            username = hikariConfig.username
            password = hikariConfig.password
            maximumPoolSize = hikariConfig.maximumPoolSize
            isAutoCommit = false
//            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        val exposedDataSource = HikariDataSource(exposedHikariConfig)

        Database.connect(exposedDataSource)

        DatabaseValidator.validateAllTables()

    }
}


private fun createTrigger(transaction: Transaction) {
    TransactionManager.current().exec("""
        CREATE OR REPLACE FUNCTION set_default_character_image_order_index()
        RETURNS TRIGGER AS $$
        DECLARE
          max_order_index INTEGER;
        BEGIN
          IF NEW.order_index IS NOT NULL THEN
            RETURN NEW;
          END IF;

          SELECT COALESCE(MAX(order_index), 0) INTO max_order_index
          FROM characterimages
          WHERE character_public_id = NEW.character_public_id;

          NEW.order_index := max_order_index + 1;
          RETURN NEW;
        END;
        $$ LANGUAGE plpgsql;

        DROP TRIGGER IF EXISTS character_images_order_index_trigger ON characterimages;

        CREATE TRIGGER character_images_order_index_trigger
        BEFORE INSERT ON characterimages
        FOR EACH ROW
        EXECUTE FUNCTION set_default_character_image_order_index();
    """.trimIndent()) { }
}

private fun createWelcomeCardImagesTrigger(transaction: Transaction) {
    TransactionManager.current().exec("""
        CREATE OR REPLACE FUNCTION set_default_welcome_card_image_order_index()
        RETURNS TRIGGER AS $$
        DECLARE
          max_order_index INTEGER;
        BEGIN
          IF NEW.order_index IS NOT NULL THEN
            RETURN NEW;
          END IF;

          SELECT COALESCE(MAX(order_index), 0) INTO max_order_index
          FROM welcomecardimages
          WHERE character_public_id = NEW.character_public_id;

          NEW.order_index := max_order_index + 1;
          RETURN NEW;
        END;
        $$ LANGUAGE plpgsql;

        DROP TRIGGER IF EXISTS welcome_card_images_order_index_trigger ON welcomecardimages;

        CREATE TRIGGER welcome_card_images_order_index_trigger
        BEFORE INSERT ON welcomecardimages
        FOR EACH ROW
        EXECUTE FUNCTION set_default_welcome_card_image_order_index();
    """.trimIndent()) { }
}

fun createDatabaseIfNotExists() {
    val connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "NewDown1337")
    val statement = connection.createStatement()
    statement.executeUpdate("CREATE DATABASE chatDb")
    statement.close()
    connection.close()
}

suspend fun insertStartData(
    categoriesRepository: CategoriesRepository,
    globalSettingsRepository: GlobalSettingsRepository
) {
    println("Запуск проверки и вставки начальных данных...")

    // --- ПРОВЕРКА и ВСТАВКА КАТЕГОРИЙ ---
    println("Синхронизация категорий...")
    val categories = CategoryFactory.getCategories()

    if (!categories.isEmpty()) {
        categoriesRepository.upsertSelectableCategories(categories)
        println("Категории синхронизированы (${categories.size} шт.).")
    } else {
        println("Нет категорий для синхронизации от CategoryFactory.")
    }

    val totalCount = categoriesRepository.getSelectableCategoriesCount()
    println("Всего категорий в базе: $totalCount")

    // --- ПРОВЕРКА и ВСТАВКА СЕРВЕРНЫХ КАТЕГОРИЙ ---
    if (categoriesRepository.getServerCategoriesCount() == 0L) {
        println("Категории не найдены. Вставляем...")
        val categories = CategoryFactory.getServerCategory()
        if (!categories.isEmpty()) {
            categoriesRepository.replaceServerCategories(categories)
            println("Категории (${categories.size} шт.) вставлены.")
        } else {
            println("Нет категорий для вставки от CategoryFactory.")
        }
    } else {
        println("Категории уже существуют. Вставка пропущена.")
    }

    println("---")

    globalSettingsRepository.ensureSettingsExist()

    println("Проверка и вставка начальных данных завершена.")
}


//fun insertMockCharacters(characterRepository: CharacterRepository) {
//    val mockOwner = UUID.randomUUID()
//    val mockCharacters = SystemCharactersFactory.getMockCharacters(100, "helper", mockOwner)
//    mockCharacters.forEach {
//        characterRepository.saveCharacter(
//            it,
//            mockOwner,
//            null,
//            null,
//            null
//        )
//    }
//}

//fun addNickNameMigration(transaction: Transaction) {
//    // Добавляем новую колонку с проверкой на существование
//    transaction.exec("""
//        DO $$
//        BEGIN
//            IF NOT EXISTS (
//                SELECT 1
//                FROM information_schema.columns
//                WHERE table_name = 'users' AND column_name = 'nick_name'
//            ) THEN
//                ALTER TABLE users ADD COLUMN nick_name VARCHAR(50);
//            END IF;
//        END $$;
//    """)
//
//    transaction.exec("""
//        UPDATE users
//        SET nick_name = LEFT('user_' || id::text, 20)
//        WHERE nick_name IS NULL
//    """)
//}