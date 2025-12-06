package cd.software.flowchat.admob

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobManager @Inject constructor() : AdManager {
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private val _isRewardedAdReady = MutableStateFlow(false)
    override val isRewardedAdReady: StateFlow<Boolean> = _isRewardedAdReady

    private val _isInterstitialAdReady = MutableStateFlow(false)
    override val isInterstitialAdReady: StateFlow<Boolean> = _isInterstitialAdReady

    private var interactionCount = 0
    private val interactionThreshold = 5 // Mostrar anuncio cada 5 interacciones

    // Modificado: añadir estado de consentimiento más granular
    private var consentStatus = ConsentStatus.UNKNOWN

    // Enum para manejar estados de consentimiento más detallados
    enum class ConsentStatus {
        UNKNOWN,        // Estado inicial, no sabemos
        PERSONALIZED,   // Consentimiento completo para anuncios personalizados
        NON_PERSONALIZED, // Rechazó consentimiento, pero podemos mostrar anuncios no personalizados
        NO_ADS          // No debemos mostrar ningún anuncio
    }

    // Método actualizado para manejar diferentes estados de consentimiento
    fun updateConsent(status: ConsentStatus, context: Context) {
        this.consentStatus = status
        if (status != ConsentStatus.NO_ADS) {
            initialize(context) // Cargar anuncios si no es NO_ADS
        }
    }

    // Método conveniente para actualizar desde boolean
    fun updateConsentFromBoolean(hasConsent: Boolean, context: Context) {
        updateConsent(
            if (hasConsent) ConsentStatus.PERSONALIZED else ConsentStatus.NON_PERSONALIZED,
            context
        )
    }

    companion object {
        private const val INTERSTITIAL_AD_ID = "ca-app-pub-2196269772372009/7276647340" // ID de prueba
        private const val REWARDED_AD_ID = "ca-app-pub-2196269772372009/1330412433" // ID de prueba
    }

    override fun initialize(context: Context) {
        if (consentStatus != ConsentStatus.NO_ADS) {
            MobileAds.initialize(context) {
                loadInterstitialAd(context)
                loadRewardedAd(context)
            }
        }
    }

    override fun trackInteractionForAds() {
        interactionCount++
    }

    override fun showInterstitial(activity: Activity, onAdClosed: () -> Unit) {
        if (consentStatus == ConsentStatus.NO_ADS) {
            onAdClosed()
            return
        }

        if (interstitialAd != null) {
            _isInterstitialAdReady.value = false
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onAdClosed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            loadInterstitialAd(activity)
            onAdClosed()
        }
    }

    override fun showRewardedAd(activity: Activity, onRewarded: (RewardItem) -> Unit, onAdClosed: () -> Unit) {
        if (consentStatus == ConsentStatus.NO_ADS) {
            onAdClosed()
            return
        }

        if (rewardedAd != null) {
            _isRewardedAdReady.value = false
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(activity)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewardedAd(activity)
                    onAdClosed()
                }
            }
            rewardedAd?.show(activity) { reward ->
                onRewarded(reward)
            }
        } else {
            loadRewardedAd(activity)
            onAdClosed()
        }
    }

    // Método modificado para crear solicitudes de anuncios según el estado de consentimiento
    private fun buildAdRequest(): AdRequest {
        val builder = AdRequest.Builder()

        // Si el usuario rechazó el consentimiento pero podemos mostrar anuncios no personalizados
        if (consentStatus == ConsentStatus.NON_PERSONALIZED) {
            Bundle().apply {
                putString("npa", "1")  // non-personalized ads
                builder.addNetworkExtrasBundle(AdMobAdapter::class.java, this)
            }
            android.util.Log.d("AdMobManager", "Solicitando anuncios NO personalizados")
        } else {
            android.util.Log.d("AdMobManager", "Solicitando anuncios personalizados")
        }

        return builder.build()
    }

    private fun loadInterstitialAd(context: Context) {
        val adRequest = buildAdRequest()  // Usar el método modificado
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    _isInterstitialAdReady.value = true
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    _isInterstitialAdReady.value = false
                    context.postDelayedAdLoad { loadInterstitialAd(context) }
                }
            }
        )
    }

    private fun loadRewardedAd(context: Context) {
        val adRequest = buildAdRequest()  // Usar el método modificado
        RewardedAd.load(
            context,
            REWARDED_AD_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _isRewardedAdReady.value = true
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    _isRewardedAdReady.value = false
                    context.postDelayedAdLoad { loadRewardedAd(context) }
                }
            }
        )
    }

    private fun Context.postDelayedAdLoad(loadAction: () -> Unit) {
        android.os.Handler(mainLooper).postDelayed({
            loadAction()
        }, 60000) // Reintenta después de 1 minuto
    }

    fun showInterstitialAfterInteractions(activity: Activity, onAdClosed: () -> Unit = {}) {
        if (interactionCount >= interactionThreshold) {
            interactionCount = 0
            showInterstitial(activity, onAdClosed)
        } else {
            onAdClosed()
        }
    }
}
