package com.dam2jms.gestiongastosapp.states

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@RequiresApi(Build.VERSION_CODES.O)
data class CalculadoraUiState(
    // Loan calculator fields
    val prestamoFilas: List<FilaPrestamo>? = null,
    val cuotaPrestamo: Double = 0.0,
    val totalIntereses: Double = 0.0,

    // Expense division calculator fields
    val divisionGastos: DivisionGastosResult? = null,

    // Financial analysis fields
    val totalIngresos: Double = 0.0,
    val totalGastos: Double = 0.0,
    val ahorroPotencial: Double = 0.0,
    val gastosReducibles: Double = 0.0,

    // Projected cash flow
    val proyeccionIngresosPorCategoria: Map<String, Double> = emptyMap(),
    val proyeccionGastosPorCategoria: Map<String, Double> = emptyMap()
)

// Supporting data classes for complex results
data class FilaPrestamo(
    val mes: Int,
    val cuota: Double,
    val intereses: Double,
    val capital: Double
)

data class DivisionGastosResult(
    val gastoPorPersona: Double
)



