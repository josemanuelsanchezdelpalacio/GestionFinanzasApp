package com.dam2jms.gestiongastosapp.states

import android.os.Build
import androidx.annotation.RequiresApi
import com.dam2jms.gestiongastosapp.models.GraficosViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
data class GraficosUiState(

    val ingresos: List<Double> = emptyList(),
    val gastos: List<Double> = emptyList(),
    val fechas: List<LocalDate> = emptyList(),

    val evolucionBalance: List<Double> = emptyList(),

    val ingresosPorCategoria: Map<String, Double> = emptyMap(),
    val gastosPorCategoria: Map<String, Double> = emptyMap(),

    val totalIngresos: Double = 0.0,
    val totalGastos: Double = 0.0,

    val balanceTotal: Double = 0.0,
    val ratioAhorro: Double = 0.0,

    val gastosPrincipales: Map<String, Double> = emptyMap(),

    val seleccionarRangoTiempo: GraficosViewModel.RangoTiempo = GraficosViewModel.RangoTiempo.MONTH,
)