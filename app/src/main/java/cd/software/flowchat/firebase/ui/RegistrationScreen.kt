package cd.software.flowchat.firebase.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cd.software.flowchat.firebase.model.AuthState
import cd.software.flowchat.firebase.model.UserProfile
import cd.software.flowchat.firebase.viewmodel.AuthViewModel
import cd.software.flowchat.utils.showToast
import coil.compose.AsyncImage
import es.chat.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    authViewModel: AuthViewModel,
    onRegistrationComplete: () -> Unit,
    onNavigateLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var preferredServer by remember { mutableStateOf("") }

    // Estados para visibilidad de contraseñas
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Estado para la imagen de perfil
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val profileState by authViewModel.profileState.collectAsState()
    val scope = rememberCoroutineScope()

    // Launcher para seleccionar imagen
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var isCheckingUsername by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }
    var progressMessage by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                showProgressDialog = false
                onRegistrationComplete()
            }
            is AuthState.Error -> {
                showProgressDialog = false
                val errorMessage = (authState as AuthState.Error).message
                if (errorMessage.contains("nombre") || errorMessage.contains("name") ||
                    errorMessage.contains("ya está en uso")) {
                    nameError = context.getString(R.string.name_error, name)
                    Toast.makeText(context, nameError, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
            is AuthState.Loading -> {
                showProgressDialog = true
                progressMessage = context.getString(R.string.registering_user)
            }
            else -> {
                showProgressDialog = false
            }
        }
    }

    if (showProgressDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.processing)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(progressMessage)
                }
            },
            confirmButton = { }
        )
    }

    // Usar LazyColumn para mejor rendimiento y scroll más fluido
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // Importante para manejar el teclado
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp) // Padding superior e inferior
    ) {
        // Spacer inicial para centrar mejor el contenido
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Sección de foto de perfil al inicio
        item {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePicker.launch("image/*") }
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder cuando no hay imagen
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Add profile picture",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Icono de cámara para indicar que se puede cambiar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Change picture",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.add_profile_photo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }

        // Campo de nombre
        item {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = {
                    if (nameError != null) {
                        Text(
                            text = nameError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                trailingIcon = {
                    if (isCheckingUsername) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            )
        }

        // Botón de verificar disponibilidad
        item {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        isCheckingUsername = true
                        progressMessage = context.getString(R.string.checking_nick)
                        showProgressDialog = true

                        scope.launch {
                            val profile = authViewModel.findUserProfileByUsername(name)
                            isCheckingUsername = false
                            showProgressDialog = false

                            if (profile != null) {
                                nameError = context.getString(R.string.name_error, name)
                                Toast.makeText(context, nameError, Toast.LENGTH_LONG).show()
                            } else {
                                context.showToast(R.string.nick_available)
                            }
                        }
                    } else {
                        context.showToast(R.string.enter_nick_to_check)
                    }
                },
                enabled = name.isNotBlank() && !isCheckingUsername
            ) {
                Text(stringResource(R.string.check_availability))
            }
        }

        // Campo de email
        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        }

        // Campo de edad
        item {
            OutlinedTextField(
                value = age,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        age = it
                    }
                },
                label = { Text(stringResource(R.string.age)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Campo de país
        item {
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text(stringResource(R.string.country)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Campo de servidor preferido
        item {
            OutlinedTextField(
                value = preferredServer,
                onValueChange = { preferredServer = it },
                label = { Text(stringResource(R.string.preferred_server)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Campo de contraseña con visibilidad
        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Lock
                    else Icons.Filled.Lock

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )
        }

        // Campo de confirmar contraseña con visibilidad
        item {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.confirm_password)) },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible)
                        Icons.Filled.Lock
                    else Icons.Filled.Lock

                    val description = if (confirmPasswordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )
        }

        // Spacer antes del botón
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón de registro
        item {
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank() || name.isBlank()) {
                        context.showToast(R.string.required_fields)
                        return@Button
                    }

                    if (password != confirmPassword) {
                        context.showToast(R.string.password_mismatch)
                        return@Button
                    }

                    isCheckingUsername = true
                    progressMessage = context.getString(R.string.checking_nick)
                    showProgressDialog = true

                    scope.launch {
                        val profile = authViewModel.findUserProfileByUsername(name)

                        if (profile != null) {
                            isCheckingUsername = false
                            showProgressDialog = false
                            nameError = context.getString(R.string.name_error, name)
                            Toast.makeText(context, nameError, Toast.LENGTH_LONG).show()
                        } else {
                            val userProfile = UserProfile(
                                name = name,
                                age = age.toIntOrNull() ?: 0,
                                country = country,
                                preferredServer = preferredServer
                            )

                            progressMessage = context.getString(R.string.registering_user)
                            // Modificar el signUp para incluir la imagen
                            authViewModel.signUp(email, password, userProfile, selectedImageUri)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCheckingUsername && authState !is AuthState.Loading && nameError == null
            ) {
                Text(stringResource(R.string.register_button))
            }
        }

        // Botón para ir al login
        item {
            TextButton(
                onClick = onNavigateLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.already_have_account))
            }
        }

        // Indicador de carga
        item {
            when (authState) {
                is AuthState.Loading -> CircularProgressIndicator()
                else -> {}
            }
        }

        // Spacer final para dar espacio al final
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}