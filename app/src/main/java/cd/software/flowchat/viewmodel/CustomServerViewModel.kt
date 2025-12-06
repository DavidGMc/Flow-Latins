package cd.software.flowchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cd.software.flowchat.CustomServerManager
import cd.software.flowchat.model.IRCServerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomServerViewModel(
    private val customServerManager: CustomServerManager
) : ViewModel() {

    private val _customServers = MutableStateFlow<List<IRCServerConfig>>(emptyList())
    val customServers: StateFlow<List<IRCServerConfig>> = _customServers.asStateFlow()

    init {
        loadCustomServers()
    }

    private fun loadCustomServers() {
        viewModelScope.launch {
            _customServers.value = customServerManager.getCustomServers()
        }
    }

    fun saveCustomServer(server: IRCServerConfig) {
        viewModelScope.launch {
            customServerManager.saveCustomServer(server)
            loadCustomServers()
        }
    }
}