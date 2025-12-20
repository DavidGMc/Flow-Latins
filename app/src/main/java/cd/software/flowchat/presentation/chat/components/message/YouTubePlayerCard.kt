package cd.software.flowchat.presentation.chat.components.message
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * Card que muestra un reproductor de YouTube embebido usando AndroidView
 * Compatible con la versión 13.0.0 de android-youtube-player
 */
@Composable
fun YouTubePlayerCard(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPlayerReady by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Título indicador
            Text(
                text = "📺 Video de YouTube",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                // AndroidView para YouTubePlayerView (versión 13.0.0)
                AndroidView(
                    factory = { ctx ->
                        YouTubePlayerView(ctx).apply {
                            // Vincular el lifecycle para gestión automática
                            lifecycleOwner.lifecycle.addObserver(this)

                            // Inicializar el reproductor
                            addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                    isPlayerReady = true
                                    // Cargar el video sin reproducir automáticamente
                                    youTubePlayer.cueVideo(videoId, 0f)
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Indicador de carga
                if (!isPlayerReady) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Nota informativa
            Text(
                text = "Toca play para reproducir",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    // Cleanup cuando el composable se destruye
    DisposableEffect(lifecycleOwner) {
        onDispose {
            // El lifecycle observer se encarga del cleanup automáticamente
        }
    }
}