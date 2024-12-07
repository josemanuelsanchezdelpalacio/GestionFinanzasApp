package com.dam2jms.gestiongastosapp.screens

import ItemComponents.SelectorCategoria
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.dam2jms.gestiongastosapp.data.Categoria
import com.dam2jms.gestiongastosapp.data.CategoriaAPI
import com.dam2jms.gestiongastosapp.models.AuxViewModel
import com.dam2jms.gestiongastosapp.models.HistoryViewModel
import com.dam2jms.gestiongastosapp.navigation.AppScreen
import com.dam2jms.gestiongastosapp.states.UiState
import com.dam2jms.gestiongastosapp.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryScreen(navController: NavController, auxViewModel: AuxViewModel, mvvm: HistoryViewModel) {

    val uiState by mvvm.uiState.collectAsState()
    val context = LocalContext.current

    //leo las transacciones de firestore para que muestre la informacion
    LaunchedEffect(Unit) {
        auxViewModel.leerTransacciones(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HISTORIAL DE TRANSACCIONES",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = blanco
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorFondo),
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(AppScreen.HomeScreen.route) }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "atras", tint = blanco)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBarReutilizable(
                navController = navController,
                screenActual = AppScreen.HistoryScreen,
                cambiarSeccion = { pantalla ->
                    navController.navigate(pantalla.route)
                }
            )
        },
        containerColor = colorFondo
    ) { paddingValues ->
        HistoryBodyScreen(
            paddingValues = paddingValues,
            mvvm = mvvm,
            auxViewModel = auxViewModel,
            navController = navController,
            uiState = uiState
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryBodyScreen(
    paddingValues: PaddingValues,
    mvvm: HistoryViewModel,
    auxViewModel: AuxViewModel,
    navController: NavController,
    uiState: UiState
) {

    var tipo by remember { mutableStateOf("todos") }
    var buscarTipo by remember { mutableStateOf("fecha") }

    var buscarFecha by remember { mutableStateOf(LocalDate.now()) }
    var buscarCategoria by remember { mutableStateOf("") }

    var categorias by remember { mutableStateOf<List<Categoria>>(emptyList()) }

    val context = LocalContext.current

    LaunchedEffect(tipo, buscarFecha, buscarCategoria) {
        categorias = CategoriaAPI.obtenerCategorias(if (tipo == "todos") "" else tipo)
        mvvm.buscarTransacciones(buscarTipo, tipo, buscarFecha, buscarCategoria)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondo)
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

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
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                OutlinedButton(
                    onClick = { buscarTipo = "fecha" },
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (buscarTipo == "fecha") naranjaClaro else Color.Transparent,
                        contentColor = if (buscarTipo == "fecha") blanco else naranjaClaro
                    ),
                    border = BorderStroke(1.dp, if (buscarTipo == "fecha") naranjaClaro else naranjaClaro)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Fecha")
                }

                OutlinedButton(
                    onClick = { buscarTipo = "categoria" },
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (buscarTipo == "categoria") naranjaClaro else Color.Transparent,
                        contentColor = if (buscarTipo == "categoria") blanco else naranjaClaro
                    ),
                    border = BorderStroke(1.dp, if (buscarTipo == "categoria") naranjaClaro else naranjaClaro)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text("Categoria")
                }
            }
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { tipo = "todos" },
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (tipo == "todos") naranjaClaro else Color.Transparent,
                        contentColor = if (tipo == "todos") blanco else naranjaClaro
                    ),
                    border = BorderStroke(1.dp, if (tipo == "todos") naranjaClaro else naranjaClaro)
                ) {
                    Text("Todos")
                }

                OutlinedButton(
                    onClick = { tipo = "ingreso" },
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (tipo == "ingreso") verde else Color.Transparent,
                        contentColor = if (tipo == "ingreso") blanco else naranjaClaro
                    ),
                    border = BorderStroke(1.dp, if (tipo == "ingreso") verde else naranjaClaro)
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
                    onClick = { tipo = "gasto" },
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (tipo == "gasto") rojo else Color.Transparent,
                        contentColor = if (tipo == "gasto") blanco else naranjaClaro
                    ),
                    border = BorderStroke(1.dp, if (tipo == "gasto") rojo else naranjaClaro)
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

        if (buscarTipo == "fecha") {
            OutlinedButton(
                onClick = {
                    showDatePicker(context, buscarFecha) { nuevaFecha ->
                        buscarFecha = nuevaFecha
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = grisClaro,
                    contentColor = naranjaClaro
                ),
                border = BorderStroke(1.dp, naranjaClaro)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                Spacer(modifier = Modifier.width(8.dp))
                Text(buscarFecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            }
        } else {
            SelectorCategoria(
                categorias = categorias,
                categoriaSeleccionada = buscarCategoria,
                onCategorySelected = { categoria ->
                    buscarCategoria = categoria
                },
                tipo = uiState.tipo
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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

                if (uiState.transaccionesFiltradas.isNotEmpty()) {
                    LazyColumn {
                        items(uiState.transaccionesFiltradas) { transaccion ->
                            ItemComponents.TransaccionItem(
                                transaccion = transaccion,
                                monedaActual = uiState.monedaActual,
                                navController = navController,
                                onEliminar = { transactionId ->
                                    auxViewModel.eliminarTransaccionExistente(transaccion.tipo, transactionId, context)
                                },
                                onClick = { navController.navigate(AppScreen.EditTransactionScreen.createRoute(transaccion.id!!)) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
                    Text(
                        "No hay transacciones que coincidan con los criterios de búsqueda",
                        color = grisClaro,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}



