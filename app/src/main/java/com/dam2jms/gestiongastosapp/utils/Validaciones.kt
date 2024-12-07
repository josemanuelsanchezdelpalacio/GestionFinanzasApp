package com.dam2jms.gestiongastosapp.utils

import android.content.Context
import android.util.Patterns
import android.widget.Toast

object Validaciones{

    //validar que el correo tiene el formato correcto
    fun validarEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
        return email.isNotBlank() && emailRegex.matches(email)
    }

    //validar que la contraseña cumpla con las restrincciones de firebase
    fun validarPassword(password: String): Boolean{
        return password.length >= 6
    }

    fun validarCredenciales(email: String, password: String, context: Context): Boolean {

        if(!Validaciones.validarEmail(email)){
            Toast.makeText(context, "El correo no tiene un formato valido", Toast.LENGTH_SHORT).show()
            return false
        }

        if(!Validaciones.validarPassword(password)){
            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}


