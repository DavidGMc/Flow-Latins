package cd.software.flowchat.workers

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cd.software.flowchat.ConnectionService

/**
 * Worker que verifica periódicamente la salud del servicio de conexión IRC.
 * Si el servicio debería estar activo pero no lo está, lo reinicia.
 */
class ServiceHealthWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ServiceHealthWorker"
        const val WORK_NAME = "service_health_check"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Verificando salud del servicio IRC")

        try {
            // Verificar si el servicio debería estar activo
            // Esto se determina verificando si hay información de conexión guardada
            val shouldBeActive = checkIfServiceShouldBeActive()

            if (shouldBeActive) {
                // Verificar si el servicio está realmente activo
                val isServiceRunning = isConnectionServiceRunning()

                if (!isServiceRunning) {
                    Log.w(TAG, "Servicio debería estar activo pero no lo está - reiniciando")
                    restartConnectionService()
                } else {
                    Log.d(TAG, "Servicio está activo y funcionando correctamente")
                }
            } else {
                Log.d(TAG, "Servicio no debería estar activo")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error verificando salud del servicio: ${e.message}")
            return Result.retry()
        }
    }

    /**
     * Verifica si el servicio debería estar activo.
     * Esto se determina verificando las preferencias de conexión.
     */
    private fun checkIfServiceShouldBeActive(): Boolean {
        val prefs = applicationContext.getSharedPreferences("connection_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("was_connected", false)
    }

    /**
     * Verifica si el ConnectionService está ejecutándose.
     */
    private fun isConnectionServiceRunning(): Boolean {
        // En Android, no hay una forma directa de verificar si un servicio está corriendo
        // desde WorkManager. Una alternativa es verificar si hay una notificación activa
        // o usar un archivo de flag.
        
        // Por ahora, asumimos que si el worker se ejecuta y el servicio debería estar activo,
        // intentamos reiniciarlo de forma segura (el servicio ignorará el intent si ya está activo)
        return false // Siempre intentar reiniciar para asegurar que esté activo
    }

    /**
     * Reinicia el servicio de conexión.
     */
    private fun restartConnectionService() {
        try {
            val intent = Intent(applicationContext, ConnectionService::class.java).apply {
                action = ConnectionService.ACTION_START_SERVICE
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(intent)
            } else {
                applicationContext.startService(intent)
            }

            Log.d(TAG, "Servicio de conexión reiniciado")
        } catch (e: Exception) {
            Log.e(TAG, "Error reiniciando servicio: ${e.message}")
        }
    }
}
