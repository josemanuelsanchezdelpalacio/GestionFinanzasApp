package com.dam2jms.gestiongastosapp.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam2jms.gestiongastosapp.data.CurrencyConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Currency
import java.util.Locale

class MonedasViewModel() : ViewModel() {

    //estado que contiene la lista de monedas disponibles
    private val _monedasDisponibles = MutableStateFlow<List<String>>(emptyList())
    val monedasDisponibles: StateFlow<List<String>> = _monedasDisponibles

    //instancia del conversor de divisas
    private val monedaConvertida = CurrencyConverter()

    //lista de codigos de las monedas importantes a seguir
    private val monedasImportantes = listOf(
        "USD", "EUR", "JPY", "GBP", "AUD", "CAD", "CHF", "CNY", "SEK", "NZD",
        "MXN", "SGD", "HKD", "NOK", "KRW", "TRY", "INR", "RUB", "BRL", "ZAR"
    )

    //inicializo el viewmodel y obtengo las monedas disponibles
    init {
        obtenerMonedasDisponibles()
    }

    /** metodo para obtener las monedas disponibles a partir de las tasas de cambio
     * filtro las monedas importantes que están disponibles en las tasas
     */
    internal fun obtenerMonedasDisponibles() {
        viewModelScope.launch {
            //obtengo las tasas de cambio
            val tasas = monedaConvertida.obtenerTasasMonedas("EUR")

            //actualizo el estado con las monedas importantes que estan disponibles
            _monedasDisponibles.value = monedasImportantes.filter { it in tasas.keys }
        }
    }

    /**metodo para obtener el simbolo de la moneda a partir de su nombre*/
    fun obtenerSimboloMoneda(nombreMoneda: String): String {
        return try {
            //devuelvo el simbolo de la moneda usando su nombre
            Currency.getInstance(nombreMoneda).symbol
        } catch (e: IllegalArgumentException) {
            //si el nombre no es valido devuelve el mismo nombre
            nombreMoneda
        }
    }

    /**metodo para obtener el nombre de la moneda a partir de su codigo*/
    fun obtenerNombreMoneda(nombreMoneda: String): String {
        return try {
            //obtengo la instancia de la moneda y su nombre
            val moneda = Currency.getInstance(nombreMoneda)
            val nombre = moneda.getDisplayName(Locale.getDefault())
            //formato
            "$nombre ($nombreMoneda)"
        } catch (e: IllegalArgumentException) {
            //si el nombre no es valido devuelve el mismo nombre
            nombreMoneda
        }
    }

    /***metodo para obtener la tasa de cambio entre dos monedas*/
    suspend fun obtenerTasaCambio(monedaOrigen: String, monedaDestino: String): Double {
        return try {
            //obtengo las tasas de cambio para la moneda de origen
            val rates = monedaConvertida.obtenerTasasMonedas(monedaOrigen)

            //devuelvo la tasa de cambio para la moneda de destino
            rates[monedaDestino] ?: 1.0

        } catch (e: Exception) {
            //devuelvo 1.0 como tasa de cambio por defecto si da error
            1.0
        }
    }
}



