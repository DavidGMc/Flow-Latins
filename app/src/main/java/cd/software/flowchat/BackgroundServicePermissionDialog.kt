package cd.software.flowchat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BackgroundServicePermissionDialog {
    companion object {
        private const val PREF_NAME = "background_service_prefs"
        private const val KEY_PERMISSION_GRANTED = "background_permission_granted"
        private const val KEY_DONT_ASK_AGAIN = "dont_ask_again"

        fun shouldShowDialog(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val permissionGranted = prefs.getBoolean(KEY_PERMISSION_GRANTED, false)
            val dontAskAgain = prefs.getBoolean(KEY_DONT_ASK_AGAIN, false)
            return !permissionGranted && !dontAskAgain
        }

        fun markPermissionGranted(context: Context) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_PERMISSION_GRANTED, true)
                .apply()
        }

        fun markDontAskAgain(context: Context) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DONT_ASK_AGAIN, true)
                .apply()
        }

        fun resetPermissionState(context: Context) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }

        @Composable
        fun ShowDialog(
            onPermissionGranted: () -> Unit,
            onDismiss: () -> Unit
        ) {
            var showDetailedInfo by remember { mutableStateOf(false) }
            val context = LocalContext.current

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Permiso para sincronización en segundo plano") },
                text = {
                    Column {
                        Text(
                            "LatinsIRC necesita ejecutar un servicio en segundo plano para mantener activa la conexión IRC y sincronizar mensajes en tiempo real incluso cuando la app no está en primer plano."
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Esta sincronización permite:",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("• Recibir mensajes nuevos en tiempo real")
                        Text("• Mantener actualizadas las listas de canales y usuarios")
                        Text("• Enviar mensajes sin interrupciones")
                        Text("• Mostrar notificaciones de menciones y mensajes privados")

                        if (showDetailedInfo) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Detalles técnicos del uso de FOREGROUND_SERVICE_DATA_SYNC:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("• Mantiene una conexión TCP persistente con el servidor IRC")
                                    Text("• Procesa paquetes de red y eventos del servidor en tiempo real")
                                    Text("• Sincroniza el estado de canales, conversaciones y usuarios")
                                    Text("• Envía y recibe mensajes incluso cuando la app está en segundo plano")
                                    Text("• Maneja la reconexión automática en caso de pérdida de conexión")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { showDetailedInfo = !showDetailedInfo },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(if (showDetailedInfo) "Menos información" else "Más información")
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onPermissionGranted
                        ) {
                            Text("Conceder permiso")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text("Cancelar")
                        }
                    }
                }
            )
        }
    }
}