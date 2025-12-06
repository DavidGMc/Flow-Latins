package cd.software.flowchat.firebase.model

import androidx.annotation.Keep

@Keep
data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val age: Int = 0,
    val country: String = "",
    val preferredServer: String = "",
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
){
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "age" to age,
            "country" to country,
            "preferredServer" to preferredServer,
            "profileImageUrl" to profileImageUrl,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}
