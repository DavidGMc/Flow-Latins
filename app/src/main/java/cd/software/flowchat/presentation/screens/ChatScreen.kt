package cd.software.flowchat.presentation

// Componentes de formato
// Componentes de entrada
// Componentes de mensaje
// Utilidades
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DensitySmall
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.twotone.AccountBox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cd.software.flowchat.Routes
import cd.software.flowchat.admob.AdManager
import cd.software.flowchat.admob.AdViewModel
import cd.software.flowchat.calculateWindowSizeClass
import cd.software.flowchat.firebase.model.AuthState
import cd.software.flowchat.firebase.viewmodel.AuthViewModel
import cd.software.flowchat.mircolors.IRCColors
import cd.software.flowchat.mircolors.IRCMessageFormatter
import cd.software.flowchat.model.ConnectionState
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.model.WindowWidthSizeClass
import cd.software.flowchat.presentation.chat.components.common.FloatingScrollButton
import cd.software.flowchat.presentation.chat.components.common.FormattedClickableText
import cd.software.flowchat.presentation.chat.components.format.SimpleFormatOptionsDropdown
import cd.software.flowchat.presentation.chat.components.input.InputActionButton
import cd.software.flowchat.presentation.chat.components.message.UrlPreviewCard
import cd.software.flowchat.presentation.chat.utils.copyToClipboard
import cd.software.flowchat.presentation.chat.utils.extractUrls
import cd.software.flowchat.presentation.chat.utils.formatTimestamp
import cd.software.flowchat.utils.ChatTypography
import cd.software.flowchat.utils.findActivity
import cd.software.flowchat.utils.showToast
import cd.software.flowchat.viewmodel.IRCChatViewModel
import cd.software.flowchat.viewmodel.IRCConnectionViewModel
import es.chat.R
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
        connectionViewModel: IRCConnectionViewModel,
        chatViewModel: IRCChatViewModel,
        authViewModel: AuthViewModel,
        navController: NavController,
        onShowChannels: () -> Unit,
        onShowUsers: () -> Unit = {},
        initialPrivateChat: String? = null,
        initialChannel: String? = null,
        isProfileEnabled: Boolean = false,
        onProfileClick: () -> Unit = {},
        onViewProfile: (String) -> Unit = {},
        adViewModel: AdViewModel,
        adManager: AdManager,
) {
    // Estados recolectados del ViewModel
    val conversations by chatViewModel.conversations.collectAsState()
    val currentConversationIndex by chatViewModel.currentConversationIndex.collectAsState()
    val navigationState by chatViewModel.navigationState.collectAsState()
    val connectionState by connectionViewModel.connectionState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val usersInCurrentChannel by chatViewModel.usersInCurrentChannel.collectAsState()
    val messageInputState by chatViewModel.messageInput.collectAsState()
    val notifications by chatViewModel.notifications.collectAsState()
    val isRewardedAdReady by adViewModel.isRewardedAdReady.collectAsState()
    val isInterstitialAdReady by adViewModel.isInterstitialAdReady.collectAsState()
    val disableColorCodes by chatViewModel.getDisableColorCodes().collectAsState()

    // Estados locales - OPTIMIZADO: menos estados reactivos
    var messageInput by remember { mutableStateOf("") }
    var showDisconnectDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showRewardAdForUsers by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var hasProcessedInitialParams by remember { mutableStateOf(false) }

    // NUEVO: Estados optimizados para teclado
    val keyboardController = LocalSoftwareKeyboardController.current
    val imeInsets = WindowInsets.ime
    val navigationBarsInsets = WindowInsets.navigationBars
    val density = LocalDensity.current
    val context = LocalContext.current

    // OPTIMIZACIÓN: Detección mejorada de teclado
    val isKeyboardVisible by remember { derivedStateOf { imeInsets.getBottom(density) > 0 } }

    val keyboardHeight by remember {
        derivedStateOf { with(density) { imeInsets.getBottom(density).toDp() } }
    }

    // Configuración del pager - OPTIMIZADO: menos recálculos
    val pagerState =
            rememberPagerState(
                    initialPage =
                            currentConversationIndex.coerceIn(0, maxOf(0, conversations.size - 1)),
                    pageCount = { conversations.size }
            )
    val coroutineScope = rememberCoroutineScope()

    // OPTIMIZACIÓN: Control de scroll mejorado
    var isUserScrolling by remember { mutableStateOf(false) }
    var lastUserScrollTime by remember { mutableLongStateOf(0L) }

    // Efecto optimizado para sincronización de navegación
    LaunchedEffect(pagerState, navigationState) {
        if (navigationState.isProgrammatic && navigationState.targetPage != null) {
            val target = navigationState.targetPage!!
            if (target in 0 until pagerState.pageCount && target != pagerState.currentPage) {
                pagerState.scrollToPage(target)

                snapshotFlow { pagerState.isScrollInProgress }.filter { !it }.first()

                if (pagerState.currentPage == target) {
                    chatViewModel.onPagerSettled(target)
                }
            }
        }
    }

    // Sincronización optimizada del pager
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != chatViewModel.currentConversationIndex.value) {
            chatViewModel.switchToConversation(pagerState.currentPage, fromUser = true)
        }
    }

    // Efecto optimizado para cambios en conversaciones
    LaunchedEffect(conversations) {
        if (hasProcessedInitialParams && conversations.isNotEmpty() && !isUserScrolling) {
            val timeSinceLastScroll = System.currentTimeMillis() - lastUserScrollTime
            if (timeSinceLastScroll > 1000) {
                val newPrivateChat =
                        conversations.asReversed().find { conversation ->
                            val isPrivate = conversation.type == ConversationType.PRIVATE_MESSAGE
                            val isNotClosed =
                                    conversation.name != chatViewModel.lastClosedPrivateChat.value
                            val isNotCurrent =
                                    conversation !=
                                            conversations.getOrNull(currentConversationIndex)
                            isPrivate && isNotClosed && isNotCurrent
                        }

                newPrivateChat?.let { chat ->
                    val newIndex = conversations.indexOf(chat)
                    if (newIndex != -1 && newIndex != currentConversationIndex) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(newIndex)
                            chatViewModel.switchToConversation(newIndex, fromUser = false)
                        }
                    }
                }
            }
        }
    }

    // Efecto para navegación inicial optimizado
    LaunchedEffect(initialPrivateChat, initialChannel) {
        if (!hasProcessedInitialParams && (initialPrivateChat != null || initialChannel != null)) {
            initialPrivateChat?.let { chatViewModel.startPrivateConversationAndNavigate(it) }
            initialChannel?.let { chatViewModel.joinChannelFromListAndNavigate(it) }
            hasProcessedInitialParams = true
        }
    }

    LaunchedEffect(currentConversationIndex) {
        val currentConversation = conversations.getOrNull(currentConversationIndex)
        if (currentConversation?.type == ConversationType.CHANNEL) {
            chatViewModel.loadUsersForCurrentChannel()
        }
        chatViewModel.setActiveConversation(currentConversation?.name)
    }

    // Efecto para manejar desconexión
    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Disconnected) {
            navController.navigate(Routes.Connection.route) { popUpTo(0) { inclusive = true } }
        }
    }

    // Efecto para sincronizar input de mensaje
    LaunchedEffect(messageInputState) {
        if (messageInputState.isNotEmpty()) {
            messageInput = messageInputState
            chatViewModel.updateMessageInput("")
        }
    }

    // Cleanup optimizado
    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.setActiveConversation(null)
            keyboardController?.hide()
        }
    }

    fun exitPrivateConversation() {
        val currentConversation = chatViewModel.getCurrentConversation()
        if (currentConversation?.type == ConversationType.PRIVATE_MESSAGE) {
            chatViewModel.setLastClosedPrivateChat(currentConversation.name)
        }
        chatViewModel.exitCurrentConversation()
    }

    // Diálogo para ver lista de usuarios con anuncio
    if (showRewardAdForUsers) {
        AlertDialog(
                onDismissRequest = { showRewardAdForUsers = false },
                title = { Text(stringResource(R.string.user_list_dialog_title)) },
                text = { Text(stringResource(R.string.user_list_dialog_message)) },
                confirmButton = {
                    TextButton(
                            onClick = {
                                showRewardAdForUsers = false
                                val activity = context.findActivity()
                                if (activity != null && isRewardedAdReady) {
                                    adManager.showRewardedAd(
                                            activity,
                                            onRewarded = { reward ->
                                                chatViewModel.loadUsersForCurrentChannel()
                                                onShowUsers()
                                            },
                                            onAdClosed = {}
                                    )
                                } else {
                                    chatViewModel.loadUsersForCurrentChannel()
                                    onShowUsers()
                                }
                            }
                    ) { Text(stringResource(R.string.watch_ad_button)) }
                },
                dismissButton = {
                    TextButton(onClick = { showRewardAdForUsers = false }) {
                        Text(stringResource(R.string.cancel_button))
                    }
                }
        )
    }

    // Diálogo de desconexión
    if (showDisconnectDialog) {
        AlertDialog(
                onDismissRequest = { showDisconnectDialog = false },
                title = { Text(stringResource(R.string.disconnect_dialog_title)) },
                text = { Text(stringResource(R.string.disconnect_dialog_message)) },
                confirmButton = {
                    TextButton(
                            onClick = {
                                adViewModel.trackUserInteraction()
                                showDisconnectDialog = false

                                val activity = context.findActivity()
                                if (activity != null && isInterstitialAdReady) {
                                    adManager.showInterstitial(activity) {
                                        connectionViewModel.disconnect()
                                        chatViewModel.clearState()
                                        navController.navigate(Routes.Connection.route) {
                                            popUpTo(Routes.Chat.route) { inclusive = true }
                                        }
                                    }
                                } else {
                                    connectionViewModel.disconnect()
                                    chatViewModel.clearState()
                                    navController.navigate(Routes.Connection.route) {
                                        popUpTo(Routes.Chat.route) { inclusive = true }
                                    }
                                }
                            }
                    ) { Text(stringResource(R.string.disconnect_button)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDisconnectDialog = false }) {
                        Text(stringResource(R.string.cancel_button))
                    }
                }
        )
    }

    // Diálogo de autenticación
    if (showAuthDialog) {
        AlertDialog(
                onDismissRequest = { showAuthDialog = false },
                title = { Text(stringResource(R.string.premium_dialog_title)) },
                text = { Text(stringResource(R.string.premium_dialog_message)) },
                confirmButton = {
                    TextButton(
                            onClick = {
                                showAuthDialog = false
                                navController.navigate(Routes.Registration.route)
                            }
                    ) { Text(stringResource(R.string.register_button)) }
                },
                dismissButton = {
                    TextButton(
                            onClick = {
                                showAuthDialog = false
                                if (isRewardedAdReady) {
                                    showRewardAdForUsers = true
                                } else {
                                    chatViewModel.loadUsersForCurrentChannel()
                                    onShowUsers()
                                }
                            }
                    ) { Text(stringResource(R.string.watch_ad_button)) }
                }
        )
    }

    // Scaffold principal OPTIMIZADO
    Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                        title = { Text(stringResource(R.string.chat_title)) },
                        actions = {
                            // Indicador de estado de conexión
                            when (connectionState) {
                                ConnectionState.Connecting ->
                                        CircularProgressIndicator(
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                        )
                                ConnectionState.Connected ->
                                        Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription =
                                                        stringResource(R.string.connected_status),
                                                tint = MaterialTheme.colorScheme.secondary
                                        )
                                else -> {}
                            }

                            // Acciones de la barra superior
                            if (currentConversationIndex < conversations.size &&
                                            conversations[currentConversationIndex].type !=
                                                    ConversationType.SERVER_STATUS
                            ) {
                                IconButton(onClick = { exitPrivateConversation() }) {
                                    Icon(Icons.Default.Clear, "Exit Conversation")
                                }
                            }

                            IconButton(onClick = onShowChannels) {
                                Icon(Icons.Default.DensitySmall, "Channel List")
                            }

                            IconButton(
                                    onClick = {
                                        chatViewModel.loadUsersForCurrentChannel()
                                        onShowUsers()
                                    }
                            ) {
                                Icon(
                                        Icons.Default.SupervisorAccount,
                                        contentDescription = "Channel Users",
                                        tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Menú desplegable
                            Box {
                                IconButton(onClick = { showOverflowMenu = true }) {
                                    Icon(Icons.Default.AddCircle, "More Options")
                                }
                                DropdownMenu(
                                        expanded = showOverflowMenu,
                                        onDismissRequest = { showOverflowMenu = false }
                                ) {
                                    if (authState !is AuthState.Success) {
                                        DropdownMenuItem(
                                                text = {
                                                    Text(stringResource(R.string.action_login))
                                                },
                                                leadingIcon = {
                                                    Icon(Icons.TwoTone.AccountBox, null)
                                                },
                                                onClick = {
                                                    showOverflowMenu = false
                                                    navController.navigate(Routes.Login.route)
                                                }
                                        )
                                        DropdownMenuItem(
                                                text = {
                                                    Text(stringResource(R.string.action_register))
                                                },
                                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                                onClick = {
                                                    showOverflowMenu = false
                                                    navController.navigate(
                                                            Routes.Registration.route
                                                    )
                                                }
                                        )
                                        Divider()
                                    }

                                    val currentChannel =
                                            chatViewModel.getCurrentConversation()?.let {
                                                    conversation ->
                                                if (conversation.type == ConversationType.CHANNEL)
                                                        conversation.name
                                                else "default"
                                            }
                                                    ?: "default"

                                    DropdownMenuItem(
                                            text = {
                                                Text(stringResource(R.string.action_settings))
                                            },
                                            leadingIcon = { Icon(Icons.Default.Settings, null) },
                                            onClick = {
                                                showOverflowMenu = false
                                                navController.navigate(
                                                        Routes.Settings.createRoute(currentChannel)
                                                )
                                            }
                                    )

                                    if (authState is AuthState.Success) {
                                        DropdownMenuItem(
                                                text = {
                                                    Text(stringResource(R.string.action_profile))
                                                },
                                                leadingIcon = {
                                                    Icon(Icons.Default.AccountCircle, null)
                                                },
                                                onClick = {
                                                    showOverflowMenu = false
                                                    onProfileClick()
                                                }
                                        )
                                    }

                                    DropdownMenuItem(
                                            text = {
                                                Text(stringResource(R.string.action_community))
                                            },
                                            leadingIcon = {
                                                Icon(painterResource(R.drawable.community), null)
                                            },
                                            onClick = {
                                                showOverflowMenu = false
                                                navController.navigate(Routes.Community.route)
                                            }
                                    )

                                    DropdownMenuItem(
                                            text = { Text(stringResource(R.string.action_ads)) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Info, contentDescription = null)
                                            },
                                            onClick = {
                                                showOverflowMenu = false
                                                navController.navigate(Routes.WebContent.route)
                                            }
                                    )

                                    DropdownMenuItem(
                                            text = { Text(stringResource(R.string.action_colors)) },
                                            leadingIcon = {
                                                Icon(
                                                        painter =
                                                                painterResource(
                                                                        R.drawable.ic_paleta_colors
                                                                ),
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            onClick = {
                                                navController.navigate(Routes.ColorSettings.route)
                                            }
                                    )

                                    DropdownMenuItem(
                                            text = {
                                                Text(stringResource(R.string.action_disconnect))
                                            },
                                            leadingIcon = {
                                                Icon(
                                                        painter =
                                                                painterResource(
                                                                        id = R.drawable.power_off
                                                                ),
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                showOverflowMenu = false
                                                showDisconnectDialog = true
                                            }
                                    )
                                }
                            }
                        }
                )
            }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // ===== TABS FIJAS - SIEMPRE VISIBLES =====
            if (conversations.isNotEmpty()) {
                ScrollableTabRow(
                        selectedTabIndex =
                                pagerState.currentPage.coerceIn(0, conversations.size - 1),
                        modifier = Modifier.fillMaxWidth(),
                        edgePadding = 16.dp
                ) {
                    conversations.forEachIndexed { index, conversation ->
                        Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        isUserScrolling = true
                                        lastUserScrollTime = System.currentTimeMillis()
                                        chatViewModel.switchToConversation(index, fromUser = true)
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) { Text(conversation.name) }
                        }
                    }
                }
            }

            // ===== ÁREA DE CONVERSACIONES - SE AJUSTA CON EL TECLADO =====
            Box(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                    // .imePadding()// Ocupa el espacio disponible entre tabs e input
                    ) {
                if (conversations.isNotEmpty()) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) {
                            pageIndex ->
                        val currentConversation = conversations.getOrNull(pageIndex)

                        if (currentConversation != null) {
                            if (currentConversation.type == ConversationType.SERVER_STATUS) {
                                StatusConversationContent(
                                        conversation = currentConversation,
                                        chatViewModel = chatViewModel
                                )
                            } else {
                                ConversationContent(
                                        conversation = currentConversation,
                                        onNickClick = { nickname ->
                                            messageInput =
                                                    if (messageInput.isBlank()) {
                                                        "$nickname: "
                                                    } else {
                                                        "$messageInput $nickname "
                                                    }
                                        },
                                        onOpenPrivateChat = { username ->
                                            chatViewModel.startPrivateConversationAndNavigate(
                                                    username
                                            )
                                        },
                                        chatViewModel = chatViewModel,
                                        authViewModel = authViewModel,
                                        adViewModel = adViewModel,
                                        navController = navController,
                                        pagerState = pagerState
                                )
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay conversaciones activas")
                    }
                }
            }

            // ===== INPUT FIJO - RESPETA BARRA DE NAVEGACIÓN =====
            Surface(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .windowInsetsPadding(
                                            WindowInsets.systemBars.only(WindowInsetsSides.Bottom)
                                    ),
                    shadowElevation = 8.dp
            ) {
                OptimizedChatInputSection(
                        messageInput = messageInput,
                        onMessageChange = { messageInput = it },
                        onSendMessage = { message ->
                            if (message.isNotBlank()) {
                                chatViewModel.sendMessage(message)
                                messageInput = ""
                            }
                        },
                        users = usersInCurrentChannel.map { it.nickname },
                        isKeyboardVisible = isKeyboardVisible,
                        keyboardHeight = keyboardHeight,
                        chatViewModel = chatViewModel,
                        keyboardController = keyboardController,
                        modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Mostrar indicador de carga cuando sea necesario
    if (navigationState.isLoading) {
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { /* Evitar interacciones durante la carga */},
                contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun OptimizedChatInputSection(
        messageInput: String,
        onMessageChange: (String) -> Unit,
        onSendMessage: (String) -> Unit,
        users: List<String>,
        isKeyboardVisible: Boolean,
        keyboardHeight: Dp,
        chatViewModel: IRCChatViewModel,
        keyboardController: SoftwareKeyboardController?,
        modifier: Modifier = Modifier
) {
    var isSending by remember { mutableStateOf(false) }
    val disableColorCodes by chatViewModel.getDisableColorCodes().collectAsState()

    LaunchedEffect(isSending) {
        if (isSending) {
            delay(100)
            keyboardController?.hide()
            isSending = false
        }
    }

    val optimizedSendMessage: (String) -> Unit = { message ->
        if (message.isNotBlank() && !isSending) {
            isSending = true
            onSendMessage(message)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
        ) {
            ChatInput(
                    message = messageInput,
                    onMessageChange = onMessageChange,
                    onSendMessage = optimizedSendMessage,
                    users = users,
                    disableColorCodes = disableColorCodes, // Solo este parámetro necesario
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatusMessageItem(
        message: IRCMessage,
        chatViewModel: IRCChatViewModel,
        context: Context, // <-- Nuevo parámetro
        modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val fontSize by chatViewModel.fontSize.collectAsState()
    val formatter = remember { IRCMessageFormatter() }

    // 1. Formatear el mensaje (sanitizado y con colores IRC)
    val formattedContent =
            remember(message.content) {
                formatter.formatMessage(
                        message.content
                ) // <-- Usar formatMessage, NO stripAllFormats
            }

    // 2. Obtener los rangos de las URLs para hacerlas clickeables
    val urlRanges = remember(message.content) { message.content.extractUrls() }

    // Sistema de colores para nicknames
    val nickColors =
            listOf(
                    Color(0xFF3498DB),
                    Color(0xFFE74C3C),
                    Color(0xFF2ECC71),
                    Color(0xFFF39C12),
                    Color(0xFF9B59B6),
                    Color(0xFF1ABC9C),
                    MaterialTheme.colorScheme.primary,
            )
    val nickColor = nickColors[kotlin.math.abs(message.sender.hashCode()) % nickColors.size]

    Box(
            modifier =
                    modifier.fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        context.copyToClipboard(message.content)
                                    } // <-- Copiar el texto original
                            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header (nick + timestamp)
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                        text = message.sender,
                        style = MaterialTheme.typography.bodySmall,
                        color = nickColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize.sp
                )

                Text(
                        text = formatTimestamp(message.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = fontSize.sp
                )
            }

            // 3. CONTENIDO PRINCIPAL REEMPLAZADO:
            // Usar FormattedClickableText en lugar de Text simple
            FormattedClickableText( // <-- Componente que sí soporta enlaces
                    message = message.content, // <-- Pasar el contenido original
                    style =
                            MaterialTheme.typography.bodyLarge.copy(
                                    color = colorScheme.onSurface,
                                    fontSize = fontSize.sp
                            ),
                    modifier = Modifier.padding(top = 2.dp),
                    onClickUrl = { url ->
                        // Misma lógica que en ConversationContent
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            context.showToast(R.string.url_open_error)
                        }
                    }
            )

            // 4. OPCIONAL: Previews de URLs (si quieres mantener esta funcionalidad)
            // Usa el contenido original para extraer URLs
            message.content.extractUrls().takeIf { it.isNotEmpty() }?.let { urls ->
                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = 0.5.dp
                )
                Column {
                    urls.forEach { urlRange ->
                        val url = message.content.substring(urlRange.first, urlRange.second)
                        UrlPreviewCard(
                                url = url,
                                context = context,
                                modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
// COMPONENTE CONVERSATIONCONTENT OPTIMIZADO
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConversationContent(
        conversation: Conversation,
        onNickClick: (String) -> Unit = {},
        onOpenPrivateChat: (String) -> Unit = {},
        chatViewModel: IRCChatViewModel,
        authViewModel: AuthViewModel,
        adViewModel: AdViewModel,
        navController: NavController,
        pagerState: PagerState
) {

    val listState = rememberLazyListState()
    val context = LocalContext.current

    // OPTIMIZACIÓN: Estado derivado para mejor performance
    val showScrollToBottom by remember {
        derivedStateOf {
            val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) false
            else visibleItemsInfo.last().index < conversation.messages.lastIndex - 2
        }
    }

    // OPTIMIZACIÓN: Efecto más eficiente para auto-scroll
    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            val lastIndex = conversation.messages.lastIndex
            val currentIndex = listState.firstVisibleItemIndex
            val visibleItems = listState.layoutInfo.visibleItemsInfo.size

            // Solo hacer scroll si estamos cerca del final
            if (lastIndex - currentIndex <= visibleItems + 3) {
                // Usar animateScrollToItem para transición suave
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                flingBehavior =
                        androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                                listState
                        )
        ) {
            items(
                    items = conversation.messages,
                    key = { it.hashCode() } // OPTIMIZACIÓN: Keys únicos para mejor performance
            ) { message ->
                MessageItem(
                        message = message,
                        isOwnMessage = message.isOwnMessage,
                        onNickClick = onNickClick,
                        onOpenPrivateChat = onOpenPrivateChat,
                        chatViewModel = chatViewModel,
                        authViewModel = authViewModel,
                        adViewModel = adViewModel,
                        context = context,
                        navController = navController,
                        pagerState = pagerState,
                        conversations = chatViewModel.conversations.collectAsState().value,
                        fontSize = chatViewModel.fontSize.collectAsState().value
                )
            }
        }

        val coroutineScope = rememberCoroutineScope()
        // Botón de scroll optimizado
        AnimatedVisibility(
                visible = showScrollToBottom,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            FloatingScrollButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = {
                        coroutineScope.launch {
                            // Scroll suave al final
                            listState.animateScrollToItem(conversation.messages.lastIndex)
                        }
                    }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
        message: IRCMessage,
        isOwnMessage: Boolean,
        onNickClick: (String) -> Unit = {},
        onOpenPrivateChat: (String) -> Unit = {},
        chatViewModel: IRCChatViewModel,
        authViewModel: AuthViewModel,
        adViewModel: AdViewModel,
        context: Context,
        navController: NavController,
        pagerState: PagerState,
        fontSize: Int,
        conversations: List<Conversation>
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showNickMenu by remember { mutableStateOf(false) }
    var nickMenuAnchor by remember { mutableStateOf<Offset?>(null) }
    // Aplicar fondo rojo si el mensaje menciona al usuario
    val eventBackgroundColor =
            if (message.isMentioned) {
                Color.Red.copy(alpha = 0.15f)
            } else {
                message.eventColorType.getColor().copy(alpha = 0.1f)
            }
    val isPremiumUser = authViewModel.isPremiumUser()
    var showAuthDialog by remember { mutableStateOf(false) }
    var premiumFeatureRequested by remember { mutableStateOf<() -> Unit>({}) }
    val isRewardedAdReady by adViewModel.isRewardedAdReady.collectAsState()
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }
    val filteredContent = chatViewModel.contentFilter.filterText(message.content)
    val urlRanges = remember(filteredContent) { filteredContent.extractUrls() }
    val urls =
            remember(filteredContent, urlRanges) {
                urlRanges.map { (start, end) -> filteredContent.substring(start, end) }
            }
    val formatter = remember { IRCMessageFormatter() }
    val formattedContent = remember(message.content) { formatter.formatMessage(message.content) }

    val annotatedText =
            remember(filteredContent, urlRanges) {
                buildAnnotatedString {
                    append(filteredContent)
                    urlRanges.forEach { (start, end) ->
                        val url = filteredContent.substring(start, end)
                        addStyle(
                                style =
                                        SpanStyle(
                                                color = Color.Blue,
                                                textDecoration = TextDecoration.Underline
                                        ),
                                start = start,
                                end = end
                        )
                        addStringAnnotation("URL", url, start, end)
                    }
                }
            }
    val nickColors =
            listOf(
                    Color(0xFF3498DB),
                    Color(0xFFE74C3C),
                    Color(0xFF2ECC71),
                    Color(0xFFF39C12),
                    Color(0xFF9B59B6),
                    Color(0xFF1ABC9C),
                    Color(0xFFE67E22),
                    Color(0xFF34495E),
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.secondary
            )
    val nickColor = nickColors[kotlin.math.abs(message.sender.hashCode()) % nickColors.size]
    val isSystemMessage = message.sender.isEmpty() || message.sender == "SYSTEM"
    val copyMessage = {
        context.copyToClipboard(message.content)
        context.showToast(R.string.message_copied)
    }
    fun handleOpenPrivateChat(username: String) {
        // 1. Primero crear la conversación privada
        onOpenPrivateChat(username)
        // 2. Esperar un momento para que se cree y luego navegar
        coroutineScope.launch {
            // Pequeña pausa para asegurar que la conversación se haya creado
            delay(100)
            // Buscar en las conversaciones actualizadas
            val updatedConversations = chatViewModel.conversations.value
            val targetIndex =
                    updatedConversations.indexOfFirst {
                        it.type == ConversationType.PRIVATE_MESSAGE &&
                                it.name.equals(username, ignoreCase = true)
                    }
            if (targetIndex != -1) {
                // Navegar al índice y también actualizar el viewModel
                pagerState.animateScrollToPage(targetIndex)
                chatViewModel.switchToConversation(targetIndex)
            }
        }
    }
    fun handlePremiumAction(action: () -> Unit) {
        if (isPremiumUser) {
            action()
        } else {
            premiumFeatureRequested = action
            showAuthDialog = true
        }
    }
    fun handleNickClick(offset: Offset) {
        nickMenuAnchor = offset
        showNickMenu = true
    }
    if (isSystemMessage) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
        ) {
            Text(
                    text = message.content,
                    style = ChatTypography.systemMessage(fontSize),
                    modifier =
                            Modifier.background(
                                            color = eventBackgroundColor,
                                            shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                                    .combinedClickable(onClick = {}, onLongClick = copyMessage),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
            )
        }
    } else {
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
        ) {
            Box(
                    modifier =
                            Modifier.widthIn(max = 280.dp)
                                    .combinedClickable(onClick = {}, onLongClick = copyMessage)
            ) {
                Column(
                        modifier =
                                Modifier.background(
                                                color = eventBackgroundColor,
                                                shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp)
                ) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                                modifier =
                                        Modifier.weight(1f).pointerInput(Unit) {
                                            detectTapGestures(
                                                    onTap = { offset ->
                                                        handleNickClick(Offset(offset.x, offset.y))
                                                    },
                                                    onLongPress = { onNickClick(message.sender) }
                                            )
                                        }
                        ) {
                            Text(
                                    text = message.sender,
                                    style = ChatTypography.nicknameText(fontSize),
                                    color = nickColor,
                                    fontWeight = FontWeight.Bold
                            )
                        }
                        // Icono de mención si el mensaje menciona al usuario
                        if (message.isMentioned) {
                            Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Mencionado",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = copyMessage, modifier = Modifier.size(24.dp)) {
                            Icon(
                                    painter = painterResource(R.drawable.content_copy),
                                    contentDescription = "Copiar mensaje",
                                    tint =
                                            MaterialTheme.colorScheme.onBackground.copy(
                                                    alpha = 0.5f
                                            ),
                                    modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                                text = formatTimestamp(message.timestamp),
                                style = ChatTypography.timestampText(fontSize),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                    FormattedClickableText(
                            message = message.content,
                            style =
                                    ChatTypography.messageText(
                                            fontSize = fontSize,
                                            isOwn = isOwnMessage
                                    ),
                            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                            isMentioned = message.isMentioned,
                            onClickUrl = { url ->
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    context.showToast(R.string.url_open_error)
                                }
                            }
                    )
                    if (urls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                        )
                        Column {
                            urls.forEach { url ->
                                UrlPreviewCard(
                                        url = url,
                                        context = context,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNickMenu && nickMenuAnchor != null) {
        DropdownMenu(
                expanded = showNickMenu,
                onDismissRequest = { showNickMenu = false },
                offset = DpOffset(x = nickMenuAnchor!!.x.dp, y = nickMenuAnchor!!.y.dp),
                modifier = Modifier.widthIn(min = 200.dp)
        ) {
            val isBlocked by
                    chatViewModel.isUserBlocked(message.sender).collectAsState(initial = false)
            val isIgnored by chatViewModel.ignoredUsers.collectAsState(initial = emptySet())
            val isIgnoredGlobally = isIgnored.contains(message.sender)
            val currentChannel = chatViewModel.getCurrentConversation()?.name ?: ""
            val ignoredUsersInChannel by
                    chatViewModel
                            .ignoredUsersInChannel(currentChannel)
                            .collectAsState(initial = emptySet())
            val isIgnoredInChannel = ignoredUsersInChannel.contains(message.sender)

            DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Edit, "Mention") },
                    onClick = {
                        showNickMenu = false
                        onNickClick(message.sender)
                    },
                    text = { Text(stringResource(R.string.menu_mention)) }
            )

            DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Person, "Open Private Chat") },
                    onClick = {
                        showNickMenu = false

                        handleOpenPrivateChat(message.sender)
                    },
                    text = { Text(stringResource(R.string.menu_open_private)) },
                    enabled = !isBlocked
            )

            DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.AccountCircle, "View Profile") },
                    onClick = {
                        showNickMenu = false
                        handlePremiumAction {
                            navController.navigate(
                                    Routes.UserDetails.createUsernameRoute(message.sender)
                            )
                        }
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.see_profile))
                            if (!isPremiumUser) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Premium",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    enabled = !isBlocked
            )

            Divider()

            DropdownMenuItem(
                    leadingIcon = {
                        if (isBlocked) Icon(Icons.Default.Lock, "Unblock User")
                        else Icon(Icons.Default.Delete, "Block User")
                    },
                    onClick = {
                        showNickMenu = false
                        handlePremiumAction {
                            if (isBlocked) chatViewModel.unblockUser(message.sender)
                            else chatViewModel.blockUser(message.sender)
                        }
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                    if (isBlocked) stringResource(R.string.menu_unblock_user)
                                    else stringResource(R.string.menu_block_user)
                            )
                            if (!isPremiumUser) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Premium",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
            )

            DropdownMenuItem(
                    leadingIcon = {
                        if (isIgnoredGlobally) Icon(Icons.Default.Close, "Unignore User")
                        else Icon(Icons.Default.Lock, "Ignore User")
                    },
                    onClick = {
                        showNickMenu = false
                        handlePremiumAction {
                            if (isIgnoredGlobally) chatViewModel.unignoreUser(message.sender)
                            else chatViewModel.ignoreUser(message.sender)
                        }
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                    if (isIgnoredGlobally)
                                            stringResource(R.string.menu_unignore_user)
                                    else stringResource(R.string.menu_ignore_user)
                            )
                            if (!isPremiumUser) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Premium",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
            )

            DropdownMenuItem(
                    leadingIcon = {
                        if (isIgnoredInChannel)
                                Icon(Icons.Default.Delete, "Unignore User in Channel")
                        else Icon(Icons.Default.List, "Ignore User in Channel")
                    },
                    onClick = {
                        showNickMenu = false
                        handlePremiumAction {
                            if (isIgnoredInChannel)
                                    chatViewModel.unignoreUserInChannel(
                                            message.sender,
                                            currentChannel
                                    )
                            else chatViewModel.ignoreUserInChannel(message.sender, currentChannel)
                        }
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                    if (isIgnoredInChannel)
                                            stringResource(R.string.menu_unignore_channel)
                                    else stringResource(R.string.menu_ignore_channel)
                            )
                            if (!isPremiumUser) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Premium",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
            )

            Divider()
            DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Warning, "Report User") },
                    onClick = {
                        showNickMenu = false
                        showReportDialog = true
                    },
                    text = { Text(stringResource(R.string.menu_report_user)) }
            )
            DropdownMenuItem(
                    leadingIcon = { painterResource(id = R.drawable.content_copy) },
                    onClick = {
                        showNickMenu = false
                        copyMessage()
                    },
                    text = { Text(stringResource(R.string.menu_copy_message)) }
            )
        }
    }

    if (showAuthDialog) {
        AlertDialog(
                onDismissRequest = { showAuthDialog = false },
                title = { Text(stringResource(R.string.premium_dialog_title)) },
                text = { Text(stringResource(R.string.premium_feature_description)) },
                confirmButton = {
                    TextButton(
                            onClick = {
                                showAuthDialog = false
                                navController.navigate(Routes.Login.route)
                            }
                    ) { Text(stringResource(R.string.action_login)) }
                },
                dismissButton = {
                    Row {
                        TextButton(
                                onClick = {
                                    showAuthDialog = false
                                    navController.navigate(Routes.Registration.route)
                                }
                        ) { Text(stringResource(R.string.action_register)) }
                        if (isRewardedAdReady && activity != null) {
                            TextButton(
                                    onClick = {
                                        showAuthDialog = false
                                        adViewModel.adManager.showRewardedAd(
                                                activity,
                                                onRewarded = {
                                                    authViewModel.activateTemporaryPremium()
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                                message =
                                                                        context.getString(
                                                                                R.string
                                                                                        .premium_access_granted
                                                                        ),
                                                                duration = SnackbarDuration.Long
                                                        )
                                                    }
                                                    premiumFeatureRequested()
                                                },
                                                onAdClosed = {}
                                        )
                                    }
                            ) { Text(stringResource(R.string.watch_video)) }
                        }
                        TextButton(onClick = { showAuthDialog = false }) {
                            Text(stringResource(R.string.cancel_button))
                        }
                    }
                }
        )
    }

    if (showReportDialog) {
        AlertDialog(
                onDismissRequest = { showReportDialog = false },
                title = { Text(stringResource(R.string.report_dialog_title)) },
                text = {
                    Column {
                        Text(text = stringResource(R.string.report_dialog_message, message.sender))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                stringResource(R.string.report_email_warning)
                        ) // Nuevo string que debes añadir
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                                value = reportReason,
                                onValueChange = { reportReason = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(stringResource(R.string.report_hint)) },
                                singleLine = false,
                                maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                            onClick = {
                                if (reportReason.isNotBlank()) {
                                    chatViewModel.reportUser(
                                            userId = message.sender,
                                            reason = reportReason
                                    )
                                    showReportDialog = false
                                    reportReason = ""
                                }
                            },
                            enabled = reportReason.isNotBlank()
                    ) { Text(stringResource(R.string.report_button)) }
                },
                dismissButton = {
                    TextButton(
                            onClick = {
                                showReportDialog = false
                                reportReason = ""
                            }
                    ) { Text(stringResource(R.string.cancel_button)) }
                }
        )
    }

    val reportActionComplete by chatViewModel.reportActionComplete.collectAsState()
    LaunchedEffect(reportActionComplete) {
        if (reportActionComplete) {
            snackbarHostState.showSnackbar(
                    message = context.getString(R.string.report_success),
                    duration = SnackbarDuration.Long
            )
        }
    }
}

// COMPONENTE CHATINPUT MODIFICADO - CORREGIDO
@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChatInput(
        message: String,
        onMessageChange: (String) -> Unit,
        onSendMessage: (String) -> Unit,
        users: List<String>,
        modifier: Modifier = Modifier,
        disableColorCodes: Boolean = false
) {
    // Screen size detection
    val configuration = LocalConfiguration.current
    val windowSizeClass = calculateWindowSizeClass(configuration)
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Dynamic sizing based on screen size
    val inputMinHeight =
            when {
                isCompact && isLandscape -> 44.dp
                isCompact -> 56.dp
                else -> 64.dp
            }

    val inputMaxHeight =
            when {
                isCompact && isLandscape -> 80.dp
                isCompact -> 120.dp
                else -> 140.dp
            }

    val iconSize =
            when {
                isCompact && isLandscape -> 16.dp
                isCompact -> 20.dp
                else -> 24.dp
            }

    val buttonSize =
            when {
                isCompact && isLandscape -> 32.dp
                isCompact -> 40.dp
                else -> 48.dp
            }

    val horizontalPadding =
            when {
                screenWidth < 360.dp -> 8.dp
                screenWidth < 600.dp -> 16.dp
                screenWidth < 840.dp -> 24.dp
                else -> 32.dp
            }

    val cornerRadius =
            when {
                isCompact -> 16.dp
                else -> 20.dp
            }

    val suggestionMaxHeight =
            when {
                isCompact && isLandscape -> (screenHeight * 0.4f)
                isCompact -> (screenHeight * 0.3f)
                else -> 240.dp
            }

    // ESTADOS PRINCIPALES
    var textFieldValue by remember { mutableStateOf(TextFieldValue(message)) }
    var showNickSuggestions by remember { mutableStateOf(false) }
    var showFormatOptions by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    // FILTRAR USUARIOS CUANDO SE PRESIONE EL BOTÓN
    val userSuggestions =
            remember(textFieldValue.text, users, showNickSuggestions) {
                if (!showNickSuggestions) {
                    emptyList()
                } else {
                    val currentText = textFieldValue.text

                    if (currentText.isEmpty()) {
                        // Si no hay texto, mostrar todos los usuarios
                        users.sorted()
                    } else {
                        // Extraer las palabras para buscar coincidencias
                        val words = currentText.split(' ', ',', ':', ';')
                        val lastWord = words.lastOrNull()?.trim() ?: ""

                        // Si la última palabra está vacía, mostrar todos
                        if (lastWord.isEmpty()) {
                            users.sorted()
                        } else {
                            // Filtrar usuarios que contengan la última palabra
                            users
                                    .filter { user -> user.contains(lastWord, ignoreCase = true) }
                                    .sortedBy { user ->
                                        when {
                                            user.startsWith(lastWord, ignoreCase = true) -> 0
                                            else -> 1
                                        }
                                    }
                        }
                    }
                }
            }

    // Sincronización del texto
    LaunchedEffect(message) {
        if (message != textFieldValue.text) {
            textFieldValue = TextFieldValue(message, selection = TextRange(message.length))
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val inputBackgroundColor = remember { colorScheme.surface }

    // Helper functions
    fun insertFormatting(formatChar: Char) {
        val currentText = textFieldValue.text
        val selection = textFieldValue.selection
        textFieldValue =
                TextFieldValue(
                        text =
                                "${currentText.substring(0, selection.start)}$formatChar${
                currentText.substring(
                    selection.start
                )
            }",
                        selection = TextRange(selection.start + 1)
                )
        onMessageChange(textFieldValue.text)
    }

    fun resetMessageFormat() {
        val formatter = IRCMessageFormatter()
        val strippedText = formatter.stripAllFormats(textFieldValue.text)
        textFieldValue =
                TextFieldValue(
                        text = strippedText,
                        selection =
                                TextRange(min(strippedText.length, textFieldValue.selection.start))
                )
        onMessageChange(strippedText)
    }

    fun insertResetFormat() {
        if (disableColorCodes) return

        val currentText = textFieldValue.text
        val cursorPos = textFieldValue.selection.start

        textFieldValue =
                TextFieldValue(
                        text =
                                "${
                currentText.substring(
                    0,
                    cursorPos
                )
            }${IRCColors.RESET_CHAR}${currentText.substring(cursorPos)}",
                        selection = TextRange(cursorPos + 1)
                )
        onMessageChange(textFieldValue.text)
    }

    fun insertNickname(nickname: String) {
        val currentText = textFieldValue.text

        if (currentText.isEmpty()) {
            // Si no hay texto, simplemente insertar el nick
            val newText = "$nickname "
            textFieldValue = TextFieldValue(text = newText, selection = TextRange(newText.length))
            onMessageChange(newText)
        } else {
            // Buscar la última palabra para reemplazarla
            val words = currentText.split(' ', ',', ':', ';')
            val lastWord = words.lastOrNull()?.trim() ?: ""

            if (lastWord.isEmpty()) {
                // Si la última "palabra" está vacía, añadir al final
                val newText = "$currentText$nickname "
                textFieldValue =
                        TextFieldValue(text = newText, selection = TextRange(newText.length))
                onMessageChange(newText)
            } else {
                // Reemplazar la última palabra por el nick
                val lastIndex = currentText.lastIndexOf(lastWord)
                val textBeforeLastWord = currentText.substring(0, lastIndex)
                val newText = "$textBeforeLastWord$nickname "

                textFieldValue =
                        TextFieldValue(text = newText, selection = TextRange(newText.length))
                onMessageChange(newText)
            }
        }

        showNickSuggestions = false

        // Si solo hay una sugerencia, ocultar el diálogo automáticamente
        if (userSuggestions.size == 1) {
            showNickSuggestions = false
        }
    }

    fun toggleNickSuggestions() {
        if (showNickSuggestions) {
            showNickSuggestions = false
        } else {
            showFormatOptions = false // Cerrar menú de formato si está abierto
            showNickSuggestions = true

            // Si solo hay una sugerencia, autocompletar automáticamente
            val currentText = textFieldValue.text
            val words = currentText.split(' ', ',', ':', ';')
            val lastWord = words.lastOrNull()?.trim() ?: ""

            if (lastWord.isNotEmpty()) {
                val filtered = users.filter { user -> user.startsWith(lastWord, ignoreCase = true) }

                if (filtered.size == 1) {
                    // Autocompletar con la única coincidencia
                    insertNickname(filtered[0])
                    return
                }
            }
        }
    }

    fun sendMessage() {
        if (textFieldValue.text.isNotBlank()) {
            val messageToSend = textFieldValue.text
            textFieldValue = TextFieldValue("")
            onMessageChange("")
            onSendMessage(messageToSend)
            showNickSuggestions = false
            showFormatOptions = false
        }
    }

    // Cerrar sugerencias cuando se hace clic fuera
    val density = LocalDensity.current
    Box(
            modifier =
                    modifier.fillMaxWidth()
                            .heightIn(min = inputMinHeight)
                            .padding(horizontal = horizontalPadding)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                        onTap = { offset ->
                                            // Cerrar sugerencias si se toca fuera
                                            if (showNickSuggestions) {
                                                showNickSuggestions = false
                                            }
                                        }
                                )
                            }
    ) {
        Column {
            // SUGERENCIAS DE NICKS SOLO CUANDO SE PRESIONA EL BOTÓN
            AnimatedVisibility(
                    visible = showNickSuggestions,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut()
            ) {
                Card(
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(vertical = if (isCompact) 4.dp else 8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = suggestionMaxHeight)) {
                        // Encabezado con información del filtro
                        Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                            Modifier.fillMaxWidth()
                                                    .padding(
                                                            horizontal =
                                                                    if (isCompact) 8.dp else 12.dp,
                                                            vertical = if (isCompact) 4.dp else 6.dp
                                                    )
                            ) {
                                val currentText = textFieldValue.text
                                val words = currentText.split(' ', ',', ':', ';')
                                val lastWord = words.lastOrNull()?.trim() ?: ""

                                Text(
                                        if (lastWord.isEmpty()) {
                                            stringResource(R.string.all_users, users.size)
                                        } else {
                                            stringResource(
                                                    R.string.filtered_users,
                                                    lastWord,
                                                    userSuggestions.size
                                            )
                                        },
                                        style =
                                                if (isCompact) MaterialTheme.typography.labelSmall
                                                else MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                )

                                // Botón para cerrar
                                IconButton(
                                        onClick = { showNickSuggestions = false },
                                        modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Cerrar",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Lista de usuarios filtrados
                        LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            if (userSuggestions.isEmpty()) {
                                item {
                                    Text(
                                            text = stringResource(R.string.user_no_matches),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .padding(16.dp)
                                                            .clickable {
                                                                showNickSuggestions = false
                                                            }
                                    )
                                }
                            } else {
                                items(userSuggestions.take(50)) { user -> // Límite de 50 resultados
                                    Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .clickable { insertNickname(user) }
                                                            .padding(
                                                                    horizontal =
                                                                            if (isCompact) 8.dp
                                                                            else 12.dp,
                                                                    vertical =
                                                                            if (isCompact) 6.dp
                                                                            else 8.dp
                                                            )
                                    ) {
                                        Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                modifier =
                                                        Modifier.size(
                                                                if (isCompact) 14.dp else 16.dp
                                                        ),
                                                tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                                text = user,
                                                style =
                                                        if (isCompact)
                                                                MaterialTheme.typography.bodySmall
                                                        else MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // INPUT PRINCIPAL
            Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(cornerRadius),
                    color = inputBackgroundColor,
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                                Modifier.padding(
                                        horizontal = if (isCompact) 4.dp else 8.dp,
                                        vertical = if (isCompact) 4.dp else 6.dp
                                )
                ) {
                    // Botón de autocompletar nicks
                    InputActionButton(
                            icon = Icons.Default.Person,
                            contentDescription = "Autocompletar nick",
                            isActive = showNickSuggestions,
                            onClick = { toggleNickSuggestions() },
                            modifier = Modifier.size(buttonSize)
                    )

                    // Campo de texto
                    Box(modifier = Modifier.weight(1f).heightIn(min = inputMinHeight - 8.dp)) {
                        val textStyle =
                                when {
                                    isCompact && isLandscape -> MaterialTheme.typography.bodyMedium
                                    isCompact -> MaterialTheme.typography.bodyLarge
                                    else -> MaterialTheme.typography.bodyLarge
                                }

                        BasicTextField(
                                value = textFieldValue,
                                onValueChange = { newValue ->
                                    val cleanedText =
                                            if (disableColorCodes) {
                                                val formatter = IRCMessageFormatter()
                                                formatter.stripAllFormats(newValue.text)
                                            } else {
                                                newValue.text
                                            }

                                    textFieldValue =
                                            if (cleanedText != newValue.text) {
                                                newValue.copy(text = cleanedText)
                                            } else {
                                                newValue
                                            }
                                    onMessageChange(textFieldValue.text)
                                },
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .heightIn(
                                                        min = inputMinHeight - 8.dp,
                                                        max = inputMaxHeight
                                                )
                                                .verticalScroll(scrollState),
                                textStyle =
                                        textStyle.copy(
                                                color = colorScheme.onSurface,
                                                lineHeight = if (isCompact) 16.sp else 18.sp
                                        ),
                                decorationBox = { innerTextField ->
                                    Box(
                                            contentAlignment = Alignment.CenterStart,
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .padding(
                                                                    vertical =
                                                                            if (isCompact) 6.dp
                                                                            else 8.dp
                                                            )
                                    ) {
                                        if (textFieldValue.text.isEmpty()) {
                                            Text(
                                                    text = stringResource(R.string.message_hint),
                                                    style = textStyle,
                                                    color = colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                keyboardOptions =
                                        KeyboardOptions(
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Send,
                                                capitalization = KeyboardCapitalization.Sentences
                                        ),
                                keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                                singleLine = false,
                                maxLines = if (isCompact && isLandscape) 2 else 4,
                                cursorBrush = SolidColor(colorScheme.primary)
                        )
                    }

                    // Botón formato
                    if (!disableColorCodes) {
                        InputActionButton(
                                icon = painterResource(R.drawable.format_color_text),
                                contentDescription = "Opciones de formato",
                                isActive = showFormatOptions,
                                onClick = {
                                    showFormatOptions = !showFormatOptions
                                    showNickSuggestions =
                                            false // Cerrar sugerencias si estaban abiertas
                                },
                                modifier = Modifier.size(buttonSize)
                        )
                    }

                    // Botón enviar
                    AnimatedContent(
                            targetState = textFieldValue.text.isNotBlank(),
                            transitionSpec = {
                                (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                            },
                            label = "send_button_animation"
                    ) { hasText ->
                        InputActionButton(
                                icon = Icons.Rounded.Send,
                                contentDescription = "Enviar mensaje",
                                isActive = false,
                                enabled = hasText,
                                onClick = { sendMessage() },
                                modifier =
                                        Modifier.size(buttonSize)
                                                .background(
                                                        if (hasText) colorScheme.primary
                                                        else Color.Transparent,
                                                        CircleShape
                                                )
                        )
                    }
                }
            }

            // OPCIONES DE FORMATO SIMPLIFICADAS
            AnimatedVisibility(
                    visible = showFormatOptions,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
            ) {
                Surface(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(top = if (isCompact) 4.dp else 8.dp),
                        shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp),
                        color = colorScheme.surfaceContainer,
                        tonalElevation = 2.dp,
                        shadowElevation = 4.dp
                ) {
                    SimpleFormatOptionsDropdown(
                            showFormatOptions = showFormatOptions,
                            onDismiss = { showFormatOptions = false },
                            onInsertFormatting = { insertFormatting(it) },
                            onInsertReset = { insertResetFormat() },
                            onResetAll = { resetMessageFormat() },
                            isCompact = isCompact
                    )
                }
            }
        }
    }
}

@Composable
fun StatusConversationContent(
        conversation: Conversation,
        chatViewModel: IRCChatViewModel,
        modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current // <-- Necesario para FormattedClickableText

    // Efecto para hacer scroll al último mensaje
    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            listState.scrollToItem(conversation.messages.lastIndex)
        }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        items(conversation.messages) { message ->
            StatusMessageItem(
                    message = message,
                    chatViewModel = chatViewModel,
                    context = context, // <-- Pasar el contexto
                    modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
