package com.dam2jms.gestiongastosapp.models

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam2jms.gestiongastosapp.states.GraficosUiState
import com.dam2jms.gestiongastosapp.states.TransactionUiState
import com.dam2jms.gestiongastosapp.utils.FireStoreUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
class GraficosViewModel : ViewModel() {

    //para controlar el estado de la UI
    private val _uiState = MutableStateFlow(GraficosUiState())
    val uiState: StateFlow<GraficosUiState> = _uiState.asStateFlow()

    //defino los rangos de tiempo disponibles para los graficos
    enum class RangoTiempo {
        WEEK, MONTH, QUARTER, YEAR
    }

    //lista para guardar todas las transacciones
    private var allTransacciones: List<TransactionUiState> = emptyList()

    //rango de tiempo actual seleccionado para los graficos
    private var tiempoRangoActual: RangoTiempo = RangoTiempo.MONTH

    //inicializo el viewmodel y cargo las transacciones
    init {
        RecuperarTransacciones()
    }

    /**metodo para recuperar las transacciones desde firestore*/
    fun RecuperarTransacciones() {
        viewModelScope.launch {
            FireStoreUtil.obtenerTransacciones(
                onSuccess = { transacciones ->
                    allTransacciones = transacciones
                    procesarTransacciones(tiempoRangoActual)
                },
                onFailure = { exception -> }
            )
        }
    }

    /**metodo para establecer el rango de tiempo para los graficos y procesa las transacciones*/
    fun establecerRangoGraficos(rangoTiempo: RangoTiempo) {
        tiempoRangoActual = rangoTiempo
        procesarTransacciones(rangoTiempo)
    }

    /**metodo para filtrar y procesas las transacciones en base al tiempo seleccionado*/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun procesarTransacciones(rangoTiempo: RangoTiempo) {

        val fechaActual = LocalDate.now()

        //filtro las transacciones para aplicarles el rango de tiempo
        val transaccionesFiltradas = allTransacciones.filter { transaction ->

            //parseo la fecha de la transaccion
            val fechaTransaccion = LocalDate.parse(transaction.fecha)

            //establezco el tiempo para cada rango
            when (rangoTiempo) {
                RangoTiempo.WEEK -> ChronoUnit.DAYS.between(fechaTransaccion, fechaActual) < 7
                RangoTiempo.MONTH -> ChronoUnit.MONTHS.between(fechaTransaccion, fechaActual) < 1
                RangoTiempo.QUARTER -> ChronoUnit.MONTHS.between(fechaTransaccion, fechaActual) < 3
                RangoTiempo.YEAR -> ChronoUnit.YEARS.between(fechaTransaccion, fechaActual) < 1
            }
        }

        //separo los ingresos y los gastos
        val (ingresos, gastos) = transaccionesFiltradas.partition { it.tipo == "ingreso" }

        //calculo los datos de cada transaccion
        val totalIngresos = ingresos.sumOf { it.cantidad }
        val totalGastos = gastos.sumOf { it.cantidad }
        val balanceTotal = totalIngresos - totalGastos
        val ratioAhorro = if (totalIngresos > 0) (balanceTotal / totalIngresos) * 100 else 0.0

        //agrupo las transacciones por categoria
        val ingresosPorCategoria = ingresos.groupBy { it.categoria }
            .mapValues { it.value.sumOf { transaction -> transaction.cantidad } }
        val gastosPorCategoria = gastos.groupBy { it.categoria }
            .mapValues { it.value.sumOf { transaction -> transaction.cantidad } }

        //obtengo los gastos principales
        val gastosPrincipales = gastosPorCategoria.entries
            .sortedByDescending { it.value }
            .take(3)
            .associate { it.key to it.value }

        //preparo los datos para los graficos
        val (ingresosGraf, gastosGraf, fechas) = prepararDatosGraficos(ingresos, gastos)
        val evolucionBalance = prepararDatosBalanceEvolucion(transaccionesFiltradas)

        //actualizo la UI con los nuevos datos procesados
        _uiState.update { currentState ->
            currentState.copy(
                ingresos = ingresosGraf,
                gastos = gastosGraf,
                fechas = fechas,
                evolucionBalance = evolucionBalance,
                ingresosPorCategoria = ingresosPorCategoria,
                gastosPorCategoria = gastosPorCategoria,
                totalIngresos = totalIngresos,
                totalGastos = totalGastos,
                balanceTotal = balanceTotal,
                ratioAhorro = ratioAhorro,
                gastosPrincipales = gastosPrincipales,
                seleccionarRangoTiempo = rangoTiempo
            )
        }
    }

    /**metodo para preparar los datos de los graficos a partir de las listas de ingresos y gastos*/
    private fun prepararDatosGraficos(ingresosLista: List<TransactionUiState>, gastosLista: List<TransactionUiState>): Triple<List<Double>, List<Double>, List<LocalDate>> {

        //agrupo los ingresos y gastos por fecha
        val ingresosAgrupados = ingresosLista.groupBy { LocalDate.parse(it.fecha) }
            .mapValues { it.value.sumOf { transaccion -> transaccion.cantidad } }
        val gastosAgrupados = gastosLista.groupBy { LocalDate.parse(it.fecha) }
            .mapValues { it.value.sumOf { transaccion -> transaccion.cantidad } }

        //obtengo las fechas y las ordenamos
        val fechas = (ingresosAgrupados.keys + gastosAgrupados.keys).sorted()

        //creo las listas de ingresos y gastos por fecha
        val ingresos = fechas.map { dato -> ingresosAgrupados[dato] ?: 0.0 }
        val gastos = fechas.map { dato -> gastosAgrupados[dato] ?: 0.0 }

        //devuelvo los datos en una lista triple
        return Triple(ingresos, gastos, fechas)
    }

    /**metodo para preparar los datos de evolucion del balance a partir de las transacciones filtradas*/
    private fun prepararDatosBalanceEvolucion(transacciones: List<TransactionUiState>): List<Double> {

        //ordeno las transacciones por fecha
        val transaccionesOrdenadas = transacciones.sortedBy { LocalDate.parse(it.fecha) }

        //calculo los ingresos y gastos acumulados a lo largo del tiempo
        var balanceActual = 0.0
        return transaccionesOrdenadas.map { transaccion ->
            balanceActual += if (transaccion.tipo == "ingreso") transaccion.cantidad else - transaccion.cantidad
            balanceActual
        }
    }
}

