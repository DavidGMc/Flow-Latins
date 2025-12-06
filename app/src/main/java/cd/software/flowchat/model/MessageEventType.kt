package cd.software.flowchat.model

enum class MessageEventType {

    TEXT,           // Mensaje de texto normal
    USER_JOIN,      // Usuario se une al canal
    USER_PART,      // Usuario sale del canal
    USER_QUIT,      // Usuario se desconecta completamente
    USER_KICK,      // Usuario es expulsado
    NICK_CHANGE,    // Usuario cambia de nick
    MODE_CHANGE,    // Cambio de modo de usuario/canal
    SERVER_NOTICE,
    USER_DISCONNECTED,
    USER_BAN,
    USER_UNBAN,
    USER_OP,
    USER_DEOP,
    USER_VOICE,
    USER_DEVOICE,
    USER_HALFOP,
    USER_DEHALFOP,
    NOTICE,
    TOPIC_CHANGE,
    ERROR,
    USER_LIST,
    INVITE,
    WHOIS_RESPONSE,
    UNKNOWN_COMMAND,
    CTCP_REQUEST,
    CHANNEL_ERROR,
    USER_MODE_CHANGE,
    ACTION,
}