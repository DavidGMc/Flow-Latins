package cd.software.flowchat.commands

import cd.software.flowchat.IRCService
import org.pircbotx.Channel
import org.pircbotx.User

/**
 * Utilidad para validar permisos de usuarios en canales IRC.
 * Verifica si un usuario tiene los permisos necesarios para ejecutar comandos de moderación.
 */
object PermissionValidator {

    /**
     * Verifica si el usuario actual tiene permisos de operador en el canal especificado.
     * 
     * @param service El servicio IRC
     * @param channelName El nombre del canal a verificar
     * @return true si el usuario tiene permisos de operador, false en caso contrario
     */
    fun hasOpPermission(service: IRCService, channelName: String): Boolean {
        return try {
            val bot = service.bot ?: return false
            val channel = bot.userChannelDao.getChannel(channelName) ?: return false
            val user = bot.userChannelDao.getUser(bot.nick) ?: return false
            
            // Verificar si el usuario tiene modo +o (operador)
            channel.isOp(user)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica si el usuario actual tiene permisos de half-op en el canal especificado.
     * 
     * @param service El servicio IRC
     * @param channelName El nombre del canal a verificar
     * @return true si el usuario tiene permisos de half-op, false en caso contrario
     */
    fun hasHalfOpPermission(service: IRCService, channelName: String): Boolean {
        return try {
            val bot = service.bot ?: return false
            val channel = bot.userChannelDao.getChannel(channelName) ?: return false
            val user = bot.userChannelDao.getUser(bot.nick) ?: return false
            
            // Verificar si el usuario tiene modo +h (half-op)
            channel.isHalfOp(user)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica si el usuario actual tiene permisos de voz en el canal especificado.
     * 
     * @param service El servicio IRC
     * @param channelName El nombre del canal a verificar
     * @return true si el usuario tiene voz, false en caso contrario
     */
    fun hasVoicePermission(service: IRCService, channelName: String): Boolean {
        return try {
            val bot = service.bot ?: return false
            val channel = bot.userChannelDao.getChannel(channelName) ?: return false
            val user = bot.userChannelDao.getUser(bot.nick) ?: return false
            
            // Verificar si el usuario tiene modo +v (voice)
            channel.hasVoice(user)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica si el usuario actual tiene algún nivel de permisos en el canal.
     * Retorna true si tiene op, half-op o voice.
     * 
     * @param service El servicio IRC
     * @param channelName El nombre del canal a verificar
     * @return true si el usuario tiene algún permiso, false en caso contrario
     */
    fun hasAnyPermission(service: IRCService, channelName: String): Boolean {
        return hasOpPermission(service, channelName) || 
               hasHalfOpPermission(service, channelName) || 
               hasVoicePermission(service, channelName)
    }

    /**
     * Verifica si el usuario actual tiene permisos de operador o half-op.
     * Útil para comandos que requieren permisos de moderación.
     * 
     * @param service El servicio IRC
     * @param channelName El nombre del canal a verificar
     * @return true si el usuario tiene op o half-op, false en caso contrario
     */
    fun hasModeratorPermission(service: IRCService, channelName: String): Boolean {
        return hasOpPermission(service, channelName) || hasHalfOpPermission(service, channelName)
    }

    /**
     * Obtiene una descripción de los permisos del usuario en el canal.
     * 
     * @param service El servicio IRC
     * @param channelName El nombre del canal
     * @return String describiendo los permisos del usuario
     */
    fun getPermissionDescription(service: IRCService, channelName: String): String {
        return when {
            hasOpPermission(service, channelName) -> "Operator (@)"
            hasHalfOpPermission(service, channelName) -> "Half-Operator (%)"
            hasVoicePermission(service, channelName) -> "Voice (+)"
            else -> "No special permissions"
        }
    }
}
