package com.dam2jms.gestiongastosapp.navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dam2jms.gestiongastosapp.models.AddTransactionViewModel
import com.dam2jms.gestiongastosapp.models.AuxViewModel
import com.dam2jms.gestiongastosapp.models.CalculadoraViewModel
import com.dam2jms.gestiongastosapp.models.MonedasViewModel
import com.dam2jms.gestiongastosapp.models.EditTransactionViewModel
import com.dam2jms.gestiongastosapp.models.GraficosViewModel
import com.dam2jms.gestiongastosapp.screens.GraficosScreen
import com.dam2jms.gestiongastosapp.models.HistoryViewModel
import com.dam2jms.gestiongastosapp.models.HomeViewModel
import com.dam2jms.gestiongastosapp.models.LoginViewModel
import com.dam2jms.gestiongastosapp.models.RegisterViewModel
import com.dam2jms.gestiongastosapp.models.TransactionViewModel
import com.dam2jms.gestiongastosapp.screens.AddTransactionScreen
import com.dam2jms.gestiongastosapp.screens.CalculadoraScreen
import com.dam2jms.gestiongastosapp.screens.EditTransactionScreen
import com.dam2jms.gestiongastosapp.screens.HistoryScreen
import com.dam2jms.gestiongastosapp.screens.HomeScreen
import com.dam2jms.gestiongastosapp.screens.RegisterScreen
import com.dam2jms.gestiongastosapp.screens.LoginScreen
import com.dam2jms.gestiongastosapp.screens.TransactionScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current

    val googleSignInClient = setupGoogleSignIn(context)
    // Forzar el cierre de sesión al inicio para asegurar que se muestre el selector
    googleSignInClient.signOut()

    NavHost(navController = navController, startDestination = AppScreen.LoginScreen.route) {

        composable(AppScreen.RegisterScreen.route) {
            RegisterScreen(
                viewModel = RegisterViewModel(),
                navController = navController
            )
        }
        composable(AppScreen.LoginScreen.route) {
            LoginScreen(
                viewModel = LoginViewModel(),
                navController = navController,
                googleSignInClient = googleSignInClient
            )
        }
        composable(AppScreen.HomeScreen.route) {
            HomeScreen(
                navController = navController,
                homeViewModel = HomeViewModel(),
                auxViewModel = AuxViewModel(),
                monedasViewModel = MonedasViewModel()
            )
        }
        composable(AppScreen.AddTransactionScreen.route) {
            AddTransactionScreen(
                navController = navController,
                auxViewModel = AuxViewModel(),
                mvvm = AddTransactionViewModel()
            )
        }
        composable(
            route = AppScreen.TransactionScreen.route,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val selectedDate = backStackEntry.arguments?.getString("date")
                ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            TransactionScreen(
                navController,
                mvvm = TransactionViewModel(),
                auxViewModel = AuxViewModel(),
                seleccionarFecha = selectedDate
            )
        }
        composable(route = AppScreen.EditTransactionScreen.route) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            EditTransactionScreen(
                navController = navController,
                auxViewModel = AuxViewModel(),
                mvvm = EditTransactionViewModel(),
                transactionId = transactionId
            )
        }
        composable(AppScreen.CalculadoraScreen.route) {
            CalculadoraScreen(navController, calculadoraViewModel = CalculadoraViewModel(), auxViewModel = AuxViewModel())
        }
        composable(AppScreen.HistoryScreen.route) {
            HistoryScreen(navController, auxViewModel = AuxViewModel(), mvvm = HistoryViewModel())
        }
        composable(AppScreen.GraficosScreen.route) {
            GraficosScreen(navController, auxViewModel = AuxViewModel(), graficosViewModel = GraficosViewModel(), monedasViewModel = MonedasViewModel())
        }
    }
}


private fun setupGoogleSignIn(context: Context): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("484530452726-0qsqchbvv2idp9vln7kfgebmf1gr1vhg.apps.googleusercontent.com")
        .requestEmail()
        .build()

    return GoogleSignIn.getClient(context, gso)
}
