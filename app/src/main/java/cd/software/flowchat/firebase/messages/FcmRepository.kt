package cd.software.flowchat.firebase.messages

interface FcmRepository {
    suspend fun saveToken(userId: String, token: String)
    suspend fun deleteToken(userId: String)
}