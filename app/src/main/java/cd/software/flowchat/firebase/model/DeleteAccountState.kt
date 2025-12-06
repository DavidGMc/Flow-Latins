package cd.software.flowchat.firebase.model

sealed class DeleteAccountState {
    object Initial : DeleteAccountState()
    object Loading : DeleteAccountState()
    object Success : DeleteAccountState()
    data class Error(val message: String) : DeleteAccountState()
}