package cd.software.flowchat


import android.Manifest

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import cd.software.flowchat.admob.AdConsentManager
import cd.software.flowchat.admob.AdMobManager
import cd.software.flowchat.admob.AdViewModel
import cd.software.flowchat.firebase.messages.presentation.FcmViewModel
import cd.software.flowchat.firebase.repository.FirebaseAuthRepository
import cd.software.flowchat.firebase.repository.FirebaseProfileRepository
import cd.software.flowchat.firebase.viewmodel.AuthViewModel
import cd.software.flowchat.model.ConnectionState
import cd.software.flowchat.preferences.AuthDataStore
import cd.software.flowchat.preferences.ChatPreferences
import cd.software.flowchat.preferences.ServerPreferenceManager
import cd.software.flowchat.ui.theme.FlowChatTheme
import cd.software.flowchat.viewmodel.IRCChatViewModel
import cd.software.flowchat.viewmodel.IRCConnectionViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import com.google.firebase.storage.storage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var showBackgroundPermissionDialog by mutableStateOf(false)
    private val ircService by lazy { IRCService(chatPreferences) }
    private val preferenceManager by lazy {
        ServerPreferenceManager(applicationContext)
    }
    private val chatPreferences by lazy {
        ChatPreferences(applicationContext)
    }
    private val adConsentManager by lazy { AdConsentManager(this) }

    private val customServerManager by lazy { CustomServerManager(applicationContext) }
    private val auth by lazy { Firebase.auth }
    private val authDataStore by lazy {
        AuthDataStore(applicationContext)
    }

    // Repositorios
    private val authRepository by lazy {
        FirebaseAuthRepository(auth,Firebase.firestore,authDataStore)
    }
    private val profileRepository by lazy {
        FirebaseProfileRepository(Firebase.firestore,Firebase.storage)
    }
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var fcmViewModel: FcmViewModel

    private val connectionViewModel by lazy {
        IRCConnectionViewModel(ircService, applicationContext,preferenceManager = preferenceManager, customServerManager = customServerManager,  adViewModel = adViewModel)
    }

    private val chatViewModel by lazy {
        IRCChatViewModel(ircService, NotificationService(applicationContext),chatPreferences, context = applicationContext)
    }
    private val authViewModel by lazy {
        AuthViewModel(
            authRepository = authRepository,
            profileRepository = profileRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initializeFirebase()

        // Configurar contexto para IRCService
        ircService.setApplicationContext(applicationContext)
        // Verificar estado de autenticación desde persistencia local
        lifecycleScope.launch {
            val isAuthenticated = authRepository.checkAuthStatus()
            if (isAuthenticated) {
                // El usuario está autenticado según DataStore y Firebase
                Log.d("Auth", "Usuario autenticado desde persistencia local")
                // Aquí podrías inicializar datos del usuario o ejecutar lógica específica
            }
        }

        requestNotificationPermission()
        showBackgroundPermissionDialog = BackgroundServicePermissionDialog.shouldShowDialog(this)
        setupUI(savedInstanceState)
        fcmViewModel.registerToken()

        adConsentManager.initialize(this) { canShowPersonalizedAds, isEuUser ->
            android.util.Log.d("Consentimiento",
                "Resultado: anuncios personalizados = $canShowPersonalizedAds, en EU = $isEuUser")

            // Actualiza el AdViewModel
            adViewModel.setCanShowAds(true) // Siempre permitir algún tipo de anuncios

            // Determinar el estado de consentimiento para AdMobManager
            val consentStatus = when {
                canShowPersonalizedAds -> AdMobManager.ConsentStatus.PERSONALIZED
                isEuUser -> AdMobManager.ConsentStatus.NON_PERSONALIZED // Usuario de EU que rechazó personalización
                else -> AdMobManager.ConsentStatus.PERSONALIZED // Usuario no-EU, no necesita consentimiento
            }

            // Actualiza el AdMobManager con el estado apropiado
            (adViewModel.adManager as AdMobManager).updateConsent(consentStatus, applicationContext)
        }

        // Iniciar observación del estado de sincronización
        observeIRCSync()
        observeAuthState()
    }
    private fun observeAuthState() {
        lifecycleScope.launch {
            authRepository.currentUserIdFlow.collect { userId ->
                if (userId != null) {
                    // El usuario inició sesión o ya estaba autenticado
                    Log.d("Auth", "Usuario autenticado: $userId")
                    // Realizar acciones necesarias cuando el usuario está autenticado
                } else {
                    // El usuario cerró sesión o no está autenticado
                    Log.d("Auth", "Usuario no autenticado")
                    // Realizar acciones necesarias cuando el usuario no está autenticado
                }
            }
        }
    }
    private fun setupUI(savedInstanceState: Bundle?) {
        handleNotificationIntent(intent)

        val requiresReconnect = intent?.getBooleanExtra(NotificationService.EXTRA_REQUIRES_RECONNECT, false) == true
        if (requiresReconnect) {
            safeReconnect()
        }

        setContent {
            FlowChatTheme {
                MainScreen(
                    connectionViewModel = connectionViewModel,
                    chatViewModel = chatViewModel,
                    authViewModel = authViewModel

                )
                // Mostrar diálogo de permiso si es necesario
                if (showBackgroundPermissionDialog) {
                    BackgroundServicePermissionDialog.ShowDialog(
                        onPermissionGranted = {
                            BackgroundServicePermissionDialog.markPermissionGranted(this@MainActivity)
                            showBackgroundPermissionDialog = false
                            startBackgroundService()
                        },
                        onDismiss = {
                            showBackgroundPermissionDialog = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun rememberStartDestination(
        connectionViewModel: IRCConnectionViewModel,
        intent: Intent?
    ): State<String> {
        val startDestination = remember { mutableStateOf(Routes.Connection.route) }
        val connectionState by connectionViewModel.connectionState.collectAsState()
        val destination = remember { intent?.getStringExtra("destination") }

        LaunchedEffect(connectionState, destination) {
            when {
                connectionState is ConnectionState.Connected -> {
                    if (destination != null) {
                        // Si hay una ruta de notificación, usarla
                        startDestination.value = destination
                    } else {
                        // Si no hay ruta de notificación pero estamos conectados, ir al chat
                        startDestination.value = Routes.Chat.route
                    }
                }
                connectionState is ConnectionState.Connecting -> {
                    // No cambiar la ruta mientras se está conectando
                }
                else -> {
                    startDestination.value = Routes.Connection.route
                }
            }
        }
        // Si viene de una notificación, intentar reconectar
        LaunchedEffect(Unit) {
            if (destination != null) {
                safeReconnect()
            }
        }
        return startDestination
    }
    private fun initializeFirebase() {
        // Inicializar Firebase si aún no está inicializado
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
    private fun handleNotificationIntent(intent: Intent?) {
        val messageId = intent?.getStringExtra(NotificationService.EXTRA_MESSAGE_ID)
        val conversationName = intent?.getStringExtra(NotificationService.EXTRA_CONVERSATION_NAME)
        val shouldSwitchTab = intent?.getBooleanExtra(NotificationService.EXTRA_SHOULD_SWITCH_TAB, false) ?: false

        if (messageId != null && conversationName != null) {
            // Cancelar la notificación específica
            chatViewModel.notificationService.cancelNotification(messageId)
            // Cancelar todas las notificaciones de esta conversación
            chatViewModel.notificationService.cancelConversationNotifications(conversationName)

            if (shouldSwitchTab) {
                // El cambio de tab se manejará en ChatScreen
                chatViewModel.conversations.value.indexOfFirst { it.name == conversationName }.let { index ->
                    if (index != -1) {
                        chatViewModel.switchToConversation(index)
                    }
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
        setIntent(intent)
    }
    override fun onDestroy() {
        super.onDestroy()
        // No desconectamos el servicio aquí para mantener la conexión en segundo plano
        // Solo se desconectará cuando el usuario explícitamente presione desconectar
    }
    // Método para iniciar el servicio en primer plano
    private fun startBackgroundService() {
        val intent = Intent(this, ConnectionService::class.java).apply {
            action = ConnectionService.ACTION_START_SERVICE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    // Método para observar el estado de sincronización de IRC
    private fun observeIRCSync() {
        lifecycleScope.launch {
            // Observar el estado de sincronización
            ircService.syncState.collect { syncState ->
                val intent = Intent(this@MainActivity, ConnectionService::class.java).apply {
                    action = ConnectionService.ACTION_UPDATE_STATUS
                    putExtra(ConnectionService.EXTRA_SYNC_STATE, syncState)
                }
                startService(intent)
            }
        }

        lifecycleScope.launch {
            // Observar el estado de conexión
            ircService.connectionState.collect { state ->
                updateServiceStatus(state)
            }
        }
    }

    // Método para actualizar el estado del servicio
    fun updateServiceStatus(connectionState: ConnectionState) {
        val stateString = when(connectionState) {
            is ConnectionState.Connected -> "CONNECTED"
            is ConnectionState.Connecting -> "CONNECTING"
            is ConnectionState.Disconnected -> "DISCONNECTED"
            is ConnectionState.Error -> "ERROR"
            else -> "UNKNOWN"
        }

        Log.d("MainActivity", "Actualizando estado del servicio: $stateString")

        val intent = Intent(this, ConnectionService::class.java).apply {
            action = ConnectionService.ACTION_UPDATE_STATUS
            putExtra(ConnectionService.EXTRA_CONNECTION_STATE, stateString)
            putExtra(ConnectionService.EXTRA_SYNC_STATE, ircService.syncState.value)
        }

        startService(intent)

        // Enviar también como broadcast para asegurar que se reciba
        val broadcastIntent = Intent(ConnectionService.ACTION_UPDATE_STATUS).apply {
            putExtra(ConnectionService.EXTRA_CONNECTION_STATE, stateString)
            putExtra(ConnectionService.EXTRA_SYNC_STATE, ircService.syncState.value)
        }
        sendBroadcast(broadcastIntent)
    }
    private fun safeReconnect() {
        Log.d("MainActivity", "Iniciando reconexión segura...")

        // 1. Primero asegurar que no haya un servicio activo
        val stopIntent = Intent(this, ConnectionService::class.java).apply {
            action = ConnectionService.ACTION_STOP_SERVICE
        }
        startService(stopIntent)

        // 2. Esperar un momento para que el servicio se detenga completamente
        lifecycleScope.launch {
            kotlinx.coroutines.delay(500) // Esperar 500ms

            // 3. Ahora iniciar el servicio nuevamente
            val startIntent = Intent(this@MainActivity, ConnectionService::class.java).apply {
                action = ConnectionService.ACTION_START_SERVICE
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startIntent)
            } else {
                startService(startIntent)
            }

            // 4. Ahora intentar reconectar a través del ViewModel
            connectionViewModel.reconnectIfNeeded()
        }
    }
}