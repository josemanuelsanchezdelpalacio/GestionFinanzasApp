package com.dam2jms.gestiongastosapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dam2jms.gestiongastosapp.components.BottomAppBarReutilizable
import com.dam2jms.gestiongastosapp.models.HomeViewModel
import com.dam2jms.gestiongastosapp.models.MonedasViewModel
import com.dam2jms.gestiongastosapp.models.AuxViewModel
import com.dam2jms.gestiongastosapp.navigation.AppScreen
import com.dam2jms.gestiongastosapp.states.Article
import com.dam2jms.gestiongastosapp.states.TransactionUiState
import com.dam2jms.gestiongastosapp.states.UiState
import com.dam2jms.gestiongastosapp.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    auxViewModel: AuxViewModel,
    monedasViewModel: MonedasViewModel
) {
    val uiState by homeViewModel.uiState.collectAsState()
    var showCurrencyDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        homeViewModel.initializeNews(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Financial Dashboard",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = blanco
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = blanco)
                    }
                },
                actions = {
                    IconButton(onClick = { showCurrencyDialog = true }) {
                        Icon(Icons.Filled.CurrencyExchange, contentDescription = "Change Currency", tint = blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorFondo)
            )
        },
        bottomBar = {
            BottomAppBarReutilizable(
                navController = navController,
                screenActual = AppScreen.HomeScreen,
                cambiarSeccion = { pantalla -> navController.navigate(pantalla.route) }
            )
        },
        containerColor = colorFondo
    ) { paddingValues ->
        HomeScreenContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            homeViewModel = homeViewModel,
            onNewsRefresh = { homeViewModel.refreshNews(context) },
            onCurrencyChangeRequest = { homeViewModel.actualizarMoneda(it) }
        )

        if (showCurrencyDialog) {
            CurrencySelectionDialog(
                onDismiss = { showCurrencyDialog = false },
                onCurrencySelected = {
                    homeViewModel.actualizarMoneda(it)
                    showCurrencyDialog = false
                },
                monedasViewModel = monedasViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionDialog(
    onDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit,
    monedasViewModel: MonedasViewModel
) {
    val monedasDisponibles by monedasViewModel.monedasDisponibles.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredMonedas = remember(searchQuery, monedasDisponibles) {
        monedasDisponibles.filter {
            it.contains(searchQuery, ignoreCase = true) ||
                    monedasViewModel.obtenerNombreMoneda(it).contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Seleccionar Moneda",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar moneda") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        text = {
            LazyColumn {
                items(filteredMonedas) { moneda ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCurrencySelected(moneda) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = monedasViewModel.obtenerNombreMoneda(moneda),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = monedasViewModel.obtenerSimboloMoneda(moneda),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Divider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    uiState: UiState,
    homeViewModel: HomeViewModel,
    onNewsRefresh: () -> Unit,
    onCurrencyChangeRequest: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorFondo)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Financial Overview Card actualizado con más información
        FinancialOverviewCard(
            balanceTotal = uiState.balanceTotal,
            dailyIncome = uiState.ingresosDiarios,
            dailyExpenses = uiState.gastosDiarios,
            moneda = uiState.monedaActual,
            monthlyIncome = uiState.ingresosMensuales,
            monthlyExpenses = uiState.gastosMensuales,
            yearlyIncome = uiState.ingresosAnuales,
            yearlyExpenses = uiState.gastosAnuales
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Transacciones recientes
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    color = blanco
                )
                uiState.transaccionesRecientes.take(3).forEach { transaction ->
                    TransactionItem(transaction)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Sección de noticias financieras
            Column(
                modifier = Modifier.weight(1f)
            ) {
                FinancialNewsSection(
                    articles = uiState.newsArticles,
                    isLoading = uiState.isLoadingNews,
                    onRefresh = onNewsRefresh
                )
            }
        }
    }
}

@Composable
fun FinancialOverviewCard(
    balanceTotal: Double,
    dailyIncome: Double,
    dailyExpenses: Double,
    moneda: String,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    yearlyIncome: Double,
    yearlyExpenses: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = azul.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Título del componente
            Text(
                text = "Financial Overview",
                style = MaterialTheme.typography.titleMedium,
                color = blanco
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sección: Balance Total
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = grisClaro
                )
                Text(
                    text = "$moneda ${String.format("%.2f", balanceTotal)}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = verde,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(
                color = grisClaro.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Sección: Ingresos y gastos diarios
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Daily Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = grisClaro
                    )
                    Text(
                        text = "+$moneda ${String.format("%.2f", dailyIncome)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = verde
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Daily Expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = grisClaro
                    )
                    Text(
                        text = "-$moneda ${String.format("%.2f", dailyExpenses)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = rojo
                    )
                }
            }

            Divider(
                color = grisClaro.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Sección: Ingresos y gastos mensuales
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Monthly Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = grisClaro
                    )
                    Text(
                        text = "+$moneda ${String.format("%.2f", monthlyIncome)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = verde
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Monthly Expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = grisClaro
                    )
                    Text(
                        text = "-$moneda ${String.format("%.2f", monthlyExpenses)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = rojo
                    )
                }
            }

            Divider(
                color = grisClaro.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Sección: Ingresos y gastos anuales
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Yearly Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = grisClaro
                    )
                    Text(
                        text = "+$moneda ${String.format("%.2f", yearlyIncome)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = verde
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Yearly Expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = grisClaro
                    )
                    Text(
                        text = "-$moneda ${String.format("%.2f", yearlyExpenses)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = rojo
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionUiState) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val parsedDate = LocalDate.parse(transaction.fecha)
    val formattedDate = parsedDate.format(formatter)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = azul.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.categoria,
                    style = MaterialTheme.typography.bodyMedium,
                    color = blanco
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = grisClaro
                )
            }
            Text(
                text = "${transaction.tipo.uppercase()} ${String.format("%.2f", transaction.cantidad)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (transaction.tipo == "ingreso") verde else rojo,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FinancialNewsSection(
    articles: List<Article>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Financial News",
                style = MaterialTheme.typography.titleMedium,
                color = blanco
            )
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh News",
                    tint = blanco
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = azul)
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(articles.take(5)) { article ->
                    NewsArticleCard(article)
                }
            }
        }
    }
}

@Composable
fun NewsArticleCard(article: Article) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { /* Optional: Open article URL */ },
        colors = CardDefaults.cardColors(containerColor = azul.copy(alpha = 0.1f))
    ) {
        Column {
            AsyncImage(
                model = article.urlToImage,
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = article.title ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = blanco,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = article.source?.name ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = grisClaro
                )
            }
        }
    }
}


