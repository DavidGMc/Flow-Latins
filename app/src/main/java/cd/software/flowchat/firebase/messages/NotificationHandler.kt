package cd.software.flowchat.firebase.messages

import com.google.firebase.messaging.RemoteMessage

interface NotificationHandler {
    fun handleRemoteMessage(message: RemoteMessage)
    fun createNotificationChannel()
}