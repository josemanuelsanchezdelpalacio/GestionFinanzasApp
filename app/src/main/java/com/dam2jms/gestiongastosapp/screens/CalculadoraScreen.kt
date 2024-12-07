package com.dam2jms.gestiongastosapp.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dam2jms.gestiongastosapp.components.BottomAppBarReutilizable
import com.dam2jms.gestiongastosapp.models.CalculadoraViewModel
import com.dam2jms.gestiongastosapp.models.AuxViewModel
import com.dam2jms.gestiongastosapp.navigation.AppScreen
import com.dam2jms.gestiongastosapp.states.CalculadoraUiState
import com.dam2jms.gestiongastosapp.ui.theme.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculadoraScreen(
    navController: NavController,
    calculadoraViewModel: CalculadoraViewModel,
    auxViewModel: AuxViewModel
) {
    val uiState by calculadoraViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var seleccionarCalculadora by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CALCULADORAS FINANCIERAS",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = blanco
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorFondo),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "atras",
                            tint = blanco
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBarReutilizable(
                navController = navController,
                screenActual = AppScreen.CalculadoraScreen,
                cambiarSeccion = { pantalla ->
                    navController.navigate(pantalla.route)
                }
            )
        },
        containerColor = colorFondo
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Calculator Selector
            item {
                calculadoraViewModel.calculadoraSelector(
                    seleccionarCalculadora = seleccionarCalculadora,
                    onSeleccionarCalculadora = { seleccionarCalculadora = it },
                    colorFondo = colorFondo,
                    colorSeleccionado = naranjaClaro,
                    colorTexto = blanco
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Calculator Sections
            item {
                when (seleccionarCalculadora) {
                    0 -> PrestamoCalculator(
                        uiState = uiState,
                        onCalcularPrestamo = { monto, tasaInteres, plazo ->
                            calculadoraViewModel.calcularPrestamo(monto, tasaInteres, plazo)
                        }
                    )
                    1 -> DivisionGastosCalculator(
                        uiState = uiState,
                        onCalcularDivisionGastos = { gastoTotal, cantidadPersonas ->
                            calculadoraViewModel.calcularDivisionGastos(gastoTotal, cantidadPersonas)
                        }
                    )
                    2 -> AnalisisFinancieroCalculator(
                        uiState = uiState,
                        onCalcularAhorroPotencial = {
                            calculadoraViewModel.calcularAhorroPotencial(context)
                        }
                    )
                    3 -> ProyeccionFinancieraCalculator(
                        uiState = uiState,
                        onProyectarFlujoEfectivo = {
                            calculadoraViewModel.proyectarFlujoEfectivo(context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PrestamoCalculator(
    uiState: CalculadoraUiState,
    onCalcularPrestamo: (Double, Double, Int) -> Unit
) {
    var monto by remember { mutableStateOf("") }
    var tasaInteres by remember { mutableStateOf("") }
    var plazo by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = blanco)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Calculadora de Préstamos", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = monto,
                onValueChange = { monto = it },
                label = { Text("Monto del Préstamo") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            TextField(
                value = tasaInteres,
                onValueChange = { tasaInteres = it },
                label = { Text("Tasa de Interés (%)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            TextField(
                value = plazo,
                onValueChange = { plazo = it },
                label = { Text("Plazo (meses)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = {
                    onCalcularPrestamo(
                        monto.toDoubleOrNull() ?: 0.0,
                        tasaInteres.toDoubleOrNull() ?: 0.0,
                        plazo.toIntOrNull() ?: 0
                    )
                }
            ) {
                Text("Calcular")
            }

            // Display loan results
            uiState.prestamoFilas?.let { filas ->
                Text("Resultados del Préstamo", style = MaterialTheme.typography.titleSmall)
                filas.forEach { fila ->
                    Text(
                        "${fila.mes}: Cuota ${fila.cuota}, " +
                                "Interés ${fila.intereses}, " +
                                "Capital ${fila.capital}"
                    )
                }
            }
        }
    }
}

@Composable
fun DivisionGastosCalculator(
    uiState: CalculadoraUiState,
    onCalcularDivisionGastos: (Double, Int) -> Unit
) {
    var gastoTotal by remember { mutableStateOf("") }
    var cantidadPersonas by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = blanco)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("División de Gastos", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = gastoTotal,
                onValueChange = { gastoTotal = it },
                label = { Text("Gasto Total") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            TextField(
                value = cantidadPersonas,
                onValueChange = { cantidadPersonas = it },
                label = { Text("Cantidad de Personas") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = {
                    onCalcularDivisionGastos(
                        gastoTotal.toDoubleOrNull() ?: 0.0,
                        cantidadPersonas.toIntOrNull() ?: 0
                    )
                }
            ) {
                Text("Calcular")
            }

            // Display expense division results
            uiState.divisionGastos?.let { division ->
                Text(
                    "Gasto por Persona: ${division.gastoPorPersona}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun AnalisisFinancieroCalculator(
    uiState: CalculadoraUiState,
    onCalcularAhorroPotencial: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = blanco)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Análisis Financiero", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onCalcularAhorroPotencial) {
                Text("Calcular Ahorro Potencial")
            }

            // Display financial analysis results
            Column(horizontalAlignment = Alignment.Start) {
                Text("Total Ingresos: ${uiState.totalIngresos}")
                Text("Total Gastos: ${uiState.totalGastos}")
                Text("Ahorro Potencial: ${uiState.ahorroPotencial}")
                Text("Gastos Reducibles: ${uiState.gastosReducibles}")
            }
        }
    }
}

@Composable
fun ProyeccionFinancieraCalculator(
    uiState: CalculadoraUiState,
    onProyectarFlujoEfectivo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = blanco)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Proyección Financiera", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onProyectarFlujoEfectivo) {
                Text("Proyectar Flujo de Efectivo")
            }

            // Display projection results
            Column(horizontalAlignment = Alignment.Start) {
                Text("Proyección de Ingresos por Categoría:")
                uiState.proyeccionIngresosPorCategoria.forEach { (categoria, monto) ->
                    Text("$categoria: $monto")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Proyección de Gastos por Categoría:")
                uiState.proyeccionGastosPorCategoria.forEach { (categoria, monto) ->
                    Text("$categoria: $monto")
                }
            }
        }
    }
}