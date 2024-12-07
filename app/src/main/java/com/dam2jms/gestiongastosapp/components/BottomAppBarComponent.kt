package com.dam2jms.gestiongastosapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dam2jms.gestiongastosapp.navigation.AppScreen
import com.dam2jms.gestiongastosapp.ui.theme.colorFondo
import com.dam2jms.gestiongastosapp.ui.theme.grisClaro
import com.dam2jms.gestiongastosapp.ui.theme.naranjaClaro

@Composable
fun BottomAppBarReutilizable(navController: NavController, screenActual: AppScreen, cambiarSeccion: (AppScreen) -> Unit) {

    BottomAppBar(
        containerColor = colorFondo,
        contentColor = naranjaClaro,
        tonalElevation = 8.dp,
        modifier = Modifier.height(120.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Seccion(
                icono = Icons.Filled.ShowChart,
                label = "Graficos",
                seleccionado = screenActual == AppScreen.GraficosScreen,
                onClick = {
                    cambiarSeccion(AppScreen.GraficosScreen)
                    navController.navigate(AppScreen.GraficosScreen.route)
                }
            )

            Seccion(
                icono = Icons.Filled.Calculate,
                label = "Calculadora",
                seleccionado = screenActual == AppScreen.CalculadoraScreen,
                onClick = {
                    cambiarSeccion(AppScreen.CalculadoraScreen)
                    navController.navigate(AppScreen.CalculadoraScreen.route)
                }
            )

            Seccion(
                icono = Icons.Filled.Home,
                label = "Inicio",
                seleccionado = screenActual == AppScreen.HomeScreen,
                onClick = {
                    cambiarSeccion(AppScreen.HomeScreen)
                    navController.navigate(AppScreen.HomeScreen.route)
                }
            )

            Seccion(
                icono = Icons.Filled.AddCard,
                label = "Transacciones",
                seleccionado = screenActual == AppScreen.TransactionScreen,
                onClick = {
                    cambiarSeccion(AppScreen.TransactionScreen)
                    navController.navigate(AppScreen.TransactionScreen.route)
                }
            )

            Seccion(
                icono = Icons.Filled.History,
                label = "Historial",
                seleccionado = screenActual == AppScreen.HistoryScreen,
                onClick = {
                    cambiarSeccion(AppScreen.HistoryScreen)
                    navController.navigate(AppScreen.HistoryScreen.route)
                }
            )
        }
    }
}

@Composable
private fun Seccion(icono: ImageVector, label: String, seleccionado: Boolean, onClick: () -> Unit) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (seleccionado) naranjaClaro else grisClaro
        )
        Text(
            text = label,
            color = if (seleccionado) naranjaClaro else grisClaro,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


