package cd.software.flowchat.model

sealed class ConnectionState {

    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    object Disconnecting : ConnectionState() // Nuevo estado
    data class Error(val message: String) : ConnectionState()
}