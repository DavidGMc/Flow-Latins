package cd.software.flowchat.firebase.repository

import cd.software.flowchat.firebase.model.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(email: String, password: String): AuthResult
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun resetPassword(email: String): AuthResult
    suspend fun updatePassword(newPassword: String): AuthResult
    suspend fun signOut()
    suspend fun deleteAccount(): Result<Unit>
    fun getCurrentUserId(): String?
    fun isUserSignedIn(): Boolean

    // Nuevos métodos para la persistencia con DataStore
    val currentUserIdFlow: Flow<String?>
    val currentUserEmailFlow: Flow<String?>
    suspend fun getCurrentUserIdFromDataStore(): String?
    suspend fun getCurrentUserEmailFromDataStore(): String?
    suspend fun checkAuthStatus(): Boolean
}