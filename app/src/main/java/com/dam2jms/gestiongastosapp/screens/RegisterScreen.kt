package com.dam2jms.gestiongastosapp.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.dam2jms.gestiongastosapp.models.RegisterViewModel
import com.dam2jms.gestiongastosapp.navigation.AppScreen
import com.dam2jms.gestiongastosapp.states.UiState
import com.dam2jms.gestiongastosapp.ui.theme.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Crear cuenta",
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
            RegisterBodyScreen(
                uiState = uiState,
                onRegister = { email, password, username ->
                    viewModel.registroConEmail(email, password, username)
                },
                onLogin = {
                    navController.navigate(AppScreen.LoginScreen.route) // Navigate to login screen
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterBodyScreen(
    uiState: UiState,
    onRegister: (String, String, String) -> Unit,
    onLogin: () -> Unit
) {
    // State for managing visibility of password
    var isPasswordVisible by remember { mutableStateOf(uiState.visibilidadPassword) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
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

        // Username field
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { uiState.email = it },
            label = { Text("Nombre de usuario", color = blanco) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = naranjaClaro)
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

        // Email field
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { uiState.email = it },
            label = { Text("Correo electrónico", color = blanco) },
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

        // Password field
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { uiState.password = it },
            label = { Text("Contraseña", color = blanco) },
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

        // Register button
        Button(
            onClick = { onRegister(uiState.email, uiState.password, "") }, // Modify to include username input if needed
            enabled = uiState.email.isNotBlank() && uiState.password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.email.isNotBlank() && uiState.password.isNotBlank()) naranjaClaro else grisClaro,
                contentColor = Color.Black
            )
        ) {
            Text(text = "Registrarse")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login navigation button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = onLogin) {
                Text(text = "¿Ya tienes una cuenta? Inicia sesión", color = naranjaClaro)
            }
        }

        // Error message if any
        if (uiState.error.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = uiState.error, color = Color.Red)
        }
    }
}
