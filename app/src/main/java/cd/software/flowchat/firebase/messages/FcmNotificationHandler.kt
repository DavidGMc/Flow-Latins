package cd.software.flowchat.firebase.messages

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import cd.software.flowchat.MainActivity

import com.google.firebase.messaging.RemoteMessage
import es.chat.R

class FcmNotificationHandler(private val context: Context) : NotificationHandler {

    override fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "fcm_default_channel"
            val channelName = "Remote Notifications"
            val channelDescription = "Notifications from Firebase Cloud Messaging"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun handleRemoteMessage(message: RemoteMessage) {
        // Verificamos si hay datos o notificación
        if (message.notification != null || message.data.isNotEmpty()) {
            createNotificationChannel()

            val title = message.notification?.title ?: message.data["title"] ?: "Nueva notificación"
            val body = message.notification?.body ?: message.data["body"]
            ?: "Tienes una nueva notificación"

            // Intent para abrir la app cuando se toca la notificación
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                // Si necesitas pasar datos específicos, los añadirías aquí
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(context, "fcm_default_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            // ID único para cada notificación
            val notificationId = System.currentTimeMillis().toInt()

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }
}