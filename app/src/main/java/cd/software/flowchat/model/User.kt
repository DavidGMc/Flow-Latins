package cd.software.flowchat.model

data class User(
    val nickname: String,   // Apodo del usuario.
    val username: String?,  // Nombre de usuario (opcional, depende del servidor).
    val host: String?
)
