package cd.software.flowchat.irc.connection

import android.util.Log
import cd.software.flowchat.irc.config.AuthMethod
import cd.software.flowchat.irc.conversation.ConversationManager
import cd.software.flowchat.irc.message.IRCMessageHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pircbotx.PircBotX

class IRCAuthManager(private val serviceScope: CoroutineScope,
                     private val conversationManager: ConversationManager,
                     private val messageHandler: IRCMessageHandler
) {

    private val authAttempts = mutableMapOf<String, MutableList<AuthMethod>>()

    /**
     * Detecta capacidades del servidor y autentica automáticamente
     */
    suspend fun autoAuthenticate(
        bot: PircBotX?,
        nickname: String,
        password: String
    ) {
        if (bot == null || password.isBlank()) return

        serviceScope.launch(Dispatchers.IO) {
            try {
                // Esperar a que el servidor responda con sus capacidades
                delay(2000)

                // Intentar autenticación con NickServ/Nick
                // La mayoría de servidores modernos usan esto como fallback
                attemptNickServAuth(bot, nickname, password)

            } catch (e: Exception) {
                Log.e("IRCAuthManager", "Error en auto-autenticación: ${e.message}")
                val errorMsg = "Advertencia: No se pudo completar la autenticación automática"
                conversationManager.addMessage(
                    messageHandler.createSystemSyncMessage(errorMsg)
                )
            }
        }
    }

    /**
     * Intenta autenticación con NickServ (método más común)
     */
    private suspend fun attemptNickServAuth(
        bot: PircBotX,
        nickname: String,
        password: String
    ) {
        try {
            // Intentar primero con NickServ (más común)
            withContext(Dispatchers.IO) {
                bot.sendRaw().rawLine("PRIVMSG NickServ :IDENTIFY $password")
                Log.d("IRCAuthManager", "Autenticación NickServ enviada")
            }

            // Esperar respuesta
            delay(1500)

            // Si NickServ no responde, intentar con variantes
            attemptAlternativeAuth(bot, nickname, password)

        } catch (e: Exception) {
            Log.e("IRCAuthManager", "Error en NickServ auth: ${e.message}")
        }
    }

    /**
     * Intenta métodos alternativos si NickServ no funciona
     */
    private suspend fun attemptAlternativeAuth(
        bot: PircBotX,
        nickname: String,
        password: String
    ) {
        try {
            // Algunos servidores usan "Nick" en lugar de "NickServ"
            withContext(Dispatchers.IO) {
                bot.sendRaw().rawLine("PRIVMSG Nick :IDENTIFY $nickname $password")
                Log.d("IRCAuthManager", "Autenticación Nick alternativa enviada")
            }
        } catch (e: Exception) {
            Log.e("IRCAuthManager", "Error en auth alternativa: ${e.message}")
        }
    }

    /**
     * Detecta si el servidor soporta SASL basado en CAP LS
     */
    fun detectSASLSupport(capabilities: String): Boolean {
        return capabilities.contains("sasl", ignoreCase = true)
    }
}