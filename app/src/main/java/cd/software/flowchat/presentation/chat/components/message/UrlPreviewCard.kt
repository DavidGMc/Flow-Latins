package cd.software.flowchat.presentation.chat.components.message

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.chat.R
import cd.software.flowchat.utils.showToast

/**
 * Card de preview para URLs Muestra un preview clickeable de una URL que abre el navegador al hacer
 * click
 */
@Composable
fun UrlPreviewCard(url: String, context: Context, modifier: Modifier = Modifier) {
    Card(
            modifier =
                    modifier.fillMaxWidth().clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            context.showToast(R.string.url_open_error)
                        }
                    },
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Título truncado de la URL
            val displayUrl =
                    remember(url) {
                        try {
                            val uri = Uri.parse(url)
                            val host = uri.host ?: url
                            val path =
                                    uri.path?.let {
                                        if (it.length > 15) it.substring(0, 15) + "..." else it
                                    }
                                            ?: ""
                            host + path
                        } catch (e: Exception) {
                            url.take(30) + if (url.length > 30) "..." else ""
                        }
                    }

            Text(
                    text = displayUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
            )

            // Pequeño indicador de que es un enlace
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Link",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                        text = stringResource(R.string.external_link),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
