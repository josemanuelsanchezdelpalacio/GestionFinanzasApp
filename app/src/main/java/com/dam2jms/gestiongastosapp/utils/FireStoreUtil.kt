package com.dam2jms.gestiongastosapp.utils

import android.util.Log
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
    fun obtenerTransacciones(onSuccess: (List<TransactionUiState>) -> Unit, onFailure: (Exception) -> Unit) {
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
    fun añadirTransaccion(coleccion: String, transaccion: TransactionUiState, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
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
    fun eliminarTransaccion(coleccion: String, transaccionId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
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
    fun editarTransaccion(coleccion: String, transaccion: TransactionUiState, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
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

    // Método para obtener transacciones por mes
    suspend fun obtenerTransaccionesPorMes(año: Int, mes: Int): List<TransactionUiState> {
        val userId = Firebase.auth.currentUser?.uid
            ?: throw Exception("Usuario no autenticado")

        val fechaInicio = LocalDate.of(año, mes, 1)
        val fechaFin = fechaInicio.withDayOfMonth(fechaInicio.lengthOfMonth())

        return obtenerTransaccionesPorRango(fechaInicio, fechaFin)
    }

    // Método para obtener transacciones de los últimos meses
    suspend fun obtenerTransaccionesUltimosPeriodo(meses: Int): List<TransactionUiState> {
        val userId = Firebase.auth.currentUser?.uid
            ?: throw Exception("Usuario no autenticado")

        val fechaFin = LocalDate.now()
        val fechaInicio = fechaFin.minusMonths(meses.toLong())

        return obtenerTransaccionesPorRango(fechaInicio, fechaFin)
    }

    // Método para obtener balance total
    suspend fun obtenerBalanceTotal(): Double {
        val userId = Firebase.auth.currentUser?.uid
            ?: throw Exception("Usuario no autenticado")

        val transacciones = try {
            obtenerTransaccionesPorRango(LocalDate.MIN, LocalDate.now())
        } catch (e: Exception) {
            emptyList()
        }

        val totalIngresos = transacciones
            .filter { it.tipo == "ingreso" }
            .sumByDouble { it.cantidad }

        val totalGastos = transacciones
            .filter { it.tipo == "gasto" }
            .sumByDouble { it.cantidad }

        return totalIngresos - totalGastos
    }

    // Método para obtener gastos por categoría en un rango de fechas
    suspend fun obtenerGastosPorCategoria(
        fechaInicio: LocalDate,
        fechaFin: LocalDate
    ): Map<String, Double> {
        val transacciones = obtenerTransaccionesPorRango(fechaInicio, fechaFin)

        return transacciones
            .filter { it.tipo == "gasto" }
            .groupBy { it.categoria }
            .mapValues { (_, transaccionesPorCategoria) ->
                transaccionesPorCategoria.sumByDouble { it.cantidad }
            }
    }

    // Métodos para gestión de metas financieras
    fun guardarMetaFinanciera(
        metaFinanciera: Double,
        fechaMeta: LocalDate,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return

        val userRef = db.collection("users").document(userId)

        val diasHastaMeta = LocalDate.now().until(fechaMeta).days
        val ahorroDiarioNecesario = if (diasHastaMeta > 0) metaFinanciera / diasHastaMeta else 0.0

        val metaData = hashMapOf(
            "metaFinanciera" to metaFinanciera,
            "fechaMeta" to fechaMeta.toString(),
            "diasHastaMeta" to diasHastaMeta,
            "ahorroDiarioNecesario" to ahorroDiarioNecesario,
            "progresoMeta" to 0.0,
            "financialGoalId" to UUID.randomUUID().toString()
        )

        userRef.update(metaData as Map<String, Any>)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Método para eliminar meta financiera
    fun eliminarMetaFinanciera(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        val defaultMetaData = hashMapOf(
            "metaFinanciera" to 0.0,
            "fechaMeta" to null,
            "diasHastaMeta" to -1,
            "ahorroDiarioNecesario" to 0.0,
            "progresoMeta" to 0.0,
            "financialGoalId" to ""
        )

        userRef.update(defaultMetaData as Map<String, Any>)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Método para actualizar progreso de meta financiera
    fun actualizarProgresoMeta(
        progresoActual: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        userRef.update("progresoMeta", progresoActual)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Método para obtener estadísticas financieras
    suspend fun obtenerEstadisticasFinancieras(): Map<String, Any> {
        val userId = Firebase.auth.currentUser?.uid
            ?: throw Exception("Usuario no autenticado")

        // Obtener transacciones del mes actual
        val fechaActual = LocalDate.now()
        val transaccionesMesActual = obtenerTransaccionesPorMes(fechaActual.year, fechaActual.monthValue)

        // Calcular totales
        val totalIngresosMes = transaccionesMesActual
            .filter { it.tipo == "ingreso" }
            .sumByDouble { it.cantidad }

        val totalGastosMes = transaccionesMesActual
            .filter { it.tipo == "gasto" }
            .sumByDouble { it.cantidad }

        // Obtener categorías de gastos más frecuentes
        val categoriasGastos = transaccionesMesActual
            .filter { it.tipo == "gasto" }
            .groupBy { it.categoria }
            .mapValues { (_, transacciones) ->
                transacciones.sumByDouble { it.cantidad }
            }
            .toList()
            .sortedByDescending { (_, total) -> total }
            .take(3)
            .toMap()

        return mapOf(
            "totalIngresosMes" to totalIngresosMes,
            "totalGastosMes" to totalGastosMes,
            "balanceMes" to (totalIngresosMes - totalGastosMes),
            "categoriasGastosPrincipales" to categoriasGastos
        )
    }
}
