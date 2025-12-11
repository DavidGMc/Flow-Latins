package cd.software.flowchat.firebase.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cd.software.flowchat.firebase.model.AuthState
import cd.software.flowchat.firebase.model.CommunityState
import cd.software.flowchat.firebase.model.DeleteAccountState
import cd.software.flowchat.firebase.model.ProfileState
import cd.software.flowchat.firebase.model.ResetPasswordState
import cd.software.flowchat.firebase.model.UserProfile
import cd.software.flowchat.firebase.model.UserProfileState
import cd.software.flowchat.firebase.repository.AuthRepository
import cd.software.flowchat.firebase.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import cd.software.flowchat.firebase.model.TemporaryPremiumState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val ircService: cd.software.flowchat.IRCService
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState = _profileState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Initial)
    val resetPasswordState = _resetPasswordState.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Initial)
    val deleteAccountState = _deleteAccountState.asStateFlow()

    private val _communityState = MutableStateFlow(CommunityState())
    val communityState = _communityState.asStateFlow()

    val _userProfileState = MutableStateFlow(UserProfileState())
    val userProfileState = _userProfileState.asStateFlow()
    private var searchJob: Job? = null

    private val _profilesViewedCount = mutableStateOf(0)
    val profilesViewedCount: State<Int> = _profilesViewedCount

    private val _temporaryPremiumState = MutableStateFlow<TemporaryPremiumState>(TemporaryPremiumState.Inactive)
    val temporaryPremiumState = _temporaryPremiumState.asStateFlow()

    // Flujos para observar los datos del usuario desde DataStore
    val currentUserIdFlow = authRepository.currentUserIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val currentUserEmailFlow = authRepository.currentUserEmailFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // Verificar el estado de autenticación al iniciar el ViewModel
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            // Verifica si el usuario está autenticado tanto en Firebase como en DataStore
            val isAuthenticated = authRepository.checkAuthStatus()
            if (isAuthenticated) {
                // Si está autenticado, carga el perfil del usuario
                authRepository.getCurrentUserIdFromDataStore()?.let { userId ->
                    loadProfile(userId)
                }
            }
        }
    }

    fun incrementProfilesViewedCount() {
        _profilesViewedCount.value++
    }

    companion object {
        private const val DEFAULT_PROFILE_IMAGE = "https://firebasestorage.googleapis.com/v0/b/chateapp-latino.appspot.com/o/Default%20Users%2FLogo%20Flow%20Users%20Default.png?alt=media&token=b711d4b7-e499-4f00-9864-21706cc62eb5"
        private const val SEARCH_DEBOUNCE_TIME = 300L
        // O podrías tener varias opciones de avatares por defecto
        private val DEFAULT_AVATARS = listOf(
            "https://firebasestorage.googleapis.com/your-storage-path/avatar1.png",
            "https://firebasestorage.googleapis.com/your-storage-path/avatar2.png",
            "https://firebasestorage.googleapis.com/your-storage-path/avatar3.png"
        )
    }

    fun signUp(
        email: String,
        password: String,
        profile: UserProfile,
        profileImageUri: Uri? = null
    ) = viewModelScope.launch {
        _authState.value = AuthState.Loading

        // Primero verificamos si el nombre ya está en uso
        profileRepository.isUsernameTaken(profile.name).fold(
            onSuccess = { isTaken ->
                if (isTaken) {
                    // Si el nombre ya está en uso, mostramos un error y cancelamos el registro
                    _authState.value = AuthState.Error("El nombre '${profile.name}' ya está en uso. Por favor, elige otro nombre.")
                    return@fold
                }

                // Si el nombre está disponible, procedemos con el registro
                proceedWithSignUp(email, password, profile, profileImageUri)
            },
            onFailure = { exception ->
                // Si hay un error al verificar, informamos al usuario
                _authState.value = AuthState.Error("Error al verificar disponibilidad del nombre: ${exception.message}")
            }
        )
    }


    private fun proceedWithSignUp(
        email: String,
        password: String,
        profile: UserProfile,
        profileImageUri: Uri? = null
    ) = viewModelScope.launch {
        val authResult = authRepository.signUp(email, password)
        if (authResult.success && authResult.userId != null) {
            // Manejar la imagen de perfil: subir si existe, o usar imagen por defecto
            val profileImageUrl = when {
                profileImageUri != null -> {
                    // Subir la imagen seleccionada
                    profileRepository.uploadProfileImage(profileImageUri).fold(
                        onSuccess = { imageUrl -> imageUrl },
                        onFailure = { e ->
                            _authState.value = AuthState.Error("Error subiendo imagen: ${e.message}")
                            return@launch
                        }
                    )
                }
                profile.profileImageUrl.isNotBlank() -> profile.profileImageUrl
                else -> DEFAULT_PROFILE_IMAGE
            }

            val profileWithImage = profile.copy(
                userId = authResult.userId,
                profileImageUrl = profileImageUrl
            )

            profileRepository.createProfile(profileWithImage).fold(
                onSuccess = {
                    _authState.value = AuthState.Success
                    _profileState.value = ProfileState.Success(profileWithImage)
                    
                    // Registrar automáticamente en el servidor IRC con NickServ
                    // Solo si el usuario está conectado al servidor IRC
                    if (ircService.isConnected()) {
                        try {
                            // Enviar comando REGISTER a NickServ con la contraseña y email
                            ircService.sendNickServCommand("REGISTER $password $email")
                        } catch (e: Exception) {
                            // Si falla el registro en IRC, no afecta el registro en Firebase
                            // que ya fue exitoso
                        }
                    }
                },
                onFailure = { e ->
                    _authState.value = AuthState.Error(e.message ?: "Error creating profile")
                }
            )
        } else {
            _authState.value = AuthState.Error(authResult.error ?: "Sign up failed")
        }
    }

    fun signIn(email: String, password: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading

        val authResult = authRepository.signIn(email, password)
        if (authResult.success && authResult.userId != null) {
            // El userId ahora se almacena en DataStore automáticamente en el AuthRepository
            loadProfile(authResult.userId)
        } else {
            _authState.value = AuthState.Error(authResult.error ?: "Sign in failed")
        }
    }

    private fun loadProfile(userId: String) = viewModelScope.launch {
        profileRepository.getProfile(userId).fold(
            onSuccess = { profile ->
                _authState.value = AuthState.Success
                _profileState.value = ProfileState.Success(profile)
            },
            onFailure = { e ->
                _authState.value = AuthState.Error(e.message ?: "Error loading profile")
            }
        )
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        _resetPasswordState.value = ResetPasswordState.Loading

        val authResult = authRepository.resetPassword(email)
        if (authResult.success) {
            _resetPasswordState.value = ResetPasswordState.Success
        } else {
            _resetPasswordState.value = ResetPasswordState.Error(authResult.error ?: "Unknown error")
        }
    }

    fun updateProfile(profile: UserProfile, imageUri: Uri? = null) = viewModelScope.launch {
        _profileState.value = ProfileState.Loading

        // Si no hay nueva imagen y el perfil no tiene imagen, usar la imagen por defecto
        val imageToUse = when {
            imageUri != null -> {
                profileRepository.uploadProfileImage(imageUri).fold(
                    onSuccess = { imageUrl -> imageUrl },
                    onFailure = { e ->
                        _profileState.value = ProfileState.Error(e.message ?: "Error uploading image")
                        return@launch
                    }
                )
            }
            profile.profileImageUrl.isBlank() -> DEFAULT_PROFILE_IMAGE
            else -> profile.profileImageUrl
        }

        val updatedProfile = profile.copy(profileImageUrl = imageToUse)

        profileRepository.updateProfile(updatedProfile).fold(
            onSuccess = {
                _profileState.value = ProfileState.Success(updatedProfile)
            },
            onFailure = { e ->
                _profileState.value = ProfileState.Error(e.message ?: "Error updating profile")
            }
        )
    }

    fun deleteAccount() = viewModelScope.launch {
        _deleteAccountState.value = DeleteAccountState.Loading

        // First, get the current user profile for cleanup
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _deleteAccountState.value = DeleteAccountState.Error("No user logged in")
            return@launch
        }

        try {
            // Delete the account from Firebase Auth
            authRepository.deleteAccount().fold(
                onSuccess = {
                    _deleteAccountState.value = DeleteAccountState.Success
                    // Reset states after successful deletion
                    _authState.value = AuthState.Initial
                    _profileState.value = ProfileState.Initial
                },
                onFailure = { e ->
                    _deleteAccountState.value = DeleteAccountState.Error(
                        e.message ?: "Failed to delete account"
                    )
                }
            )
        } catch (e: Exception) {
            _deleteAccountState.value = DeleteAccountState.Error(
                e.message ?: "An unexpected error occurred"
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                // Cerrar sesión en Firebase y limpiar DataStore
                authRepository.signOut()

                // Limpiar datos de sesión en los estados del ViewModel
                _authState.value = AuthState.Initial
                _profileState.value = ProfileState.Initial
                // Cualquier otra limpieza necesaria
            } catch (e: Exception) {
                // Manejar errores si es necesario
            }
        }
    }

    fun loadCommunityProfiles() {
        viewModelScope.launch {
            _communityState.update { it.copy(isLoading = true) }

            profileRepository.getAllProfiles()
                .onSuccess { profiles ->
                    _communityState.update { state ->
                        state.copy(
                            profiles = profiles,
                            // Mantén los filtros si hay una búsqueda activa
                            filteredProfiles = if (state.searchQuery.isBlank()) profiles
                            else filterProfiles(profiles, state.searchQuery),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _communityState.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Error loading profiles"
                    ) }
                }
        }
    }

    fun searchProfiles(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val trimmedQuery = query.trim()
            _communityState.update { it.copy(searchQuery = trimmedQuery) }

            delay(SEARCH_DEBOUNCE_TIME)

            // Si la consulta está vacía, mostrar todos los perfiles
            if (trimmedQuery.isBlank()) {
                _communityState.update { it.copy(
                    filteredProfiles = it.profiles,
                    isLoading = false
                ) }
                return@launch
            }

            _communityState.update { it.copy(isLoading = true) }

            // En lugar de hacer una nueva consulta a la base de datos,
            // filtramos los perfiles que ya tenemos en memoria
            val filteredProfiles = _communityState.value.profiles.filter { profile ->
                profile.name.contains(trimmedQuery, ignoreCase = true) ||
                        (profile.country?.contains(trimmedQuery, ignoreCase = true) == true)
            }

            _communityState.update { it.copy(
                filteredProfiles = filteredProfiles,
                isLoading = false,
                error = null
            ) }
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _userProfileState.update { it.copy(isLoading = true) }

            val currentUserId = authRepository.getCurrentUserId()

            profileRepository.getProfileByUserId(userId)
                .onSuccess { profile ->
                    _userProfileState.update {
                        it.copy(
                            profile = profile,
                            isLoading = false,
                            error = null,
                            isCurrentUser = userId == currentUserId
                        )
                    }
                }
                .onFailure { exception ->
                    _userProfileState.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Error loading user profile"
                    ) }
                }
        }
    }

    private fun filterProfiles(profiles: List<UserProfile>, query: String): List<UserProfile> {
        val trimmedQuery = query.trim().lowercase()
        return profiles.filter { profile ->
            profile.name.lowercase().contains(trimmedQuery) ||
                    (profile.country?.lowercase()?.contains(trimmedQuery) == true)
        }
    }

    fun activateTemporaryPremium(durationMinutes: Long = 60) {
        val expirationTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000)
        _temporaryPremiumState.value = TemporaryPremiumState.Active(expirationTime)
    }

    // Method to check if temporary premium is currently active
    fun isTemporaryPremiumActive(): Boolean {
        val currentState = _temporaryPremiumState.value
        return when (currentState) {
            is TemporaryPremiumState.Active -> {
                // Check if the current time is before the expiration time
                val isActive = System.currentTimeMillis() < currentState.expirationTime
                if (!isActive) {
                    // Automatically deactivate if expired
                    _temporaryPremiumState.value = TemporaryPremiumState.Inactive
                }
                isActive
            }
            is TemporaryPremiumState.Inactive -> false
        }
    }

    // Method to deactivate temporary premium
    fun deactivateTemporaryPremium() {
        _temporaryPremiumState.value = TemporaryPremiumState.Inactive
    }

    // Modify existing premium-related checks to include temporary premium
    fun isPremiumUser(): Boolean {
        // Check both permanent authentication and temporary premium status
        return (authState.value is AuthState.Success) || isTemporaryPremiumActive()
    }

    suspend fun findUserProfileByUsername(username: String): UserProfile? {
        return try {
            // First, try to find an exact match
            val allProfiles = profileRepository.getAllProfiles().getOrNull()

            // Try exact match first
            allProfiles?.find { it.name == username }
            // If exact match fails, try case-insensitive match
                ?: allProfiles?.find { it.name?.equals(username, ignoreCase = true) == true }
        } catch (e: Exception) {
            null
        }
    }
}



