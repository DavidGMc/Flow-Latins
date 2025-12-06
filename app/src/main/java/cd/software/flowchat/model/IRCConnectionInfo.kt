package cd.software.flowchat.model

data class IRCConnectionInfo(
    val nickname: String,
    val password: String = "",
    val username: String? = null,
    val serverConfig: IRCServerConfig, // Using our Firebase config model
    val autoJoinChannels: List<String> = listOf("#Chat", "#Colombia")

)
