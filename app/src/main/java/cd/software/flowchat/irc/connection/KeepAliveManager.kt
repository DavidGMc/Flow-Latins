package cd.software.flowchat.irc.connection

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Gestiona el sistema de keep-alive para mantener la conexión IRC activa.
 * Envía pings periódicos al servidor para evitar desconexiones por inactividad.
 */
class KeepAliveManager(
    private val scope: CoroutineScope,
    private val sendRawLine: (String) -> Unit
) {
    private var keepAliveJob: Job? = null
    private var isRunning = false

    companion object {
        private const val TAG = "KeepAliveManager"
        // Intervalo de ping: 2.5 minutos (150 segundos)
        private const val PING_INTERVAL_MS = 150_000L
        // Timeout para considerar que el servidor no responde
        private const val PING_TIMEOUT_MS = 30_000L
    }

    /**
     * Inicia el sistema de keep-alive.
     * Envía pings periódicos al servidor IRC.
     */
    fun start() {
        if (isRunning) {
            Log.d(TAG, "Keep-alive ya está activo")
            return
        }

        isRunning = true
        keepAliveJob = scope.launch {
            Log.d(TAG, "Keep-alive iniciado - enviando PING cada ${PING_INTERVAL_MS / 1000}s")
            
            while (isActive && isRunning) {
                try {
                    // Enviar PING al servidor
                    val timestamp = System.currentTimeMillis()
                    sendRawLine("PING :keepalive_$timestamp")
                    Log.d(TAG, "PING enviado al servidor")
                    
                    // Esperar antes del próximo ping
                    delay(PING_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "Error enviando PING: ${e.message}")
                    // Continuar intentando incluso si hay un error
                    delay(PING_INTERVAL_MS)
                }
            }
            
            Log.d(TAG, "Keep-alive detenido")
        }
    }

    /**
     * Detiene el sistema de keep-alive.
     */
    fun stop() {
        if (!isRunning) {
            return
        }

        isRunning = false
        keepAliveJob?.cancel()
        keepAliveJob = null
        Log.d(TAG, "Keep-alive cancelado")
    }

    /**
     * Verifica si el keep-alive está activo.
     */
    fun isActive(): Boolean = isRunning && keepAliveJob?.isActive == true
}
