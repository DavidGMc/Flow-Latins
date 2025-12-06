package cd.software.flowchat.model



// @Serializable
data class IRCServerConfig(
    val name: String,
    val url: String,
    val port: Int,
    val ssl: Boolean,
    val realname: String,
    val charset: String = "UTF-8",
    val autoJoinChannels: List<String> = emptyList(),
    val isCustom: Boolean = false

)
