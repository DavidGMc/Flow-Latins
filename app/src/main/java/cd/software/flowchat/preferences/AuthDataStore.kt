package cd.software.flowchat.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Claves para almacenar datos
    companion object {
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val EMAIL_KEY = stringPreferencesKey("email")
    }

    // DataStore para autenticación
    private val dataStore: DataStore<Preferences> = context.authDataStore

    // Guardar ID de usuario
    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    // Guardar correo electrónico
    suspend fun saveEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = email
        }
    }

    // Obtener ID de usuario como Flow
    val userIdFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    // Obtener correo electrónico como Flow
    val emailFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[EMAIL_KEY]
    }

    // Limpiar datos de autenticación
    suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(EMAIL_KEY)
        }
    }
}
