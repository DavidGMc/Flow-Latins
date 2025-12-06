package cd.software.flowchat.admob

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class AdViewModel @Inject constructor(
    val adManager: AdManager
) : ViewModel() {

    // Estado para el consentimiento de anuncios
    private val _canShowAds = MutableStateFlow(false)

    val canShowAds: StateFlow<Boolean> = _canShowAds
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    // Método para actualizar el estado de consentimiento
    fun setCanShowAds(canShow: Boolean) {
        _canShowAds.value = canShow
    }

    val isRewardedAdReady: StateFlow<Boolean> = adManager.isRewardedAdReady
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    val isInterstitialAdReady: StateFlow<Boolean> = adManager.isInterstitialAdReady
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    // Método para rastrear la interacción del usuario
    fun trackUserInteraction() {
        adManager.trackInteractionForAds()
    }

    // Contador específico para interacciones del menú
    private val _menuInteractionsCount = MutableStateFlow(0)
    val menuInteractionsCount: StateFlow<Int> = _menuInteractionsCount

    fun incrementMenuInteractionsCount() {
        _menuInteractionsCount.value++
    }

    fun shouldShowAdAfterMenuInteractions(): Boolean {
        return _menuInteractionsCount.value % 5 == 0 && _menuInteractionsCount.value > 0
    }

    // Método para mostrar anuncio después de interacciones de menú
    fun showAdAfterMenuInteractions(activity: Activity, onAdClosed: () -> Unit) {
        if (shouldShowAdAfterMenuInteractions() && isInterstitialAdReady.value && canShowAds.value) {
            adManager.showInterstitial(activity, onAdClosed)
        } else {
            onAdClosed()
        }
    }
}