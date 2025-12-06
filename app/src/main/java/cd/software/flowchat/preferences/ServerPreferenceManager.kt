package cd.software.flowchat.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cd.software.flowchat.model.IRCServerConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ServerPreferenceManager(private val context: Context) {
    private val dataStore = context.serverDataStore

    companion object {
        private val SELECTED_SERVER_URL_KEY = stringPreferencesKey("selected_server_url")
        private val SAVED_NICKNAME_KEY = stringPreferencesKey("saved_nickname")
        private val SAVED_PASSWORD_KEY = stringPreferencesKey("saved_password")
        private val PRIVACY_POLICY_ACCEPTED_KEY = booleanPreferencesKey("privacy_policy_accepted")
    }

    suspend fun saveConnectionPreferences(serverUrl: String, nickname: String, password: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_SERVER_URL_KEY] = serverUrl
            preferences[SAVED_NICKNAME_KEY] = nickname
            if (password.isNotEmpty()) {
                preferences[SAVED_PASSWORD_KEY] = password
            }
        }
    }

    suspend fun savePrivacyPolicyAcceptance(accepted: Boolean) {
        dataStore.edit { it[PRIVACY_POLICY_ACCEPTED_KEY] = accepted }
    }

    suspend fun getPrivacyPolicyAcceptance(): Boolean {
        return dataStore.data.map { it[PRIVACY_POLICY_ACCEPTED_KEY] ?: false }.first()
    }

    suspend fun getConnectionPreferences(): ConnectionPreferences {
        return dataStore.data.map { preferences ->
            ConnectionPreferences(
                serverUrl = preferences[SELECTED_SERVER_URL_KEY],
                nickname = preferences[SAVED_NICKNAME_KEY] ?: "",
                password = preferences[SAVED_PASSWORD_KEY] ?: ""
            )
        }.first()
    }

    suspend fun getSelectedServer(availableServers: List<IRCServerConfig>): IRCServerConfig {
        val savedServerUrl = dataStore.data.map { it[SELECTED_SERVER_URL_KEY] }.first()
        return availableServers.find { it.url == savedServerUrl }
            ?: availableServers.firstOrNull()
            ?: throw IllegalStateException("No IRC servers available")
    }
}

data class ConnectionPreferences(
    val serverUrl: String?,
    val nickname: String,
    val password: String
)
