package com.dam2jms.gestiongastosapp.models

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam2jms.gestiongastosapp.states.TransactionUiState
import com.dam2jms.gestiongastosapp.states.UiState
import com.dam2jms.gestiongastosapp.ui.theme.*
import com.dam2jms.gestiongastosapp.utils.FireStoreUtil
import com.patrykandpatrick.vico.core.extension.sumOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class TransactionViewModel : ViewModel() {

    //para controlar el estado de la UI
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**inicializo el viewmodel cargando las transacciones **/
    init {
        cargarTransacciones()
    }

    /*** metodo para obtener las transacciones de firestore y actualizo la UI*/
    private fun cargarTransacciones(){

        viewModelScope.launch {
            FireStoreUtil.obtenerTransacciones(
                onSuccess = { transacciones ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            ingresos = transacciones.filter { it.tipo == "ingreso" },
                            gastos = transacciones.filter { it.tipo == "gasto" },
                            transaccionesFiltradas = transacciones
                        )
                    }
                },
                onFailure = {}
            )
        }
    }

    /**metodo para calcular el balance diario basado en la fecha seleccionada*/
    fun calcularBalanceDiario(fecha: LocalDate): Float {

        //filtro los ingresos y gastos diarios
        val ingresosDelDia = filtrarTransacciones(fecha, "ingresos").sumOf { it.cantidad.toFloat() }
        val gastosDelDia = filtrarTransacciones(fecha, "gastos").sumOf { it.cantidad.toFloat() }

        return ingresosDelDia - gastosDelDia
    }

    /**metodo para filtrar las transacciones por tipo en base a sus fechas**/
    fun filtrarTransacciones(fecha: LocalDate, tipo: String): List<TransactionUiState> {

        val fechaString = fecha.format(DateTimeFormatter.ISO_DATE)

        return when (tipo) {
            "ingresos" -> _uiState.value.ingresos.filter { it.fecha == fechaString }
            "gastos" -> _uiState.value.gastos.filter { it.fecha == fechaString }
            else -> emptyList()
        }
    }

    /**metodo para exportar las transacciones en un CSV**/
    fun exportarTransaccionesCSV(context: Context, uri: Uri) {

        viewModelScope.launch {
            try {
                //obtengo las transacciones de ingresos y gastos
                val transacciones = uiState.value.ingresos + uiState.value.gastos

                //creo el contenido del CSV
                val datosCSV = StringBuilder()

                //encabezados del CSV
                datosCSV.append("Tipo,Categoría,Cantidad,Fecha\n")
                transacciones.forEach { transaccion ->
                    datosCSV.append("${transaccion.tipo},${transaccion.categoria},${transaccion.cantidad},${transaccion.fecha}\n")
                }

                //escribo el archivo CSV
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(datosCSV.toString().toByteArray())
                    outputStream.flush()
                }

                Toast.makeText(context, "Transacciones exportadas correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al exportar las transacciones: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}



