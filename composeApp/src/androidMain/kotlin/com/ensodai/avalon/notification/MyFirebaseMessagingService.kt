package com.ensodai.avalon.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ensodai.shared.avalon.rpc.services.AuthRpcService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val authRpcService: AuthRpcService by inject()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            try {
                authRpcService.updatePushToken(token)
            } catch (e: Exception) {
                // Игнорируем ошибки при отправке токена при холодном старте (например, если нет сети или авторизации)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val characterName = data["characterName"] ?: remoteMessage.notification?.title ?: "AI Character"
        val text = data["text"] ?: remoteMessage.notification?.body ?: ""
        val characterId = data["characterId"]
        val sessionId = data["sessionId"]

        if (text.isNotEmpty()) {
            showNotification(characterName, text, characterId, sessionId)
        }
    }

    private fun showNotification(
        characterName: String,
        text: String,
        characterId: String?,
        sessionId: String?
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "messages_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new messages from AI characters"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val activityClass = try {
            Class.forName("com.ensodai.avalon.MainActivity")
        } catch (e: ClassNotFoundException) {
            null
        }

        val intent = if (activityClass != null) {
            Intent(this, activityClass)
        } else {
            packageManager.getLaunchIntentForPackage(packageName)
        }?.apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (characterId != null) putExtra("character_id", characterId)
            if (sessionId != null) putExtra("session_id", sessionId)
        }

        val requestCode = characterId?.hashCode() ?: 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconResId = resources.getIdentifier("ic_launcher", "mipmap", packageName).let {
            if (it != 0) it else android.R.drawable.stat_notify_chat
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(iconResId)
            .setContentTitle(characterName)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationId = characterId?.hashCode() ?: System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
