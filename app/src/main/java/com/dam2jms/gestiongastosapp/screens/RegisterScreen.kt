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

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    navController.navigate(AppScreen.HomeScreen.route) // Navegar a HomeScreen después del registro
                },
                onEmailChange = viewModel::updateEmail,
                onPasswordChange = viewModel::updatePassword,
                onUsernameChange = viewModel::updateUsername,
                onLogin = {
                    navController.navigate(AppScreen.LoginScreen.route) // Navegar a la pantalla de inicio de sesión
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
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(uiState.visibilidadPassword) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        // Nombre de usuario
        OutlinedTextField(
            value = uiState.username, // Cambiado de 'email' a 'username'
            onValueChange = onUsernameChange,
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

        // Correo electrónico
        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
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

        // Contraseña
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña", color = blanco) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = naranjaClaro)
            },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = grisClaro
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = naranjaClaro,
                unfocusedBorderColor = grisClaro,
                cursorColor = naranjaClaro
            ),
            textStyle = TextStyle(color = blanco)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onRegister(uiState.email, uiState.password, uiState.email) },
            enabled = uiState.email.isNotBlank() && uiState.password.isNotBlank() && uiState.email.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = naranjaClaro,
                contentColor = Color.Black
            )
        ) {
            Text(text = "Registrarse")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onLogin) {
            Text(text = "¿Ya tienes una cuenta? Inicia sesión", color = naranjaClaro)
        }

        if (uiState.error.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = uiState.error, color = Color.Red)
        }
    }
}
