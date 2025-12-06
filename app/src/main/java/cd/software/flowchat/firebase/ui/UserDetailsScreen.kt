package cd.software.flowchat.firebase.ui

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cd.software.flowchat.firebase.model.UserProfile
import coil.compose.AsyncImage
import es.chat.R
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(
    profile: UserProfile?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.loading_profile))
                    }
                }
            }
            profile != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Image
                    AsyncImage(
                        model = profile.profileImageUrl,
                        contentDescription = stringResource(R.string.profile_picture_desc, profile.name),
                        modifier = Modifier
                            .size(330.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                        error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Profile Details
                    ProfileDetailItem(
                        label = stringResource(R.string.name),
                        value = profile.name
                    )
                    ProfileDetailItem(
                        label = stringResource(R.string.age),
                        value = profile.age.toString()
                    )
                    ProfileDetailItem(
                        label = stringResource(R.string.country),
                        value = profile.country
                    )
                    ProfileDetailItem(
                        label = stringResource(R.string.preferred_server),
                        value = profile.preferredServer
                    )

                    // Fechas formateadas
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    ProfileDetailItem(
                        label = stringResource(R.string.member_since),
                        value = dateFormat.format(Date(profile.createdAt))
                    )
                    if (profile.updatedAt > profile.createdAt) {
                        ProfileDetailItem(
                            label = stringResource(R.string.last_updated),
                            value = dateFormat.format(Date(profile.updatedAt))
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.user_not_found))
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailItem(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}