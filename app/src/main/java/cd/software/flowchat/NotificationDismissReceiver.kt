package cd.software.flowchat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class NotificationDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getStringExtra(NotificationService.EXTRA_MESSAGE_ID)
        val notificationManager = NotificationManagerCompat.from(context)

        messageId?.let { id ->
            // En lugar de usar App, simplemente cancelamos la notificación
            notificationManager.cancel(id.hashCode())
        }
    }
}