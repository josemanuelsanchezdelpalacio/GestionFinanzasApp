package com.dam2jms.gestiongastosapp.models

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.dam2jms.gestiongastosapp.data.Categoria
import com.dam2jms.gestiongastosapp.states.TransactionUiState
import com.dam2jms.gestiongastosapp.states.UiState
import com.dam2jms.gestiongastosapp.ui.theme.naranjaClaro
import com.dam2jms.gestiongastosapp.utils.FireStoreUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@RequiresApi(Build.VERSION_CODES.O)
class EditTransactionViewModel : ViewModel() {

    //para controlar el estado de la UI
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** metodo para cargar una transaccion desde firestore usando su ID y actualizo la UI*/
    fun cargarTransaccion(transaccionId: String, context: Context) {

        FireStoreUtil.obtenerTransacciones(
            onSuccess = { transacciones ->
                val transaccion = transacciones.find { it.id == transaccionId }
                transaccion?.let { transaction ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            cantidad = transaction.cantidad,
                            categoria = transaction.categoria,
                            tipo = transaction.tipo,
                            fechaTransaccion = transaction.fecha
                        )
                    }
                } ?: run {
                    Toast.makeText(context, "Transaccion no encontrada", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = { exception ->
                Toast.makeText(context, "Error al cargar la transaccion: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    /** metodo para actualizar los datos de la transaccion en la UI UI*/
    fun actualizarDatosTransaccion(cantidad: String, categoria: String, tipo: String, fecha: String) {

        _uiState.update { currentState ->
            currentState.copy(
                cantidad = cantidad.toDoubleOrNull() ?: currentState.cantidad,
                categoria = categoria,
                tipo = tipo,
                fechaTransaccion = fecha
            )
        }
    }

    /** metodo para editar una transacción en firestore */
    fun editarTransaccion(transaccion: TransactionUiState, context: Context) {

        //nombre de la coleccion
        val nombreColeccion = if (transaccion.tipo == "ingreso") "ingresos" else "gastos"

        FireStoreUtil.editarTransaccion(
            coleccion = nombreColeccion,
            transaccion = transaccion,
            onSuccess = {
                Toast.makeText(context, "${transaccion.tipo.capitalize()} editado correctamente", Toast.LENGTH_SHORT).show()
                AuxViewModel().leerTransacciones(context)
            },
            onFailure = { exception ->
                Toast.makeText(context, "Error al editar el ${transaccion.tipo}: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

