package cd.software.flowchat.firebase.model

sealed class ResetPasswordState {
    object Initial : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()


}