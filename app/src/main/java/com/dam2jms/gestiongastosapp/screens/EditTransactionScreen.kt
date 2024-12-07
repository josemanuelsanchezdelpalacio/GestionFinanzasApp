package com.dam2jms.gestiongastosapp.screens

import ItemComponents.SelectorCategoria
import android.os.Build
import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.dam2jms.gestiongastosapp.models.EditTransactionViewModel
import com.dam2jms.gestiongastosapp.models.AuxViewModel
import com.dam2jms.gestiongastosapp.navigation.AppScreen
import com.dam2jms.gestiongastosapp.states.TransactionUiState
import com.dam2jms.gestiongastosapp.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(navController: NavController, auxViewModel: AuxViewModel, mvvm: EditTransactionViewModel, transactionId: String) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "EDITAR TRANSACCION",
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
                screenActual = AppScreen.EditTransactionScreen,
                cambiarSeccion = { pantalla ->
                    navController.navigate(pantalla.route)
                }
            )
        },
        containerColor = colorFondo,
    ) { paddingValues ->
        EditTransactionBodyScreen(
            paddingValues = paddingValues,
            navController = navController,
            mvvm = mvvm,
            transactionId = transactionId
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditTransactionBodyScreen(paddingValues: PaddingValues, navController: NavController, mvvm: EditTransactionViewModel, transactionId: String) {

    val uiState by mvvm.uiState.collectAsState()
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var seleccionarFecha by remember { mutableStateOf(LocalDate.now()) }
    var categorias by remember { mutableStateOf<List<Categoria>>(emptyList()) }

    LaunchedEffect(transactionId) {
        mvvm.cargarTransaccion(transactionId, context)
    }

    LaunchedEffect(uiState.tipo) {
        categorias = CategoriaAPI.obtenerCategorias(uiState.tipo)
    }

    LaunchedEffect(uiState.fechaTransaccion) {
        seleccionarFecha = LocalDate.parse(uiState.fechaTransaccion, DateTimeFormatter.ISO_DATE)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(colorFondo)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TransactionTypeSelection(
                    tipo = uiState.tipo,
                    onTipoChange = { tipo ->
                        mvvm.actualizarDatosTransaccion(
                            uiState.cantidad.toString(),
                            uiState.categoria,
                            tipo,
                            uiState.fechaTransaccion
                        )
                    }
                )

                EstablecerCantidad(
                    cantidad = uiState.cantidad.toString(),
                    onCantidadChange = { nuevaCantidad ->
                        mvvm.actualizarDatosTransaccion(
                            nuevaCantidad,
                            uiState.categoria,
                            uiState.tipo,
                            uiState.fechaTransaccion
                        )
                    }
                )

                SelectorCategoria(
                    categorias = categorias,
                    categoriaSeleccionada = uiState.categoria,
                    onCategorySelected = { categoria ->
                        mvvm.actualizarDatosTransaccion(
                            uiState.cantidad.toString(),
                            categoria,
                            uiState.tipo,
                            uiState.fechaTransaccion
                        )
                    },
                    tipo = uiState.tipo
                )

                SeleccionarFecha(
                    selectedDate = seleccionarFecha,
                    onDateSelected = { nuevaFecha ->
                        seleccionarFecha = nuevaFecha
                        mvvm.actualizarDatosTransaccion(
                            uiState.cantidad.toString(),
                            uiState.categoria,
                            uiState.tipo,
                            nuevaFecha.format(DateTimeFormatter.ISO_DATE)
                        )
                    }
                )
            }
        }

        Button(
            onClick = {
                if (uiState.cantidad > 0 && uiState.categoria.isNotEmpty() && uiState.tipo.isNotEmpty()) {
                    val transaccion = TransactionUiState(
                        id = transactionId,
                        cantidad = uiState.cantidad,
                        categoria = uiState.categoria,
                        tipo = uiState.tipo,
                        fecha = seleccionarFecha.format(DateTimeFormatter.ISO_DATE)
                    )
                    scope.launch {
                        mvvm.editarTransaccion(transaccion, context)
                        navController.popBackStack()
                    }
                } else {
                    Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = naranjaClaro,
                contentColor = blanco
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Guardar Cambios",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun TransactionTypeSelection(tipo: String, onTipoChange: (String) -> Unit) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Tipo de transaccion",
            style = MaterialTheme.typography.bodyLarge,
            color = blanco,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(grisClaro.copy(alpha = 0.1f))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (tipo == "ingreso") verde.copy(alpha = 0.1f) else Color.Transparent)
                    .padding(8.dp)
            ) {
                RadioButton(
                    selected = tipo == "ingreso",
                    onClick = { onTipoChange("ingreso") },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = verde,
                        unselectedColor = grisClaro
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Ingreso",
                    color = if (tipo == "ingreso") verde else blanco,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (tipo == "gasto") rojo.copy(alpha = 0.1f) else Color.Transparent)
                    .padding(8.dp)
            ) {
                RadioButton(
                    selected = tipo == "gasto",
                    onClick = { onTipoChange("gasto") },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = rojo,
                        unselectedColor = grisClaro
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Gasto",
                    color = if (tipo == "gasto") rojo else blanco,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstablecerCantidad(cantidad: String, onCantidadChange: (String) -> Unit) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Cantidad",
            style = MaterialTheme.typography.bodyLarge,
            color = blanco,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = cantidad,
            onValueChange = onCantidadChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Icon(
                    Icons.Filled.AttachMoney,
                    contentDescription = "Cantidad",
                    tint = naranjaClaro
                )
            },
            textStyle = TextStyle(blanco),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = naranjaClaro,
                unfocusedBorderColor = naranjaClaro,
                focusedLabelColor = naranjaClaro,
                unfocusedLabelColor = naranjaClaro,
                cursorColor = naranjaClaro
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SeleccionarFecha(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Fecha",
            style = MaterialTheme.typography.bodyLarge,
            color = blanco,
            fontWeight = FontWeight.Medium
        )

        OutlinedButton(
            onClick = {
                showDatePicker(context, selectedDate, onDateSelected)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = blanco
            ),
            border = BorderStroke(1.dp, naranjaClaro),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Seleccionar fecha",
                    tint = naranjaClaro
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    color = blanco
                )
            }
        }
    }
}
