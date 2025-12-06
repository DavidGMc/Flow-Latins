package cd.software.flowchat

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cd.software.flowchat.admob.AdViewModel
import cd.software.flowchat.firebase.model.AuthState
import cd.software.flowchat.firebase.viewmodel.AuthViewModel
import cd.software.flowchat.model.ChannelUser
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.viewmodel.IRCChatViewModel
import es.chat.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    chatViewModel: IRCChatViewModel,
    authViewModel: AuthViewModel,
    adViewModel: AdViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onOpenPrivateChat: (String) -> Unit,
    onMention: (String) -> Unit,
    onViewProfile: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var searchQuery by remember { mutableStateOf("") }
    val users by chatViewModel.usersInCurrentChannel.collectAsState()
    val currentChannel by chatViewModel.currentChannel.collectAsState()
    val blockedUsers by chatViewModel.blockedUsers.collectAsState()
    val ignoredUsers by chatViewModel.ignoredUsers.collectAsState()
    val ignoredUsersInChannel by chatViewModel.ignoredUsersInCurrentChannel.collectAsState()
    val isRewardedAdReady by adViewModel.isRewardedAdReady.collectAsState()

    val isPremiumUser = authViewModel.isPremiumUser()
    var showAuthDialog by remember { mutableStateOf(false) }
    var premiumFeatureRequested by remember { mutableStateOf<() -> Unit>({}) }
    val coroutineScope = rememberCoroutineScope()

    // Limpiar el estado del último chat cerrado cuando se entra a la lista de usuarios
    LaunchedEffect(Unit) {
        chatViewModel.clearLastClosedPrivateChat()
    }

    // Filtrar usuarios basado en la búsqueda
    val filteredUsers = users.filter {
        it.nickname.contains(searchQuery, ignoreCase = true)
    }

    fun handlePremiumAction(action: () -> Unit) {
        if (isPremiumUser) {
            action()
        } else {
            premiumFeatureRequested = action
            showAuthDialog = true
        }
    }


    // Diálogo para solicitar registro de usuario para funcionalidades premium
    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = { Text(stringResource(R.string.premium_dialog_title)) },
            text = {
                Text(stringResource(R.string.premium_feature_description))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAuthDialog = false
                        navController.navigate(Routes.Login.route)
                    }
                ) {
                    Text(stringResource(R.string.action_login))
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showAuthDialog = false
                            navController.navigate(Routes.Registration.route)
                        }
                    ) {
                        Text(stringResource(R.string.register_button))
                    }
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
                                                message = context.getString(R.string.premium_access_granted),
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                        premiumFeatureRequested()
                                    },
                                    onAdClosed = {}
                                )
                            }
                        ) {
                            Text(stringResource(R.string.watch_video))
                        }
                    }
                    TextButton(
                        onClick = { showAuthDialog = false }
                    ) {
                        Text(stringResource(R.string.cancel_button))
                    }
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (currentChannel.isNotEmpty())
                            stringResource(R.string.users_in_channel_name, currentChannel)
                        else
                            stringResource(R.string.users_in_channel)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_users)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            LazyColumn {
                items(filteredUsers) { user ->
                    UserListItem(
                        user = user,
                        onOpenPrivateChat = { username ->
                            chatViewModel.startPrivateConversationFromUsers(username) // Nuevo método
                            navController.navigateUp()
                        },
                        onMention = onMention,
                        onViewProfile = { username ->
                            handlePremiumAction {
                                navController.navigate(Routes.UserDetails.createUsernameRoute(username))
                            }
                        },
                        isBlocked = blockedUsers.contains(user.nickname),
                        isIgnoredGlobally = ignoredUsers.contains(user.nickname),
                        isIgnoredInChannel = ignoredUsersInChannel.contains(user.nickname),
                        currentChannel = currentChannel,
                        onBlockUser = {
                            handlePremiumAction {
                                val username = user.nickname
                                if (blockedUsers.contains(username)) {
                                    chatViewModel.unblockUser(username)
                                } else {
                                    chatViewModel.blockUser(username)
                                }
                            }
                        },
                        onIgnoreGlobally = {
                            handlePremiumAction {
                                val username = user.nickname
                                if (ignoredUsers.contains(username)) {
                                    chatViewModel.unignoreUser(username)
                                } else {
                                    chatViewModel.ignoreUser(username)
                                }
                            }
                        },
                        onIgnoreInChannel = {
                            handlePremiumAction {
                                val username = user.nickname
                                if (ignoredUsersInChannel.contains(username)) {
                                    chatViewModel.unignoreUserInChannel(username, currentChannel)
                                } else {
                                    chatViewModel.ignoreUserInChannel(username, currentChannel)
                                }
                            }
                        },
                        onReportUser = { username, reason ->
                            chatViewModel.reportUser(
                                userId = username,
                                reason = reason
                            )
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.user_reported_successfully),
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        isPremiumUser = isPremiumUser,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}


@Composable
fun UserListItem(
    user: ChannelUser,
    onOpenPrivateChat: (String) -> Unit,
    onMention: (String) -> Unit,
    onViewProfile: (String) -> Unit,
    isBlocked: Boolean,
    isIgnoredGlobally: Boolean,
    isIgnoredInChannel: Boolean,
    currentChannel: String,
    onBlockUser: () -> Unit,
    onIgnoreGlobally: () -> Unit,
    onIgnoreInChannel: () -> Unit,
    onReportUser: (String, String) -> Unit,
    isPremiumUser: Boolean,
    snackbarHostState: SnackbarHostState
) {
    var expanded by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }
    val username = user.nickname
    val prefixString = user.getPrefixString()

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(stringResource(R.string.report_dialog_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.report_dialog_message, username))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.report_email_warning))
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
                            onReportUser(username, reportReason)
                            showReportDialog = false
                            reportReason = ""
                        }
                    },
                    enabled = reportReason.isNotBlank()
                ) {
                    Text(stringResource(R.string.report_button))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showReportDialog = false
                    reportReason = ""
                }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.padding(end = 8.dp)) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                tint = when {
                    isIgnoredGlobally -> Color.Red
                    isBlocked -> Color.Blue
                    isIgnoredInChannel -> Color.Yellow
                    else -> LocalContentColor.current
                }
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (prefixString.isNotEmpty()) {
                    Text(
                        text = prefixString,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (isIgnoredGlobally || isBlocked || isIgnoredInChannel) {
                val message = when {
                    isIgnoredGlobally -> stringResource(R.string.ignored_globally)
                    isBlocked -> stringResource(R.string.blocked)
                    else -> stringResource(R.string.ignored_in_channel)
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_mention)) },
                onClick = {
                    expanded = false
                    onMention(username)
                },
                leadingIcon = { Icon(Icons.Default.Edit, null) }
            )

            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_open_private)) },
                onClick = {
                    expanded = false
                    onOpenPrivateChat(username)
                },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                enabled = !isBlocked
            )

            DropdownMenuItem(
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
                onClick = {
                    expanded = false
                    onViewProfile(username)
                },
                leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                enabled = !isBlocked
            )

            Divider()

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isBlocked) stringResource(R.string.menu_unblock_user) else stringResource(R.string.menu_block_user))
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
                onClick = {
                    expanded = false
                    onBlockUser()
                },
                leadingIcon = {
                    Icon(
                        if (isBlocked) Icons.Default.Lock else Icons.Default.Delete,
                        null
                    )
                }
            )

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isIgnoredGlobally) stringResource(R.string.menu_unignore_user) else stringResource(R.string.menu_ignore_user))
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
                onClick = {
                    expanded = false
                    onIgnoreGlobally()
                },
                leadingIcon = {
                    Icon(
                        if (isIgnoredGlobally) Icons.Default.Close else Icons.Default.Lock,
                        null
                    )
                }
            )

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val label = if (isIgnoredInChannel) {
                            stringResource(R.string.unignore_in_channel, currentChannel)
                        } else {
                            stringResource(R.string.ignore_in_channel, currentChannel)
                        }
                        Text(label)
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
                onClick = {
                    expanded = false
                    onIgnoreInChannel()
                },
                leadingIcon = {
                    Icon(
                        if (isIgnoredInChannel) Icons.Default.Delete else Icons.Default.List,
                        null
                    )
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(R.string.report_dialog_title)) },
                onClick = {
                    expanded = false
                    showReportDialog = true
                },
                leadingIcon = { Icon(Icons.Default.Warning, null) }
            )
        }
    }
}