package cd.software.flowchat.preferences


import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Usar nombres únicos para cada archivo
val Context.serverDataStore by preferencesDataStore("irc_server_prefs")
val Context.customServerDataStore by preferencesDataStore("custom_servers_prefs")
val Context.chatSettingsDataStore by preferencesDataStore("chat_settings")
val Context.authDataStore by preferencesDataStore("auth_preferences")