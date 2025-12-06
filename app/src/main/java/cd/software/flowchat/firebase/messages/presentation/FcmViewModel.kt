package cd.software.flowchat.firebase.messages.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cd.software.flowchat.firebase.messages.ManageFcmTokenUseCase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FcmViewModel(
    private val manageFcmTokenUseCase: ManageFcmTokenUseCase
) : ViewModel() {

    fun registerToken() {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                manageFcmTokenUseCase.registerToken(token)
                Log.d("FCM", "Token refreshed: $token")
            } catch (e: Exception) {
                Log.e("FCM", "Failed to register token: ${e.message}")
            }
        }
    }
}