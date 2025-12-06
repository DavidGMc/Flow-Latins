package cd.software.flowchat.firebase.repository

import android.net.Uri
import cd.software.flowchat.firebase.model.UserProfile

interface ProfileRepository {
    suspend fun createProfile(profile: UserProfile): Result<Unit>
    suspend fun updateProfile(profile: UserProfile): Result<Unit>
    suspend fun getProfile(userId: String): Result<UserProfile>
    suspend fun uploadProfileImage(imageUri: Uri): Result<String>

    suspend fun getAllProfiles(): Result<List<UserProfile>>
    suspend fun searchProfiles(query: String): Result<List<UserProfile>>
    suspend fun getProfileByUserId(userId: String): Result<UserProfile?>
    // Nuevo método para verificar si un nombre de usuario ya existe
    suspend fun isUsernameTaken(username: String): Result<Boolean>

}