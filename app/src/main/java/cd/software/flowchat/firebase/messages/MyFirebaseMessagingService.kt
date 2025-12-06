package cd.software.flowchat.firebase.messages

import cd.software.flowchat.firebase.messages.presentation.FcmEntryPoint
import cd.software.flowchat.firebase.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Usamos propiedades perezosas en lugar de inyección directa
    private val notificationHandler: NotificationHandler by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            FcmEntryPoint::class.java
        ).notificationHandler()
    }

    private val fcmRepository: FcmRepository by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            FcmEntryPoint::class.java
        ).fcmRepository()
    }

    private val authRepository: AuthRepository by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            FcmEntryPoint::class.java
        ).authRepository()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        notificationHandler.handleRemoteMessage(remoteMessage)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                fcmRepository.saveToken(userId, token)
            }
        }
    }
}
