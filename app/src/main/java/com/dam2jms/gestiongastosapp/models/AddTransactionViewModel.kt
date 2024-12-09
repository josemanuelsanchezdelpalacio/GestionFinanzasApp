package com.dam2jms.gestiongastosapp.models

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam2jms.gestiongastosapp.navigation.AppScreen
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
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class AddTransactionViewModel : ViewModel() {

    // UI State for Transactions
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Financial Goal State
    private val _financialGoalState = MutableStateFlow<List<FinancialGoalState>>(emptyList())
    val financialGoalState: StateFlow<List<FinancialGoalState>> = _financialGoalState.asStateFlow()

    // Budget State
    private val _budgetState = MutableStateFlow<List<BudgetState>>(emptyList())
    val budgetState: StateFlow<List<BudgetState>> = _budgetState.asStateFlow()

    // Financial Progress State
    private val _financialProgressState = MutableStateFlow(
        Triple(0.0, 0.0, emptyList<FinancialGoalState>())
    )
    val financialProgressState: StateFlow<Triple<Double, Double, List<FinancialGoalState>>> =
        _financialProgressState.asStateFlow()

    // Error State
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    // Initialize ViewModel and load initial data
    init {
        loadFinancialGoals()
        loadBudgets()
        calculateFinancialProgress()
    }

    // Update transaction data
    fun updateTransactionData(
        cantidad: String?,
        categoria: String?,
        tipo: String
    ) {
        val cantidadDouble = cantidad?.toDoubleOrNull()
        if (cantidadDouble != null && cantidadDouble > 0) {
            _uiState.update {
                it.copy(
                    cantidad = cantidadDouble,
                    categoria = categoria ?: it.categoria,
                    tipo = tipo
                )
            }
        }
    }

    // Add a new transaction
    fun addTransaction(
        context: Context,
        seleccionarFecha: LocalDate,
        onNavigate: (String) -> Unit
    ) {
        val cantidad = uiState.value.cantidad
        val categoria = uiState.value.categoria
        val tipo = uiState.value.tipo

        if (cantidad > 0 && categoria.isNotEmpty() && tipo.isNotEmpty()) {
            val transaccion = TransactionUiState(
                cantidad = cantidad,
                categoria = categoria,
                tipo = tipo,
                fecha = seleccionarFecha.format(DateTimeFormatter.ISO_DATE)
            )

            // Determine collection based on transaction type
            val nombreColeccion = if (tipo == "ingreso") "ingresos" else "gastos"

            viewModelScope.launch(Dispatchers.IO) {
                // Add transaction to Firestore
                FireStoreUtil.añadirTransaccion(
                    coleccion = nombreColeccion,
                    transaccion = transaccion,
                    onSuccess = {
                        // Update budget if it's an expense
                        if (tipo == "gasto") {
                            updateBudgetOnTransaction(categoria, cantidad, context)
                        }

                        // Show success toast
                        viewModelScope.launch(Dispatchers.Main) {
                            Toast.makeText(context, "${tipo.capitalize()} agregado correctamente", Toast.LENGTH_SHORT).show()
                        }

                        // Recalculate financial progress
                        calculateFinancialProgress()
                    },
                    onFailure = { exception ->
                        // Show error toast
                        viewModelScope.launch(Dispatchers.Main) {
                            Toast.makeText(context, "Error al agregar el ${tipo}: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        } else {
            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    // Load Financial Goals
    private fun loadFinancialGoals() {
        viewModelScope.launch(Dispatchers.IO) {
            FireStoreUtil.getFinancialGoals(
                onSuccess = { goals ->
                    _financialGoalState.value = goals
                },
                onFailure = { exception ->
                    _errorState.value = "Error loading financial goals: ${exception.message}"
                }
            )
        }
    }

    // Load Budgets
    private fun loadBudgets() {
        viewModelScope.launch(Dispatchers.IO) {
            FireStoreUtil.getBudgets(
                onSuccess = { budgets ->
                    _budgetState.value = budgets
                },
                onFailure = { exception ->
                    _errorState.value = "Error loading budgets: ${exception.message}"
                }
            )
        }
    }

    // Create or Update Financial Goal
    fun createFinancialGoal(
        context: Context,
        goalName: String,
        targetAmount: Double,
        goalCategory: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        val newGoal = FinancialGoalState(
            id = UUID.randomUUID().toString(),
            targetAmount = targetAmount,
            currentAmount = 0.0,
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            goalName = goalName,
            goalCategory = goalCategory,
            progress = 0.0
        )

        viewModelScope.launch(Dispatchers.IO) {
            FireStoreUtil.setFinancialGoal(
                goal = newGoal,
                onSuccess = {
                    // Reload financial goals after creating
                    loadFinancialGoals()

                    // Show success toast
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Meta financiera creada correctamente", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { exception ->
                    // Show error toast
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Error al crear meta financiera: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // Create or Update Budget
    fun createBudget(
        context: Context,
        category: String,
        budgetAmount: Double,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        val newBudget = BudgetState(
            id = UUID.randomUUID().toString(),
            category = category,
            budgetAmount = budgetAmount,
            currentSpent = 0.0,
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            remainingAmount = budgetAmount
        )

        viewModelScope.launch(Dispatchers.IO) {
            FireStoreUtil.setBudget(
                budget = newBudget,
                onSuccess = {
                    // Reload budgets after creating
                    loadBudgets()

                    // Show success toast
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Presupuesto creado correctamente", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { exception ->
                    // Show error toast
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Error al crear presupuesto: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // Update Budget when Transaction is Added
    private fun updateBudgetOnTransaction(
        category: String,
        amount: Double,
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            FireStoreUtil.updateBudgetOnTransaction(
                category = category,
                amount = amount,
                onSuccess = {
                    // Reload budgets after updating
                    loadBudgets()
                },
                onFailure = { exception ->
                    // Show error toast if budget update fails
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Error al actualizar presupuesto: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // Calculate Financial Progress
    private fun calculateFinancialProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            FireStoreUtil.calculateFinancialProgress(
                onSuccess = { totalSavings, totalGoalProgress, goals ->
                    _financialProgressState.value = Triple(
                        totalSavings,
                        totalGoalProgress,
                        goals
                    )
                },
                onFailure = { exception ->
                    _errorState.value = "Error calculating financial progress: ${exception.message}"
                }
            )
        }
    }

    // Clear Error State
    fun clearErrorState() {
        _errorState.value = null
    }
}



