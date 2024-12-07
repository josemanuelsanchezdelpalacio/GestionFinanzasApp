package com.dam2jms.gestiongastosapp.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

data class Categoria(val id: String, val nombre: String, val tipo: String)

object CategoriaAPI {

    /**constantes para los tipos de categorias */
    object TipoCategoria {
        const val INGRESO = "ingreso"
        const val GASTO = "gasto"
    }

    /**metodo para obtener una lista de categorias segun el tipo (ingreso o gasto) */
    fun obtenerCategorias(tipo: String): List<Categoria> {

        return when (tipo) {
            TipoCategoria.INGRESO -> listOf(
                Categoria(id = "1", nombre = "reembolso", tipo = TipoCategoria.INGRESO),
                Categoria(id = "2", nombre = "salario", tipo = TipoCategoria.INGRESO),
                Categoria(id = "3", nombre = "otros", tipo = TipoCategoria.INGRESO)
            )
            TipoCategoria.GASTO -> listOf(
                Categoria(id = "4", nombre = "casa", tipo = TipoCategoria.GASTO),
                Categoria(id = "5", nombre = "ropa", tipo = TipoCategoria.GASTO),
                Categoria(id = "6", nombre = "educacion", tipo = TipoCategoria.GASTO),
                Categoria(id = "7", nombre = "entretenimiento", tipo = TipoCategoria.GASTO),
                Categoria(id = "8", nombre = "regalo", tipo = TipoCategoria.GASTO),
                Categoria(id = "9", nombre = "mascota", tipo = TipoCategoria.GASTO),
                Categoria(id = "10", nombre = "viajes", tipo = TipoCategoria.GASTO),
                Categoria(id = "11", nombre = "otros", tipo = TipoCategoria.GASTO)
            )
            else -> {
                println("Tipo de categoria no valido: $tipo")
                emptyList()
            }
        }
    }
}

/**metodo para añadir un icono a cada categoría */
fun obtenerIconoCategoria(nombreCategoria: String): ImageVector {
    return when (nombreCategoria.lowercase()) {
        "reembolso" -> Icons.Default.MonetizationOn
        "salario" -> Icons.Default.Money
        "casa" -> Icons.Default.Home
        "ropa" -> Icons.Default.ShoppingBag
        "educación" -> Icons.Default.School
        "entretenimiento" -> Icons.Default.Movie
        "regalo" -> Icons.Default.CardGiftcard
        "mascota" -> Icons.Default.Pets
        "viajes" -> Icons.Default.Flight
        else -> Icons.Default.Category
    }
}


