package cd.software.flowchat.model

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.json.JSONArray
import org.json.JSONException

object FirebaseRemoteConfigHelper {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private val defaults: Map<String, Any> = mapOf(
        "web_content_url" to "https://flowirc.com/contenidos"
    )

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(60) // segundos
            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(defaults)
    }

    fun fetchRemoteConfig(onComplete: () -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete()
                } else {
                    // Si hay error, igualmente llamamos a onComplete para usar valores por defecto
                    onComplete()
                }
            }
    }

    fun getString(key: String): String {
        return remoteConfig.getString(key)
    }

    fun getInt(key: String): Int {
        return remoteConfig.getLong(key).toInt()
    }
    fun getBoolean(key: String): Boolean {
        return remoteConfig.getBoolean(key)
    }

    fun getStringList(key: String): List<String> {
        val jsonString = remoteConfig.getString(key)
        val stringList = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                stringList.add(jsonArray.getString(i))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return stringList
    }


}