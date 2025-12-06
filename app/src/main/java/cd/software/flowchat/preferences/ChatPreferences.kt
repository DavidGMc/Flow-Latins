package cd.software.flowchat.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore


class ChatPreferences(private val context: Context) {

    companion object {
        private val BLOCK_PRIVATE_MESSAGES = booleanPreferencesKey("block_private_messages_latins")
        private val BLOCKED_USERS = stringSetPreferencesKey("blocked_users_latins")
        private val IGNORED_USERS = stringSetPreferencesKey("ignored_users_latins")
        private val SHOW_JOIN_EVENTS = booleanPreferencesKey("show_join_events_latins")
        private val SHOW_QUIT_EVENTS = booleanPreferencesKey("show_quit_events_latins")
        private val SHOW_PART_EVENTS = booleanPreferencesKey("show_part_events_latins")
        private val SHOW_BAN_EVENTS = booleanPreferencesKey("show_ban_events_latins")
        private val FONT_SIZE = intPreferencesKey("font_size_latins")
        val COMPACT_MESSAGE_FORMAT = booleanPreferencesKey("compact_message_format_latins")


        fun IGNORED_USERS_IN_CHANNEL(channel: String) = stringSetPreferencesKey("ignored_users_$channel")
    }

    private val dataStore = context.chatSettingsDataStore
    val compactMessageFormat: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[COMPACT_MESSAGE_FORMAT] ?: false }

    suspend fun setCompactMessageFormat(enabled: Boolean) {
        dataStore.edit { settings ->
            settings[COMPACT_MESSAGE_FORMAT] = enabled
        }
    }
    // Métodos generales para gestionar preferencias
    suspend fun <T> setPreference(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    suspend fun setBooleanPreference(key: String, value: Boolean) {
        val prefKey = booleanPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }

    suspend fun setIntPreference(key: String, value: Int) {
        val prefKey = intPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }

    fun getBooleanPreference(key: String, defaultValue: Boolean): Flow<Boolean> {
        val prefKey = booleanPreferencesKey(key)
        return dataStore.data.map { it[prefKey] ?: defaultValue }
    }

    fun getIntPreference(key: String, defaultValue: Int): Flow<Int> {
        val prefKey = intPreferencesKey(key)
        return dataStore.data.map { it[prefKey] ?: defaultValue }
    }


    val blockPrivateMessages: Flow<Boolean> = dataStore.data.map {
        it[BLOCK_PRIVATE_MESSAGES] ?: false
    }

    suspend fun setBlockPrivateMessages(block: Boolean) {
        dataStore.edit { it[BLOCK_PRIVATE_MESSAGES] = block }
    }

    val blockedUsers: Flow<Set<String>> = dataStore.data.map {
        it[BLOCKED_USERS] ?: emptySet()
    }

    suspend fun blockUser(username: String) {
        dataStore.edit {
            it[BLOCKED_USERS] = (it[BLOCKED_USERS] ?: emptySet()) + username
        }
    }

    suspend fun unblockUser(username: String) {
        dataStore.edit {
            it[BLOCKED_USERS] = (it[BLOCKED_USERS] ?: emptySet()) - username
        }
    }

    fun isUserBlocked(username: String): Flow<Boolean> {
        return blockedUsers.map { it.contains(username) }
    }

    val ignoredUsers: Flow<Set<String>> = dataStore.data.map {
        it[IGNORED_USERS] ?: emptySet()
    }

    suspend fun ignoreUser(username: String) {
        dataStore.edit {
            it[IGNORED_USERS] = (it[IGNORED_USERS] ?: emptySet()) + username
        }
    }

    suspend fun unignoreUser(username: String) {
        dataStore.edit {
            it[IGNORED_USERS] = (it[IGNORED_USERS] ?: emptySet()) - username
        }
    }

    fun ignoredUsersInChannel(channel: String): Flow<Set<String>> {
        return dataStore.data.map {
            it[IGNORED_USERS_IN_CHANNEL(channel)] ?: emptySet()
        }
    }

    suspend fun ignoreUserInChannel(username: String, channel: String) {
        dataStore.edit {
            val key = IGNORED_USERS_IN_CHANNEL(channel)
            it[key] = (it[key] ?: emptySet()) + username
        }
    }

    suspend fun unignoreUserInChannel(username: String, channel: String) {
        dataStore.edit {
            val key = IGNORED_USERS_IN_CHANNEL(channel)
            it[key] = (it[key] ?: emptySet()) - username
        }
    }

    val allBlockedAndIgnoredUsers: Flow<Pair<Set<String>, Set<String>>> = dataStore.data.map {
        val blocked = it[BLOCKED_USERS] ?: emptySet()
        val ignored = it[IGNORED_USERS] ?: emptySet()
        blocked to ignored
    }
    // Nuevas preferencias
    val showJoinEvents: Flow<Boolean> = dataStore.data.map { it[SHOW_JOIN_EVENTS] ?: true }
    val showQuitEvents: Flow<Boolean> = dataStore.data.map { it[SHOW_QUIT_EVENTS] ?: false }
    val showPartEvents: Flow<Boolean> = dataStore.data.map { it[SHOW_PART_EVENTS] ?: false }
    val showBanEvents: Flow<Boolean> = dataStore.data.map { it[SHOW_BAN_EVENTS] ?: false }
    val fontSize: Flow<Int> = dataStore.data.map { it[FONT_SIZE] ?: 14 }
    suspend fun setShowJoinEvents(show: Boolean) {
        dataStore.edit { it[SHOW_JOIN_EVENTS] = show }
    }

    suspend fun setShowQuitEvents(show: Boolean) {
        dataStore.edit { it[SHOW_QUIT_EVENTS] = show }
    }

    suspend fun setShowPartEvents(show: Boolean) {
        dataStore.edit { it[SHOW_PART_EVENTS] = show }
    }

    suspend fun setShowBanEvents(show: Boolean) {
        dataStore.edit { it[SHOW_BAN_EVENTS] = show }
    }

    suspend fun setFontSize(size: Int) {
        dataStore.edit { it[FONT_SIZE] = size.coerceIn(10, 24) }
    }

}
