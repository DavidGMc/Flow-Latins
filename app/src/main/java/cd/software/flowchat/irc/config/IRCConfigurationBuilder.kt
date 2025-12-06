package cd.software.flowchat.irc.config

import cd.software.flowchat.irc.events.IRCEventHandler
import cd.software.flowchat.model.IRCConnectionInfo
import java.nio.charset.Charset
import org.pircbotx.Configuration
import org.pircbotx.UtilSSLSocketFactory

/**
 * Construye la configuración de PircBotX para la conexión IRC. Responsable de configurar SSL,
 * charset, autenticación y auto-join de canales.
 */
class IRCConfigurationBuilder(private val eventHandler: IRCEventHandler) {

    /** Construye la configuración completa de PircBotX. */
    fun buildConfiguration(connectionInfo: IRCConnectionInfo): Configuration {
        return Configuration.Builder()
                .setName(connectionInfo.nickname)
                .setLogin(connectionInfo.username ?: connectionInfo.nickname)
                .setRealName(connectionInfo.serverConfig.realname)
                .addServer(connectionInfo.serverConfig.url, connectionInfo.serverConfig.port)
                .apply {
                    configureAutoJoin(this, connectionInfo.autoJoinChannels)
                    configureSSL(this, connectionInfo.serverConfig.ssl)
                    configureCharset(this, connectionInfo.serverConfig.charset)
                    configureAuthentication(this, connectionInfo)
                    setAutoReconnect(false)
                }
                .setAutoReconnect(true)
                .addListener(eventHandler)
                .buildConfiguration()
    }

    /** Configura SSL si está habilitado. */
    private fun configureSSL(builder: Configuration.Builder, ssl: Boolean) {
        if (ssl) {
            builder.setSocketFactory(UtilSSLSocketFactory().trustAllCertificates())
        }
    }

    /** Configura el charset de la conexión. */
    private fun configureCharset(builder: Configuration.Builder, charset: String) {
        try {
            builder.setEncoding(Charset.forName(charset))
        } catch (e: Exception) {
            // Fallback a UTF-8 si el charset es inválido
            builder.setEncoding(Charset.forName("UTF-8"))
        }
    }

    /** Configura la autenticación con NickServ. */
    private fun configureAuthentication(
            builder: Configuration.Builder,
            connectionInfo: IRCConnectionInfo
    ) {
        if (connectionInfo.password.isNotBlank()) {
            builder.setNickservPassword(connectionInfo.password)
        }
    }

    /** Configura los canales para auto-join. */
    private fun configureAutoJoin(builder: Configuration.Builder, channels: List<String>) {
        channels.forEach { channel -> builder.addAutoJoinChannel(channel) }
    }
}
