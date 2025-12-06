package cd.software.flowchat.firebase.messages.data

import android.os.Build
import android.util.Log
import cd.software.flowchat.firebase.messages.FcmRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirebaseFcmRepository(private val firestore: FirebaseFirestore) : FcmRepository {

    override suspend fun saveToken(userId: String, token: String) {
        try {
            // Crear un ID único para este registro token-usuario
            val tokenDocId = "$userId-${token.takeLast(10)}"

            // Datos a guardar
            val tokenData = hashMapOf(
                "userId" to userId,
                "token" to token,
                "createdAt" to FieldValue.serverTimestamp(),
                "deviceInfo" to getDeviceInfo() // Función auxiliar para obtener información del dispositivo
            )

            // Guardar en una colección dedicada para tokens FCM
            firestore.collection("fcm_tokens")
                .document(tokenDocId)
                .set(tokenData)
                .await()

            Log.d("FCM", "Token guardado exitosamente para usuario $userId")
        } catch (e: Exception) {
            Log.e("FCM", "Error al guardar token: ${e.message}")
        }
    }

    override suspend fun deleteToken(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            val tokenDocId = "$userId-${token.takeLast(10)}"

            // Eliminar el documento de token específico
            firestore.collection("fcm_tokens")
                .document(tokenDocId)
                .delete()
                .await()

            Log.d("FCM", "Token eliminado exitosamente para usuario $userId")
        } catch (e: Exception) {
            Log.e("FCM", "Error al eliminar token: ${e.message}")
        }
    }

    // Función auxiliar para obtener información básica del dispositivo
    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "model" to Build.MODEL,
            "manufacturer" to Build.MANUFACTURER,
            "androidVersion" to Build.VERSION.RELEASE,
            "lastUpdated" to System.currentTimeMillis().toString()
        )
    }
}

