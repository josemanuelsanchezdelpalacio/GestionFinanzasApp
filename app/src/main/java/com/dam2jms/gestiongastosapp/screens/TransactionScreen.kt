package com.dam2jms.gestiongastosapp.screens

import ItemComponents.TransaccionItem
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.dam2jms.gestiongastosapp.states.TransactionUiState
import com.dam2jms.gestiongastosapp.states.UiState
import com.dam2jms.gestiongastosapp.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionScreen(navController: NavController, mvvm: TransactionViewModel, auxViewModel: AuxViewModel, seleccionarFecha: String) {

    val uiState by mvvm.uiState.collectAsState()
    val context = LocalContext.current

    var desplegarTipo by remember { mutableStateOf("ingresos") }

    //controlar errores de la fecha
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

    //fecha seleccionada desde el datePicker
    var fechaSeleccionada by remember { mutableStateOf(fechaInicial) }

    //inicio el launcher para descargar los datos en un CSV
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
            desplegarTipo = desplegarTipo,
            onDesplegarTipoChange = { nuevoTipo -> desplegarTipo = nuevoTipo },
            fecha = fechaSeleccionada,
            uiState = uiState
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionScreenContent(
    paddingValues: PaddingValues,
    mvvm: TransactionViewModel,
    navController: NavController,
    desplegarTipo: String,
    onDesplegarTipoChange: (String) -> Unit,
    fecha: LocalDate,
    uiState: UiState
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(colorFondo)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BalanceFechaCard(
            fecha = fecha,
            balance = mvvm.calcularBalanceDiario(fecha),
            ingresos = mvvm.filtrarTransacciones(fecha, "ingresos").sumOf { it.cantidad },
            gastos = mvvm.filtrarTransacciones(fecha, "gastos").sumOf { it.cantidad }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TipoTransaccionSeleccionCard(
            desplegarTipo = desplegarTipo,
            onDesplegarTipoChange = onDesplegarTipoChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        TransaccionesListCard(
            transacciones = if (desplegarTipo == "ingresos")
                mvvm.filtrarTransacciones(fecha, "ingresos")
            else
                mvvm.filtrarTransacciones(fecha, "gastos"),
            monedaActual = uiState.monedaActual,
            navController = navController,
            onEliminar = { transactionId ->
                AuxViewModel().eliminarTransaccionExistente(desplegarTipo, transactionId, context)
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BalanceFechaCard(fecha: LocalDate, balance: Float, ingresos: Double, gastos: Double) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        border = BorderStroke(2.dp, naranjaClaro)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Transacciones del día", color = grisClaro, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                color = blanco,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {

            Text("Resumen diario", color = grisClaro, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Ingresos", color = grisClaro, style = MaterialTheme.typography.bodySmall)
                    Text(
                        String.format("%.2f", ingresos),
                        color = verde,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Gastos", color = grisClaro, style = MaterialTheme.typography.bodySmall)
                    Text(
                        String.format("%.2f", gastos),
                        color = rojo,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = naranjaClaro)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Balance neto", color = grisClaro, style = MaterialTheme.typography.bodySmall)

                Text(
                    String.format("%.2f", balance),
                    color = blanco,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun TipoTransaccionSeleccionCard(desplegarTipo: String, onDesplegarTipoChange: (String) -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        border = BorderStroke(2.dp, naranjaClaro)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedButton(
                onClick = { onDesplegarTipoChange("ingresos") },
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (desplegarTipo == "ingresos") verde else Color.Transparent,
                    contentColor = if (desplegarTipo == "ingresos") Color.White else naranjaClaro // Cambia el color del texto a blanco
                ),
                border = BorderStroke(1.dp, if (desplegarTipo == "ingresos") verde else naranjaClaro)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ingresos")
            }

            OutlinedButton(
                onClick = { onDesplegarTipoChange("gastos") },
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (desplegarTipo == "gastos") rojo else Color.Transparent,
                    contentColor = if (desplegarTipo == "gastos") Color.White else naranjaClaro // Cambia el color del texto a blanco
                ),
                border = BorderStroke(1.dp, if (desplegarTipo == "gastos") rojo else naranjaClaro)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gastos")
            }

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransaccionesListCard(transacciones: List<TransactionUiState>, monedaActual: String, navController: NavController, onEliminar: (String) -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        border = BorderStroke(2.dp, naranjaClaro)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text("Transacciones", color = grisClaro, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (transacciones.isNotEmpty()) {
                transacciones.forEach { transaccion ->
                    TransaccionItem(
                        transaccion = transaccion,
                        monedaActual = monedaActual,
                        navController = navController,
                        onEliminar = { onEliminar(transaccion.id!!) },
                        onClick = { navController.navigate(AppScreen.EditTransactionScreen.createRoute(transaccion.id!!)) }
                    )
                }
            } else {
                Text(
                    "No hay transacciones recientes.",
                    color = grisClaro,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


