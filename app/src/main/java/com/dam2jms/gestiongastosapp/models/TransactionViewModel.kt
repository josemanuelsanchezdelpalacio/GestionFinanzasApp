package com.dam2jms.gestiongastosapp.models

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam2jms.gestiongastosapp.states.BudgetState
import com.dam2jms.gestiongastosapp.states.FinancialGoalState
import com.dam2jms.gestiongastosapp.states.TransactionUiState
import com.dam2jms.gestiongastosapp.states.UiState
import com.dam2jms.gestiongastosapp.utils.FireStoreUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class TransactionViewModel : ViewModel() {

    // State for UI
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // State for Financial Goals
    private val _financialGoalState = MutableStateFlow<List<FinancialGoalState>>(emptyList())
    val financialGoalState: StateFlow<List<FinancialGoalState>> = _financialGoalState.asStateFlow()

    // State for Budgets
    private val _budgetState = MutableStateFlow<List<BudgetState>>(emptyList())
    val budgetState: StateFlow<List<BudgetState>> = _budgetState.asStateFlow()

    // Initialize ViewModel and load data
    init {
        loadTransactions()
        loadFinancialGoals()
        loadBudgets()
    }

    // Load transactions from Firestore
    private fun loadTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            FireStoreUtil.obtenerTransacciones(
                onSuccess = { transactions ->
                    val ingresos = transactions.filter { it.tipo == "ingreso" }
                    val gastos = transactions.filter { it.tipo == "gasto" }

                    _uiState.update { currentState ->
                        currentState.copy(
                            ingresos = ingresos,
                            gastos = gastos
                        )
                    }
                },
                onFailure = { exception ->
                    Log.e("TransactionViewModel", "Error loading transactions: ${exception.message}")
                }
            )
        }
    }

    // Load financial goals from Firestore
    private fun loadFinancialGoals() {
        viewModelScope.launch(Dispatchers.IO) {
            FireStoreUtil.getFinancialGoals(
                onSuccess = { goals ->
                    _financialGoalState.value = goals
                },
                onFailure = { exception ->
                    Log.e("TransactionViewModel", "Error loading financial goals: ${exception.message}")
                }
            )
        }
    }

    // Load budgets from Firestore
    private fun loadBudgets() {
        viewModelScope.launch(Dispatchers.IO) {
            FireStoreUtil.getBudgets(
                onSuccess = { budgets ->
                    _budgetState.value = budgets
                },
                onFailure = { exception ->
                    Log.e("TransactionViewModel", "Error loading budgets: ${exception.message}")
                }
            )
        }
    }

    // Method to filter transactions by date
    fun filtrarTransacciones(fecha: LocalDate, tipo: String): List<TransactionUiState> {
        val fechaString = fecha.format(DateTimeFormatter.ISO_DATE)

        return when (tipo) {
            "ingresos" -> _uiState.value.ingresos.filter { it.fecha == fechaString }
            "gastos" -> _uiState.value.gastos.filter { it.fecha == fechaString }
            else -> emptyList()
        }
    }

    // Method to export transactions to CSV
    fun exportarTransaccionesCSV(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val transacciones = uiState.value.ingresos + uiState.value.gastos

                val datosCSV = StringBuilder()
                datosCSV.append("Tipo,Categoría,Cantidad,Fecha\n")
                transacciones.forEach { transaccion ->
                    datosCSV.append("${transaccion.tipo},${transaccion.categoria},${transaccion.cantidad},${transaccion.fecha}\n")
                }

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

    // Method to delete a transaction
    fun eliminarTransaccion(context: Context, transaccion: TransactionUiState) {
        viewModelScope.launch(Dispatchers.IO) {
            val coleccion = if (transaccion.tipo == "ingreso") "ingresos" else "gastos"
            FireStoreUtil.eliminarTransaccion(
                coleccion = coleccion,
                transaccionId = transaccion.id ?: return@launch,
                onSuccess = {
                    // Reload transactions after deletion
                    loadTransactions()

                    // Show success toast on main thread
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Transacción eliminada correctamente", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { exception ->
                    // Show error toast on main thread
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Error al eliminar la transacción: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}