package com.dam2jms.gestiongastosapp.models

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.dam2jms.gestiongastosapp.states.CalculadoraUiState
import com.dam2jms.gestiongastosapp.states.FilaPrestamo
import com.dam2jms.gestiongastosapp.states.DivisionGastosResult
import com.dam2jms.gestiongastosapp.ui.theme.blanco
import com.dam2jms.gestiongastosapp.ui.theme.grisClaro
import com.dam2jms.gestiongastosapp.ui.theme.naranjaClaro
import com.dam2jms.gestiongastosapp.utils.FireStoreUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.math.pow
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
class CalculadoraViewModel : ViewModel() {

    // Para controlar el estado de la UI
    private val _uiState = MutableStateFlow(CalculadoraUiState())
    val uiState: StateFlow<CalculadoraUiState> = _uiState.asStateFlow()

    @Composable
    fun calculadoraSelector(
        seleccionarCalculadora: Int,
        onSeleccionarCalculadora: (Int) -> Unit,
        colorFondo: Color = grisClaro,
        colorSeleccionado: Color = naranjaClaro,
        colorTexto: Color = blanco
    ) {
        ScrollableTabRow(
            selectedTabIndex = seleccionarCalculadora,
            containerColor = colorFondo,
            contentColor = colorSeleccionado,
            edgePadding = 8.dp
        ) {
            listOf(
                "Prestamos" to Icons.Default.AttachMoney,
                "Division gastos" to Icons.Default.Groups,
                "Analisis Financiero" to Icons.Default.Calculate,
                "Proyección" to Icons.Default.Timeline
            ).forEachIndexed { index, (titulo, icono) ->
                Tab(
                    selected = seleccionarCalculadora == index,
                    onClick = { onSeleccionarCalculadora(index) },
                    text = {
                        Text(
                            text = titulo,
                            color = if (seleccionarCalculadora == index) colorSeleccionado else colorTexto
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = icono,
                            contentDescription = titulo,
                            tint = if (seleccionarCalculadora == index) colorSeleccionado else colorTexto
                        )
                    }
                )
            }
        }
    }

    // Método para calcular préstamo
    fun calcularPrestamo(monto: Double, tasaInteres: Double, plazo: Int) {
        // Convertir tasa de interés anual a mensual
        val tasaMensual = tasaInteres / 12 / 100

        // Calcular cuota mensual usando fórmula de amortización
        val cuotaMensual = if (tasaMensual > 0) {
            monto * (tasaMensual * (1 + tasaMensual).pow(plazo)) / ((1 + tasaMensual).pow(plazo) - 1)
        } else {
            monto / plazo
        }

        // Crear tabla de amortización
        val tablaPrestamo = (1..plazo).map { mes ->
            val intereses = if (tasaMensual > 0) {
                calcularInteresesMes(monto, tasaMensual, mes, cuotaMensual)
            } else 0.0

            val capital = cuotaMensual - intereses

            FilaPrestamo(
                mes = mes,
                cuota = cuotaMensual.roundToDecimal(2),
                intereses = intereses.roundToDecimal(2),
                capital = capital.roundToDecimal(2)
            )
        }

        _uiState.update {
            it.copy(
                prestamoFilas = tablaPrestamo
            )
        }
    }

    // Método auxiliar para calcular intereses por mes
    private fun calcularInteresesMes(
        saldoInicial: Double,
        tasaMensual: Double,
        mes: Int,
        cuotaMensual: Double
    ): Double {
        var saldo = saldoInicial
        var interesesAcumulados = 0.0

        repeat(mes) {
            val intereses = saldo * tasaMensual
            val capital = cuotaMensual - intereses
            saldo -= capital
            interesesAcumulados += intereses
        }

        return saldo * tasaMensual
    }

    // Método para calcular división de gastos
    fun calcularDivisionGastos(gastoTotal: Double, cantidadPersonas: Int) {
        val gastoPorPersona = if (cantidadPersonas > 0) {
            gastoTotal / cantidadPersonas
        } else {
            0.0
        }

        _uiState.update {
            it.copy(
                divisionGastos = DivisionGastosResult(
                    gastoPorPersona = gastoPorPersona.roundToDecimal(2)
                )
            )
        }
    }

    // Calcula el ahorro potencial basado en transacciones
    fun calcularAhorroPotencial(context: Context) {
        runBlocking {
            try {
                val transacciones = FireStoreUtil.obtenerTransaccionesUltimosPeriodo(3)

                val totalIngresos = transacciones
                    .filter { it.tipo == "ingreso" }
                    .sumByDouble { it.cantidad }

                val totalGastos = transacciones
                    .filter { it.tipo == "gasto" }
                    .sumByDouble { it.cantidad }

                val ahorroPotencial = totalIngresos * 0.3 // Asume un 30% de ahorro
                val gastosMensualesReducibles = transacciones
                    .filter { it.tipo == "gasto" && esCategoriaReducible(it.categoria) }
                    .sumByDouble { it.cantidad }

                _uiState.update {
                    it.copy(
                        totalIngresos = totalIngresos.roundToDecimal(2),
                        totalGastos = totalGastos.roundToDecimal(2),
                        ahorroPotencial = ahorroPotencial.roundToDecimal(2),
                        gastosReducibles = gastosMensualesReducibles.roundToDecimal(2)
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al calcular ahorro potencial", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Proyecta el flujo de efectivo futuro
    fun proyectarFlujoEfectivo(context: Context) {
        runBlocking {
            try {
                val transaccionesUltimosTresMeses = FireStoreUtil.obtenerTransaccionesUltimosPeriodo(3)

                // Calcula promedios de ingresos y gastos
                val promedioIngresos = transaccionesUltimosTresMeses
                    .filter { it.tipo == "ingreso" }
                    .groupBy { it.categoria }
                    .mapValues { (_, transacciones) ->
                        transacciones.sumByDouble { it.cantidad } / 3
                    }

                val promedioGastos = transaccionesUltimosTresMeses
                    .filter { it.tipo == "gasto" }
                    .groupBy { it.categoria }
                    .mapValues { (_, transacciones) ->
                        transacciones.sumByDouble { it.cantidad } / 3
                    }

                // Proyección para los próximos 6 meses
                val proyeccionIngresos = promedioIngresos.mapValues { (_, promedio) -> promedio * 6 }
                val proyeccionGastos = promedioGastos.mapValues { (_, promedio) -> promedio * 6 }

                _uiState.update {
                    it.copy(
                        proyeccionIngresosPorCategoria = proyeccionIngresos,
                        proyeccionGastosPorCategoria = proyeccionGastos
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al proyectar flujo de efectivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Identifica categorías de gastos reducibles
    private fun esCategoriaReducible(categoria: String): Boolean {
        val categoriasReducibles = listOf(
            "entretenimiento", "suscripciones", "comida fuera",
            "compras no esenciales", "ocio"
        )
        return categoria.lowercase() in categoriasReducibles
    }

    // Método para redondear un número
    private fun Double.roundToDecimal(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return (this * factor).roundToInt() / factor
    }
}
