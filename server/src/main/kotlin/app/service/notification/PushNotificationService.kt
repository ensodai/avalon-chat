package app.service.notification

import app.data.db.tables.AuthSessions
import app.utils.dbQuery
import app.utils.EnvProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.jdbc.select
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class PushNotificationService {
    private val log = KotlinLogging.logger {}

    init {
        try {
            val serviceAccountJson = EnvProvider.getFirebaseServiceAccountJSON()
            val credentials = GoogleCredentials.fromStream(serviceAccountJson.byteInputStream())
            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                log.info { "FirebaseApp инициализирован успешно с использованием EnvProvider." }
            }
        } catch (e: Exception) {
            log.error(e) { "Ошибка инициализации FirebaseApp" }
        }
    }

    /**
     * Асинхронно отправляет push-уведомление на все активные устройства оффлайн-пользователя.
     */
    suspend fun sendNewMessagePush(
        userId: Uuid,
        characterName: String,
        text: String,
        characterId: Uuid,
        sessionId: Uuid
    ) {
        log.info { "PushNotificationService: Подготовка пуш-уведомления для пользователя $userId от $characterName..." }

        val tokens = dbQuery {
            AuthSessions
                .select(AuthSessions.fcmToken)
                .where { 
                    (AuthSessions.userId eq userId) and 
                    (AuthSessions.revoked eq false) and 
                    (AuthSessions.fcmToken.isNotNull()) 
                }
                .mapNotNull { it[AuthSessions.fcmToken] }
                .toSet()
        }

        if (tokens.isEmpty()) {
            log.info { "PushNotificationService: FCM-токены не найдены для пользователя $userId. Отправка отменена." }
            return
        }

        log.info { "PushNotificationService: Найдено ${tokens.size} активных токенов для пользователя $userId. Отправка..." }

        for (token in tokens) {
            try {
                val message = Message.builder()
                    .setToken(token)
                    .setNotification(
                        Notification.builder()
                            .setTitle(characterName)
                            .setBody(text)
                            .build()
                    )
                    .putData("characterId", characterId.toString())
                    .putData("sessionId", sessionId.toString())
                    .putData("click_action", "OPEN_CHAT")
                    .build()

                val response = FirebaseMessaging.getInstance().sendAsync(message).get()
                log.info { "PushNotificationService: Пуш успешно отправлен. Результат: $response" }
            } catch (e: Exception) {
                log.error(e) { "PushNotificationService: Ошибка отправки пуша на токен ${token.take(15)}..." }
            }
        }
    }
}
