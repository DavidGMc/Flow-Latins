package cd.software.flowchat.firebase.repository

import android.net.Uri
import cd.software.flowchat.firebase.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,

) : ProfileRepository {

    private val profilesCollection = firestore.collection("profiles")
    private val profileImagesRef = storage.reference.child("profile_images")

    override suspend fun createProfile(profile: UserProfile): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                profilesCollection.document(profile.userId)
                    .set(profile)
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    override suspend fun isUsernameTaken(username: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val querySnapshot = profilesCollection
                    .whereEqualTo("name", username)
                    .limit(1)
                    .get()
                    .await()

                Result.success(!querySnapshot.isEmpty)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateProfile(profile: UserProfile): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                profilesCollection.document(profile.userId)
                    .update(profile.toMap())
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getProfile(userId: String): Result<UserProfile> =
        withContext(Dispatchers.IO) {
            try {
                val document = profilesCollection.document(userId).get().await()
                if (document.exists()) {
                    Result.success(document.toObject<UserProfile>()!!)
                } else {
                    Result.failure(NoSuchElementException("Profile not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun uploadProfileImage(imageUri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val filename = "${UUID.randomUUID()}.jpg"
                val imageRef = profileImagesRef.child(filename)
                val uploadTask = imageRef.putFile(imageUri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await()
                Result.success(downloadUrl.toString())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    override suspend fun getAllProfiles(): Result<List<UserProfile>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = profilesCollection.get().await()
                val profiles = snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(UserProfile::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
                Result.success(profiles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun searchProfiles(query: String): Result<List<UserProfile>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = profilesCollection
                    .orderBy("name")
                    .startAt(query)
                    .endAt(query + '\uf8ff')
                    .get()
                    .await()

                val profiles = snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(UserProfile::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
                Result.success(profiles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getProfileByUserId(userId: String): Result<UserProfile?> =
        withContext(Dispatchers.IO) {
            try {
                val document = profilesCollection.document(userId).get().await()
                val profile = document.toObject(UserProfile::class.java)
                Result.success(profile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    suspend fun findProfileByUsername(username: String): UserProfile? {
        return try {
            // Query Firestore to find a profile where the name matches the username
            val querySnapshot = firestore
                .collection("profiles")
                .whereEqualTo("name", username)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                // Convert the first document to a UserProfile
                querySnapshot.documents.first().toObject(UserProfile::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

}
