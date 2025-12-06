package cd.software.flowchat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cd.software.flowchat.viewmodel.IRCChatViewModel
import es.chat.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    chatViewModel: IRCChatViewModel,
    currentChannel: String,
    onNavigateBack: () -> Unit
) {
    val blockPrivateMessages by chatViewModel.blockPrivateMessages.collectAsState()
    val blockedUsers by chatViewModel.blockedUsers.collectAsState()
    val ignoredUsers by chatViewModel.ignoredUsers.collectAsState()
    val ignoredUsersInChannel by chatViewModel.ignoredUsersInChannel(currentChannel).collectAsState(initial = emptySet())

    val allBlockedAndIgnored by chatViewModel.allBlockedAndIgnoredUsers().collectAsState(initial = emptySet())
    val showJoinEvents by chatViewModel.showJoinEvents.collectAsState()
    val showQuitEvents by chatViewModel.showQuitEvents.collectAsState()
    val showPartEvents by chatViewModel.showPartEvents.collectAsState()
    val showBanEvents by chatViewModel.showBanEvents.collectAsState()
    val fontSize by chatViewModel.fontSize.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_desc)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sección de Eventos del Chat
            item { SectionTitle(stringResource(R.string.events_section)) }

            item {
                PreferenceSwitch(
                    title = stringResource(R.string.show_joins),
                    subtitle = stringResource(R.string.show_joins_desc),
                    checked = showJoinEvents,
                    onCheckedChange = { chatViewModel.setShowJoinEvents(it) }
                )
            }

            item {
                PreferenceSwitch(
                    title = stringResource(R.string.show_quits),
                    subtitle = stringResource(R.string.show_quits_desc),
                    checked = showQuitEvents,
                    onCheckedChange = { chatViewModel.setShowQuitEvents(it) }
                )
            }

            item {
                PreferenceSwitch(
                    title = stringResource(R.string.show_parts),
                    subtitle = stringResource(R.string.show_parts_desc),
                    checked = showPartEvents,
                    onCheckedChange = { chatViewModel.setShowPartEvents(it) }
                )
            }

            item {
                PreferenceSwitch(
                    title = stringResource(R.string.show_bans),
                    subtitle = stringResource(R.string.show_bans_desc),
                    checked = showBanEvents,
                    onCheckedChange = { chatViewModel.setShowBanEvents(it) }
                )
            }

            // Sección de Apariencia
            item { SectionTitle(stringResource(R.string.appearance_section)) }

            item {
                PreferenceSlider(
                    title = stringResource(R.string.font_size),
                    value = fontSize.toFloat(),
                    valueRange = 10f..24f,
                    steps = 7,
                    onValueChange = { chatViewModel.setFontSize(it.toInt()) },
                    valueDisplay = { context.getString(R.string.font_size_unit, it.toInt()) }
                )
            }

            // Opción para bloquear mensajes privados
            item {
                PreferenceSwitch(
                    title = stringResource(R.string.block_pms),
                    subtitle = stringResource(R.string.block_pms_desc),
                    checked = blockPrivateMessages,
                    onCheckedChange = { chatViewModel.setBlockPrivateMessages(it) }
                )
            }

            // Lista de usuarios bloqueados globalmente
            if (blockedUsers.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.blocked_users_section)) }
                items(blockedUsers.toList()) { username ->
                    BlockedUserItem(
                        username = username,
                        onUnblock = { chatViewModel.unblockUser(username) }
                    )
                }
            }

            // Lista de usuarios ignorados globalmente
            if (ignoredUsers.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.global_ignored_section)) }
                items(ignoredUsers.toList()) { username ->
                    BlockedUserItem(
                        username = username,
                        onUnblock = { chatViewModel.unignoreUser(username) }
                    )
                }
            }

            // Lista de usuarios ignorados en el canal actual
            if (ignoredUsersInChannel.isNotEmpty()) {
                item {
                    SectionTitle(stringResource(R.string.channel_ignored_section, currentChannel))
                }
                items(ignoredUsersInChannel.toList()) { username ->
                    BlockedUserItem(
                        username = username,
                        onUnblock = { chatViewModel.unignoreUserInChannel(username, currentChannel) }
                    )
                }
            }

            // Mostrar todos los bloqueados e ignorados combinados
            if (allBlockedAndIgnored.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.all_blocked_section)) }
                items(allBlockedAndIgnored.toList()) { username ->
                    Text(
                        text = username,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferenceSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun BlockedUserItem(
    username: String,
    onUnblock: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(username, modifier = Modifier.weight(1f))
        IconButton(onClick = onUnblock) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.unblock_user_desc)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun PreferenceSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    valueDisplay: (Float) -> String
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = valueDisplay(value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}