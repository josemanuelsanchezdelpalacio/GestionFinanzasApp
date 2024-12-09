package com.dam2jms.gestiongastosapp.states

import android.os.Build
import androidx.annotation.RequiresApi
import com.dam2jms.gestiongastosapp.navigation.AppScreen

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
data class UiState(

    // Autenticación
    var email: String = "",
    var password: String = "",
    var visibilidadPassword: Boolean = false,
    val error: String = "",

    // Transacciones
    var id: String = "",
    val cantidad: Double = 0.0,
    val tipo: String = "",
    val categoria: String = "",
    val fecha: String = "",
    val descripcion: String = "",
    val moneda: String = "",

    // Nuevos campos para resumen financiero
    val totalIngresos: Double = 0.0,
    val totalGastos: Double = 0.0,
    val transaccionesPorCategoria: Map<String, Double> = emptyMap(),
    val transaccionesPorMes: Map<String, Double> = emptyMap(),


    //balance general (homescreen)
    val balanceTotal: Double = 0.0,
    val balanceAnual: Double = 0.0,
    val balanceDiario: Double = 0.0,
    val balanceMensual: Double = 0.0,

    //ingresos (homescreen)
    val ingresosDiarios: Double = 0.0,
    val ingresosMensuales: Double = 0.0,
    val ingresosAnuales: Double = 0.0,
    val ingresosPorCategoria: Map<String, Double> = emptyMap(),
    val promedioIngresosDiario: Double = 0.0,

    //gastos (homescreen)
    val gastosDiarios: Double = 0.0,
    val gastosMensuales: Double = 0.0,
    val gastosAnuales: Double = 0.0,
    val gastosPorCategoria: Map<String, Double> = emptyMap(),
    val promedioGastoDiario: Double = 0.0,
    val promedioGastoMensual: Double = 0.0,
    val porcentajeGastosMensual: Double = 0.0,
    val tendenciaMensual: Double = 0.0,

    //ahorros (homescreen)
    val ahorrosDiarios: Double = 0.0,
    val ahorrosMensuales: Double = 0.0,
    val ahorroProgreso: Float = 0.0f,
    val tasaAhorro: Double = 0.0,
    val tasaAhorroDiaria: Double = 0.0,
    val tasaAhorroMensual: Double = 0.0,

    //meta financiera (homescreen)
    val objetivoFinanciero: Double = 0.0,
    val fechaObjetivo: LocalDate? = null,
    val diasHastaMeta: Int = 0,
    val diasRestantesMes: Int = 0,
    val ahorroDiarioNecesario: Double = 0.0,
    val ahorrosTotales: Double = 0.0,
    val progresoMeta: Double = 0.0,
    val estadoMeta: String = "",
    val idObjetivoFinanciero: String = "",

    //para transactionscreen
    val transaccionesRecientes: List<TransactionUiState> = emptyList(),
    val transaccionesFiltradas: List<TransactionUiState> = emptyList(),
    val gastos: List<TransactionUiState> = emptyList(),
    val ingresos: List<TransactionUiState> = emptyList(),

    //para editransactionscreen
    val fechaTransaccion: String = LocalDate.now().format(DateTimeFormatter.ISO_DATE),

    //para manejo de monedas
    val monedaActual: String = "USD",
    val cantidadConvertida: Double = 0.0,


    //estado de la UI
    val screenActual: AppScreen = AppScreen.HomeScreen,

    val newsArticles: List<Article> = emptyList(),
    val isLoadingNews: Boolean = false,
    val newsError: String? = null

)

// Enums and Supporting Types
enum class CalendarView {
    GOALS, REMINDERS
}

enum class ReminderType {
    PAYMENT, GOAL
}
