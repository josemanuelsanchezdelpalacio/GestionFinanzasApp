package com.dam2jms.gestiongastosapp.navigation

sealed class AppScreen(val route: String) {

    object RegisterScreen : AppScreen("register_screen")
    object HomeScreen : AppScreen("home_screen")
    object LoginScreen : AppScreen("login_screen")
    object TransactionScreen : AppScreen("transaction_screen/{date}") {
        fun createRoute(date: String) = "transaction_screen/$date"
    }
    object AddTransactionScreen : AppScreen("addTransaction_screen")
    object EditTransactionScreen : AppScreen("editTransaction_screen/{transactionId}") {
        fun createRoute(transactionId: String) = "editTransaction_screen/$transactionId"
    }
    object HistoryScreen : AppScreen("history_screen")
    object CalculadoraScreen : AppScreen("calculadora_screen")
    object GraficosScreen : AppScreen("graficos_screen")
}
