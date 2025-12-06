package cd.software.flowchat

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cd.software.flowchat.admob.AdViewModel
import cd.software.flowchat.model.IRCServerConfig
import es.chat.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerScreen(
    initialServer: IRCServerConfig? = null,
    onServerAdded: () -> Unit,
    onNavigateBack: () -> Unit,
    customServerManager: CustomServerManager,
    adViewModel: AdViewModel // Añadir ViewModel de anuncios
) {
    val activity = LocalContext.current as Activity
    val isRewardedAdReady by adViewModel.isRewardedAdReady.collectAsState()

    var serverName by remember { mutableStateOf(initialServer?.name ?: "") }
    var serverUrl by remember { mutableStateOf(initialServer?.url ?: "") }
    var serverPort by remember { mutableStateOf(initialServer?.port?.toString() ?: "6667") }
    var useSSL by remember { mutableStateOf(initialServer?.ssl ?: false) }
    var realName by remember { mutableStateOf(initialServer?.realname ?: "") }
    var autoJoinChannels by remember { mutableStateOf(initialServer?.autoJoinChannels?.joinToString(", ") ?: "") }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Estado para controlar si hay una acción de guardado pendiente
    var pendingSave by remember { mutableStateOf(false) }

    // Estado para mostrar un diálogo cuando el anuncio no está listo
    var showAdNotReadyDialog by remember { mutableStateOf(false) }

    // Función para preparar el servidor
    fun prepareServer(): IRCServerConfig? {
        if (serverName.isBlank() || serverUrl.isBlank()) {
            errorMessage = "Server name and URL are required"
            showErrorDialog = true
            return null
        }

        // Aplicar valor por defecto si realName está vacío
        val finalRealName = if (realName.isBlank()) {
            "$serverName Flowirc Android"
        } else {
            realName
        }

        return IRCServerConfig(
            name = serverName,
            url = serverUrl,
            port = serverPort.toIntOrNull() ?: 6667,
            ssl = useSSL,
            realname = finalRealName,  // Usar el valor calculado
            autoJoinChannels = autoJoinChannels
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        )
    }

    // Función para guardar el servidor
    fun saveServer(server: IRCServerConfig) {
        CoroutineScope(Dispatchers.IO).launch {
            customServerManager.saveCustomServer(server)
            withContext(Dispatchers.Main) {
                onServerAdded()
            }
        }
    }

    // Efecto LaunchedEffect para mostrar anuncio cuando se activa pendingSave
    LaunchedEffect(pendingSave) {
        if (pendingSave) {
            val server = prepareServer()
            if (server != null) {
                if (isRewardedAdReady) {
                    adViewModel.adManager.showRewardedAd(
                        activity = activity,
                        onRewarded = { reward ->
                            // El usuario ha visto el anuncio completamente y recibido la recompensa
                            saveServer(server)
                            pendingSave = false
                        },
                        onAdClosed = {
                            // Se cierra el anuncio (puede que el usuario no haya obtenido la recompensa)
                            pendingSave = false
                        }
                    )
                } else {
                    // Si el anuncio no está listo, mostramos un diálogo
                    showAdNotReadyDialog = true
                    pendingSave = false
                }
            } else {
                pendingSave = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_custom_server)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = serverName,
                onValueChange = { serverName = it },
                label = { Text(stringResource(R.string.server_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text(stringResource(R.string.server_url)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = serverPort,
                onValueChange = {
                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                        serverPort = it
                    }
                },
                label = { Text(stringResource(R.string.port)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.use_ssl))
                Switch(
                    checked = useSSL,
                    onCheckedChange = { useSSL = it }
                )
            }

            OutlinedTextField(
                value = realName,
                onValueChange = { realName = it },
                label = { Text(stringResource(R.string.real_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = autoJoinChannels,
                onValueChange = { autoJoinChannels = it },
                label = { Text(stringResource(R.string.auto_join_channels)) },
                placeholder = { Text(stringResource(R.string.channel_example)) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    adViewModel.trackUserInteraction()
                    pendingSave = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_server))
            }

            if (!isRewardedAdReady) {
                Text(
                    text = stringResource(R.string.ad_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    if (showAdNotReadyDialog) {
        AlertDialog(
            onDismissRequest = { showAdNotReadyDialog = false },
            title = { Text(stringResource(R.string.ad_not_ready)) },
            text = { Text(stringResource(R.string.ad_not_ready_message)) },
            confirmButton = {
                TextButton(onClick = { showAdNotReadyDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}