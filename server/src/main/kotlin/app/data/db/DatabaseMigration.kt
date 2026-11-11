package app.data.db

import org.flywaydb.core.Flyway
import javax.sql.DataSource

fun migrateDatabase(dataSource: DataSource) {
    val flyway = Flyway.configure()
        .dataSource(dataSource)
        .load()

    try {
        flyway.migrate() // Запускаем миграцию
        println("Database migration successful")
    } catch (e: Exception) {
        println("Database migration failed: ${e.message}")
        throw e
    }
}