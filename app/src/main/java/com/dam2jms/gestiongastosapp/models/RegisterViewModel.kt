package com.dam2jms.gestiongastosapp.models

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam2jms.gestiongastosapp.states.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.O)
class RegisterViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Actualizar el email en el estado
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    // Actualizar la contraseña en el estado
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    // Actualizar el nombre de usuario en el estado
    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(email = username)
    }

    // Registro con email y password
    fun registroConEmail(email: String, password: String, username: String) {
        if (!validateRegistrationInputs(email, password, username)) return

        viewModelScope.launch {
            try {
                val resultado = auth.createUserWithEmailAndPassword(email, password).await()
                resultado.user?.let { user ->
                    initializeNewUser(user, username)
                }
                _uiState.value = UiState(error = "") // Limpiar errores
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Registration failed: ${e.localizedMessage}")
            }
        }
    }

    // Validar entradas de registro
    private fun validateRegistrationInputs(email: String, password: String, username: String): Boolean {
        return when {
            email.isBlank() || password.isBlank() || username.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "All fields are required.")
                false
            }
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters long.")
                false
            }
            else -> true
        }
    }

    // Inicializar nuevo usuario en Firestore
    private suspend fun initializeNewUser(user: FirebaseUser?, username: String) {
        user?.let {
            val userMap = mapOf(
                "username" to username,
                "email" to user.email,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("users").document(user.uid).set(userMap).await()
        }
    }
}
