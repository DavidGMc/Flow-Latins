package cd.software.flowchat

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cd.software.flowchat.model.IRCServerConfig
import cd.software.flowchat.preferences.customServerDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class CustomServerManager(private val context: Context) {
    private val dataStore = context.customServerDataStore

    companion object {
        private val CUSTOM_SERVERS_KEY = stringPreferencesKey("custom_servers")
    }

    suspend fun saveCustomServer(server: IRCServerConfig) {
        dataStore.edit { preferences ->
            val existingServers = getCustomServers().toMutableList()
            val index = existingServers.indexOfFirst { it.url == server.url }
            val customServer = server.copy(isCustom = true)

            if (index != -1) {
                existingServers[index] = customServer
            } else {
                existingServers.add(customServer)
            }

            val jsonArray = JSONArray().apply {
                existingServers.forEach { serverConfig ->
                    put(JSONObject().apply {
                        put("name", serverConfig.name)
                        put("url", serverConfig.url)
                        put("port", serverConfig.port)
                        put("ssl", serverConfig.ssl)
                        put("realname", serverConfig.realname)
                        put("charset", serverConfig.charset)
                        put("isCustom", true)
                        put("autoJoinChannels", JSONArray(serverConfig.autoJoinChannels))
                    })
                }
            }

            preferences[CUSTOM_SERVERS_KEY] = jsonArray.toString()
        }
    }

    suspend fun getCustomServers(): List<IRCServerConfig> {
        return dataStore.data.map { preferences ->
            preferences[CUSTOM_SERVERS_KEY]?.let { jsonString ->
                try {
                    val jsonArray = JSONArray(jsonString)
                    val servers = mutableListOf<IRCServerConfig>()

                    for (i in 0 until jsonArray.length()) {
                        val serverJson = jsonArray.getJSONObject(i)
                        val channelsArray = serverJson.getJSONArray("autoJoinChannels")
                        val autoJoinChannels = List(channelsArray.length()) { j ->
                            channelsArray.getString(j)
                        }
                        servers.add(
                            IRCServerConfig(
                                name = serverJson.getString("name"),
                                url = serverJson.getString("url"),
                                port = serverJson.getInt("port"),
                                ssl = serverJson.getBoolean("ssl"),
                                realname = serverJson.getString("realname"),
                                charset = serverJson.optString("charset", "UTF-8"),
                                isCustom = serverJson.optBoolean("isCustom", false),
                                autoJoinChannels = autoJoinChannels
                            )
                        )
                    }
                    servers
                } catch (e: JSONException) {
                    emptyList()
                }
            } ?: emptyList()
        }.first()
    }

    suspend fun deleteCustomServer(serverToDelete: IRCServerConfig) {
        dataStore.edit { preferences ->
            val existingServers = getCustomServers().toMutableList()
            existingServers.removeAll { it.url == serverToDelete.url }

            val jsonArray = JSONArray().apply {
                existingServers.forEach { serverConfig ->
                    put(JSONObject().apply {
                        put("name", serverConfig.name)
                        put("url", serverConfig.url)
                        put("port", serverConfig.port)
                        put("ssl", serverConfig.ssl)
                        put("realname", serverConfig.realname)
                        put("charset", serverConfig.charset)
                        put("autoJoinChannels", JSONArray(serverConfig.autoJoinChannels))
                    })
                }
            }

            preferences[CUSTOM_SERVERS_KEY] = jsonArray.toString()
        }
    }
}