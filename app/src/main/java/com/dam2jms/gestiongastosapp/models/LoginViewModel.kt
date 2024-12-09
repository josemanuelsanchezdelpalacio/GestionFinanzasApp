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

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loginConCorreo(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Los campos no pueden estar vacíos")
            return
        }

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _uiState.value = _uiState.value.copy(error = "")
                onSuccess()
            } catch (e: Exception) {
                onFailure("Error al iniciar sesión: ${e.message}")
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "El correo no puede estar vacío")
            return
        }

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.value = _uiState.value.copy(error = "Correo enviado exitosamente.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al resetear contraseña: ${e.message}")
            }
        }
    }

    fun handleGoogleSignInResult(result: ActivityResult, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)

                if (account?.idToken == null) {
                    onFailure("Error: Token de Google es nulo.")
                    return@launch
                }

                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).await()

                onSuccess()
            } catch (e: ApiException) {
                onFailure("Error al obtener cuenta de Google: ${e.localizedMessage}")
            } catch (e: Exception) {
                onFailure("Error de autenticación: ${e.localizedMessage}")
            }
        }
    }
}

