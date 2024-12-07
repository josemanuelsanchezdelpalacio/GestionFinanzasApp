package com.dam2jms.gestiongastosapp.screens

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dam2jms.gestiongastosapp.R
import com.dam2jms.gestiongastosapp.models.LoginViewModel
import com.dam2jms.gestiongastosapp.navigation.AppScreen
import com.dam2jms.gestiongastosapp.states.UiState
import com.dam2jms.gestiongastosapp.ui.theme.blanco
import com.dam2jms.gestiongastosapp.ui.theme.grisClaro
import com.dam2jms.gestiongastosapp.ui.theme.naranjaClaro
import com.dam2jms.gestiongastosapp.ui.theme.colorFondo
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    navController: NavController,
    googleSignInClient: GoogleSignInClient
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Launcher para manejar el resultado del intent de Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(
            result = result,
            onSuccess = {
                // Navegar a la pantalla principal si el inicio de sesión es exitoso
                navController.navigate(AppScreen.HomeScreen.route)
            },
            onFailure = { error ->
                // Mostrar mensaje de error en un Toast
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        )
    }

    // State to control the visibility of the AlertDialog
    var showDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Iniciar sesión",
                        color = blanco,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorFondo),
                modifier = Modifier.shadow(4.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(colorFondo)
        ) {
            LoginBodyScreen(
                uiState = uiState,
                onEmailPasswordLogin = { email, password ->
                    viewModel.loginConCorreo(email, password)
                },
                onGoogleSignIn = {
                    // Lanzar el intent de Google Sign-In
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                },
                onForgotPassword = {
                    // Show the AlertDialog to reset the password
                    showDialog = true
                },
                onCreateAccount = {
                    navController.navigate(AppScreen.RegisterScreen.route)
                }
            )

            // AlertDialog for password reset
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(text = "Restablecer contraseña") },
                    text = {
                        Column {
                            Text("Por favor ingrese su correo electrónico para restablecer la contraseña:")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Correo electrónico") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.resetPassword(emailInput)
                                showDialog = false
                            }
                        ) {
                            Text("Enviar enlace")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBodyScreen(
    uiState: UiState,
    onEmailPasswordLogin: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit
) {
    // Estado para controlar la visibilidad de la contraseña
    var isPasswordVisible by remember { mutableStateOf(uiState.visibilidadPassword) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icono central
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(naranjaClaro, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.imagen_logo),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de email
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { uiState.email = it },
            label = { Text(text = "Email", color = blanco) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = naranjaClaro)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = naranjaClaro,
                unfocusedBorderColor = grisClaro,
                cursorColor = naranjaClaro
            ),
            textStyle = TextStyle(color = blanco)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de contraseña
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { uiState.password = it },
            label = { Text(text = "Contraseña", color = blanco) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = naranjaClaro)
            },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = grisClaro
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = naranjaClaro,
                unfocusedBorderColor = grisClaro,
                cursorColor = naranjaClaro
            ),
            textStyle = TextStyle(color = blanco)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para iniciar sesión
        Button(
            onClick = { onEmailPasswordLogin(uiState.email, uiState.password) },
            enabled = uiState.email.isNotBlank() && uiState.password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.email.isNotBlank() && uiState.password.isNotBlank()) naranjaClaro else grisClaro,
                contentColor = Color.Black
            )
        ) {
            Text(text = "Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Google Sign-In
        OutlinedButton(
            onClick = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            border = BorderStroke(1.dp, grisClaro),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Black,
                contentColor = blanco
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icono_google),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Continuar con Google")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onForgotPassword) {
                Text(text = "¿Olvidaste la contraseña?", color = naranjaClaro)
            }

            TextButton(onClick = onCreateAccount) {
                Text(text = "Crear cuenta", color = naranjaClaro)
            }
        }
    }
}

