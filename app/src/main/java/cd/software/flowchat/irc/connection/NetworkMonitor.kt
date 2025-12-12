package cd.software.flowchat.irc.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log

/**
 * Monitorea cambios en la conectividad de red y notifica cuando cambia
 * entre WiFi, datos móviles, o se pierde la conexión.
 */
class NetworkMonitor(
    private val context: Context,
    private val onNetworkAvailable: () -> Unit,
    private val onNetworkLost: () -> Unit,
    private val onNetworkChanged: () -> Unit
) {
    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private var isMonitoring = false
    private var currentNetwork: Network? = null
    private var currentNetworkType: NetworkType = NetworkType.NONE

    companion object {
        private const val TAG = "NetworkMonitor"
    }

    private enum class NetworkType {
        NONE,
        WIFI,
        CELLULAR,
        OTHER
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Red disponible: $network")
            
            val newNetworkType = getNetworkType(network)
            
            // Si no había red antes, notificar disponibilidad
            if (currentNetwork == null) {
                currentNetwork = network
                currentNetworkType = newNetworkType
                onNetworkAvailable()
            } 
            // Si cambió el tipo de red (WiFi <-> Datos móviles)
            else if (newNetworkType != currentNetworkType) {
                Log.d(TAG, "Cambio de red detectado: $currentNetworkType -> $newNetworkType")
                currentNetwork = network
                currentNetworkType = newNetworkType
                onNetworkChanged()
            }
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "Red perdida: $network")
            
            // Solo notificar si era nuestra red actual
            if (network == currentNetwork) {
                currentNetwork = null
                currentNetworkType = NetworkType.NONE
                onNetworkLost()
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val newNetworkType = getNetworkType(network)
            
            // Detectar cambios en el tipo de red
            if (network == currentNetwork && newNetworkType != currentNetworkType) {
                Log.d(TAG, "Capacidades de red cambiadas: $currentNetworkType -> $newNetworkType")
                currentNetworkType = newNetworkType
                onNetworkChanged()
            }
        }
    }

    /**
     * Inicia el monitoreo de cambios de red.
     */
    fun startMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "El monitoreo ya está activo")
            return
        }

        try {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build()

            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            isMonitoring = true
            
            // Obtener red actual
            currentNetwork = connectivityManager.activeNetwork
            currentNetworkType = currentNetwork?.let { getNetworkType(it) } ?: NetworkType.NONE
            
            Log.d(TAG, "Monitoreo de red iniciado - Red actual: $currentNetworkType")
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando monitoreo de red: ${e.message}")
        }
    }

    /**
     * Detiene el monitoreo de cambios de red.
     */
    fun stopMonitoring() {
        if (!isMonitoring) {
            return
        }

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            isMonitoring = false
            currentNetwork = null
            currentNetworkType = NetworkType.NONE
            Log.d(TAG, "Monitoreo de red detenido")
        } catch (e: Exception) {
            Log.e(TAG, "Error deteniendo monitoreo de red: ${e.message}")
        }
    }

    /**
     * Verifica si hay conexión a internet disponible.
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Obtiene el tipo de red actual.
     */
    private fun getNetworkType(network: Network): NetworkType {
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.OTHER
            else -> NetworkType.OTHER
        }
    }

    /**
     * Obtiene información sobre la red actual.
     */
    fun getCurrentNetworkInfo(): String {
        return when (currentNetworkType) {
            NetworkType.WIFI -> "WiFi"
            NetworkType.CELLULAR -> "Datos móviles"
            NetworkType.OTHER -> "Otra red"
            NetworkType.NONE -> "Sin conexión"
        }
    }
}
