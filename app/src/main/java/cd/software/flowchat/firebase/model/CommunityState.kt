package cd.software.flowchat.firebase.model

data class CommunityState(
    val profiles: List<UserProfile> = emptyList(),
    val filteredProfiles: List<UserProfile> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
