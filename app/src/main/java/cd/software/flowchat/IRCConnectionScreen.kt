package cd.software.flowchat

import android.app.Activity
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import cd.software.flowchat.admob.AdManager
import cd.software.flowchat.model.ConnectionState
import cd.software.flowchat.model.FirebaseRemoteConfigHelper
import cd.software.flowchat.model.IRCServerConfig
import cd.software.flowchat.utils.findActivity
import cd.software.flowchat.viewmodel.IRCConnectionViewModel
import es.chat.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IRCConnectionScreen(
    viewModel: IRCConnectionViewModel,
    adManager: AdManager,
    onConnected: () -> Unit,
    onAddServerClick: () -> Unit,
    onEditServer: (IRCServerConfig) -> Unit
) {

    val context = LocalContext.current


    // State observations
    val connectionState by viewModel.connectionState.collectAsState()
    val serverConfigs by viewModel.serverConfigs.collectAsState()
    val selectedServer by viewModel.selectedServerConfig.collectAsState()
    val nicknameError by viewModel.nicknameError.collectAsState()
    val savedNickname by viewModel.savedNickname.collectAsState()
    val savedPassword by viewModel.savedPassword.collectAsState()
    var button_add_Servers by remember { mutableStateOf(false) }
    var allowServerSelection by remember { mutableStateOf(false) }


    val isInterstitialAdReady by viewModel.adViewModel.isInterstitialAdReady.collectAsState()
    val privacyPolicyAccepted by viewModel.privacyPolicyAccepted.collectAsState()
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

    // Local states
    var nickname by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isServerSelectionDialogOpen by remember { mutableStateOf(false) }
    val isConnecting = connectionState is ConnectionState.Connecting
    val connectionError by viewModel.connectionError.collectAsState()
    // Remote config and server loading
    LaunchedEffect(Unit) {
        FirebaseRemoteConfigHelper.fetchRemoteConfig {
            val isCustomServersAllowed = FirebaseRemoteConfigHelper.getBoolean("bottom_add_servers")
            allowServerSelection = FirebaseRemoteConfigHelper.getBoolean("boolean_server_selection") // Nueva clave
            button_add_Servers = isCustomServersAllowed
            viewModel.loadServerConfigs()
        }
    }

    // Snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(savedNickname) {
        nickname = savedNickname
    }

    LaunchedEffect(savedPassword) {
        password = savedPassword
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.flowirc_connection)) },
                actions = {
                    if (button_add_Servers) {
                        IconButton(onClick = onAddServerClick) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_server))
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.padding(8.dp).padding(bottom = 40.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp)
                        .clip(MaterialTheme.shapes.medium), // Bordes redondeados

                    contentScale = ContentScale.Fit
                )
            }
            // Server Selection Card
          if (allowServerSelection) {
              Card(
                  modifier = Modifier
                      .fillMaxWidth()
                      .clickable { isServerSelectionDialogOpen = true },
                  elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
              ) {
                  Row(
                      modifier = Modifier.padding(16.dp),
                      verticalAlignment = Alignment.CenterVertically
                  ) {
                      Icon(
                          Icons.Default.Add,
                          contentDescription = stringResource(R.string.server_desc)
                      )
                      Spacer(modifier = Modifier.width(16.dp))
                      Text(
                          text = selectedServer?.name ?: stringResource(R.string.select_server),
                          style = MaterialTheme.typography.bodyLarge
                      )
                      Spacer(modifier = Modifier.weight(1f))
                      Icon(
                          Icons.Default.ArrowDropDown,
                          contentDescription = stringResource(R.string.dropdown_desc)
                      )
                  }
              }

              Spacer(modifier = Modifier.height(16.dp))
          }

            // Nickname TextField
            NicknameTextField(
                nickname = nickname,
                onNicknameChange = { nickname = it },
                isError = nicknameError != null,
                errorMessage = nicknameError,
                onClearError = { viewModel.clearNicknameError() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password TextField
            PasswordTextField(
                password = password,
                onPasswordChange = { password = it },
                label = stringResource(R.string.password_optional)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = privacyPolicyAccepted,
                    onCheckedChange = { viewModel.setPrivacyPolicyAccepted(it) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.accept_privacy),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.privacy_policy),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { showPrivacyPolicyDialog = true }
                )
            }

            // Connection Button
            ConnectionButton(
                isConnecting = isConnecting,
                isEnabled = !isConnecting && nickname.isNotBlank() && selectedServer != null && privacyPolicyAccepted,
                onConnect = {
                    // Guardar información de conexión
                    viewModel.saveConnectionInfo(nickname, password)

                    if (isInterstitialAdReady) {
                        // Obtener la actividad de forma segura
                        val activity = context.findActivity()
                        if (activity != null) {
                            adManager.showInterstitial(activity) {
                                viewModel.connect(nickname, password)
                            }
                        } else {
                            // Fallback si no podemos obtener la actividad
                            viewModel.connect(nickname, password)
                        }
                    } else {
                        viewModel.connect(nickname, password)
                    }

                }
            )

            // Server Selection Dialog
            if (isServerSelectionDialogOpen) {
                ServerSelectionDialog(
                    servers = serverConfigs,
                    onDismiss = { isServerSelectionDialogOpen = false },
                    onServerSelect = {
                        viewModel.selectServer(it)
                        isServerSelectionDialogOpen = false
                    },
                    onEditServer = onEditServer,
                    onDeleteServer = { viewModel.deleteCustomServer(it) }
                )
            }
            // Privacy Policy Dialog
            if (showPrivacyPolicyDialog) {
                PrivacyPolicyDialog(
                    onDismiss = { showPrivacyPolicyDialog = false },
                    onAccept = {
                        viewModel.setPrivacyPolicyAccepted(true)
                        showPrivacyPolicyDialog = false
                    }
                )
            }

            // Connection Status Handling
            ConnectionStatus(connectionState, onConnected, snackbarHostState)
        }
        // Mostrar error de conexión si existe
        connectionError?.let { errorMessage ->
            AlertDialog(
                onDismissRequest = { viewModel.clearConnectionError() },
                title = { Text(stringResource(R.string.connection_error)) },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.clearConnectionError() }
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }
    }
}

@Composable
fun ServerSelectionDialog(
    servers: List<IRCServerConfig>,
    onDismiss: () -> Unit,
    onServerSelect: (IRCServerConfig) -> Unit,
    onEditServer: (IRCServerConfig) -> Unit,
    onDeleteServer: (IRCServerConfig) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredServers = remember(searchQuery, servers) {
        if (searchQuery.isBlank()) {
            servers
        } else {
            servers.filter { server ->
                server.name.contains(searchQuery, ignoreCase = true) ||
                        server.url.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.select_irc_network),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search_servers)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))



                if (filteredServers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_servers_found), style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredServers) { server ->
                            ServerGridItem(
                                server = server,
                                onSelect = {
                                    onServerSelect(server)
                                    onDismiss()
                                },
                                onEdit = { onEditServer(server) },
                                onDelete = { onDeleteServer(server) }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ServerGridItem(
    server: IRCServerConfig,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = server.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            if (server.isCustom) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Server")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Server",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NicknameTextField(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    TextField(
        value = nickname,
        onValueChange = { newValue ->
            onNicknameChange(newValue.trim())
            if (isError) onClearError()
        },
        label = { Text(stringResource(R.string.nickname)) },
        placeholder = { Text(stringResource(R.string.nickname_hint)) },
        leadingIcon = {
            Icon(
                Icons.Default.Person,
                contentDescription = stringResource(R.string.nickname_desc)
            )
        },
        trailingIcon = {
            if (nickname.isNotEmpty()) {
                IconButton(
                    onClick = { onNicknameChange("") },
                    modifier = Modifier.semantics {
                        contentDescription = context.getString(R.string.clear_nickname_desc)
                    }
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                }
            }
        },
        isError = isError,
        supportingText = { errorMessage?.let { Text(it) } },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    label: String = stringResource(R.string.password)
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    TextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(label) },
        placeholder = { Text(stringResource(R.string.password_hint)) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = stringResource(R.string.password_desc)
            )
        },
        trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    imageVector = if (isPasswordVisible)
                        Icons.Default.Lock
                    else
                        Icons.Default.Check,
                    contentDescription = if (isPasswordVisible)
                        stringResource(R.string.hide_password_desc)
                    else
                        stringResource(R.string.show_password_desc)
                )
            }
        },
        singleLine = true
    )
}
@Composable
fun ConnectionButton(
    isConnecting: Boolean,
    isEnabled: Boolean,
    onConnect: () -> Unit
) {
    Button(
        onClick = onConnect,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        if (isConnecting) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Text(stringResource(R.string.connecting))
            }
        } else {
            Text(stringResource(R.string.connect_button))
        }
    }
}

@Composable
fun ConnectionStatus(
    connectionState: ConnectionState,
    onConnected: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    LaunchedEffect(connectionState) {
        when (connectionState) {
            is ConnectionState.Error -> {
                snackbarHostState.showSnackbar(
                    message = connectionState.message,
                    duration = SnackbarDuration.Long
                )
            }
            is ConnectionState.Connected -> {
                onConnected()
            }
            is ConnectionState.Disconnecting -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.disconnecting),
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }
}
@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = stringResource(R.string.privacy_policy_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // WebView for showing the privacy policy
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            // Load your privacy policy URL
                            loadUrl("https://chateapp-latino.web.app/radio/privacy-policy.html")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.reject))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onAccept) {
                        Text(stringResource(R.string.accept))
                    }
                }
            }
        }
    }
}