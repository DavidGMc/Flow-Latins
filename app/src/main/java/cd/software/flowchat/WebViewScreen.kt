package cd.software.flowchat

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.util.Log
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cd.software.flowchat.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import cd.software.flowchat.viewmodel.WebContentViewModel
import es.chat.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    navController: NavController,
    viewModel: WebContentViewModel = viewModel()
) {
    val contentUrl by viewModel.contentUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val headerText by viewModel.headerText.collectAsState() // Texto del encabezado desde Firebase

    var webView by remember { mutableStateOf<WebView?>(null) }
    var isWebViewLoading by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Barra superior con botón de retroceso
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                }
            }
        )

        // 2. Texto clickable con valor de Firebase
        // Texto clickable con valor de Firebase
        if (headerText != null) {
            Text(
                text = headerText!!,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        // Verificar si es una URL válida antes de intentar abrirla
                        if (URLUtil.isValidUrl(headerText) &&
                            (headerText!!.startsWith("http://") || headerText!!.startsWith("https://"))) {
                            // Es una URL válida, intentar abrirla
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(headerText!!))
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                // Mostrar mensaje si no hay app para manejar la URL

                                context.showToast(R.string.url_open_error)
                            }
                        } else {
                            context.showToast(R.string.url_open_not_link)
                        }
                    },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // 3. WebView
        Box(modifier = Modifier.weight(1f)) {
            if (contentUrl != null) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isWebViewLoading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)

                                    // Inyectar JavaScript/CSS para ocultar elementos no deseados
                                    view?.evaluateJavascript("""
                        (function() {
                            // Ocultar menús, encabezados, navegación, etc.
                            var elements = document.querySelectorAll('header, nav, .menu, .navbar, .navigation, .site-header, .header, .footer, .site-footer, #header, #footer, #menu, #navbar');
                            for (var i = 0; i < elements.length; i++) {
                                elements[i].style.display = 'none';
                            }
                            
                            // Alternativamente, puedes identificar elementos específicos de tu sitio web
                            // Por ejemplo:
                            // document.getElementById('specific-menu-id').style.display = 'none';
                            
                            // También puedes hacer que el contenido principal ocupe toda la pantalla
                            var content = document.querySelector('.content, .main-content, article, .article, #content, #main-content, main');
                            if (content) {
                                content.style.width = '100%';
                                content.style.margin = '0';
                                content.style.padding = '16px';
                            }
                        })();
                    """, null)

                                    isWebViewLoading = false
                                    canGoBack = view?.canGoBack() ?: false
                                }

                                // Resto del código del WebViewClient...
                            }

                            settings.apply {
                                javaScriptEnabled = true  // Importante: habilitar JavaScript
                                domStorageEnabled = true
                                setSupportZoom(true)
                                loadsImagesAutomatically = true

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                }
                            }

                            loadUrl(contentUrl!!)
                            webView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        // Si la URL cambia, actualizamos el WebView
                        contentUrl?.let { url ->
                            if (view.url != url) {
                                view.loadUrl(url)
                            }
                        }
                    }
                )
            }

            // Indicador de carga
            if (isWebViewLoading || isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}