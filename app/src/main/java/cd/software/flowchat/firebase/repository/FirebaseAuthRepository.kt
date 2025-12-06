package cd.software.flowchat.firebase.repository

import cd.software.flowchat.firebase.model.AuthResult
import cd.software.flowchat.firebase.model.UserProfile
import cd.software.flowchat.preferences.AuthDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val authDataStore: AuthDataStore
) : AuthRepository {

    // Implementación de los nuevos flows desde DataStore
    override val currentUserIdFlow: Flow<String?> = authDataStore.userIdFlow
    override val currentUserEmailFlow: Flow<String?> = authDataStore.emailFlow

    override suspend fun getCurrentUserIdFromDataStore(): String? {
        return authDataStore.userIdFlow.first()
    }

    override suspend fun getCurrentUserEmailFromDataStore(): String? {
        return authDataStore.emailFlow.first()
    }

    override suspend fun signUp(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid

                // Guardar datos en DataStore si el registro es exitoso
                if (userId != null) {
                    authDataStore.saveUserId(userId)
                    authDataStore.saveEmail(email)
                }

                AuthResult(success = true, userId = userId)
            } catch (e: Exception) {
                AuthResult(success = false, error = e.localizedMessage)
            }
        }

    override suspend fun signIn(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid

                // Guardar datos en DataStore si el inicio de sesión es exitoso
                if (userId != null) {
                    authDataStore.saveUserId(userId)
                    authDataStore.saveEmail(email)
                }

                AuthResult(success = true, userId = userId)
            } catch (e: Exception) {
                AuthResult(success = false, error = e.localizedMessage)
            }
        }

    override suspend fun resetPassword(email: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                auth.sendPasswordResetEmail(email).await()
                AuthResult(success = true)
            } catch (e: Exception) {
                AuthResult(success = false, error = e.localizedMessage)
            }
        }

    override suspend fun updatePassword(newPassword: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                auth.currentUser?.updatePassword(newPassword)?.await()
                AuthResult(success = true)
            } catch (e: Exception) {
                AuthResult(success = false, error = e.localizedMessage)
            }
        }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun isUserSignedIn(): Boolean = auth.currentUser != null

    override suspend fun signOut() {
        auth.signOut()
        // Limpiar datos en DataStore
        authDataStore.clearAuthData()
    }

    override suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Get current user
            val user = auth.currentUser
            if (user == null) {
                return@withContext Result.failure(Exception("No user is currently signed in"))
            }

            // Delete user document from Firestore first
            try {
                firestore.collection("profiles")
                    .document(user.uid)
                    .delete()
                    .await()
            } catch (e: FirebaseFirestoreException) {
                return@withContext Result.failure(Exception("Failed to delete user data: ${e.message}"))
            }

            // Then delete the authentication account
            user.delete().await()

            // Clear any local auth state
            auth.signOut()
            // Limpiar datos en DataStore
            authDataStore.clearAuthData()

            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            // Handle specific Firebase Auth errors
            val errorMessage = when (e) {
                is FirebaseAuthRecentLoginRequiredException -> {
                    "For security reasons, please sign in again before deleting your account"
                }
                is FirebaseAuthInvalidUserException -> {
                    "User account not found or has already been deleted"
                }
                else -> e.localizedMessage ?: "Failed to delete account"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar si hay un usuario autenticado al iniciar la app
    override suspend fun checkAuthStatus(): Boolean {
        // Verificar tanto en Firebase como en DataStore
        val firebaseUser = auth.currentUser
        val storedUserId = getCurrentUserIdFromDataStore()

        return firebaseUser != null && storedUserId != null
    }
}