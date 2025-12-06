package cd.software.flowchat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import android.app.Notification
import androidx.annotation.RequiresApi
import es.chat.R

class ConnectionService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "connection_status"

        // Actions
        const val ACTION_START_SERVICE = "action_start_service"
        const val ACTION_STOP_SERVICE = "action_stop_service"
        const val ACTION_UPDATE_STATUS = "cd.software.flowchat.ACTION_UPDATE_STATUS"
        const val EXTRA_CONNECTION_STATE = "extra_connection_state"
        const val EXTRA_SYNC_STATE = "extra_sync_state"

        private const val TAG = "ConnectionService"
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    // BroadcastReceiver para recibir actualizaciones de estado desde IRCService
    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_UPDATE_STATUS) {
                val state = intent.getStringExtra(EXTRA_CONNECTION_STATE)
                val syncState = intent.getStringExtra(EXTRA_SYNC_STATE)
                Log.d(TAG, "Received status broadcast: $state, syncState: $syncState")
                updateNotification(state, syncState)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(statusReceiver, IntentFilter(ACTION_UPDATE_STATUS), Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(statusReceiver, IntentFilter(ACTION_UPDATE_STATUS))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        when (intent?.action) {
            ACTION_START_SERVICE -> startForegroundService()
            ACTION_STOP_SERVICE -> stopForegroundService()
            ACTION_UPDATE_STATUS -> {
                val state = intent.getStringExtra(EXTRA_CONNECTION_STATE)
                val syncState = intent.getStringExtra(EXTRA_SYNC_STATE)
                Log.d(TAG, "Updating status: $state, syncState: $syncState")
                updateNotification(state, syncState)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(statusReceiver)
            Log.d(TAG, "Broadcast receiver unregistered")
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)

            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.connection_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    enableLights(true)
                    lightColor = Color.BLUE
                    enableVibration(false)
                    setShowBadge(true)
                    description = getString(R.string.connection_channel_description)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun createBaseNotification(status: String = getString(R.string.state_connecting)): NotificationCompat.Builder {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ConnectionService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(getString(R.string.notification_title_connection))
            .setContentText(status)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$status\n${getString(R.string.notification_content_connection)}"))
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.notification_stop_action),
                stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAllowSystemGeneratedContextualActions(true)
    }

    private fun startForegroundService() {
        val notification = createBaseNotification(getString(R.string.state_connecting))
            .setColorized(true)
            .setColor(Color.rgb(33, 150, 243))
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(state: String?, syncState: String? = null) {
        Log.d(TAG, "Updating notification: state=$state, syncState=$syncState")
        val (text, color, details, emoji) = when (state) {
            "CONNECTING" -> Quadruple(
                getString(R.string.state_connecting),
                Color.rgb(255, 193, 7),
                getSyncDetails("STARTING", syncState),
                "⏳"
            )
            "CONNECTED" -> Quadruple(
                getString(R.string.state_connected),
                Color.rgb(76, 175, 80),
                getSyncDetails("ACTIVE", syncState),
                "✅"
            )
            "DISCONNECTING" -> Quadruple(
                getString(R.string.state_disconnecting),
                Color.rgb(255, 152, 0),
                getSyncDetails("STOPPING", syncState),
                "🔄"
            )
            "DISCONNECTED" -> Quadruple(
                getString(R.string.state_disconnected),
                Color.rgb(244, 67, 54),
                getSyncDetails("DISCONNECTED", syncState),
                "⭕"
            )
            "ERROR" -> Quadruple(
                getString(R.string.state_error),
                Color.rgb(244, 67, 54),
                getSyncDetails("ERROR", syncState),
                "❌"
            )
            else -> Quadruple(
                getString(R.string.state_unknown),
                Color.rgb(158, 158, 158),
                getSyncDetails("UNKNOWN", syncState),
                "❓"
            )
        }

        val notification = createBaseNotification("$emoji $text")
            .setColorized(true)
            .setColor(color)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$emoji $text\n$details")
                    .setBigContentTitle("LatinsIRC - " + when (state) {
                        "CONNECTED" -> "$emoji ${getString(R.string.title_active)}"
                        "CONNECTING" -> "$emoji ${getString(R.string.title_connecting)}"
                        "DISCONNECTED" -> "$emoji ${getString(R.string.title_disconnected)}"
                        "ERROR" -> "$emoji ${getString(R.string.title_error)}"
                        else -> "$emoji ${getString(R.string.title_generic)}"
                    })
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notification.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.setColorized(true)
        }

        notificationManager.notify(NOTIFICATION_ID, notification.build())
        Log.d(TAG, "Notification updated with state: $state")
    }

    private fun getSyncDetails(defaultState: String, syncState: String?): String {
        return when (syncState ?: defaultState) {
            "ACTIVE" -> getString(R.string.sync_active)
            "STARTING" -> getString(R.string.sync_starting)
            "DISCONNECTED" -> getString(R.string.sync_disconnected)
            "ERROR" -> getString(R.string.sync_error)
            else -> getString(R.string.sync_default, syncState ?: defaultState)
        }
    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}