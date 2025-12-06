package cd.software.flowchat.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cd.software.flowchat.ConnectionService
import cd.software.flowchat.CustomServerManager
import cd.software.flowchat.IRCService
import cd.software.flowchat.NickValidator
import cd.software.flowchat.admob.AdViewModel
import cd.software.flowchat.model.Channel
import cd.software.flowchat.model.ConnectionState
import cd.software.flowchat.model.FirebaseRemoteConfigHelper
import cd.software.flowchat.model.IRCConnectionInfo
import cd.software.flowchat.model.IRCServerConfig
import cd.software.flowchat.model.ServerConnectionState
import cd.software.flowchat.preferences.ServerPreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException

class IRCConnectionViewModel( private val ircService: IRCService,
                              private val context: Context,
                              private val preferenceManager: ServerPreferenceManager,
                              val customServerManager: CustomServerManager,
                              val adViewModel: AdViewModel
): ViewModel() {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _serverState = MutableStateFlow(ServerConnectionState())
    val serverState: StateFlow<ServerConnectionState> = _serverState.asStateFlow()

    private val _serverConfigs = MutableStateFlow<List<IRCServerConfig>>(emptyList())
    val serverConfigs: StateFlow<List<IRCServerConfig>> = _serverConfigs.asStateFlow()

    private val _selectedServerConfig = MutableStateFlow<IRCServerConfig?>(null)
    val selectedServerConfig: StateFlow<IRCServerConfig?> = _selectedServerConfig.asStateFlow()

    // Agregar nuevo estado para el error de nickname
    private val _nicknameError = MutableStateFlow<String?>(null)
    val nicknameError: StateFlow<String?> = _nicknameError.asStateFlow()

    private val _savedNickname = MutableStateFlow("")
    val savedNickname: StateFlow<String> = _savedNickname.asStateFlow()

    private val _savedPassword = MutableStateFlow("")
    val savedPassword: StateFlow<String> = _savedPassword.asStateFlow()

    private val _privacyPolicyAccepted = MutableStateFlow(false)
    val privacyPolicyAccepted: StateFlow<Boolean> = _privacyPolicyAccepted

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    private fun startConnectionService() {
        val intent = Intent(context, ConnectionService::class.java).apply {
            action = ConnectionService.ACTION_START_SERVICE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
    private fun validateNickname(nickname: String): Boolean {
        _nicknameError.value = NickValidator.getInvalidReason(nickname.trim(),context)
        return _nicknameError.value == null
    }

    init {
        viewModelScope.launch {
            loadSavedPreferences()
            _privacyPolicyAccepted.value = preferenceManager.getPrivacyPolicyAcceptance()
        }
    }


    private suspend fun loadSavedPreferences() {
        val prefs = preferenceManager.getConnectionPreferences()
        _savedNickname.value = prefs.nickname
        _savedPassword.value = prefs.password
    }


    private fun updateConnectionStatus(state: ConnectionState) {
        val statusString = when (state) {
            is ConnectionState.Connected -> "CONNECTED"
            is ConnectionState.Connecting -> "CONNECTING"
            is ConnectionState.Disconnected -> "DISCONNECTED"
            is ConnectionState.Disconnecting->"DISCONNECTING"
            is ConnectionState.Error -> "ERROR"
        }

        val intent = Intent(context, ConnectionService::class.java).apply {
            action = ConnectionService.ACTION_UPDATE_STATUS
            putExtra(ConnectionService.EXTRA_CONNECTION_STATE, statusString)
        }
        context.startService(intent)
    }

    private fun stopConnectionService() {
        val intent = Intent(context, ConnectionService::class.java).apply {
            action = ConnectionService.ACTION_STOP_SERVICE
        }
        context.startService(intent)
    }


    private var lastConnectionInfo: IRCConnectionInfo? = null
    init {
        viewModelScope.launch {
            ircService.connectionState.collect { state ->
                Log.d("ViewModel", "Nuevo estado: $state")
                _connectionState.value = state
                updateConnectionStatus(state)  // Actualizar la notificación cuando cambie el estado

                if (state is ConnectionState.Disconnected) {
                    _serverState.value = ServerConnectionState()
                }
            }
        }
    }

    fun updateServerInfo(
        server: String = _serverState.value.server,
        port: Int = _serverState.value.port,
        nickname: String = _serverState.value.nickname,
        password: String = _serverState.value.password,
        useSSL: Boolean = _serverState.value.useSSL
    ) {
        _serverState.value = ServerConnectionState(
            server = server,
            port = port,
            nickname = nickname,
            password = password,
            useSSL = useSSL
        )
    }


    fun loadServerConfigs() {
        viewModelScope.launch {
            try {
                // Load remote servers from Firebase
                val remoteServers = parseServerConfigs(
                    FirebaseRemoteConfigHelper.getString("latins_irc_server")
                )

                // Load custom servers
                val customServers = customServerManager.getCustomServers().map {
                    // Explicitly mark local servers as custom
                    it.copy(isCustom = true)
                }

                // Combine lists
                _serverConfigs.value = remoteServers + customServers

                // Load saved server
                val savedServer = preferenceManager.getSelectedServer(_serverConfigs.value)
                _selectedServerConfig.value = savedServer
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun selectServer(server: IRCServerConfig) {

        _selectedServerConfig.value = server
        // Guardar la preferencia del servidor cuando se selecciona
        viewModelScope.launch {
            preferenceManager.saveConnectionPreferences(
                serverUrl = server.url,
                nickname = _savedNickname.value,
                password = _savedPassword.value
            )
        }
    }
    fun deleteCustomServer(server: IRCServerConfig) {
        viewModelScope.launch {
            customServerManager.deleteCustomServer(server)
            loadServerConfigs()
        }
    }

    fun getServerByUrl(url: String?): IRCServerConfig? {
        return serverConfigs.value.find { it.url == url }
    }
    private fun parseServerConfigs(jsonString: String): List<IRCServerConfig> {
        val serverConfigs = mutableListOf<IRCServerConfig>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val serverJson = jsonArray.getJSONObject(i)

                // Parseamos los canales autojoin si existen
                val autoJoinChannels = mutableListOf<String>()
                if (serverJson.has("autoJoinChannels")) {
                    val channelsArray = serverJson.getJSONArray("autoJoinChannels")
                    for (j in 0 until channelsArray.length()) {
                        autoJoinChannels.add(channelsArray.getString(j))
                    }
                }

                serverConfigs.add(
                    IRCServerConfig(
                        name = serverJson.getString("name"),
                        url = serverJson.getString("url"),
                        port = serverJson.getInt("port"),
                        ssl = serverJson.getBoolean("ssl"),
                        realname = serverJson.getString("realname"),
                        charset = serverJson.optString("charset", "UTF-8"),
                        autoJoinChannels = autoJoinChannels
                    )
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return serverConfigs
    }

    fun saveConnectionInfo(nickname: String, password: String = "") {
        _selectedServerConfig.value?.let { serverConfig ->
            lastConnectionInfo = IRCConnectionInfo(
                nickname = nickname.trim(),
                password = password,
                serverConfig = serverConfig,
                autoJoinChannels = serverConfig.autoJoinChannels.ifEmpty {
                    FirebaseRemoteConfigHelper.getStringList("auto_join_channels")
                }
            )

            viewModelScope.launch {
                preferenceManager.saveConnectionPreferences(
                    serverUrl = serverConfig.url,
                    nickname = nickname,
                    password = password
                )
                _savedNickname.value = nickname
                _savedPassword.value = password
            }
        }
    }


    fun connect(nickname: String, password: String = "") {
        if (!validateNickname(nickname)) return

        if (_connectionState.value is ConnectionState.Disconnecting) {
            _connectionError.value = "Please wait for disconnection to complete"
            return
        }

        lastConnectionInfo = _selectedServerConfig.value?.let { serverConfig ->
            IRCConnectionInfo(
                nickname = nickname.trim(),
                password = password,
                serverConfig = serverConfig,
                autoJoinChannels = serverConfig.autoJoinChannels.ifEmpty {
                    FirebaseRemoteConfigHelper.getStringList("auto_join_channels")
                }
            )
        }

        lastConnectionInfo?.let { connectionInfo ->
            startConnectionService()
            viewModelScope.launch {
                try {
                    _connectionState.value = ConnectionState.Connecting
                    _connectionError.value = null // Limpiar errores previos

                    ircService.connect(connectionInfo)
                    _connectionState.value = ConnectionState.Connected

                    preferenceManager.saveConnectionPreferences(
                        connectionInfo.serverConfig.url,
                        connectionInfo.nickname,
                        connectionInfo.password
                    )
                } catch (e: Exception) {
                    _connectionState.value = ConnectionState.Disconnected
                    _connectionError.value = "Failed to connect: ${e.message ?: "Unknown error"}"
                }
            }
        }
    }




    fun reconnectIfNeeded() {
        viewModelScope.launch {
            if (_connectionState.value !is ConnectionState.Connected) {
                lastConnectionInfo?.let { connectionInfo ->
                    _connectionState.value = ConnectionState.Connecting
                    ircService.connect(connectionInfo)
                }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            // Modify the private mutable state flow instead of the public read-only one
            _connectionState.value = ConnectionState.Disconnecting
            updateConnectionStatus(ConnectionState.Disconnecting)

            // Primero detener el servicio para evitar problemas con el canal de notificación
            stopConnectionService()

            // Pequeña pausa para asegurar que el servicio se ha detenido completamente
            delay(300)

            // Luego proceder con la desconexión del IRC
            ircService.disconnect()

            // Otra pequeña pausa para asegurar que todo se ha limpiado
            delay(300)

            // Again, modify the private mutable state flow
            _connectionState.value = ConnectionState.Disconnected
            updateConnectionStatus(ConnectionState.Disconnected)
        }
    }
    fun clearNicknameError() {
        _nicknameError.value = null
    }
    fun clearConnectionError() {
        _connectionError.value = null
    }
    // Add this method to set and save privacy policy acceptance
    fun setPrivacyPolicyAccepted(accepted: Boolean) {
        viewModelScope.launch {
            preferenceManager.savePrivacyPolicyAcceptance(accepted)
            _privacyPolicyAccepted.value = accepted
        }
    }

}
