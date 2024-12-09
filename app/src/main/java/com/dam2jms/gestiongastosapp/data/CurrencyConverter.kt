package com.dam2jms.gestiongastosapp.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL

class CurrencyConverter {
    private val baseURL = "https://api.frankfurter.app"

    suspend fun obtenerTasasMonedas(moneda: String): Map<String, Double> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseURL/latest?from=$moneda")
                val respuesta = url.readText()
                val jsonObject = JSONObject(respuesta)
                val tasas = jsonObject.getJSONObject("rates")
                tasas.keys().asSequence().associateWith { tasas.getDouble(it) }
            } catch (e: FileNotFoundException) {
                Log.e("CurrencyConverter", "URL no encontrada: ${e.message}")
                emptyMap()
            } catch (e: Exception) {
                Log.e("CurrencyConverter", "Error: ${e.message}")
                emptyMap()
            }
        }
    }

    suspend fun convertirMoneda(cantidad: Double, monedaOrigen: String, monedaDestino: String): Double {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseURL/latest?amount=$cantidad&from=$monedaOrigen&to=$monedaDestino")
                val respuesta = url.readText()
                val jsonObject = JSONObject(respuesta)
                jsonObject.getJSONObject("rates").getDouble(monedaDestino)
            } catch (e: Exception) {
                Log.e("CurrencyConverter", "Error en la conversion: ${e.message}")
                0.0
            }
        }
    }
}