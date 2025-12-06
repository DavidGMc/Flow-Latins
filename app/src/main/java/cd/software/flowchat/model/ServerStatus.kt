package cd.software.flowchat.model

enum class ServerStatus {
    CONNECTED,    // Conectado al servidor.
    CONNECTING,   // Intentando conectar.
    DISCONNECTED, // Desconectado del servidor.
    ERROR
}