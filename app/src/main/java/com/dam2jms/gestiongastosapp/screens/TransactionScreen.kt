package com.dam2jms.gestiongastosapp.screens

import ItemComponents.TransaccionItem
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dam2jms.gestiongastosapp.components.BottomAppBarReutilizable
import com.dam2jms.gestiongastosapp.components.DatePickerComponents.showDatePicker
import com.dam2jms.gestiongastosapp.models.TransactionViewModel
import com.dam2jms.gestiongastosapp.models.AuxViewModel
import com.dam2jms.gestiongastosapp.navigation.AppScreen
import com.dam2jms.gestiongastosapp.states.BudgetState
import com.dam2jms.gestiongastosapp.states.FinancialGoalState
import com.dam2jms.gestiongastosapp.states.UiState
import com.dam2jms.gestiongastosapp.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionScreen(
    navController: NavController,
    mvvm: TransactionViewModel,
    auxViewModel: AuxViewModel,
    seleccionarFecha: String
) {
    val uiState by mvvm.uiState.collectAsState()
    val financialGoalState by mvvm.financialGoalState.collectAsState()
    val budgetState by mvvm.budgetState.collectAsState()
    val context = LocalContext.current

    val fechaInicial = remember {
        try {
            if (seleccionarFecha == "{date}") {
                LocalDate.now()
            } else {
                LocalDate.parse(seleccionarFecha)
            }
        } catch (e: DateTimeParseException) {
            LocalDate.now()
        }
    }

    var fechaSeleccionada by remember { mutableStateOf(fechaInicial) }

    val csv_launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { mvvm.exportarTransaccionesCSV(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "TRANSACCIONES",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = blanco
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(AppScreen.HomeScreen.route) }) {
                        Icon(Icons.Default.ArrowBackIos, "atras", tint = blanco)
                    }
                },
                actions = {
                    IconButton(onClick = { csv_launcher.launch("transacciones.csv") }) {
                        Icon(Icons.Default.FileDownload, "Exportar a CSV", tint = blanco)
                    }
                    IconButton(onClick = {
                        showDatePicker(context, fechaSeleccionada) { nuevaFecha ->
                            fechaSeleccionada = nuevaFecha
                            navController.navigate(AppScreen.TransactionScreen.createRoute(nuevaFecha.toString()))
                        }
                    }) {
                        Icon(Icons.Default.CalendarToday, "Seleccionar fecha", tint = blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorFondo)
            )
        },
        bottomBar = {
            BottomAppBarReutilizable(
                navController = navController,
                screenActual = AppScreen.TransactionScreen,
                cambiarSeccion = { pantalla ->
                    navController.navigate(pantalla.route)
                }
            )
        },
        containerColor = colorFondo,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(AppScreen.AddTransactionScreen.route) },
                containerColor = naranjaClaro,
                contentColor = blanco
            ) {
                Icon(Icons.Default.Add, "añadir transaccion")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        TransactionScreenContent(
            paddingValues = paddingValues,
            mvvm = mvvm,
            navController = navController,
            fecha = fechaSeleccionada,
            uiState = uiState,
            financialGoalState = financialGoalState,
            budgetState = budgetState
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionScreenContent(
    paddingValues: PaddingValues,
    mvvm: TransactionViewModel,
    navController: NavController,
    fecha: LocalDate,
    uiState: UiState,
    financialGoalState: List<FinancialGoalState>,
    budgetState: List<BudgetState>
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf("Transacciones") }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(colorFondo)
    ) {
        // Section Selector
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedSection == "Transacciones",
                    onClick = { selectedSection = "Transacciones" },
                    label = { Text("Transacciones") }
                )
            }
            item {
                FilterChip(
                    selected = selectedSection == "Metas",
                    onClick = { selectedSection = "Metas" },
                    label = { Text("Metas Financieras") }
                )
            }
            item {
                FilterChip(
                    selected = selectedSection == "Presupuestos",
                    onClick = { selectedSection = "Presupuestos" },
                    label = { Text("Presupuestos") }
                )
            }
        }

        // Content based on selected section
        when (selectedSection) {
            "Transacciones" -> {
                var desplegarTipo by remember { mutableStateOf("ingresos") }

                // Transaction Type Selector
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = desplegarTipo == "ingresos",
                            onClick = { desplegarTipo = "ingresos" },
                            label = { Text("Ingresos") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = desplegarTipo == "gastos",
                            onClick = { desplegarTipo = "gastos" },
                            label = { Text("Gastos") }
                        )
                    }
                }

                // Transactions List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    val transactions = mvvm.filtrarTransacciones(fecha, desplegarTipo)

                    if (transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay transacciones para esta fecha",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(transactions) { transaccion ->
                            TransaccionItem(
                                transaccion = transaccion,
                                monedaActual = "€",
                                navController = navController,
                                onEliminar = {
                                    mvvm.eliminarTransaccion(context, transaccion)
                                },
                                onClick = {
                                    navController.navigate(AppScreen.EditTransactionScreen.createRoute(transaccion.id!!))
                                }
                            )
                        }
                    }
                }
            }
            "Metas" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    if (financialGoalState.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay metas financieras",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(financialGoalState) { meta ->
                            FinancialGoalItem(meta = meta)
                        }
                    }
                }
            }
            "Presupuestos" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    if (budgetState.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay presupuestos",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(budgetState) { presupuesto ->
                            BudgetItem(presupuesto = presupuesto)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialGoalItem(
    meta: FinancialGoalState,
    onEliminar: () -> Unit = {},
    onEditar: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = blanco)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Goal Title and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meta.goalName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorFondo
                )

                Row {
                    IconButton(onClick = onEditar) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar meta",
                            tint = naranjaClaro
                        )
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar meta",
                            tint = rojo
                        )
                    }
                }
            }

            // Progress Information
            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            Box(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = {
                        (meta.currentAmount / meta.targetAmount).toFloat().coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = naranjaClaro,
                    trackColor = grisClaro
                )

                // Percentage Text
                Text(
                    text = "${((meta.currentAmount / meta.targetAmount) * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Transparent),
                    color = Color.White
                )
            }

            // Details Row
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Current Amount
                Column {
                    Text(
                        text = "Actual",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${meta.currentAmount}€",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorFondo
                    )
                }

                // Goal Amount
                Column {
                    Text(
                        text = "Objetivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${meta.targetAmount}€",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = naranjaClaro
                    )
                }

                // Deadline
                Column {
                    Text(
                        text = "Fecha Límite",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = meta.endDate,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = naranjaClaro
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetItem(
    presupuesto: BudgetState,
    onEliminar: () -> Unit = {},
    onEditar: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = blanco)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Budget Title and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = presupuesto.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorFondo
                )

                Row {
                    IconButton(onClick = onEditar) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar presupuesto",
                            tint = naranjaClaro
                        )
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar presupuesto",
                            tint = rojo
                        )
                    }
                }
            }

            // Progress Information
            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            Box(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = {
                        (presupuesto.currentSpent / presupuesto.budgetAmount).toFloat().coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = when {
                        (presupuesto.currentSpent / presupuesto.budgetAmount) > 1.0 -> Color.Red
                        (presupuesto.currentSpent / presupuesto.budgetAmount) > 0.8 -> naranjaClaro
                        else -> grisClaro
                    },
                    trackColor = Color.Gray.copy(alpha = 0.3f)
                )

                // Percentage Text
                Text(
                    text = "${((presupuesto.currentSpent / presupuesto.budgetAmount) * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Transparent),
                    color = Color.White
                )
            }

            // Details Row
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Current Spent Amount
                Column {
                    Text(
                        text = "Gastado",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${presupuesto.currentSpent}€",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorFondo
                    )
                }

                // Budgeted Amount
                Column {
                    Text(
                        text = "Presupuesto",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${presupuesto.budgetAmount}€",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = naranjaClaro
                    )
                }

                // Remaining Amount
                Column {
                    Text(
                        text = "Restante",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${presupuesto.budgetAmount - presupuesto.currentSpent}€",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = naranjaClaro
                    )
                }
            }
        }
    }
}
