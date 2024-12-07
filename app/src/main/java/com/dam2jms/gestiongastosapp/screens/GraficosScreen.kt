package com.dam2jms.gestiongastosapp.screens

import ItemComponents.SelectorMoneda
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dam2jms.gestiongastosapp.components.BottomAppBarReutilizable
import com.dam2jms.gestiongastosapp.components.GraficoBarras
import com.dam2jms.gestiongastosapp.components.GraficoCircularGastos
import com.dam2jms.gestiongastosapp.components.GraficoCircularIngresos
import com.dam2jms.gestiongastosapp.components.GraficoLineas
import com.dam2jms.gestiongastosapp.models.AuxViewModel
import com.dam2jms.gestiongastosapp.models.GraficosViewModel
import com.dam2jms.gestiongastosapp.models.MonedasViewModel
import com.dam2jms.gestiongastosapp.navigation.AppScreen
import com.dam2jms.gestiongastosapp.states.GraficosUiState
import com.dam2jms.gestiongastosapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GraficosScreen(navController: NavController, graficosViewModel: GraficosViewModel, auxViewModel: AuxViewModel, monedasViewModel: MonedasViewModel) {

    val uiState by graficosViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var seleccionSeccion by remember { mutableStateOf(0) }
    var seleccionarRangoGraficos by remember { mutableStateOf(GraficosViewModel.RangoTiempo.MONTH) }
    var mostrarListaMonedas by remember { mutableStateOf(false) }

    fun cambiarRangoTiempo(nuevoRango: GraficosViewModel.RangoTiempo) {
        seleccionarRangoGraficos = nuevoRango
        graficosViewModel.establecerRangoGraficos(nuevoRango)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GRAFICOS FINANCIEROS",
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
                        Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "atras", tint = blanco)
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarListaMonedas = true }) {
                        Icon(
                            Icons.Filled.MonetizationOn,
                            contentDescription = "cambiar moneda",
                            tint = blanco
                        )
                    }
                }
            )
        },
        bottomBar = {

            //barra inferior con las distintas pantallas
            BottomAppBarReutilizable(
                navController = navController,
                screenActual = AppScreen.TransactionScreen,
                cambiarSeccion = { pantalla ->
                    navController.navigate(pantalla.route)
                }
            )
        },
        containerColor = colorFondo
    ) { paddingValues ->
        GraficosBodyScreen(
            uiState = uiState,
            paddingValues = paddingValues,
            onSeleccionSeccionChange = { seleccionSeccion = it },
            rangoSeleccionado = { cambiarRangoTiempo(it) },
            seleccionSeccion = seleccionSeccion,
            seleccionarRango = seleccionarRangoGraficos,
        )

        if (mostrarListaMonedas) {
            SelectorMoneda(
                monedasViewModel = monedasViewModel,
                onDismiss = { mostrarListaMonedas = false },
                monedaSeleccionada = {
                    mostrarListaMonedas = false
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GraficosBodyScreen(
    uiState: GraficosUiState,
    paddingValues: PaddingValues,
    onSeleccionSeccionChange: (Int) -> Unit,
    rangoSeleccionado: (GraficosViewModel.RangoTiempo) -> Unit,
    seleccionSeccion: Int,
    seleccionarRango: GraficosViewModel.RangoTiempo,
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        item {
            ScrollableTabRow(
                selectedTabIndex = seleccionSeccion,
                containerColor = colorFondo,
                contentColor = blanco,
                edgePadding = 8.dp
            ) {
                listOf(
                    "Ingresos vs Gastos" to Icons.Default.BarChart,
                    "Evolución" to Icons.Default.ShowChart,
                    "Categorías" to Icons.Default.PieChart
                ).forEachIndexed { index, (titulo, icono) ->
                    Tab(
                        selected = seleccionSeccion == index,
                        onClick = { onSeleccionSeccionChange(index) },
                        text = { Text(text = titulo, color = blanco) },
                        icon = { Icon(imageVector = icono, contentDescription = titulo, tint = if (seleccionSeccion == index) naranjaClaro else blanco) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        //graficos según la seccion seleccionada
        item {
            when (seleccionSeccion) {
                0 -> GraficoBarras(
                    ingresos = uiState.ingresos,
                    gastos = uiState.gastos,
                    fechas = uiState.fechas
                )
                1 -> GraficoLineas(
                    datos = uiState.evolucionBalance,
                    fechas = uiState.fechas
                )
                2 -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GraficoCircularIngresos(
                        datosIngresos = uiState.ingresosPorCategoria,
                        modifier = Modifier.weight(0.5f)
                    )
                    GraficoCircularGastos(
                        datosGastos = uiState.gastosPorCategoria,
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        //card con informacion financiera y rangos de tiempo
        item {
            InformacionFinancieraCard(
                totalIngresos = uiState.totalIngresos,
                totalGastos = uiState.totalGastos,
                balanceTotal = uiState.balanceTotal,
                ratioAhorro = uiState.ratioAhorro
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeleccionRangoTiempo(
                seleccionarRango = seleccionarRango,
                rangoSeleccionado = rangoSeleccionado
            )
        }
    }
}

@Composable
fun InformacionFinancieraCard(
    totalIngresos: Double,
    totalGastos: Double,
    balanceTotal: Double,
    ratioAhorro: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        border = BorderStroke(2.dp, naranjaClaro), // Borde naranja claro
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumen Financiero", style = MaterialTheme.typography.titleMedium.copy(color = blanco)) // Texto en blanco
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Ingresos: $${String.format("%.2f", totalIngresos)}", style = MaterialTheme.typography.bodyLarge.copy(color = blanco)) // Texto en blanco
            Text("Total Gastos: $${String.format("%.2f", totalGastos)}", style = MaterialTheme.typography.bodyLarge.copy(color = blanco)) // Texto en blanco
            Text("Balance Total: $${String.format("%.2f", balanceTotal)}", style = MaterialTheme.typography.bodyLarge.copy(color = blanco)) // Texto en blanco
            Text("Ratio de Ahorro: ${String.format("%.2f", ratioAhorro)}%", style = MaterialTheme.typography.bodyLarge.copy(color = blanco)) // Texto en blanco
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SeleccionRangoTiempo(
    seleccionarRango: GraficosViewModel.RangoTiempo,
    rangoSeleccionado: (GraficosViewModel.RangoTiempo) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        GraficosViewModel.RangoTiempo.values().forEach { timeRange ->
            Button(
                onClick = { rangoSeleccionado(timeRange) },
                colors = ButtonDefaults.buttonColors(containerColor = if (seleccionarRango == timeRange) naranjaClaro else Color.LightGray) // Cambiado el color del botón no seleccionado
            ) {
                Text(text = timeRange.name.capitalize(), color = blanco)
            }
        }
    }
}


