package cd.software.flowchat.irc.config

enum class AuthMethod {

    SERVER_PASSWORD,    // Password en PASS al conectar (PRIORIDAD)
    NICKSERV_IDENTIFY,  // PRIVMSG NickServ :IDENTIFY
    SASL_PLAIN,         // SASL PLAIN
    NONE
}