package cd.software.flowchat.irc.config

import android.util.Log
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
        if (connectionInfo.password.isBlank()) return

        // MÉTODO 1: Server Password (como IRC Revolution)
        // Este es el método más común y compatible
        builder.setServerPassword(connectionInfo.password)


        // MÉTODO 2: NickServ Password (fallback automático)
        // PircBotX lo enviará automáticamente si server password no funciona
        builder.setNickservPassword(connectionInfo.password)

        // MÉTODO 3: Habilitar CAP para SASL si está disponible
        builder.setCapEnabled(true)

        Log.d("IRCConfigurationBuilder", "Autenticación multi-método configurada")
    }

    /** Configura los canales para auto-join. */
    private fun configureAutoJoin(builder: Configuration.Builder, channels: List<String>) {
        channels.forEach { channel -> builder.addAutoJoinChannel(channel) }
    }
}
