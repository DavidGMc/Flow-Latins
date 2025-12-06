package cd.software.flowchat.firebase.model

sealed class ProfileState {
    data object Initial : ProfileState()
    data object Loading : ProfileState()
    data class Success(val profile: UserProfile) : ProfileState()
    data class Error(val message: String) : ProfileState()

}