package cd.software.flowchat.firebase.model

// AuthState.kt
sealed class AuthUIState {
    object Initial : AuthUIState()
    object Loading : AuthUIState()
    data class Authenticated(val profile: UserProfile?) : AuthUIState()
    object Unauthenticated : AuthUIState()
    data class Error(val message: String) : AuthUIState()
}