package com.dam2jms.gestiongastosapp.models

import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam2jms.gestiongastosapp.states.UiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.O)
class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loginConCorreo(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = UiState(error = "Los campos no pueden estar vacios")
            return
        }

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _uiState.value = UiState(error = "Error al loguearse: ${e.message}")
            }
        }
    }

    fun resetPassword(email: String) {

        if (email.isBlank()) {
            _uiState.value = UiState(error = "El correo no puede estar vacio")
            return
        }

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
            } catch (e: Exception) {
                _uiState.value = UiState(error = "Error al resetear la contraseña: ${e.message}")
            }
        }
    }

    fun handleGoogleSignInResult(result: ActivityResult, onSuccess: () -> Unit, onFailure: (String) -> Unit) {

        viewModelScope.launch {
            try {
                // Obteniendo el resultado de la cuenta de Google desde el Intent
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)

                // Creando credenciales para Firebase Authentication
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                // Autenticación en Firebase con las credenciales obtenidas
                auth.signInWithCredential(credential).await()

                // Navegación exitosa
                onSuccess()
            } catch (e: ApiException) {
                onFailure("Error al obtener la cuenta de Google: ${e.localizedMessage}")
            } catch (e: Exception) {
                onFailure("Error de inicio de sesión: ${e.localizedMessage}")
            }
        }
    }
}

