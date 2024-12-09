package com.dam2jms.gestiongastosapp.screens

import ItemComponents.SelectorCategoria
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dam2jms.gestiongastosapp.components.BottomAppBarReutilizable
import com.dam2jms.gestiongastosapp.components.DatePickerComponents.showDatePicker
import com.dam2jms.gestiongastosapp.data.Categoria
import com.dam2jms.gestiongastosapp.data.CategoriaAPI
import com.dam2jms.gestiongastosapp.data.obtenerIconoCategoria
import com.dam2jms.gestiongastosapp.models.AddTransactionViewModel
import com.dam2jms.gestiongastosapp.models.AuxViewModel
import com.dam2jms.gestiongastosapp.navigation.AppScreen
import com.dam2jms.gestiongastosapp.states.BudgetState
import com.dam2jms.gestiongastosapp.states.FinancialGoalState
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
    val uiState by mvvm.uiState.collectAsState()
    val financialGoalState by mvvm.financialGoalState.collectAsState()
    val budgetState by mvvm.budgetState.collectAsState()
    val financialProgressState by mvvm.financialProgressState.collectAsState()
    val errorState by mvvm.errorState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Fecha por defecto
    var seleccionarFecha by remember { mutableStateOf(LocalDate.now()) }

    // Estado para controlar la pestaña activa
    var activeTab by remember { mutableStateOf(0) }

    // Categorías
    var categorias by remember { mutableStateOf<List<Categoria>>(emptyList()) }

    // Cargar categorías cuando cambia el tipo de transacción
    LaunchedEffect(uiState.tipo) {
        categorias = CategoriaAPI.obtenerCategorias(uiState.tipo)
    }

    // Manejo de errores
    errorState?.let { error ->
        LaunchedEffect(error) {
            // Mostrar error de manera apropiada
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            mvvm.clearErrorState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GESTIÓN FINANCIERA",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorFondo)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tabs para diferentes secciones
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = colorFondo,
                contentColor = blanco
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Transacción") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Meta Financiera") }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Presupuesto") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenido dinámico basado en la pestaña seleccionada
            when (activeTab) {
                0 -> AddTransactionCard(
                    uiState = uiState,
                    mvvm = mvvm,
                    seleccionarFecha = seleccionarFecha,
                    onDateChange = { seleccionarFecha = it },
                    categorias = categorias
                )
                1 -> AddFinancialGoalCard(
                    mvvm = mvvm,
                    financialGoalState = financialGoalState,
                    financialProgressState = financialProgressState
                )
                2 -> AddBudgetCard(
                    mvvm = mvvm,
                    budgetStates = budgetState
                )
            }
        }
    }
}

@Composable
fun AddTransactionCard(
    uiState: UiState,
    mvvm: AddTransactionViewModel,
    seleccionarFecha: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    categorias: List<Categoria>
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf(uiState.cantidad?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(uiState.categoria) }
    var transactionType by remember { mutableStateOf(uiState.tipo) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = blanco),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tipo de Transacción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        transactionType = "ingreso"
                        mvvm.updateTransactionData(amount, selectedCategory, "ingreso")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (transactionType == "ingreso") naranjaClaro else Color.Gray
                    ),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text("Ingreso")
                }
                Button(
                    onClick = {
                        transactionType = "gasto"
                        mvvm.updateTransactionData(amount, selectedCategory, "gasto")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (transactionType == "gasto") naranjaClaro else Color.Gray
                    ),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text("Gasto")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Monto
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    mvvm.updateTransactionData(it, selectedCategory, transactionType)
                },
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Monto") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorFondo)
                    .clickable {
                        // Pass the current date as the initial date
                        showDatePicker(context, seleccionarFecha) { selectedDate ->
                            onDateChange(selectedDate)
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Fecha")
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    seleccionarFecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Categoría
            SelectorCategoria(
                categoriaSeleccionada = selectedCategory ?: "",
                categorias = categorias.filter { it.tipo == transactionType },
                onCategorySelected = { categoria ->
                    selectedCategory = categoria
                    mvvm.updateTransactionData(amount, categoria, transactionType)
                },
                tipo = transactionType
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Guardar
            Button(
                onClick = {
                    mvvm.addTransaction(context, seleccionarFecha) { route ->
                        // Opcional: navegación después de agregar transacción
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaClaro)
            ) {
                Text("Guardar Transacción", style = TextStyle(fontSize = 16.sp))
            }
        }
    }
}

@Composable
fun AddFinancialGoalCard(
    mvvm: AddTransactionViewModel,
    financialGoalState: List<FinancialGoalState>,
    financialProgressState: Triple<Double, Double, List<FinancialGoalState>>
) {
    val context = LocalContext.current
    var goalName by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var goalCategory by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusMonths(6)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = blanco),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Crear Meta Financiera",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Nombre de la Meta
            OutlinedTextField(
                value = goalName,
                onValueChange = { goalName = it },
                label = { Text("Nombre de la Meta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Monto Objetivo
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it },
                label = { Text("Monto Objetivo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Monto") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categoría
            OutlinedTextField(
                value = goalCategory,
                onValueChange = { goalCategory = it },
                label = { Text("Categoría de la Meta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fechas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Fecha de Inicio
                Column(
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text("Fecha de Inicio", style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = {
                            showDatePicker(context, startDate) { selectedDate ->
                                startDate = selectedDate
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    }
                }

                // Fecha de Fin
                Column(
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text("Fecha de Fin", style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = {
                            showDatePicker(context, endDate) { selectedDate ->
                                endDate = selectedDate
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Guardar Meta Financiera
            Button(
                onClick = {
                    val parsedTargetAmount = targetAmount.toDoubleOrNull()
                    if (goalName.isNotEmpty() && parsedTargetAmount != null && parsedTargetAmount > 0 && goalCategory.isNotEmpty()) {
                        mvvm.createFinancialGoal(
                            context = context,
                            goalName = goalName,
                            targetAmount = parsedTargetAmount,
                            goalCategory = goalCategory,
                            startDate = startDate,
                            endDate = endDate
                        )
                    } else {
                        Toast.makeText(context, "Por favor, complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaClaro)
            ) {
                Text("Guardar Meta Financiera", style = TextStyle(fontSize = 16.sp))
            }
        }
    }
}

@Composable
fun AddBudgetCard(
    mvvm: AddTransactionViewModel,
    budgetStates: List<BudgetState>
) {
    val context = LocalContext.current
    var budgetAmount by remember { mutableStateOf("") }
    var budgetCategory by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusMonths(1)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = blanco),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Crear Presupuesto",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Monto del Presupuesto
            OutlinedTextField(
                value = budgetAmount,
                onValueChange = { budgetAmount = it },
                label = { Text("Monto del Presupuesto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Monto") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categoría del Presupuesto
            OutlinedTextField(
                value = budgetCategory,
                onValueChange = { budgetCategory = it },
                label = { Text("Categoría del Presupuesto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fechas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Fecha de Inicio
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Fecha de Inicio", style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = {
                            showDatePicker(context, startDate) { selectedDate ->
                                startDate = selectedDate
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    }
                }

                // Fecha de Fin
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text("Fecha de Fin", style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = {
                            showDatePicker(context, endDate) { selectedDate ->
                                endDate = selectedDate
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Guardar Presupuesto
            Button(
                onClick = {
                    val parsedBudgetAmount = budgetAmount.toDoubleOrNull()
                    if (budgetCategory.isNotEmpty() && parsedBudgetAmount != null && parsedBudgetAmount > 0) {
                        mvvm.createBudget(
                            context = context,
                            category = budgetCategory,
                            budgetAmount = parsedBudgetAmount,
                            startDate = startDate,
                            endDate = endDate
                        )
                    } else {
                        Toast.makeText(context, "Por favor, complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaClaro)
            ) {
                Text("Guardar Presupuesto", style = TextStyle(fontSize = 16.sp))
            }
        }
    }
}
