package com.dam2jms.gestiongastosapp.screens

import ItemComponents.SelectorCategoria
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dam2jms.gestiongastosapp.components.BottomAppBarReutilizable
import com.dam2jms.gestiongastosapp.components.DatePickerComponents.showDatePicker
import com.dam2jms.gestiongastosapp.data.Categoria
import com.dam2jms.gestiongastosapp.data.CategoriaAPI
import com.dam2jms.gestiongastosapp.models.AddTransactionViewModel
import com.dam2jms.gestiongastosapp.models.AuxViewModel
import com.dam2jms.gestiongastosapp.models.FinancialReportState
import com.dam2jms.gestiongastosapp.navigation.AppScreen
import com.dam2jms.gestiongastosapp.states.UiState
import com.dam2jms.gestiongastosapp.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    auxViewModel: AuxViewModel,
    mvvm: AddTransactionViewModel
) {
    val scope = rememberCoroutineScope()
    val uiState by mvvm.uiState.collectAsState()
    val reportState by mvvm.reportState.collectAsState()

    // Fecha por defecto
    var seleccionarFecha by remember { mutableStateOf(LocalDate.now()) }

    // Estado para diálogos
    var showReportDialog by remember { mutableStateOf(false) }
    var showForecastDialog by remember { mutableStateOf(false) }
    var forecastState by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }

    // Categorías
    var categorias by remember { mutableStateOf<List<Categoria>>(emptyList()) }

    // Cargar categorías cuando cambia el tipo de transacción
    LaunchedEffect(uiState.tipo) {
        categorias = CategoriaAPI.obtenerCategorias(uiState.tipo)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AÑADIR TRANSACCION",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = blanco
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIos, "atras", tint = blanco)
                    }
                },
                actions = {
                    // Botones de informe y predicción en la barra de acciones
                    IconButton(
                        onClick = {
                            scope.launch {
                                mvvm.generarInformeMensual(
                                    LocalDate.now().year,
                                    LocalDate.now().monthValue
                                )
                                showReportDialog = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.Analytics, contentDescription = "Informe Mensual", tint = blanco)
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                forecastState = mvvm.predecirGastosFuturos(6)
                                showForecastDialog = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.BatchPrediction, contentDescription = "Predicción Gastos", tint = blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorFondo)
            )
        },
        bottomBar = {
            BottomAppBarReutilizable(
                navController = navController,
                screenActual = AppScreen.AddTransactionScreen,
                cambiarSeccion = { pantalla ->
                    navController.navigate(pantalla.route)
                }
            )
        },
        containerColor = colorFondo
    ) { paddingValues ->
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorFondo)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta principal de transacción
            AddTransactionCard(
                uiState = uiState,
                mvvm = mvvm,
                seleccionarFecha = seleccionarFecha,
                categorias = categorias,
                onFechaChange = { newDate -> seleccionarFecha = newDate }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de añadir transacción
            AddTransactionButton(
                uiState = uiState,
                mvvm = mvvm,
                context = LocalContext.current,
                navController = navController,
                seleccionarFecha = seleccionarFecha
            )
        }

        // Diálogo de Informe Mensual
        if (showReportDialog) {
            MonthlyReportDialog(
                reportState = reportState,
                onDismiss = { showReportDialog = false }
            )
        }

        // Diálogo de Predicción de Gastos
        if (showForecastDialog) {
            ForecastDialog(
                forecastState = forecastState,
                onDismiss = { showForecastDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionCard(
    uiState: UiState,
    mvvm: AddTransactionViewModel,
    seleccionarFecha: LocalDate,
    categorias: List<Categoria>,
    onFechaChange: (LocalDate) -> Unit
) {
    val context = LocalContext.current

    val categoriaSeleccionada: (String) -> Unit = { categoria ->
        mvvm.actualizarDatosTransaccion(uiState.cantidad.toString(), categoria, uiState.tipo)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        border = BorderStroke(2.dp, naranjaClaro)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Botones de tipo de transacción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        mvvm.actualizarDatosTransaccion(
                            uiState.cantidad.toString(),
                            uiState.categoria,
                            "ingreso"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = verde),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.TrendingUp, "Ingreso")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ingreso", color = blanco)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        mvvm.actualizarDatosTransaccion(
                            uiState.cantidad.toString(),
                            uiState.categoria,
                            "gasto"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = rojo),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.TrendingDown, "Gasto")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gasto", color = blanco)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Cantidad
            Text("Cantidad", color = grisClaro, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.cantidad.toString(),
                onValueChange = { nuevaCantidad ->
                    mvvm.actualizarDatosTransaccion(
                        nuevaCantidad,
                        uiState.categoria,
                        uiState.tipo
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(
                        Icons.Filled.AttachMoney,
                        "Cantidad",
                        tint = if (uiState.tipo == "ingreso") verde else rojo
                    )
                },
                textStyle = TextStyle(blanco),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = if (uiState.tipo == "ingreso") verde else rojo,
                    unfocusedBorderColor = grisClaro
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Categoría
            Text("Categoria", color = grisClaro, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            SelectorCategoria(
                categorias = categorias,
                categoriaSeleccionada = uiState.categoria,
                onCategorySelected = categoriaSeleccionada,
                tipo = uiState.tipo
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Fecha
            Text("Fecha", color = grisClaro, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    showDatePicker(context, seleccionarFecha, onFechaChange)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (uiState.tipo == "ingreso") verde else rojo
                ),
                border = BorderStroke(1.dp, if (uiState.tipo == "ingreso") verde else rojo)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    seleccionarFecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    color = if (uiState.tipo == "ingreso") verde else rojo
                )
            }
        }
    }
}

@Composable
fun AddTransactionButton(
    uiState: UiState,
    mvvm: AddTransactionViewModel,
    context: Context,
    navController: NavController,
    seleccionarFecha: LocalDate
) {
    Button(
        onClick = {
            mvvm.añadirTransaccion(context, seleccionarFecha) { route ->
                navController.navigate(AppScreen.TransactionScreen.createRoute(route))
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (uiState.tipo == "ingreso") verde else rojo
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.Add, "Añadir")
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Añadir Transaccion",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun MonthlyReportDialog(
    reportState: FinancialReportState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Informe Financiero Mensual") },
        text = {
            Column {
                Text("Total Ingresos: ${String.format("%.2f", reportState.totalIngresos)}")
                Text("Total Gastos: ${String.format("%.2f", reportState.totalGastos)}")
                Text("Balance: ${String.format("%.2f", reportState.balance)}")

                Spacer(modifier = Modifier.height(16.dp))
                Text("Gastos por Categoría:", style = MaterialTheme.typography.titleMedium)
                reportState.gastosPorCategoria.forEach { (categoria, monto) ->
                    Text("$categoria: ${String.format("%.2f", monto)}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun ForecastDialog(
    forecastState: Map<String, Double>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Predicción de Gastos Futuros") },
        text = {
            Column {
                Text("Gastos Promedio Estimados por Categoría:")
                forecastState.forEach { (categoria, monto) ->
                    Text("$categoria: ${String.format("%.2f", monto)}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}


