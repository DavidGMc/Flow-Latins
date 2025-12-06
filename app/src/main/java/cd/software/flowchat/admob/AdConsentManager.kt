package cd.software.flowchat.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.*
import com.google.firebase.BuildConfig

/**
 * Gestor de consentimiento que cumple con el TCF de IAB para AdMob, AdSense y Ad Manager
 * en el EEE, Reino Unido y Suiza.
 */
class AdConsentManager(private val context: Context) {

    private lateinit var consentInformation: ConsentInformation

    fun initialize(activity: Activity, callback: (Boolean, Boolean) -> Unit) {
        consentInformation = UserMessagingPlatform.getConsentInformation(context)

        // PARA PRODUCCIÓN: Eliminar o comentar esta sección de debug
       /*
        val debugSettings = ConsentDebugSettings.Builder(context)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("287EF79DD9AFA1F712F0717044D3ADE6")
            .build() */


        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
           // .setConsentDebugSettings(debugSettings) // Comentar en producción
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                if (consentInformation.isConsentFormAvailable) {
                    if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                        loadAndShowConsentForm(activity, callback)
                    } else {
                        // No se requiere consentimiento (fuera de Europa)
                        android.util.Log.d("ConsentManager", "No se requiere consentimiento en esta región")
                        callback(true, false) // Personalizado: true, No-EU: false
                    }
                } else {
                    // No se requiere formulario
                    android.util.Log.d("ConsentManager", "No hay formulario disponible, se permiten anuncios")
                    callback(true, false) // Personalizado: true, No-EU: false
                }
            },
            { formError ->
                android.util.Log.e("ConsentManager", "Error al obtener consentimiento: ${formError.message}")
                callback(true, false) // En caso de error, permitir anuncios
            }
        )
    }

    private fun loadAndShowConsentForm(activity: Activity, callback: (Boolean, Boolean) -> Unit) {
        UserMessagingPlatform.loadConsentForm(
            context,
            { consentForm ->
                showConsentForm(activity, consentForm, callback)
            },
            { formError ->
                android.util.Log.e("ConsentManager", "Error al cargar formulario: ${formError.message}")
                callback(true, false) // En caso de error, permitir anuncios
            }
        )
    }

    private fun showConsentForm(activity: Activity, consentForm: ConsentForm, callback: (Boolean, Boolean) -> Unit) {
        consentForm.show(activity) { formError ->
            if (formError != null) {
                android.util.Log.e("ConsentManager", "Error al mostrar formulario: ${formError.message}")
                callback(false, true) // Sin consentimiento pero estamos en EU
            } else {
                // Verificar nivel de consentimiento
                val canRequestAds = consentInformation.canRequestAds()
                val privacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus ==
                        ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

                // Si estamos en EU, el segundo parámetro es true
                android.util.Log.d("ConsentManager",
                    "Resultado del consentimiento: anuncios personalizados = $canRequestAds, en EU = $privacyOptionsRequired")
                callback(canRequestAds, privacyOptionsRequired)
            }
        }
    }

    fun canShowPersonalizedAds(): Boolean {
        return consentInformation.canRequestAds()
    }

    fun reset() {
        consentInformation.reset()
    }
}