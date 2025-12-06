package cd.software.flowchat.firebase.model

data class UserProfileState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCurrentUser: Boolean = false
)
