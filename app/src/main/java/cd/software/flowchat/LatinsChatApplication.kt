package cd.software.flowchat

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cd.software.flowchat.admob.AdManager
import cd.software.flowchat.workers.ServiceHealthWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class LatinsChatApplication : Application() {

    @Inject
    lateinit var adManager: AdManager

    override fun onCreate() {
        super.onCreate()

        // Inicializar AdMob
        adManager.initialize(this)
        
        // Programar verificación periódica del servicio
        scheduleServiceHealthCheck()
    }
    
    private fun scheduleServiceHealthCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val healthCheckRequest = PeriodicWorkRequestBuilder<ServiceHealthWorker>(
            15, TimeUnit.MINUTES // Verificar cada 15 minutos
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ServiceHealthWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Mantener el trabajo existente
            healthCheckRequest
        )
    }
}