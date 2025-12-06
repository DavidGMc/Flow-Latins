package cd.software.flowchat.firebase.model

import kotlinx.coroutines.flow.MutableStateFlow

sealed class TemporaryPremiumState {
    object Inactive : TemporaryPremiumState()
    data class Active(val expirationTime: Long) : TemporaryPremiumState()
}

