package cd.software.flowchat.firebase.model

data class AuthResult(
    val success: Boolean,
    val userId: String? = null,
    val error: String? = null
)
