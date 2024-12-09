package com.dam2jms.gestiongastosapp.utils

import android.util.Log
import com.dam2jms.gestiongastosapp.states.BudgetState
import com.dam2jms.gestiongastosapp.states.FinancialGoalState
import com.dam2jms.gestiongastosapp.states.TransactionUiState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

object FireStoreUtil {
    private val db = FirebaseFirestore.getInstance()

    // Método para obtener transacciones
    fun obtenerTransacciones(
        onSuccess: (List<TransactionUiState>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("ingresos")
            .get()
            .addOnSuccessListener { ingresosSnapshot ->
                val ingresos = ingresosSnapshot.documents.mapNotNull { document ->
                    val transaccion = document.toObject(TransactionUiState::class.java)
                    transaccion?.apply { id = document.id }
                }
                db.collection("users")
                    .document(userId)
                    .collection("gastos")
                    .get()
                    .addOnSuccessListener { gastosSnapshot ->
                        val gastos = gastosSnapshot.documents.mapNotNull { document ->
                            val transaccion = document.toObject(TransactionUiState::class.java)
                            transaccion?.apply { id = document.id }
                        }
                        onSuccess(ingresos + gastos)
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    // Método para añadir una transacción
    fun añadirTransaccion(
        coleccion: String,
        transaccion: TransactionUiState,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection(coleccion)
            .add(transaccion)
            .addOnSuccessListener { documentReference ->
                // Asignar el ID del documento al objeto Transacción
                documentReference.update("id", documentReference.id)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    // Método para eliminar una transacción
    fun eliminarTransaccion(
        coleccion: String,
        transaccionId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection(coleccion)
            .document(transaccionId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Método para editar una transacción
    fun editarTransaccion(
        coleccion: String,
        transaccion: TransactionUiState,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        if (transaccion.id!!.isEmpty()) {
            onFailure(Exception("El ID de la transacción no puede estar vacío."))
            return
        }
        db.collection("users")
            .document(userId)
            .collection(coleccion)
            .document(transaccion.id!!)
            .set(transaccion)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun eliminarMetaFinanciera(
        idUsuario: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Referencia al documento del usuario
        val userRef = db.collection("users").document(idUsuario)

        // Primero verificamos si el documento existe
        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // El documento existe, procedemos a actualizar
                    userRef.update(
                        mapOf(
                            "metaFinanciera" to 0.0,
                            "fechaMeta" to null,
                            "diasHastaMeta" to -1,
                            "ahorroDiarioNecesario" to 0.0,
                            "progresoMeta" to 0.0,
                            "financialGoalId" to ""
                        )
                    )
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                } else {
                    // Si el documento no existe, lo creamos con valores por defecto
                    val defaultData = hashMapOf(
                        "metaFinanciera" to 0.0,
                        "fechaMeta" to null,
                        "diasHastaMeta" to -1,
                        "ahorroDiarioNecesario" to 0.0,
                        "progresoMeta" to 0.0,
                        "financialGoalId" to ""
                    )
                    userRef.set(defaultData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    // New method to create or update a financial goal
    fun setFinancialGoal(
        goal: FinancialGoalState,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return

        val goalDocument = hashMapOf(
            "targetAmount" to goal.targetAmount,
            "currentAmount" to goal.currentAmount,
            "startDate" to goal.startDate,
            "endDate" to goal.endDate,
            "goalName" to goal.goalName,
            "goalCategory" to goal.goalCategory,
            "progress" to goal.progress
        )

        db.collection("users")
            .document(userId)
            .collection("financialGoals")
            .document(goal.id ?: UUID.randomUUID().toString())
            .set(goalDocument)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Method to retrieve financial goals
    fun getFinancialGoals(
        onSuccess: (List<FinancialGoalState>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("financialGoals")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val goals = querySnapshot.documents.mapNotNull { document ->
                    FinancialGoalState(
                        id = document.id,
                        targetAmount = document.getDouble("targetAmount") ?: 0.0,
                        currentAmount = document.getDouble("currentAmount") ?: 0.0,
                        startDate = document.getString("startDate") ?: "",
                        endDate = document.getString("endDate") ?: "",
                        goalName = document.getString("goalName") ?: "",
                        goalCategory = document.getString("goalCategory") ?: "",
                        progress = document.getDouble("progress") ?: 0.0
                    )
                }
                onSuccess(goals)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Method to create or update a budget
    fun setBudget(
        budget: BudgetState,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return

        val budgetDocument = hashMapOf(
            "category" to budget.category,
            "budgetAmount" to budget.budgetAmount,
            "currentSpent" to budget.currentSpent,
            "startDate" to budget.startDate,
            "endDate" to budget.endDate,
            "remainingAmount" to (budget.budgetAmount - budget.currentSpent)
        )

        db.collection("users")
            .document(userId)
            .collection("budgets")
            .document(budget.id ?: UUID.randomUUID().toString())
            .set(budgetDocument)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Method to retrieve budgets
    fun getBudgets(
        onSuccess: (List<BudgetState>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("budgets")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val budgets = querySnapshot.documents.mapNotNull { document ->
                    BudgetState(
                        id = document.id,
                        category = document.getString("category") ?: "",
                        budgetAmount = document.getDouble("budgetAmount") ?: 0.0,
                        currentSpent = document.getDouble("currentSpent") ?: 0.0,
                        startDate = document.getString("startDate") ?: "",
                        endDate = document.getString("endDate") ?: "",
                        remainingAmount = document.getDouble("remainingAmount") ?: 0.0
                    )
                }
                onSuccess(budgets)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Method to update budget when a transaction is added
    fun updateBudgetOnTransaction(
        category: String,
        amount: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return

        // Find the budget for the specific category
        db.collection("users")
            .document(userId)
            .collection("budgets")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val budgetDocument = querySnapshot.documents.first()
                    val currentSpent = budgetDocument.getDouble("currentSpent") ?: 0.0
                    val budgetAmount = budgetDocument.getDouble("budgetAmount") ?: 0.0

                    val updatedSpent = currentSpent + amount
                    val remainingAmount = budgetAmount - updatedSpent

                    budgetDocument.reference.update(
                        mapOf(
                            "currentSpent" to updatedSpent,
                            "remainingAmount" to remainingAmount
                        )
                    )
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                } else {
                    onFailure(Exception("No budget found for category"))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    // Method to calculate total savings and progress towards financial goals
    fun calculateFinancialProgress(
        onSuccess: (Double, Double, List<FinancialGoalState>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return

        // First, get all transactions
        obtenerTransacciones(
            onSuccess = { transactions ->
                // Calculate total income and expenses
                val totalIncome = transactions.filter { it.tipo == "ingreso" }.sumByDouble { it.cantidad }
                val totalExpenses = transactions.filter { it.tipo == "gasto" }.sumByDouble { it.cantidad }
                val totalSavings = totalIncome - totalExpenses

                // Get financial goals
                getFinancialGoals(
                    onSuccess = { goals ->
                        // Calculate overall goal progress
                        val totalGoalProgress = goals.sumByDouble { it.progress }
                        onSuccess(totalSavings, totalGoalProgress, goals)
                    },
                    onFailure = onFailure
                )
            },
            onFailure = onFailure
        )
    }

    // Método para obtener transacciones por rango de fechas
    suspend fun obtenerTransaccionesPorRango(
        fechaInicio: LocalDate,
        fechaFin: LocalDate
    ): List<TransactionUiState> {
        val userId = Firebase.auth.currentUser?.uid
            ?: throw Exception("Usuario no autenticado")

        val fechaInicioString = fechaInicio.toString()
        val fechaFinString = fechaFin.toString()

        return try {
            // Obtener ingresos
            val ingresos = db.collection("users")
                .document(userId)
                .collection("ingresos")
                .whereGreaterThanOrEqualTo("fecha", fechaInicioString)
                .whereLessThanOrEqualTo("fecha", fechaFinString)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.toObject(TransactionUiState::class.java)?.apply { id = document.id }
                }

            // Obtener gastos
            val gastos = db.collection("users")
                .document(userId)
                .collection("gastos")
                .whereGreaterThanOrEqualTo("fecha", fechaInicioString)
                .whereLessThanOrEqualTo("fecha", fechaFinString)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.toObject(TransactionUiState::class.java)?.apply { id = document.id }
                }

            ingresos + gastos
        } catch (e: Exception) {
            throw Exception("Error al obtener transacciones: ${e.message}")
        }
    }

    // Método para obtener transacciones de los últimos meses
    suspend fun obtenerTransaccionesUltimosPeriodo(meses: Int): List<TransactionUiState> {
        val userId = Firebase.auth.currentUser?.uid
            ?: throw Exception("Usuario no autenticado")

        val fechaFin = LocalDate.now()
        val fechaInicio = fechaFin.minusMonths(meses.toLong())

        return obtenerTransaccionesPorRango(fechaInicio, fechaFin)
    }

}
