package com.dam2jms.gestiongastosapp.states

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

data class TransactionUiState(
    var id: String? = null,
    val cantidad: Double = 0.0,
    val categoria: String = "",
    var tipo: String = "",
    val fecha: String = LocalDate.now().toString(),
    val descripcion: String = ""
)

