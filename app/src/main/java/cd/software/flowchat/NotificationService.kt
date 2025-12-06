package cd.software.flowchat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.IRCMessage
import es.chat.R
import java.util.Collections

class NotificationService(val context: Context) {
    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "NotificationService"
        private const val PRIVATE_MESSAGE_CHANNEL_ID = "private_messages"
        private const val MENTION_CHANNEL_ID = "mentions"
        const val EXTRA_CONVERSATION_TYPE = "conversation_type"
        const val EXTRA_CONVERSATION_NAME = "conversation_name"
        const val EXTRA_REQUIRES_RECONNECT = "requires_reconnect"
        const val EXTRA_MESSAGE_ID = "message_id"
        const val EXTRA_SHOULD_SWITCH_TAB = "should_switch_tab"
        private const val CHANNEL_ID = "messages"

        private val activeNotifications = Collections.synchronizedSet(mutableSetOf<String>())
        private var currentActiveConversation: String? = null
        private val lastVisitedTimestamps = Collections.synchronizedMap(mutableMapOf<String, Long>())
    }

    private val notifiedMessageIds = Collections.synchronizedSet(mutableSetOf<String>())

    init {
        createNotificationChannels()
    }

    fun setActiveConversation(conversationName: String?) {
        currentActiveConversation = conversationName
        if (conversationName != null) {
            lastVisitedTimestamps[conversationName] = System.currentTimeMillis()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    PRIVATE_MESSAGE_CHANNEL_ID,
                    context.getString(R.string.notification_channel_private_messages),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableLights(true)
                    enableVibration(true)
                    setShowBadge(true)
                },
                NotificationChannel(
                    MENTION_CHANNEL_ID,
                    context.getString(R.string.notification_channel_mentions),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableLights(true)
                    enableVibration(true)
                    setShowBadge(true)
                }
            )

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            channels.forEach { channel ->
                notificationManager?.createNotificationChannel(channel)
            }
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestNotificationPermission(activity: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                ActivityCompat.requestPermissions(
                    activity as android.app.Activity,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    fun showPrivateMessageNotification(message: IRCMessage, conversation: Conversation) {
        val lastVisit = lastVisitedTimestamps[conversation.name] ?: 0L

        if (message.isOwnMessage ||
            conversation.name == currentActiveConversation ||
            message.timestamp <= lastVisit) {
            return
        }

        val messageId = "${message.timestamp}_${message.sender}_${message.content.hashCode()}"

        if (messageId in notifiedMessageIds) return
        notifiedMessageIds.add(messageId)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_REQUIRES_RECONNECT, true)
            putExtra(EXTRA_CONVERSATION_TYPE, ConversationType.PRIVATE_MESSAGE.name)
            putExtra(EXTRA_CONVERSATION_NAME, conversation.name)
            putExtra(EXTRA_MESSAGE_ID, messageId)
            putExtra(EXTRA_SHOULD_SWITCH_TAB, true)
            putExtra("destination", Routes.PrivateChat.createRoute(conversation.name))
        }

        showNotification(
            channelId = PRIVATE_MESSAGE_CHANNEL_ID,
            title = context.getString(R.string.notification_private_message_title, message.sender),
            text = message.content,
            intent = intent,
            notificationId = conversation.name.hashCode(),
            messageId = messageId
        )
    }

    fun showMentionNotification(message: IRCMessage, conversation: Conversation) {
        if (message.isOwnMessage) return
        val messageId = "${message.timestamp}_${message.sender}_${message.content.hashCode()}"

        if (messageId in notifiedMessageIds) return
        notifiedMessageIds.add(messageId)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_REQUIRES_RECONNECT, true)
            putExtra(EXTRA_CONVERSATION_TYPE, ConversationType.CHANNEL.name)
            putExtra(EXTRA_CONVERSATION_NAME, conversation.name)
            putExtra(EXTRA_MESSAGE_ID, messageId)
            putExtra(EXTRA_SHOULD_SWITCH_TAB, true)
            putExtra("destination", Routes.ChannelChat.createRoute(conversation.name))
        }

        showNotification(
            channelId = MENTION_CHANNEL_ID,
            title = context.getString(R.string.notification_mention_title, conversation.name),
            text = "${message.sender}: ${message.content}",
            intent = intent,
            notificationId = conversation.name.hashCode(),
            messageId = messageId
        )
    }

    private fun showNotification(
        channelId: String,
        title: String,
        text: String,
        intent: Intent,
        notificationId: Int,
        messageId: String,
        style: NotificationCompat.Style? = null
    ) {
        if (!hasNotificationPermission()) return
        activeNotifications.add(messageId)

        val deleteIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            putExtra(EXTRA_MESSAGE_ID, messageId)
        }
        val deletePendingIntent = PendingIntent.getBroadcast(
            context,
            messageId.hashCode(),
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(deletePendingIntent)

        style?.let { builder.setStyle(it) }
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.e(TAG, context.getString(R.string.notification_permission_error), e)
        }
    }

    fun cancelNotification(messageId: String) {
        activeNotifications.remove(messageId)
        NotificationManagerCompat.from(context).cancel(messageId.hashCode())
    }

    fun cancelConversationNotifications(conversationName: String) {
        NotificationManagerCompat.from(context).cancel(conversationName.hashCode())
        activeNotifications.removeIf { it.contains(conversationName) }
    }
}