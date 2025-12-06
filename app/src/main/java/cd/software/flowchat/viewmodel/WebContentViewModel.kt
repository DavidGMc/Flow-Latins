package cd.software.flowchat.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cd.software.flowchat.model.FirebaseRemoteConfigHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class WebContentViewModel @Inject constructor(): ViewModel() {
    private val _contentUrl = MutableStateFlow<String?>(null)
    val contentUrl: StateFlow<String?> = _contentUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Añadido para el encabezado con texto HTML
    private val _headerText = MutableStateFlow<String?>(null)
    val headerText: StateFlow<String?> = _headerText.asStateFlow()

    // Claves para Firebase Remote Config
    private val CONTENT_URL_KEY = "flow_web_contenido_anuncios"
    private val HEADER_TEXT_KEY = "flow_text_anuncio"

    // Valores por defecto
    private val DEFAULT_URL = "https://flowirc.com/contenidos"
    private val DEFAULT_HEADER = "No Hay Anuncios en el momento"

    init {
        loadContent()
    }

    private fun loadContent() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                FirebaseRemoteConfigHelper.fetchRemoteConfig {
                    // Cargar URL
                    val remoteUrl = FirebaseRemoteConfigHelper.getString(CONTENT_URL_KEY)
                    _contentUrl.value = if (remoteUrl.isNotEmpty()) remoteUrl else DEFAULT_URL

                    // Cargar texto del encabezado
                    val remoteHeader = FirebaseRemoteConfigHelper.getString(HEADER_TEXT_KEY)
                    _headerText.value = if (remoteHeader.isNotEmpty()) remoteHeader else DEFAULT_HEADER

                    _isLoading.value = false
                }
            } catch (e: Exception) {
                // Si hay algún error, usar valores por defecto
                _contentUrl.value = DEFAULT_URL
                _headerText.value = DEFAULT_HEADER
                _isLoading.value = false
            }
        }
    }

    fun refreshUrl() {
        loadContent()
    }
}