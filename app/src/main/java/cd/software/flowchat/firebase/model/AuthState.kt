package cd.software.flowchat.firebase.model

sealed class AuthState {
    data object Initial : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val message: String) : AuthState()

}