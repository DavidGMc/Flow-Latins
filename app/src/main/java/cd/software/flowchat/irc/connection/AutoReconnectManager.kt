package cd.software.flowchat.irc.connection

import android.util.Log
import cd.software.flowchat.model.IRCConnectionInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Gestiona la reconexión automática al servidor IRC con backoff exponencial.
 * Intenta reconectar automáticamente cuando se pierde la conexión de forma inesperada.
 */
class AutoReconnectManager(
    private val scope: CoroutineScope,
    private val onReconnect: suspend (IRCConnectionInfo) -> Unit,
    private val isConnected: () -> Boolean
) {
    private var reconnectJob: Job? = null
    private var isReconnecting = false
    private var connectionInfo: IRCConnectionInfo? = null
    private var currentAttempt = 0

    companion object {
        private const val TAG = "AutoReconnectManager"
        
        // Intervalos de reconexión con backoff exponencial (en milisegundos)
        private val RECONNECT_DELAYS = listOf(
            5_000L,      // 5 segundos
            10_000L,     // 10 segundos
            30_000L,     // 30 segundos
            60_000L,     // 1 minuto
            300_000L     // 5 minutos
        )
        
        private const val MAX_ATTEMPTS = 10 // Máximo de intentos antes de rendirse
    }

    /**
     * Guarda la información de conexión para reconexiones futuras.
     */
    fun saveConnectionInfo(info: IRCConnectionInfo) {
        connectionInfo = info
        Log.d(TAG, "Información de conexión guardada")
    }

    /**
     * Inicia el proceso de reconexión automática.
     */
    fun startReconnecting() {
        if (isReconnecting) {
            Log.d(TAG, "Ya hay un proceso de reconexión en curso")
            return
        }

        val info = connectionInfo
        if (info == null) {
            Log.w(TAG, "No hay información de conexión guardada")
            return
        }

        isReconnecting = true
        currentAttempt = 0
        
        reconnectJob = scope.launch {
            Log.d(TAG, "Iniciando proceso de reconexión automática")
            
            while (isActive && isReconnecting && currentAttempt < MAX_ATTEMPTS) {
                // Verificar si ya está conectado
                if (isConnected()) {
                    Log.d(TAG, "Ya está conectado, deteniendo reconexión")
                    stop()
                    return@launch
                }

                currentAttempt++
                val delayIndex = minOf(currentAttempt - 1, RECONNECT_DELAYS.size - 1)
                val delayMs = RECONNECT_DELAYS[delayIndex]
                
                Log.d(TAG, "Intento de reconexión $currentAttempt/$MAX_ATTEMPTS en ${delayMs / 1000}s")
                
                try {
                    delay(delayMs)
                    
                    if (!isActive || !isReconnecting) {
                        break
                    }
                    
                    Log.d(TAG, "Intentando reconectar...")
                    onReconnect(info)
                    
                    // Esperar un momento para verificar si la conexión fue exitosa
                    delay(3000)
                    
                    if (isConnected()) {
                        Log.d(TAG, "Reconexión exitosa")
                        stop()
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error en intento de reconexión: ${e.message}")
                }
            }
            
            if (currentAttempt >= MAX_ATTEMPTS) {
                Log.w(TAG, "Se alcanzó el máximo de intentos de reconexión")
            }
            
            isReconnecting = false
        }
    }

    /**
     * Detiene el proceso de reconexión.
     */
    fun stop() {
        if (!isReconnecting) {
            return
        }

        isReconnecting = false
        reconnectJob?.cancel()
        reconnectJob = null
        currentAttempt = 0
        Log.d(TAG, "Proceso de reconexión detenido")
    }

    /**
     * Reinicia el contador de intentos.
     * Útil cuando se conecta manualmente.
     */
    fun reset() {
        currentAttempt = 0
        Log.d(TAG, "Contador de intentos reiniciado")
    }

    /**
     * Verifica si está en proceso de reconexión.
     */
    fun isReconnecting(): Boolean = isReconnecting
}
