package cd.software.flowchat

import ColorSettingsScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cd.software.flowchat.firebase.model.AuthState
import cd.software.flowchat.firebase.ui.CommunityScreen
import cd.software.flowchat.firebase.ui.LoginScreen
import cd.software.flowchat.firebase.ui.ProfileScreen
import cd.software.flowchat.firebase.ui.RegistrationScreen
import cd.software.flowchat.firebase.ui.ResetPasswordScreen
import cd.software.flowchat.firebase.ui.UserDetailsScreen
import cd.software.flowchat.firebase.viewmodel.AuthViewModel
import cd.software.flowchat.model.ConnectionState
import cd.software.flowchat.presentation.ChatScreen
import cd.software.flowchat.viewmodel.IRCChatViewModel
import cd.software.flowchat.viewmodel.IRCConnectionViewModel
import kotlinx.coroutines.flow.update


@Composable
fun FlowChatNavHost(
    navController: NavHostController,
    connectionViewModel: IRCConnectionViewModel,
    chatViewModel: IRCChatViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.Connection.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Pantalla de conexión
        composable(Routes.Connection.route) {
            val connectionState by connectionViewModel.connectionState.collectAsState()
            val authState by authViewModel.authState.collectAsState()

            LaunchedEffect(connectionState) {
                if (connectionState is ConnectionState.Connected) {
                    navController.navigate(Routes.Chat.route) {
                        popUpTo(Routes.Connection.route) { inclusive = true }
                    }
                }
            }

            IRCConnectionScreen(
                viewModel = connectionViewModel,
                adManager = connectionViewModel.adViewModel.adManager,
                onConnected = {
                    navController.navigate(Routes.Chat.route) {
                        popUpTo(Routes.Connection.route) { inclusive = true }
                    }
                },
                onAddServerClick = {
                    navController.navigate(Routes.AddServer.route)
                },
                onEditServer = { server ->
                    navController.navigate(Routes.EditServer.createRoute(server.url))
                }
            )
        }

        // Pantalla principal de chat
        composable(Routes.Chat.route) {
            val authState by authViewModel.authState.collectAsState()

            ChatScreen(
                connectionViewModel = connectionViewModel,
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,  // Pasar el authViewModel
                navController = navController,
                isProfileEnabled = authState is AuthState.Success,
                onShowChannels = {
                    navController.navigate(Routes.Channels.route)
                },
                onShowUsers = {
                    navController.navigate(Routes.Users.route)
                },
                onProfileClick = {
                    if (authState is AuthState.Success) {
                        navController.navigate(Routes.Profile.route)
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                },
                onViewProfile = { username ->
                    if (authState is AuthState.Success) {
                        navController.navigate(Routes.Profile.createRoute(username))
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                },
                adViewModel = connectionViewModel.adViewModel,  // Añadido
                adManager = connectionViewModel.adViewModel.adManager
            )
        }

        // Chat privado
        composable(
            route = Routes.PrivateChat.route,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
            val authState by authViewModel.authState.collectAsState()

            ChatScreen(
                connectionViewModel = connectionViewModel,
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,  // Añadido
                navController = navController,
                initialPrivateChat = username,
                isProfileEnabled = authState is AuthState.Success,
                onShowChannels = {
                    navController.navigate(Routes.Channels.route)
                },
                onShowUsers = {  // Añadido
                    navController.navigate(Routes.Users.route)
                },
                onProfileClick = {  // Añadido
                    if (authState is AuthState.Success) {
                        navController.navigate(Routes.Profile.route)
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                },
                onViewProfile = { profileUsername ->  // Añadido
                    if (authState is AuthState.Success) {
                        navController.navigate(Routes.Profile.createRoute(profileUsername))
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                },
                adViewModel = connectionViewModel.adViewModel,  // Añadido
                adManager = connectionViewModel.adViewModel.adManager
            )
        }

        // Chat de canal
        composable(
            route = Routes.ChannelChat.route,
            arguments = listOf(
                navArgument("channelName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val channelName = backStackEntry.arguments?.getString("channelName")
            val authState by authViewModel.authState.collectAsState()

            ChatScreen(
                connectionViewModel = connectionViewModel,
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,  // Añadido
                navController = navController,
                initialChannel = channelName,
                isProfileEnabled = authState is AuthState.Success,
                onShowChannels = {
                    navController.navigate(Routes.Channels.route)
                },
                onShowUsers = {  // Añadido
                    navController.navigate(Routes.Users.route)
                },
                onProfileClick = {  // Añadido
                    if (authState is AuthState.Success) {
                        navController.navigate(Routes.Profile.route)
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                },
                onViewProfile = { username ->  // Añadido
                    if (authState is AuthState.Success) {
                        navController.navigate(Routes.Profile.createRoute(username))
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                },
                adViewModel = connectionViewModel.adViewModel,  // Añadido
                adManager = connectionViewModel.adViewModel.adManager
            )
        }

        // Lista de canales
        composable(Routes.Channels.route) {
            ChannelsScreen(
                chatViewModel = chatViewModel,
                onChannelJoined = { channelName ->
                    navController.navigate(Routes.ChannelChat.createRoute(channelName)) {
                        popUpTo(Routes.Channels.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Lista de usuarios
        composable(Routes.Users.route) {
            val authState by authViewModel.authState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()

            UsersScreen(
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,
                adViewModel = connectionViewModel.adViewModel,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onBack = {
                    navController.navigateUp()
                    chatViewModel.clearLastClosedPrivateChat()
                },
                onOpenPrivateChat = { username ->
                    // SOLO crear la conversación, NO navegar aquí
                    chatViewModel.startPrivateConversation(username)

                    // Navegar de vuelta y dejar que ChatScreen maneje el cambio
                    navController.navigateUp()

                    // Opcional: Un pequeño delay para asegurar que la conversación se creó
                    // antes de que ChatScreen intente cambiar a ella
                },
                onMention = { username ->
                    chatViewModel.updateMessageInput("$username: ")
                    navController.navigateUp()
                },
                onViewProfile = { username ->
                    if (authState is AuthState.Success) {
                        navController.navigate(Routes.Profile.createRoute(username))
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                }
            )

        }

        // Gestión de servidores
        composable(Routes.AddServer.route) {
            AddServerScreen(
                onServerAdded = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.navigateUp()
                },
                customServerManager = connectionViewModel.customServerManager,
                adViewModel = connectionViewModel.adViewModel
            )
        }

        composable(route = Routes.EditServer.route,
            arguments = listOf(
                navArgument("serverUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val serverUrl = backStackEntry.arguments?.getString("serverUrl")
            val server = connectionViewModel.getServerByUrl(serverUrl)

            AddServerScreen(
                initialServer = server,
                onServerAdded = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.navigateUp()
                },
                customServerManager = connectionViewModel.customServerManager,
                adViewModel = connectionViewModel.adViewModel
            )
        }

        // Autenticación y perfil
        composable(Routes.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.popBackStack()
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.Registration.route)
                },
                onNavigateToResetPassword = { navController.navigate("reset_password") }
            )
        }
        composable("reset_password") {
            ResetPasswordScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Registration.route) {
            RegistrationScreen(
                authViewModel = authViewModel,
                onRegistrationComplete = {
                    navController.popBackStack()
                },
                onNavigateLogin = {
                    navController.navigate(Routes.Login.route) {
                        // Esto asegura que no se pueda volver atrás a la pantalla de registro
                        popUpTo(Routes.Registration.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Profile.route) {
            val profileState by authViewModel.profileState.collectAsState()

            ProfileScreen(
                profileState = profileState,
                deleteAccountState = authViewModel.deleteAccountState.collectAsState().value,
                onUpdateProfile = { profile, imageUri ->
                    authViewModel.updateProfile(profile, imageUri)
                },
                onDeleteAccount = {
                    authViewModel.deleteAccount()
                },
                onLogout = {
                    authViewModel.logout() // Asegúrate de tener esta función en tu AuthViewModel
                    navController.navigate(Routes.Login.route) {
                        // Limpia el back stack para que el usuario no pueda volver atrás
                        popUpTo(Routes.Profile.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToChat = {
                    navController.navigate(Routes.Chat.route) {
                        popUpTo(Routes.Profile.route) { inclusive = true }
                    }
                }
            )
        }

        // Configuración
        composable(
            route = Routes.Settings.route,
            arguments = listOf(
                navArgument("currentChannel") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val currentChannel = backStackEntry.arguments?.getString("currentChannel") ?: "default"
            SettingsScreen(
                chatViewModel = chatViewModel,
                currentChannel = currentChannel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable(Routes.Community.route) {
            val communityState by authViewModel.communityState.collectAsState()

            CommunityScreen(
                profiles = communityState.profiles,
                isLoading = communityState.isLoading,
                onNavigateBack = { navController.navigateUp() },
                onUserClick = { userId ->
                    navController.navigate(Routes.UserDetails.createRoute(userId))
                },
                viewModel = authViewModel,
                adViewModel = connectionViewModel.adViewModel// Pasamos el viewModel
            )
        }


        // En tu NavHost
        composable(
            route = Routes.UserDetails.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("isUsername") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )

        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            val isUsername = backStackEntry.arguments?.getBoolean("isUsername") ?: false
            val userProfileState by authViewModel.userProfileState.collectAsState()

            LaunchedEffect(userId, isUsername) {
                userId?.let {
                    if (isUsername) {
                        // Si es nombre de usuario, buscar el perfil correspondiente
                        val profile = authViewModel.findUserProfileByUsername(it)
                        if (profile != null) {
                            authViewModel.loadUserProfile(profile.userId)
                        } else {
                            // Si no se encuentra perfil, actualizar el estado
                            authViewModel._userProfileState.update { state ->
                                state.copy(profile = null, error = "No profile found")
                            }
                        }
                    } else {
                        // Si es un ID de usuario, cargar directamente
                        authViewModel.loadUserProfile(it)
                    }
                }
            }

            // El resto del código se mantiene igual...
            when {
                userProfileState.isLoading -> {
                    // Show loading indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                userProfileState.profile != null -> {
                    UserDetailsScreen(
                        profile = userProfileState.profile!!,
                        isLoading = false,
                        onNavigateBack = { navController.navigateUp() }
                    )
                }

                else -> {
                    // Show "No profile" dialog or screen
                    AlertDialog(
                        onDismissRequest = { navController.navigateUp() },
                        title = { Text("Perfil no encontrado") },
                        text = { Text("Este usuario no tiene un perfil registrado en Flow.") },
                        confirmButton = {
                            TextButton(onClick = { navController.navigateUp() }) {
                                Text("Aceptar")
                            }
                        }
                    )
                }
            }
        }
        composable(Routes.WebContent.route) {
            // El ViewModel se creará automáticamente y obtendrá la URL de Firebase
            WebViewScreen(navController = navController)
        }
        composable(Routes.ColorSettings.route) {
            ColorSettingsScreen(
                viewModel = chatViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

    }
}

@Composable
fun MainScreen(
    connectionViewModel: IRCConnectionViewModel,
    chatViewModel: IRCChatViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.Connection.route
) {
    val navController = rememberNavController()
    val connectionState by connectionViewModel.connectionState.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        FlowChatNavHost(
            navController = navController,
            connectionViewModel = connectionViewModel,
            chatViewModel = chatViewModel,
            authViewModel = authViewModel,
            startDestination = when {
                // Si está conectado, ir al chat
                connectionState is ConnectionState.Connected -> Routes.Chat.route
                // Si no está conectado, ir a la pantalla de conexión
                else -> Routes.Connection.route
            }
        )
    }

    // Manejo de navegación por notificaciones
    HandleNotificationNavigation(
        navController = navController,
        connectionState = connectionState,
        authState = authState
    )
}

@Composable
private fun HandleNotificationNavigation(
    navController: NavController,
    connectionState: ConnectionState,
    authState: AuthState
) {
    val context = LocalContext.current
    val intent = (context as? MainActivity)?.intent
    val destination = intent?.getStringExtra("destination")
    val conversationType = intent?.getStringExtra(NotificationService.EXTRA_CONVERSATION_TYPE)
    val conversationName = intent?.getStringExtra(NotificationService.EXTRA_CONVERSATION_NAME)

    LaunchedEffect(connectionState, destination) {
        if (connectionState is ConnectionState.Connected && destination != null) {
            // Asegurarse de que estamos en la pantalla de chat primero
            navController.navigate(Routes.Chat.route) {
                popUpTo(Routes.Connection.route) { inclusive = true }
            }

            // Si la notificación requiere autenticación y el usuario no está autenticado
            if (requiresAuth(conversationType) && authState !is AuthState.Success) {
                navController.navigate(Routes.Login.route)
                return@LaunchedEffect
            }

            // Navegar al destino específico
            navController.navigate(destination) {
                popUpTo(Routes.Chat.route) { inclusive = false }
            }

            // Limpiar los extras del intent para evitar reprocesamiento
            (context as? MainActivity)?.intent?.removeExtra("destination")
        }
    }
}

private fun requiresAuth(conversationType: String?): Boolean {
    return when (conversationType) {
        "profile" -> true
        "community" -> true
        else -> false
    }
}