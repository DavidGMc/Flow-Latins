package cd.software.flowchat.firebase.ui

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cd.software.flowchat.admob.AdViewModel
import cd.software.flowchat.firebase.model.UserProfile
import cd.software.flowchat.firebase.viewmodel.AuthViewModel
import coil.compose.AsyncImage
import es.chat.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    profiles: List<UserProfile>,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: AuthViewModel,
    adViewModel: AdViewModel
) {
    val context = LocalContext.current
    val activity = context as Activity
    val profilesViewedCount by viewModel.profilesViewedCount

    // Estado para la búsqueda
    var searchQuery by remember { mutableStateOf("") }

    // Observamos el estado de comunidad para tener acceso a los perfiles filtrados
    val communityState by viewModel.communityState.collectAsState()


    // Efecto para cargar los perfiles al entrar a la pantalla
    LaunchedEffect(Unit) {
        if (communityState.profiles.isEmpty()) {
            viewModel.loadCommunityProfiles()
        } else if (communityState.searchQuery.isNotEmpty() && searchQuery.isEmpty()) {
            // Si hay una búsqueda activa en el ViewModel pero el campo está vacío,
            // sincroniza el campo de búsqueda con el estado
            searchQuery = communityState.searchQuery
        }
    }

    // Función para mostrar un anuncio intersticial después de ver 5 perfiles
    fun checkAndShowInterstitial(userId: String) {
        viewModel.incrementProfilesViewedCount()
        val shouldShowAd = viewModel.profilesViewedCount.value % 5 == 0
        val isInterstitialReady = adViewModel.isInterstitialAdReady.value

        if (shouldShowAd && isInterstitialReady) {
            adViewModel.adManager.showInterstitial(activity) {
                onUserClick(userId)
            }
        } else {
            onUserClick(userId)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.Community)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )

                // Campo de búsqueda
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchProfiles(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_icon_desc))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.searchProfiles("")
                            }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear_search_desc)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                communityState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                communityState.filteredProfiles.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isEmpty())
                                stringResource(R.string.no_profiles_found)
                            else
                                stringResource(R.string.no_matching_profiles),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty())
                                stringResource(R.string.try_refreshing)
                            else
                                stringResource(R.string.try_different_term),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (searchQuery.isEmpty()) {
                                    viewModel.loadCommunityProfiles()
                                } else {
                                    searchQuery = ""
                                    viewModel.searchProfiles("")
                                }
                            }
                        ) {
                            Icon(
                                if (searchQuery.isEmpty())
                                    Icons.Default.Refresh
                                else
                                    Icons.Default.Clear,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (searchQuery.isEmpty())
                                    stringResource(R.string.refresh_button)
                                else
                                    stringResource(R.string.clear_search_button)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = communityState.filteredProfiles,
                            key = { it.userId }
                        ) { profile ->
                            UserCard(
                                profile = profile,
                                onClick = {
                                    checkAndShowInterstitial(profile.userId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun UserCard(
    profile: UserProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = profile.profileImageUrl,
                contentDescription = "Profile picture of ${profile.name}",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium
                )
                profile.country?.let { country ->
                    Text(
                        text = country,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}