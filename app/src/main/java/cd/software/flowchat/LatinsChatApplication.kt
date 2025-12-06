package cd.software.flowchat

import android.app.Application
import cd.software.flowchat.admob.AdManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LatinsChatApplication : Application() {

    @Inject
    lateinit var adManager: AdManager

    override fun onCreate() {
        super.onCreate()

        // Inicializar AdMob
        adManager.initialize(this)
    }
}